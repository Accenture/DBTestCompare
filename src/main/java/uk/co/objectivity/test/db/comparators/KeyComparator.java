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
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.log4j.Logger;
import org.testng.TestException;

import uk.co.objectivity.test.db.beans.FileRow;
import uk.co.objectivity.test.db.beans.TestParams;
import uk.co.objectivity.test.db.beans.TestResults;
import uk.co.objectivity.test.db.beans.xml.BeforeSqls;
import uk.co.objectivity.test.db.beans.xml.CmpSqlResultsConfig;
import uk.co.objectivity.test.db.beans.xml.CmpSqlResultsTest;
import uk.co.objectivity.test.db.beans.xml.Compare;
import uk.co.objectivity.test.db.beans.xml.Datasource;
import uk.co.objectivity.test.db.beans.xml.Sql;
import uk.co.objectivity.test.db.beans.xml.Transformer;
import uk.co.objectivity.test.db.comparators.printer.CompareResult;
import uk.co.objectivity.test.db.comparators.printer.ExcelPrinter;
import uk.co.objectivity.test.db.comparators.printer.Headers;
import uk.co.objectivity.test.db.comparators.results.DBResults;
import uk.co.objectivity.test.db.utils.DataSource;

@SuppressWarnings("Duplicates")
public class KeyComparator extends Comparator {

    private final static Logger log = Logger.getLogger(KeyComparator.class);

    private Pattern csvLinePattern;

    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");

    @Override
    public TestResults compare(TestParams testParams) throws Exception {
        CmpSqlResultsConfig cmpSqlResultsConfig = testParams.getCmpSqlResultsConfig();
        CmpSqlResultsTest cmpSqlResultsTest = testParams.getCmpSqlResultsTest();

        if (cmpSqlResultsTest.getCompare().getKeyColumns() == null)
            throw new TestException("Key Columns not defined! Please set keyColumns attribute");
        List<Integer> keyColumns = getColumnNumbers(cmpSqlResultsTest.getCompare().getKeyColumns());

        Sql sql = cmpSqlResultsTest.getCompare().getSqls().get(0);
        Datasource datasource = cmpSqlResultsConfig.getDatasourceByName(sql.getDatasourceName());
        if (datasource == null) {
            throw new TestException("Datasource not found! Please check configuration");
        }

        String fileAbsPath = testParams.getTestConfigFile().getParentFile().getAbsolutePath();
        fileAbsPath += "/" + cmpSqlResultsTest.getCompare().getFile().getFilename(); // null/empty checked in
                                                                                     // TestDataProvider
        File file = new File(fileAbsPath);
        if (!file.exists()) {
            throw new TestException("File '" + file.getAbsolutePath() + "' not found! Please check configuration");
        }
        if (!sql.getDuplicatesArbitratorColumns().isEmpty()
                && cmpSqlResultsTest.getCompare().getFile().getAggregators() != null)
            throw new TestException(
                    "You can not configure both: duplicatesArbitratorColumn (<sql> attribute) and aggregators (<file> element)");
        prepareLinePatterns(cmpSqlResultsTest.getCompare().getFile().getSeparator());

        Connection connection = null;
        try {
            connection = DataSource.getConnection(datasource.getName());
            return getTestResults(testParams, connection, file, keyColumns);
        } catch (Exception e) {
            throw new Exception(e);
        } finally {
            DataSource.closeConnection(connection);
        }
    }

