// <copyright file="MinusComparator.java" company="Objectivity Bespoke Software Specialists">
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

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.testng.TestException;

import uk.co.objectivity.test.db.beans.TestParams;
import uk.co.objectivity.test.db.beans.TestResults;
import uk.co.objectivity.test.db.beans.xml.CmpSqlResultsConfig;
import uk.co.objectivity.test.db.beans.xml.CmpSqlResultsTest;
import uk.co.objectivity.test.db.beans.xml.Compare;
import uk.co.objectivity.test.db.beans.xml.Datasource;
import uk.co.objectivity.test.db.utils.DataSource;


public class MinusComparator extends Comparator {

    private final static Logger log = Logger.getLogger(MinusComparator.class);

    @Override
    public TestResults compare(TestParams testParams) throws Exception {
        CmpSqlResultsConfig cmpSqlResultsConfig = testParams.getCmpSqlResultsConfig();
        CmpSqlResultsTest cmpSqlResultsTest = testParams.getCmpSqlResultsTest();

        // TODO xml config validation -> in MINUS mode sql queries shpould NOT have own datasources - only
        // default on "compare" level (in XML). Schema would be nice!
        String dataSrcName = cmpSqlResultsTest.getCompare().getDefaultDatasourceName();
        Datasource datasource = cmpSqlResultsConfig.getDatasourceByName(dataSrcName);
        if (datasource == null) {
            throw new TestException("Datasource '" + dataSrcName + "' not found! Please check configuration");
        }
        // (get(0)/get(1) - IndexOutOfBoundsException) checked in TestDataProvider, but still - schema would be nice!
        String sql1 = cmpSqlResultsTest.getCompare().getSqls().get(0).getSql();
        String sql2 = cmpSqlResultsTest.getCompare().getSqls().get(1).getSql();
        String query = getMinusQuery(datasource, sql1, sql2);

        Connection connection = null;
        try {
            connection = DataSource.getConnection(datasource.getName());
            return getTestResults(connection, query, testParams, dataSrcName);
        } catch (Exception e) {
            throw e;
        } finally {
            DataSource.closeConnection(connection);
        }
    }

    public TestResults getTestResults(Connection conn, String query, TestParams testParams,String dataSrcName) throws Exception {
        Compare compare = testParams.getCmpSqlResultsTest().getCompare();
        PreparedStatement stmt = null;
        PrintWriter minusPWriter = null;
        try {
            boolean countOnly = compare.getDiffTableSize() <= 0;

            String executedQuery = countOnly ? getCountQuery(query) : query;

            stmt = conn.prepareStatement(executedQuery, ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);
            ResultSet rs = stmt.executeQuery();
            executedQuery  = "Datasource: " + dataSrcName + "\r\n" + executedQuery +
                    "\r\nDifftable size: " + compare.getDiffTableSize();
            if (countOnly) {
                rs.next();
                return new TestResults(executedQuery, rs.getInt(1));
            }

            int rowCount = 0;
            if (rs.last()) {
                rowCount = rs.getRow();
                rs.beforeFirst(); // not rs.first() because the rs.next() below will move on, missing the first element
            }

            TestResults testResults = new TestResults(executedQuery, rowCount);
            if (rowCount == 0) {
                return testResults;
            }

            if (compare.isFileOutputOn()) {
                minusPWriter = new PrintWriter(
                        getNewFileBasedOnTestConfigFile(testParams.getTestConfigFile(), "_minus.csv"));
            }

            // building columns
            List<String> columns = new ArrayList<>();
            int colCount = rs.getMetaData().getColumnCount();
            for (int column = 1; column <= colCount; column++) {
                columns.add(rs.getMetaData().getColumnName(column));
            }
            testResults.setColumns(columns);
            // building rows
            List<List<String>> rows = new ArrayList<>();
            int diffTabSize = compare.getDiffTableSize();
            int chunk = compare.getChunk();
            while (rs.next()) {
                List<String> row = new ArrayList<>();
                for (int colIndex = 1; colIndex <= colCount; colIndex++) {
                    Object rowObj = rs.getObject(colIndex);
                    // we want NULL to be displayed (to see differences between empty strings)
                    row.add(rowObj == null ? "<NULL>" : rowObj.toString());
                }
                writeRowAsCSV(minusPWriter, row);
                if (diffTabSize-- > 0) {
                    rows.add(row);
                } else if (!compare.isFileOutputOn()) {
                    // we break even if chunk=0 (unlimited). If file log is turned off (why should we go further - we
                    // already know nmb of results)
                    break;
                }
                // chunk has higher priority than diffTabSize
                if (chunk > 0 && rs.getRow() >= chunk) {
                    break;
                }
            }
            testResults.setRows(rows);

            return testResults;
        } finally {
            if (stmt != null)
                try {
                    stmt.close();
                } catch (SQLException e) {
                    log.error(e);
                }
            if (minusPWriter != null)
                minusPWriter.close();
        }
    }

    private String getMinusQuery(Datasource datasource, String sql1, String sql2) {
        String sqlMinus = " MINUS ";
        // TODO check if other than SQLServerDriver databases has somethings else (instead of MINUS)
        if (datasource.getDriver().contains("SQLServerDriver") || datasource.getDriver().contains("postgresql")) {
            sqlMinus = " EXCEPT ";
        }
        StringBuffer sqlStrBuff = new StringBuffer("(");
        sqlStrBuff.append(sql1).append(sqlMinus).append(sql2);
        sqlStrBuff.append(")");
        sqlStrBuff.append(" UNION ");
        sqlStrBuff.append("(");
        sqlStrBuff.append(sql2).append(sqlMinus).append(sql1);
        sqlStrBuff.append(")");
        return sqlStrBuff.toString();

    }

    private String getCountQuery(String minusSqlQuery) {
        StringBuffer sqlStrBuff = new StringBuffer("select COUNT(*) from ( ").append(minusSqlQuery).append(
                " ) countTable");
        return sqlStrBuff.toString();
    }

}
