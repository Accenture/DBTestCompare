// <copyright file="FileComparator.java" company="Objectivity Bespoke Software Specialists">
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

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
import uk.co.objectivity.test.db.utils.SavedTimes;

import static uk.co.objectivity.test.db.TestDataProvider.savedTimesList;

public class FileComparator extends Comparator {

    private final static Logger log = Logger.getLogger(MinusComparator.class);

    private Pattern csvLinePattern;

    @Override
    public TestResults compare(TestParams testParams) throws Exception {
        CmpSqlResultsConfig cmpSqlResultsConfig = testParams.getCmpSqlResultsConfig();
        CmpSqlResultsTest cmpSqlResultsTest = testParams.getCmpSqlResultsTest();

        // (get(0) - IndexOutOfBoundsException) checked in TestDataProvider validation
        Sql sql = cmpSqlResultsTest.getCompare().getSqls().get(0);
        Datasource datasource = cmpSqlResultsConfig.getDatasourceByName(sql.getDatasourceName());
        if (datasource == null) {
            throw new TestException("Datasource '" + datasource.getName() + "' not found! Please check configuration");
        }

        String fileAbsPath = testParams.getTestConfigFile().getParentFile().getAbsolutePath();
        fileAbsPath += "/" + cmpSqlResultsTest.getCompare().getFile().getFilename(); // null/empty checked in
                                                                                     // TestDataProvider
        File file = new File(fileAbsPath);
        if (!file.exists()) {
            throw new TestException("File '" + file.getAbsolutePath() + "' not found! Please check configuration");
        }
        prepareCSVPattern(cmpSqlResultsTest.getCompare().getFile().getSeparator());

        Connection connection = null;
        try {
            connection = DataSource.getConnection(datasource.getName());
            return getTestResults(testParams, connection, file);
        } catch (Exception e) {
            throw new Exception(e + "\nQuery 1: " + sql.getSql());
        } finally {
            DataSource.closeConnection(connection);
        }
    }