    public TestResults getTestResults(TestParams testParams, Connection conn, File testFile, List<Integer> keyColumns)
            throws Exception {
        Compare compare = testParams.getCmpSqlResultsTest().getCompare();
        BufferedReader bufferedReader = null;
        AtomicInteger diffCounter = new AtomicInteger();

        File diffFileName = getNewFileBasedOnTestConfigFile(testParams.getTestConfigFile(), "_diff_results.csv");
        File diffKeysFileName = getNewFileBasedOnTestConfigFile(testParams.getTestConfigFile(), "_diff_keys.csv");
        File matchFileName = getNewFileBasedOnTestConfigFile(testParams.getTestConfigFile(), "_match_results.csv");
        PrintWriter diffPWriter = new PrintWriter(diffFileName);
        PrintWriter matchPWriter = new PrintWriter(matchFileName);
        PrintWriter diffKeysWriter = new PrintWriter(diffKeysFileName);

        PrintWriter src1PWriter = new PrintWriter(
                getNewFileBasedOnTestConfigFile(testParams.getTestConfigFile(), "_SQL_results.csv"));
        Sql sql = compare.getSqls().get(0);

        String absentIndicator = compare.getFile().getAbsentIndicator();
        try {
            String executedQuery = "QUERY 1 [" + sql.getDatasourceName() + "]:\r\n" + sql.getSql() + "\r\n\r\nFile"
                    + " [" + testFile.getAbsolutePath() + "]\r\n";
            executedQuery += "\r\n\r\nDifftable size: " + compare.getDiffTableSize() + ", Key columns: "
                    + compare.getKeyColumns() + ", Ignored columns: " + compare.getFile().getIgnoredColumns()
                    + ", Empty String To Null (DB results): " + sql.isEmptyStringToNull() + ", File start at row "
                    + compare.getFile().getStartAtRow() + ", File output: true (forced)";
            TestResults testResults = new TestResults(executedQuery, -1);

            bufferedReader = new BufferedReader(new FileReader(testFile));
            int curLineNr = 0;
            if (compare.getFile().getStartAtRow() - 1 > 0) {
                while (bufferedReader.readLine() != null) {
                    if (curLineNr++ >= compare.getFile().getStartAtRow() - 2)
                        break;
                }
            }

            Map<String, String> testSetsMap = compare.getFile().getTestSetColumn() != null ? new HashMap<>() : null;
            Map<String, FileRow> fileRowsMap = getFileRowsMap(bufferedReader, keyColumns, compare.getFile(), testSetsMap,
                    sql.getDuplicatesArbitratorColumns().size() > 0);

            createKeyDBTable(conn, sql, fileRowsMap, testResults);
            List<Sql> sQLQuery = createBeforeSql(testParams, conn, compare);
            Headers headers = new Headers();
            DBResults dbResults = null;
            Statement stmt = null;
            try {
                stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql.getSql());
                ResultSetMetaData md = rs.getMetaData();
                int colCount = md.getColumnCount();
                for (int column = 1; column <= colCount; column++) {
                    headers.addHeader(md.getColumnName(column));
                }
                headers.addExtendedHeader("Source");
                if (testSetsMap != null) {
                    headers.addExtendedHeader("Thread No");
                }
                dbResults = getDBResults(rs, keyColumns, sql.getDuplicatesArbitratorColumns(), sql.getDateFormat());
            } catch (Exception e) {
                log.error(e);
            } finally {
                if (stmt != null)
                    try {
                        stmt.close();
                    } catch (SQLException e) {
                        log.error(e);
                    }
            }

            int dbColCount = headers.getHeaders().size();
            int fColCount = fileRowsMap.isEmpty() ? 0
                    : fileRowsMap.entrySet().stream().findFirst().get().getValue().getAllRows().get(0).size();
            if (dbColCount != fColCount) {
                throw new TestException("Query results and file have a different number of columns (" + dbColCount
                        + " / " + fColCount + ")");
            }
            if (dbResults.isArbitratorSet() && (dbColCount < dbResults.getMaxArbitratorColumn()
                    || fColCount < dbResults.getMaxArbitratorColumn())) {
                throw new TestException("Number of columns (in the file or DB results) is lesser than "
                        + dbResults.getMaxArbitratorColumn()
                        + ". Check your test configuration (duplicatesArbitratorColumns parameter)");
            }

            dbResults.getAll().stream().forEach(dbr -> writeRowAsCSV(src1PWriter, dbr.getRow()));
            writeKeyAsCSV(keyColumns, diffKeysWriter, headers.getExtendedHeaders(), 0);
            log.info("Rows from db: " + dbResults.getAll().size());

            List<CompareResult> diffCmpResult = new ArrayList<>();
            List<CompareResult> matchCmpResult = new ArrayList<>();
            List<CompareResult> poCmpResult = new ArrayList<>();
            int rowNmb = 0;
            Iterator<Map.Entry<String, FileRow>> fileRowsIt = fileRowsMap.entrySet().iterator();

            while (fileRowsIt.hasNext()) {
                Map.Entry<String, FileRow> csvMapEntry = fileRowsIt.next();
                List<List<String>> csvSameKeyRows = new ArrayList<>();
                if (!sql.getDuplicatesArbitratorColumns().isEmpty() || compare.getFile().getAggregators() != null
                        && !compare.getFile().getAggregators().getAggregators().isEmpty()) {
                    csvSameKeyRows.addAll(csvMapEntry.getValue().getRows());
                } else {
                    csvSameKeyRows.addAll(csvMapEntry.getValue().getAllRows());
                }

                Iterator<List<String>> it = csvSameKeyRows.iterator();
                log.debug("CSV same key rows " + csvSameKeyRows.size());
                while (it.hasNext()) {
                    List<String> csvRow = it.next();

                    rowNmb++;
                    boolean isRowDiff = false;
                    List<String> rowFile = new ArrayList<>();
                    List<String> rowDB = new ArrayList<>();
                    // we add datasource column to each row
                    rowFile.add("FILE  [" + testFile.getName() + "]");
                    rowDB.add("QUERY [" + sql.getDatasourceName() + "]");

                    String key = csvMapEntry.getKey();
                    String value1 = "";

                    Optional<List<String>> dbRow = dbResults.getRow(key, csvRow);

                    for (int column = 1; column <= dbColCount; column++) {
                        // we want NULL to be displayed (to distinguish it from the empty string)
                        // column <= csvRow.length means that CSV file is incorrect, but we try to
                        // handle this.
                        value1 = (column <= csvRow.size()) ? csvRow.get(column - 1) : "<NULL>";
                        List<String> dbRowList = dbRow.orElseGet(Collections::emptyList);
                        String value2 = (dbRowList.size() >= column) ? dbRowList.get(column - 1) : "<NULL>";
                        boolean isCellDiff = false;
                        if (value1 == null) {
                            value1 = "<NULL>";
                        }
                        if (value2 == null || (sql.isEmptyStringToNull() && value2.trim().isEmpty()))
                            value2 = "<NULL>";

                        if (!equal(value1, value2)) {
                            isRowDiff = isCellDiff = true;
                        }

                        if (isCellDiff) {
                            value1 = "<DIFF>" + value1;
                            value2 = "<DIFF>" + value2;
                        }

                        rowFile.add(value1);
                        rowDB.add(value2);
                    }

                    if (isRowDiff) {
                        diffCounter.getAndIncrement();
                        diffCmpResult.add(new CompareResult(key, rowFile, rowDB,CompareResult.Test.CHECK_ALL_COLUMNS));
                        writeRowAsCSV(diffPWriter, rowFile);
                        writeRowAsCSV(diffPWriter, rowDB);
                        writeKeyAsCSV(keyColumns, diffKeysWriter, csvRow, 1);
                    } else {
                        matchCmpResult.add(new CompareResult(key, rowFile, rowDB, CompareResult.Test.PASSED));
                        writeRowAsCSV(matchPWriter, rowFile);
                        writeRowAsCSV(matchPWriter, rowDB);
                    }
                }
            }

            List<String> emptyRowFile = new ArrayList<>();
            emptyRowFile.add("FILE  [" + testFile.getName() + "]");
            for (int column = 1; column <= dbColCount; column++) {
                emptyRowFile.add("<DIFF><NULL>");
            }
            dbResults.getUnmached().stream().forEach(dbRow -> {
                List<String> diffDBRow = dbRow.getRow().stream().map(s -> "<DIFF>" + s).collect(Collectors.toList());
                diffDBRow.add(0, "QUERY [" + sql.getDatasourceName() + "]");
                diffCounter.getAndIncrement();
                diffCmpResult
                        .add(new CompareResult(dbRow.getKey(), emptyRowFile, diffDBRow, CompareResult.Test.FAILED));
                writeRowAsCSV(diffPWriter, emptyRowFile);
                writeRowAsCSV(diffPWriter, diffDBRow);
                writeKeyAsCSV(keyColumns, diffKeysWriter, emptyRowFile, 1); // csvRow
            });

            compare.getSqls().addAll(sQLQuery);
            ExcelPrinter excel = new ExcelPrinter(createExcelFile(testParams), compare);
            excel.saveExcelEvidence(headers, testFile, diffCmpResult, matchCmpResult, poCmpResult, dbResults,
                    fileRowsMap.values(), testSetsMap, new Integer[] {});

            fillTestResults(compare, diffCounter.get(), testResults, headers, diffCmpResult, rowNmb);
            return testResults;
        } finally {
            closePrinters(diffPWriter, matchPWriter, diffKeysWriter, src1PWriter);
            if (bufferedReader != null)
                bufferedReader.close();

        }
    }

    private List<Sql> createBeforeSql(TestParams testParams, Connection conn, Compare compare) throws SQLException {

        List<Sql> sQLQuery = new ArrayList<Sql>();

        List<BeforeSqls> beforeSqls = compare.getBeforeSqls();
        if (null != beforeSqls) {
            BeforeSqls beforeSql = beforeSqls.get(0);

            if (null != beforeSql) {
                List<Sql> sqls = beforeSql.getSqls();
                if (null != sqls) {

                    String fileAbsPath = testParams.getTestConfigFile().getParentFile().getAbsolutePath();

                    Iterator<Sql> itr = sqls.iterator();
                    while (itr.hasNext()) {
                        Sql localSql = (Sql) itr.next();
                        createAdditionalSql(conn, localSql, fileAbsPath);
                        sQLQuery.add(localSql);
                    }
                }
            }
        }
        return sQLQuery;
    }

    private void fillTestResults(Compare compare, int diffCounter, TestResults testResults, Headers headers,
            List<CompareResult> diffCmpResult, int rowNmb) {
        testResults.setColumns(headers.getExtendedHeaders());
        testResults.setRows(diffCmpResult.stream().limit(compare.getDiffTableSize()).map(cr -> cr.getRows())
                .flatMap(List::stream).collect(Collectors.toList()));
        testResults.setNmbOfRows(diffCounter);
        testResults.setNmbOfComparedRows(rowNmb);
    }

    private void closePrinters(PrintWriter diffPWriter, PrintWriter matchPWriter, PrintWriter diffKeysWriter,
            PrintWriter src1PWriter) {
        if (diffPWriter != null)
            diffPWriter.close();
        if (matchPWriter != null)
            matchPWriter.close();
        if (diffKeysWriter != null)
            diffKeysWriter.close();
        if (src1PWriter != null)
            src1PWriter.close();
    }

    private File createExcelFile(TestParams testParams) {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        return getNewFileBasedOnTestConfigFile(testParams.getTestConfigFile(),
                "_" + sdf.format(timestamp) + "_evidence.xls");
    }

    private void createAdditionalSql(Connection conn, Sql sql, String fileAbsPath) throws SQLException {

        Statement stmt = null;
        String pathWithFileName = fileAbsPath + "/" + sql.getFilename();
        try {

            File file = new File(pathWithFileName);
            BufferedReader buf = new BufferedReader(new FileReader(file));
            String line = buf.readLine();

            StringBuilder sb = new StringBuilder();
            while (line != null) {
                sb.append(line).append("\n");
                line = buf.readLine();
            }

            buf.close();
            sql.setSql(sb.toString());

            stmt = conn.createStatement();

            try {
                stmt.executeUpdate(sql.getSql());
            } catch (Exception sqle) {
                log.info(sqle);
            }

            // TODO ensure if it is necessary
            if (conn.getMetaData().getURL().contains("jdbc:simba"))
                stmt.executeUpdate("COMMIT WORK");

        } catch (FileNotFoundException ex) {
            throw new TestException("File '" + pathWithFileName + "' not found! Please check configuration");
        } catch (IOException e) {
            throw new TestException("Cannot read the SQL content file '" + pathWithFileName + "' ");
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    log.error(e);
                }
            }
        }
    }

    private void createKeyDBTable(Connection conn, Sql sql, Map<String, FileRow> fileRowsMap, TestResults tr) throws SQLException {
        if (sql.getKeyTableName() != null && !sql.getKeyTableName().isEmpty()) {
            Statement stmt = null;
            String insert = null;
            try {
                stmt = conn.createStatement();
                try {
                    String drop = "DROP TABLE " + sql.getKeyTableName();
                    tr.appendOutput(drop);
                    log.info(drop);
                    stmt.execute(drop);
                } catch (Exception sqle) {
                    log.info(sqle);
                }
                String create = "CREATE TABLE " + sql.getKeyTableName() + " (" + sql.getKeyTableColumns() + ");";
                tr.appendOutput(create);
                log.trace(create);
                stmt.execute(create);

                Iterator<FileRow> it = fileRowsMap.values().iterator();
                while (it.hasNext()) {
                    List<String> s = it.next().getAggregatedRowKeyVals();
                    s.replaceAll(element -> ("\'" + element + "\'"));
                    insert= "INSERT INTO " + sql.getKeyTableName() + " VALUES (" + StringUtils.join(s, ", ") + ")";
                    tr.appendOutput(insert);
                    log.info(insert);
                    stmt.executeUpdate(insert);
                }
                // TODO ensure if it is necessary
                if (conn.getMetaData().getURL().contains("jdbc:simba"))
                    stmt.executeUpdate("COMMIT WORK");
            } finally {
                if (stmt != null)
                    try {
                        stmt.close();
                    } catch (SQLException e) {
                        log.error(e);
                    }
            }
        }
    }

    private void writeKeyAsCSV(List<Integer> keyColumns, PrintWriter diffKeysWriter, List<String> row, int shift) {
        List<String> keyHeadersRow = new ArrayList<>();
        for (Integer keyColumn : keyColumns) {
            keyHeadersRow.add(row.get(keyColumn - shift));
        }
        writeRowAsCSV(diffKeysWriter, keyHeadersRow);
    }

    private String[] getCSVFileRow(String csvLine) {
        if (csvLine == null || csvLine.isEmpty())
            return null;
        String[] results = csvLinePattern.split(csvLine, -1);
        for (int i = 0; i < results.length; i++) {
            if (results[i] != null && !results[i].isEmpty()) {
                results[i] = results[i].replaceAll("^\"|\"$", "").replaceAll("\"\"", "\"");
            } else {
                results[i] = null;
            }
        }
        return results;
    }

    private void prepareLinePatterns(String csvSeparator) {
        if (csvSeparator == null || csvSeparator.isEmpty()) {
            csvSeparator = ",";
        }
        csvLinePattern = Pattern.compile("\\Q" + csvSeparator + "\\E(?=(?:(?:[^\"]*\"){2})*[^\"]*$)");
    }

    private DBResults getDBResults(ResultSet rs, List<Integer> keyColumns, List<Integer> duplicatesArbitratorColumns,
            String dateFormat) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
        DBResults dbResults = new DBResults(duplicatesArbitratorColumns);
        int colCount = rs.getMetaData().getColumnCount();
        while (rs.next()) {
            ArrayList<String> colStringsArr = new ArrayList<>();
            String key = "";
            for (int i = 1; i <= colCount; ++i) {
                String columnStringVal = "";
                if (rs.getObject(i) instanceof java.sql.Date) {
                    columnStringVal = sdf.format(rs.getDate(i));
                } else {
                    columnStringVal = (rs.getObject(i) != null) ? rs.getObject(i).toString() : null;
                }

                if (keyColumns.contains(i))
                    key += "|" + columnStringVal;
                colStringsArr.add(columnStringVal);
            }
            dbResults.add(key, colStringsArr);
        }
        return dbResults;
    }

    private Map<String, FileRow> getFileRowsMap(BufferedReader bufferedReader, List<Integer> keyColumns,
                                                uk.co.objectivity.test.db.beans.xml.File file, Map<String, String> testSetsMap, boolean arbitratorOn)
            throws Exception {
        List<Integer> ignoredColumns = Collections.emptyList();
        if (file.getIgnoredColumns() != null)
            ignoredColumns = getColumnNumbers(file.getIgnoredColumns());

        Map<String, FileRow> fileRowsMap = new TreeMap<>();
        String[] csvRow;
        while ((csvRow = getCSVFileRow(bufferedReader.readLine())) != null) {
            List<String> row = getRowToCompare(ignoredColumns, csvRow);
            List<Transformer> transformers = new ArrayList<Transformer>();
            if (file.getTransformers() != null && file.getTransformers().getTransformers() != null) {
                transformers = file.getTransformers().getTransformers();
            }
            String key = getCsvRowKey(row, keyColumns, transformers);
            if (fileRowsMap.containsKey(key))
                fileRowsMap.get(key).add(row);
            else
                fileRowsMap.put(key, new FileRow(row, keyColumns, transformers, file.getAggregators(), arbitratorOn));
            if (testSetsMap != null) {
                String testSet = csvRow[file.getTestSetColumn() - 1];
                if (testSetsMap.containsKey(key)) {
                    boolean alreadyOnList = Arrays.stream(testSetsMap.get(key).split(","))
                            .anyMatch(s -> s.equals(testSet));
                    if (!alreadyOnList)
                        testSetsMap.put(key, testSetsMap.get(key).concat(",").concat(testSet));
                } else {
                    testSetsMap.put(key, testSet);
                }
            }
        }
        return fileRowsMap;
    }

    private List<String> getRowToCompare(List<Integer> ignoredColumns, String[] csvRow) {
        List<String> row = new ArrayList<>();
        for (int i = 1; i <= csvRow.length; i++) {
            if (!ignoredColumns.contains(i))
                row.add(csvRow[i - 1]);
        }
        return row;
    }

    private String getCsvRowKey(List<String> csvRow, List<Integer> keyColList, List<Transformer> transformers) {
        String key = "";
        for (Integer keyCol : keyColList) {
            String keyPart = csvRow.get(keyCol - 1);
            Optional<String> keyPartTransformed = transformers.stream().filter(t -> t.getColumn().equals(keyCol))
                    .map(t -> t.getTransformer()).map(t -> t.transform(keyPart)).findFirst();
            key += "|" + keyPartTransformed.orElse(keyPart);
        }
        return key;
    }

    private List<Integer> getColumnNumbers(String columnsAsString) {
        Integer[] columns;
        try {
            columns = Stream.of(columnsAsString.split(",")).mapToInt(Integer::parseInt).boxed().toArray(Integer[]::new);
        } catch (NumberFormatException nfe) {
            throw new TestException("Wrong values in the column number configration (" + columnsAsString
                    + ")! Please check keyColumns & " + "ignoredColumns attributes in your test configuration!");
        }
        if (columns.length == 0)
            throw new TestException("Please check keyColumns & ignoredColumns attributes in your test configuration!");
        return Arrays.asList(columns);
    }

    private static boolean equal(String val1, String val2) {
        if (val1.matches(".*[a-zA-Z]+.*")) {
            return val1.equals(val2);
        }
        if (NumberUtils.isCreatable(val1) && NumberUtils.isCreatable(val2)) {
            BigDecimal val1BD = NumberUtils.createBigDecimal(val1);
            BigDecimal val2BD = NumberUtils.createBigDecimal(val2);
            return val1BD.compareTo(val2BD) == 0;
        }
        return val1.equals(val2);
    }

}
