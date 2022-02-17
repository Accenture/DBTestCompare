// <copyright file="RunTests.java" company="Objectivity Bespoke Software Specialists">
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

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;
import org.testng.TestNG;
import org.testng.xml.XmlSuite;

import uk.co.objectivity.test.db.beans.xml.CmpSqlResultsConfig;
import uk.co.objectivity.test.db.beans.xml.Filter;
import uk.co.objectivity.test.db.utils.DataSource;
import uk.co.objectivity.test.db.utils.Printer;

public class RunTests {

    private static final Logger log = Logger.getLogger(RunTests.class);

    private static final String PROP_TESTS_DIR = "testsDir";
    private static final String PROP_TC_LOGS_ENABLED = "teamcityLogsEnabled";
    private static final String PROP_FILTER_INCLUDE = "filterInclude";
    private static final String PROP_FILTER_EXCLUDE = "filterExclude";

    private static String TEST_DIR = "test-definitions";
    private static final String CONFIG_FILE_NAME = "/cmpSqlResults-config.xml";
    private static String MAIN_CONFIG_FILE_PATH = TEST_DIR + CONFIG_FILE_NAME;

    public static void main(String[] args) {
        displayHelpMessage();
        String testDir = System.getProperty(PROP_TESTS_DIR);
        if (testDir != null) {
            TEST_DIR = testDir;
            MAIN_CONFIG_FILE_PATH = testDir + CONFIG_FILE_NAME;
        }

        CmpSqlResultsConfig cmpSqlResultsConfig = readConfigAndInit();
        if (cmpSqlResultsConfig == null) return;

        // we do not need to validate cmpSqlResultsConfig.getThreads() - thanks to JAXB and TestNG
        runTestNG(cmpSqlResultsConfig.getThreads(), cmpSqlResultsConfig.getFilter());

        if (!DBTestCompare.ALL_TESTS_SUCCEEDED) {
            System.exit(cmpSqlResultsConfig.getTestFailureExitCode());
        }
    }

    static CmpSqlResultsConfig readConfigAndInit() {
        log.debug("Working Directory = " + System.getProperty("user.dir"));
        log.debug("Scanning tests directory: \"" + TEST_DIR + "\" ...");
        File testsDirFile = new File(TEST_DIR);
        if (!testsDirFile.exists()) {
            log.error("Tests directory does not exists! Please create directory: " + testsDirFile.getAbsolutePath());
            return null;
        }
        CmpSqlResultsConfig cmpSqlResultsConfig = readConfiguration();
        if (cmpSqlResultsConfig == null) {
            log.error("Errors while reading configuration file: " + MAIN_CONFIG_FILE_PATH);
            return null;
        }
        log.debug("Number of Threads set in configuration: " + cmpSqlResultsConfig.getThreads());
        log.debug("Initializing all components ...");
        DataSource.init(cmpSqlResultsConfig.getDatasources()); // it also adds shutdown hook (closing connections)
        TestDataProvider.init(testsDirFile, cmpSqlResultsConfig);
        Printer.init(cmpSqlResultsConfig);
        return cmpSqlResultsConfig;
    }

    private static void runTestNG(int threadCount, Filter filter) {
        TestNG testng = new TestNG();
        testng.setTestClasses(new Class[]{DBTestCompare.class});
        String suiteName = "Compare SQL results suite";
        if (filter != null && ((filter.getIncludes() != null && !filter.getIncludes().isEmpty()) || (filter.getExcludes()
                != null && !filter.getExcludes().isEmpty()))) {
            suiteName = "FILTERED compare SQL results suite";
        }
        testng.setDefaultSuiteName(suiteName);
        testng.setPreserveOrder(true);
        testng.setParallel(XmlSuite.ParallelMode.INSTANCES);
        testng.setThreadCount(threadCount);
        // testng.setDataProviderThreadCount(cmpSqlResultsConfig.getThreads());
        testng.run();
    }

    private static CmpSqlResultsConfig readConfiguration() {
        try {
            log.debug("Reading configuration: " + MAIN_CONFIG_FILE_PATH);
            File file = new File(MAIN_CONFIG_FILE_PATH);
            JAXBContext jaxbContext = JAXBContext.newInstance(CmpSqlResultsConfig.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            CmpSqlResultsConfig cmpSqlResultsConfig = (CmpSqlResultsConfig) jaxbUnmarshaller.unmarshal(file);
            // user can override some properties from command line
            String tcLog = System.getProperty(PROP_TC_LOGS_ENABLED);
            if (tcLog != null) {
                cmpSqlResultsConfig.getLogger().setTeamcityLogsEnabled("true".equals(tcLog));
            }
            String filterInc = System.getProperty(PROP_FILTER_INCLUDE);
            if (filterInc != null) {
                cmpSqlResultsConfig.getFilter().setIncludesString(filterInc);
            }
            String filterExc = System.getProperty(PROP_FILTER_EXCLUDE);
            if (filterExc != null) {
                cmpSqlResultsConfig.getFilter().setExcludesString(filterExc);
            }
            cmpSqlResultsConfig.getFilter().trim();
            return cmpSqlResultsConfig;
        } catch (JAXBException e) {
            log.error(e);
            return null;
        }
    }

    private static void displayHelpMessage() {
        log.info("You can override some of the application configuration properties\r\nRun app with:\r\n" +
                " -D" + PROP_TESTS_DIR + "=path            - set tests directory (default: " + TEST_DIR + ")\r\n" +
                " -D" + PROP_TC_LOGS_ENABLED + "=true - log test output in TeamCity format\r\n" +
                " -D" + PROP_FILTER_INCLUDE + "=a.b,g.z.f  - comma separated directories or test files which " +
                "you want to include\r\n" +
                " -D" + PROP_FILTER_EXCLUDE + "=a.b.test   - comma separated directories or test files " +
                "which you want to exclude");
    }

}
