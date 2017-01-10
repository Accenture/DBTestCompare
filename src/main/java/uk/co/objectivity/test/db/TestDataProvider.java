// <copyright file="TestDataProvider.java" company="Objectivity Bespoke Software Specialists">
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

package uk.co.objectivity.test.db;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;
import org.testng.ITestContext;
import org.testng.TestException;
import org.testng.annotations.DataProvider;

import uk.co.objectivity.test.db.beans.CompareMode;
import uk.co.objectivity.test.db.beans.TestParams;
import uk.co.objectivity.test.db.beans.xml.CmpSqlResultsConfig;
import uk.co.objectivity.test.db.beans.xml.CmpSqlResultsTest;
import uk.co.objectivity.test.db.beans.xml.Compare;
import uk.co.objectivity.test.db.beans.xml.Sql;

public class TestDataProvider {

    private final static Logger log = Logger.getLogger(TestDataProvider.class);

    private static CmpSqlResultsConfig CMP_SQL_RESULTS_CONFIG;
    private static File TESTS_DIR_FILE;
    private static boolean INITIALIZED = false;

    static void init(File testsDirFile, CmpSqlResultsConfig cmpSqlResultsConfig) {
        log.debug("Initializing tests Data Provider...");
        CMP_SQL_RESULTS_CONFIG = cmpSqlResultsConfig;
        TESTS_DIR_FILE = testsDirFile;
        INITIALIZED = true;
    }

    @DataProvider(name = "testsProvider")
    public static Iterator<Object[]> getTestsConfiguration(ITestContext context) throws Exception {
        // tests could be run using not standard runner (check RunTests class) - eg. IntelliJ IDEA. In that case we
        // need to read default configuration in the same way as RunTests does it. Tests run by other runner will
        // be executed in 1 thread (configuration parameter <threads/> will be ignored).
        if (!INITIALIZED && RunTests.readConfigAndInit() == null) {
            throw new TestException("Can not read tests configuration");
        }
        log.debug("Reading tests configuration...");
        Collection<Object[]> dpParams = new ArrayList<>();

        File[] listOfFiles = TESTS_DIR_FILE.listFiles();
        if (listOfFiles == null) {
            throw new FileNotFoundException("Problems with listing directory " + TESTS_DIR_FILE.getAbsolutePath());
        }
        for (File file : listOfFiles) {
            if (file.isDirectory()) {
                processTestFiles(file, file.getName(), dpParams);
            }
        }
        log.debug("Running tests...");
        return dpParams.iterator();
    }

    private static void processTestFiles(File startDir, String testNamePrefix, Collection<Object[]> dpParams) {
        for (File file : startDir.listFiles()) {
            if (file.isDirectory()) {
                String testName = testNamePrefix + "." + file.getName();
                processTestFiles(file, testName, dpParams);
            } else if (file.getName().toLowerCase().endsWith(".xml")) {
                String testName = testNamePrefix + "." + file.getName().substring(0, file.getName().lastIndexOf('.'));
                if ((isDirOnWhiteList(testNamePrefix) || isTestOnWhiteList(testName)) && !isOnBlackList(testNamePrefix)
                        && !isOnBlackList(testName)) {
                    addTest(file, testName, dpParams);
                }
            }
        }
    }

    private static void addTest(File xmlConfigFile, String testName, Collection<Object[]> dpParams) {
        TestParams testParam = new TestParams(testName);
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(CmpSqlResultsTest.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            CmpSqlResultsTest cmpSqlResultsTest = (CmpSqlResultsTest) jaxbUnmarshaller.unmarshal(xmlConfigFile);
            validateTestConfig(cmpSqlResultsTest, xmlConfigFile, testParam);
            testParam.setCmpSqlResultsTest(cmpSqlResultsTest);
            testParam.setTestConfigFile(xmlConfigFile);
        } catch (Exception e) {
            String msg = "Errors while parsing file " + xmlConfigFile.getAbsolutePath() + (e.getMessage() != null ? ". " +
                    e.getMessage() : "");
            log.error(msg, e);
            testParam.setSkipTestMessage(msg);
        }
        testParam.setCmpSqlResultsConfig(CMP_SQL_RESULTS_CONFIG);
        dpParams.add(new Object[]{testParam});
    }

    private static boolean isDirOnWhiteList(String dirName) {
        List<String> includes = CMP_SQL_RESULTS_CONFIG.getFilter().getIncludes();
        // empty filter list means all tests from dirName are on the white list
        if (includes == null || includes.isEmpty() || includes.contains(dirName)) return true;
        int dotIndex = dirName.lastIndexOf('.');
        if (dotIndex < 0) {
            return false;
        }
        String shorterDirName = dirName.substring(0, dotIndex);
        return isDirOnWhiteList(shorterDirName);
    }

