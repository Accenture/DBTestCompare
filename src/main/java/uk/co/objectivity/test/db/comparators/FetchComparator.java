// <copyright file="FetchComparator.java" company="Objectivity Bespoke Software Specialists">
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

import java.io.File;
import java.io.PrintWriter;
import java.math.BigDecimal;
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
import uk.co.objectivity.test.db.beans.xml.Sql;
import uk.co.objectivity.test.db.utils.DataSource;

public class FetchComparator extends Comparator {

    private final static Logger log = Logger.getLogger(FetchComparator.class);

    @Override
    public TestResults compare(TestParams testParams) throws Exception {
        CmpSqlResultsConfig cmpSqlResultsConfig = testParams.getCmpSqlResultsConfig();
        CmpSqlResultsTest cmpSqlResultsTest = testParams.getCmpSqlResultsTest();

        // (get(0)/get(1) - IndexOutOfBoundsException) checked in TestDataProvider, but still - schema would be nice!
        Sql sql1 = cmpSqlResultsTest.getCompare().getSqls().get(0);
        Sql sql2 = cmpSqlResultsTest.getCompare().getSqls().get(1);

        Datasource datasource1 = cmpSqlResultsConfig.getDatasourceByName(sql1.getDatasourceName());
        Datasource datasource2 = cmpSqlResultsConfig.getDatasourceByName(sql2.getDatasourceName());
        if (datasource1 == null) {
            throw new TestException(
                    "Datasource '" + sql1.getDatasourceName() + "' not found! Please check configuration");
        }
        if (datasource2 == null) {
            throw new TestException(
                    "Datasource '" + sql2.getDatasourceName() + "' not found! Please check configuration");
        }

        Connection connection1 = null;
        Connection connection2 = null;
        try {
            connection1 = DataSource.getConnection(datasource1.getName());
            connection2 = DataSource.getConnection(datasource2.getName());
            return getTestResults(connection1, connection2, testParams);
        } catch (Exception e) {
            throw e;
        } finally {
            DataSource.closeConnection(connection1);
            DataSource.closeConnection(connection2);
        }
    }

