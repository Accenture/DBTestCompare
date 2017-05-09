// <copyright file="NmbOfResultsComparator.java" company="Objectivity Bespoke Software Specialists">
// Copyright (c) Objectivity Bespoke Software Specialists. All rights reserved.
// </copyright>
// <license>
//     The MIT License (MIT)
//     Permission is hereby granted, free of charge, to any person obtaining a copy
//     of this software and associated documentation files (the "Software"), to deal
//     in the Software without restriction, including without limitation the rights
//     to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
//     copies of the Software, and to permit persons to whom the Software is
//     furnished to do so, subject to the following conditions:
//     The above copyright notice and this permission notice shall be included in all
//     copies or substantial portions of the Software.
//     THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
//     IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
//     FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
//     AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
//     LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
//     OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
//     SOFTWARE.
// </license>

package uk.co.objectivity.test.db.comparators;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.testng.TestException;

import uk.co.objectivity.test.db.beans.TestParams;
import uk.co.objectivity.test.db.beans.TestResults;
import uk.co.objectivity.test.db.beans.xml.CmpSqlResultsConfig;
import uk.co.objectivity.test.db.beans.xml.CmpSqlResultsTest;
import uk.co.objectivity.test.db.beans.xml.Datasource;
import uk.co.objectivity.test.db.beans.xml.Sql;
import uk.co.objectivity.test.db.utils.DataSource;
import uk.co.objectivity.test.db.utils.SavedTimes;

import static uk.co.objectivity.test.db.TestDataProvider.savedTimesList;

public class NmbOfResultsComparator extends Comparator {

    private final static Logger log = Logger.getLogger(MinusComparator.class);

    @Override
    public TestResults compare(TestParams testParams) throws Exception {
        CmpSqlResultsConfig cmpSqlResultsConfig = testParams.getCmpSqlResultsConfig();
        CmpSqlResultsTest cmpSqlResultsTest = testParams.getCmpSqlResultsTest();

        // (get(0) - IndexOutOfBoundsException) checked in TestDataProvider, but still - schema would be nice!
        Sql sql1 = cmpSqlResultsTest.getCompare().getSqls().get(0);
        Datasource datasource = cmpSqlResultsConfig.getDatasourceByName(sql1.getDatasourceName());
        if (datasource == null) {
            throw new TestException(
                    "Datasource '" + sql1.getDatasourceName() + "' not found! Please check configuration");
        }
        String query = getCountQuery(sql1.getSql());

        Connection connection = null;
        try {
            connection = DataSource.getConnection(datasource.getName());
            return getTestResults(connection, query, sql1.getDatasourceName(), testParams);
        } catch (Exception e) {
            throw  new Exception(e+"\nAssert Query : " + query);
        } finally {
            DataSource.closeConnection(connection);
        }
    }

    public TestResults getTestResults(Connection conn, String query, String datasourceName, TestParams testParams) throws Exception {
        PreparedStatement stmt = null;
        SavedTimes savedTimes = new SavedTimes(testParams.getTestName());
        try {
            stmt = conn.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);
            savedTimes.StartMeasure("");
            ResultSet rs = stmt.executeQuery();
            savedTimes.StopMeasure();
            savedTimesList.add(savedTimes);

            rs.next();
            String executedQuery = "[" + datasourceName + "]:\r\n" + query+
                    "\nTime execution of query:\n"+
                    savedTimes.getFormattedDuration();
            return new TestResults(executedQuery, rs.getInt(1));
        } finally {
            if (stmt != null)
                try {
                    stmt.close();
                } catch (SQLException e) {
                    log.error(e);
                }
        }
    }

    private String getCountQuery(String selectSql) {
        StringBuffer sqlStrBuff = new StringBuffer("select COUNT(*) from ( ").append(selectSql).append(
                " ) countTable");
        return sqlStrBuff.toString();
    }

}