    private static boolean isTestOnWhiteList(String testName) {
        List<String> includes = CMP_SQL_RESULTS_CONFIG.getFilter().getIncludes();
        return (includes != null && !includes.isEmpty() && includes.contains(testName));
    }

    private static boolean isOnBlackList(String dirName) {
        if (dirName == null || dirName.isEmpty()) return true;
        List<String> excludes = CMP_SQL_RESULTS_CONFIG.getFilter().getExcludes();
        if (excludes != null) {
            for (String exclude : excludes) {
                if (dirName.startsWith(exclude))
                    return true;
            }
        }
        return false;
    }

    /**
     * There is no schema yet - so we do VERY simple validation in this (not nice) way...
     */
    private static void validateTestConfig(CmpSqlResultsTest cmpSqlResultsTest, File xmlConfigFile, TestParams testParam) {
        if (cmpSqlResultsTest == null) {
            testParam.addSkipTestMessage(
                    "Test config file is empty: " + xmlConfigFile.getAbsolutePath());
        }
        Compare cmp = cmpSqlResultsTest.getCompare();
        if (cmp.getCompareMode() == null) {
            testParam.addSkipTestMessage(
                    "Unknown compare mode in test configuration file " + xmlConfigFile.getAbsolutePath());
        }
        if (cmp.getCompareMode() == CompareMode.MINUS || cmp.getCompareMode() == CompareMode.FETCH) {
            if (cmp.getSqls().size() != 2) {
                testParam.addSkipTestMessage(
                        "Incorrect number of sqls (2 allowed) in test configuration file " + xmlConfigFile.getAbsolutePath());
            } else {
                validateAndPrepareSql(cmp.getSqls().get(0), xmlConfigFile, testParam);
                validateAndPrepareSql(cmp.getSqls().get(1), xmlConfigFile, testParam);
            }
        } else if (cmp.getCompareMode() == CompareMode.FILE || cmp.getCompareMode() == CompareMode.NMB_OF_RESULTS) {
            if (cmp.getSqls().size() != 1) {
                testParam.addSkipTestMessage(
                        "Incorrect number of sqls (1 allowed) in test configuration file " + xmlConfigFile.getAbsolutePath());
            } else {
                validateAndPrepareSql(cmp.getSqls().get(0), xmlConfigFile, testParam);
            }
            if (cmp.getCompareMode() == CompareMode.FILE) {
                if (cmp.getFile() == null || cmp.getFile().getFilename() == null || cmp.getFile().getFilename().trim().isEmpty()) {
                    testParam.addSkipTestMessage(
                            "In compare mode='FILE' you need to specify file with correct filename attrribute: " +
                                    xmlConfigFile.getAbsolutePath());
                }
            }
        }
        if (cmp.getDiffTableSize() < 0 || cmp.getDiffTableSize() > 500) {
            testParam.addSkipTestMessage(
                    "DiffTableSize should be between 1-500 in test configuration file " + xmlConfigFile.getAbsolutePath());

        }
    }

    /**
     * SQL element can contain sql query in body or in file (not both). It is checked here and if sql is from file it
     * is read and set to SQL element body (that is the only source of sql queries for tests - it is transparent from
     * where sqls are obtained).
     *
     * @param sql
     * @param xmlConfigFile
     * @param testParam
     */
    private static void validateAndPrepareSql(Sql sql, File xmlConfigFile, TestParams testParam) {
        if (sql.getFilename() == null && sql.getSql() == null) {
            testParam.addSkipTestMessage(
                    "Element SQL should have the filename attribute or sql value defined. Test file is " +
                            "invalid " + xmlConfigFile.getAbsolutePath());
        } else if ((sql.getFilename() != null && !sql.getFilename().isEmpty()) && (sql.getSql() != null
                && !sql.getSql().isEmpty())) {
            testParam.addSkipTestMessage(
                    "Element SQL should have the filename attribute or sql value defined (NOT both). Test " +
                            "file is invalid " + xmlConfigFile.getAbsolutePath());
        } else {
            // overriding sql body using content of the file
            if (sql.getFilename() != null && !sql.getFilename().isEmpty()) {
                try {
                    // for now encoding does not matter (for our sql files). If it will, it would be better to use i.e.:
                    // org.apache.commons.io.IOUtils
                    byte[] sqlByte = Files.readAllBytes(Paths.get(xmlConfigFile.getParent() + "/" + sql.getFilename()));
                    sql.setSql(new String(sqlByte));
                } catch (IOException e) {
                    testParam.addSkipTestMessage("Problems with reading " + e.getMessage() + ". Test " +
                            "file is invalid " + xmlConfigFile.getAbsolutePath());
                }
            }
        }
    }

}