    private TestResults getTestResults(Connection connection1, Connection connection2, TestParams testParams) throws Exception {
        Compare compare = testParams.getCmpSqlResultsTest().getCompare();
        PreparedStatement stmt1 = null;
        PreparedStatement stmt2 = null;
        PrintWriter diffPWriter = null;
        PrintWriter src1PWriter = null;
        PrintWriter src2PWriter = null;
        int diffCounter = 0;
        File diffFileName = getNewFileBasedOnTestConfigFile(testParams.getTestConfigFile(), "_diff.csv");
        try {
            Sql sql1 = compare.getSqls().get(0);
            Sql sql2 = compare.getSqls().get(1);

            stmt1 = connection1.prepareStatement(sql1.getSql(), ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet
                    .CONCUR_READ_ONLY);
            stmt1.setFetchSize(compare.getFetchSize());
            stmt2 = connection2.prepareStatement(sql2.getSql(), ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet
                    .CONCUR_READ_ONLY);
            stmt2.setFetchSize(compare.getFetchSize());

            ResultSet result1 = stmt1.executeQuery();
            ResultSet result2 = stmt2.executeQuery();

            int colCount1 = result1.getMetaData().getColumnCount();
            int colCount2 = result2.getMetaData().getColumnCount();
            if (colCount1 != colCount2) {
                throw new TestException("Queries results have a different number of columns " + colCount1 + " / " +
                        colCount2);
            }

            String executedQuery = "QUERY 1 [" + sql1.getDatasourceName() + "]:\r\n" + sql1.getSql() + "\r\n\r\nQUERY" +
                    " 2 [" + sql2.getDatasourceName() + "]:\r\n" + sql2.getSql();
            executedQuery += "\r\n\r\nFetch size: " + compare.getFetchSize() +
                    ", Chunk size: " + compare.getChunk() +
                    ", Difftable size: " + compare.getDiffTableSize() +
                    ", Delta : " + compare.getDelta() +
                    ", File output: " + compare.isFileOutputOn() + "\r\n";
            TestResults testResults = new TestResults(executedQuery, -1);

            // building columns
            List<String> columns = new ArrayList<>();
            columns.add("DB"); // we add own column with name of datasource
            int colCount = result1.getMetaData().getColumnCount();
            for (int column = 1; column <= colCount; column++) {
                columns.add(result1.getMetaData().getColumnName(column));
            }
            testResults.setColumns(columns);
            // names of columns might be different in second table (maybe log them too?)

            if (compare.isFileOutputOn()) {
                diffPWriter = new PrintWriter(diffFileName);
                src1PWriter = new PrintWriter(
                        getNewFileBasedOnTestConfigFile(testParams.getTestConfigFile(), "_sql1.csv"));
                src2PWriter = new PrintWriter(
                        getNewFileBasedOnTestConfigFile(testParams.getTestConfigFile(), "_sql2.csv"));
            }
            List<List<String>> rows = new ArrayList<>();
            int chunk = compare.getChunk();
            int rowNmb = 0;
            boolean rs1NotEmpty = result1.next(), rs2NotEmpty = result2.next();
            while (rs1NotEmpty || rs2NotEmpty) {
                rowNmb++;
                boolean isRowDiff = false;
                List<String> row1 = new ArrayList<>();
                List<String> row2 = new ArrayList<>();
                // we add datasource column to each row
                row1.add("QUERY 1 [" + sql1.getDatasourceName() + "]");
                row2.add("QUERY 2 [" + sql2.getDatasourceName() + "]");
                for (int column = 1; column <= colCount1; column++) {
                    Object value1 = rs1NotEmpty ? result1.getObject(column) : null;
                    Object value2 = rs2NotEmpty ? result2.getObject(column) : null;
                    // we want NULL to be displayed (to distinguish it from the empty string)
                    if (value1 == null) value1 = "<NULL>";
                    if (value2 == null) value2 = "<NULL>";
                    row1.add(value1.toString());
                    row2.add(value2.toString());
                    if (!equal(value1, value2, compare.getDelta())) {
                        isRowDiff = true;
                    }
                }
                if (rs1NotEmpty) writeRowAsCSV(src1PWriter, row1, true);
                if (rs2NotEmpty) writeRowAsCSV(src2PWriter, row2, true);
                if (isRowDiff) {
                    diffCounter++;
                    if (diffCounter <= compare.getDiffTableSize()) {
                        rows.add(row1);
                        rows.add(row2);
                    }
                    writeRowAsCSV(diffPWriter, row1);
                    writeRowAsCSV(diffPWriter, row2);
                }
                if (chunk > 0 && diffCounter >= chunk) {
                    break;
                }
                rs1NotEmpty = result1.next();
                rs2NotEmpty = result2.next();
            }
            testResults.setRows(rows);
            testResults.setNmbOfRows(diffCounter);
            testResults.setNmbOfComparedRows(rowNmb);
            return testResults;
        } finally {
            if (stmt1 != null)
                try {
                    stmt1.close();
                } catch (SQLException e) {
                    log.error(e);
                }
            if (stmt2 != null)
                try {
                    stmt2.close();
                } catch (SQLException e) {
                    log.error(e);
                }
            if (diffPWriter != null)
                diffPWriter.close();
            if (diffCounter == 0 && compare.isFileOutputOn())
                diffFileName.delete();
            if (src1PWriter != null)
                src1PWriter.close();
            if (src2PWriter != null)
                src2PWriter.close();
        }
    }

    private boolean equal(Object val1, Object val2, BigDecimal delta) {
        if (val1 instanceof Number && val2 instanceof Number) {
            if (isFloatingPoint(val1) && isFloatingPoint(val2)) {
                BigDecimal val1BD = getAsBigDecimal(val1);
                BigDecimal val2BD = getAsBigDecimal(val2);
                if (delta != null && delta != BigDecimal.ZERO) {
                    return val1BD.subtract(val2BD).abs().compareTo(delta) <= 0;
                }
                return val1BD.compareTo(val2BD) == 0;
            } else if (!val1.getClass().equals(val2.getClass())) {
                // not nice solution to compare two Numbers of different types
                // ie. (new Short((short)0)).equals(new Integer(0)) is always false
                return (getAsBigDecimal(val1)).compareTo(getAsBigDecimal(val2)) == 0;
            }
        }
        return val1.toString().equals(val2.toString());
    }

    private boolean isFloatingPoint(Object obj) {
        return (obj.getClass().equals(BigDecimal.class) || obj.getClass().equals(Double.class) || obj.getClass().equals(
                Float.class));
    }

    private BigDecimal getAsBigDecimal(Object obj) {
        return new BigDecimal(obj.toString());
    }

}