    public TestResults getTestResults(TestParams testParams, Connection conn, File file) throws Exception {
        Compare compare = testParams.getCmpSqlResultsTest().getCompare();
        PreparedStatement stmt = null;
        PrintWriter diffPWriter = null;
        PrintWriter src1PWriter = null;
        BufferedReader bufferedReader = null;
        int diffCounter = 0;
        SavedTimes savedTimes1 = new SavedTimes(testParams.getTestName());
        SavedTimes savedTimes2 = new SavedTimes(testParams.getTestName());
        File diffFileName = getNewFileBasedOnTestConfigFile(testParams.getTestConfigFile(), "_diff.csv");
        try {
            Sql sql = compare.getSqls().get(0);
            stmt = conn.prepareStatement(sql.getSql(), ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            savedTimes1.startMeasure("Query " + sql.getDatasourceName());
            ResultSet rs = stmt.executeQuery();
            savedTimes1.stopMeasure();
            savedTimesList.add(savedTimes1);

            savedTimes2.startMeasure("File " + file.getName());
            bufferedReader = new BufferedReader(new FileReader(file));
            int curLineNr = 0;
            if (compare.getFile().getStartAtRow() - 1 > 0) {
                while (bufferedReader.readLine() != null) {
                    if (curLineNr++ >= compare.getFile().getStartAtRow() - 2)
                        break;
                }
            }

            String[] csvRow = getCSVFileRow(bufferedReader.readLine());

            int qColCount = rs.getMetaData().getColumnCount();
            int fColCount = csvRow == null ? 0 : csvRow.length;
            if (qColCount != fColCount) {
                throw new TestException("Query results and file have a different number of columns (" + qColCount
                        + " / " + fColCount + ")");
            }

            if (compare.isFileOutputOn()) {
                diffPWriter = new PrintWriter(diffFileName);
                src1PWriter = new PrintWriter(
                        getNewFileBasedOnTestConfigFile(testParams.getTestConfigFile(), "_sql1.csv"));
            }

            String executedQuery = "QUERY 1 [" + sql.getDatasourceName() + "]:\r\n" + sql.getSql() + "\r\n\r\nFile"
                    + " [" + file.getAbsolutePath() + "]\r\n";
            executedQuery += "\r\n\r\nChunk size: " + compare.getChunk() + ", Difftable size: "
                    + compare.getDiffTableSize() + ", Comparing with file start at row "
                    + compare.getFile().getStartAtRow() + ", File output: " + compare.isFileOutputOn() + "\r\n"
                    + "Time execution of query:\n" + savedTimes1.getMeasureType() + " "
                    + savedTimes1.getFormattedDuration();
            TestResults testResults = new TestResults(executedQuery, -1);

            // building columns
            List<String> columns = new ArrayList<>();
            columns.add("DB / FILE"); // we add own column with name of datasource
            int colCount = rs.getMetaData().getColumnCount();
            for (int column = 1; column <= colCount; column++) {
                columns.add(rs.getMetaData().getColumnName(column));
            }
            testResults.setColumns(columns);

            // building rows
            List<List<String>> rows = new ArrayList<>();
            int chunk = compare.getChunk();
            int rowNmb = 0;
            boolean rsNotEmpty = rs.next();
            boolean fileNotEmpty = csvRow != null;
            while (rsNotEmpty || fileNotEmpty) {
                rowNmb++;
                boolean isRowDiff = false;
                List<String> row1 = new ArrayList<>();
                List<String> row2 = new ArrayList<>();
                // we add datasource column to each row
                row1.add("QUERY 1 [" + sql.getDatasourceName() + "]");
                row2.add("FILE    [" + file.getName() + "]");
                for (int column = 1; column <= qColCount; column++) {
                    Object value1 = rsNotEmpty ? rs.getObject(column) : null;
                    // column <= csvRow.length means that CSV file is incorrect, but we try to
                    // handle this.
                    String value2 = (fileNotEmpty && column <= csvRow.length) ? csvRow[column - 1] : null;
                    // we want NULL to be displayed (to distinguish it from the empty string)
                    if (value1 == null)
                        value1 = "<NULL>";
                    if (value2 == null)
                        value2 = "<NULL>";

                    if (!value2.equals(value1.toString())) {
                        isRowDiff = true;
                        value1 = "<DIFF>" + value1;
                        value2 = "<DIFF>" + value2;
                    }
                    row1.add(value1.toString());
                    row2.add(value2.toString());
                }
                if (rsNotEmpty)
                    writeRowAsCSV(src1PWriter, row1, true);
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

                rsNotEmpty = rs.next();
                fileNotEmpty = (csvRow = getCSVFileRow(bufferedReader.readLine())) != null;
            }
            testResults.setRows(rows);
            testResults.setNmbOfRows(diffCounter);
            testResults.setNmbOfComparedRows(rowNmb);
            savedTimes1.setNumberOfComparedRows(String.valueOf(rowNmb));
            savedTimes2.setNumberOfComparedRows(String.valueOf(rowNmb));
            if (diffCounter == 0) {
                savedTimes1.setTestResult("Passed");
                savedTimes2.setTestResult("Passed");
            }
            return testResults;
        } finally {
            savedTimes2.stopMeasure();
            savedTimesList.add(savedTimes2);
            if (stmt != null)
                try {
                    stmt.close();
                } catch (SQLException e) {
                    log.error(e);
                }
            if (diffPWriter != null)
                diffPWriter.close();
            if (diffCounter == 0 && compare.isFileOutputOn())
                diffFileName.delete();
            if (src1PWriter != null)
                src1PWriter.close();
            if (bufferedReader != null)
                bufferedReader.close();
        }
    }

    private String[] getCSVFileRow(String csvLine) {
        if (csvLine == null || csvLine.isEmpty())
            return null;
        String[] results = csvLinePattern.split(csvLine);
        for (int i = 0; i < results.length; i++) {
            if (results[i] != null && !results[i].isEmpty()) {
                results[i] = results[i].replaceAll("^\"|\"$", "").replaceAll("\"\"", "\"");
            }
        }
        return results;
    }

    private void prepareCSVPattern(String separator) {
        if (separator == null || separator.isEmpty()) {
            separator = ",";
        }
        csvLinePattern = Pattern.compile("\\Q" + separator + "\\E(?=(?:(?:[^\"]*\"){2})*[^\"]*$)");
    }

}
