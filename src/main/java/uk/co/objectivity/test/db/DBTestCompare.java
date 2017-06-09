// <copyright file="DBTestCompare.java" company="Objectivity Bespoke Software Specialists">
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

import static java.lang.System.currentTimeMillis;
import static uk.co.objectivity.test.db.TestDataProvider.savedTimesList;

import java.lang.reflect.Field;
import java.util.*;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.MDC;
import org.nocrala.tools.texttablefmt.BorderStyle;
import org.nocrala.tools.texttablefmt.CellStyle;
import org.nocrala.tools.texttablefmt.ShownBorders;
import org.nocrala.tools.texttablefmt.Table;
import org.testng.*;
import org.testng.annotations.*;
import org.testng.internal.BaseTestMethod;

import uk.co.objectivity.test.db.beans.CompareMode;
import uk.co.objectivity.test.db.beans.TestParams;
import uk.co.objectivity.test.db.beans.TestResults;
import uk.co.objectivity.test.db.beans.xml.Compare;
import uk.co.objectivity.test.db.utils.Printer;
import uk.co.objectivity.test.db.utils.SavedTimes;
import uk.co.objectivity.test.db.utils.TCMessages;

public class DBTestCompare implements ITest {

    private final static Logger log = Logger.getLogger(DBTestCompare.class);
    public static boolean ALL_TESTS_SUCCEEDED = true;

    private TestParams testParams;

    @Factory(dataProvider = "testsProvider", dataProviderClass = TestDataProvider.class)
    public DBTestCompare(TestParams testParams) {
        this.testParams = testParams;
    }

    public String getTestName() {
        return testParams.getTestName();
    }

    // to consider: setting Test timeout (i.e. 1200000ms = 20m)
    // @Test(timeOut = 1200000)
    @Test
    public void testSQLResults() {
        long startTime = currentTimeMillis();

        TCMessages tcMsgs = new TCMessages(getTestName(), true);
        MDC.put("testName", ":" + getTestName());
        log.info("Starting test...");

        TestResults testResults = null;
        Compare compare = null;
        try {
            if (testParams.isMarkedToSkip()) {
                throw new TestException(testParams.getSkipTestMessage());
            }
            compare = testParams.getCmpSqlResultsTest().getCompare();
            testResults = compare.getCompareMode().getComparator().compare(testParams);
            logInfo2All("Executed query: \r\n" + testResults.getExecutedQuery(), tcMsgs);

            // assertions
            if (compare.getCompareMode() == CompareMode.NMB_OF_RESULTS) {
                List<uk.co.objectivity.test.db.beans.xml.Assert> assertList = compare.getAssertions();
                if (assertList != null) {
                    logInfo2All("Rows count:" + testResults.getNmbOfRows(),tcMsgs);
                    TestResults effFinalTR = testResults;
                    assertList.forEach(a -> logInfo2All(a.getAssertType().name() + " " + a.getValue(), tcMsgs));
                    assertList.forEach(a -> a.getAssertType().assertByType(effFinalTR.getNmbOfRows(), a.getValue()));
                }
            } else {
                Assert.assertEquals(testResults.getNmbOfRows(), Integer.valueOf(0),
                    "Among " + testResults.getNmbOfComparedRows() + " compared rows, some differences in SQL queries results found - ");
            }

            if(compare.getCompareMode() == CompareMode.FETCH || compare.getCompareMode() == CompareMode.FILE){
                logInfo2All("TEST PASSED, Compared rows:" + testResults.getNmbOfComparedRows(),tcMsgs);
            } else {
                logInfo2All("TEST PASSED", tcMsgs);
            }

        } catch (AssertionError ae) {
            String resultsTextTable = null;
            if (compare != null && testResults != null && compare.getCompareMode() != CompareMode.NMB_OF_RESULTS &&
                    compare.getDiffTableSize() > 0) {
                resultsTextTable = Printer.getTextTable(testResults);
                log.info(resultsTextTable);
            }
            logFailed2All(ae, tcMsgs, resultsTextTable);
            throw ae;
        } catch (Exception e) {
            logFailed2All(e, tcMsgs, null);
            // it would be better to skip tests which configuration is wrong, and fail only those which really fails
            // (AssertionError) but fail is more "visible". Maybe user should choose (configuration) if skip or fail
            // on misconfiguration or i.e. DB problems
            // throw new SkipException(e.getMessage());
            throw new TestException(e);
        } finally {
            tcMsgs.appendFinished(System.currentTimeMillis() - startTime);
            log.log(Level.OFF, tcMsgs.get());
        }

    }

    private void logInfo2All(String message, TCMessages tcMsgs) {
        tcMsgs.appendStdOut(message);
        Printer.addReporterLog(message);
        log.info(message);
    }

    private void logFailed2All(Throwable throwable, TCMessages tcMsgs, String additionalTCMsg) {
        tcMsgs.appendFailed(throwable.getMessage(), additionalTCMsg);
        Printer.addReporterLog("TEST FAILED " + throwable.getMessage());
        log.info("TEST FAILED", throwable);
    }

    /**
     * For TestNG HTML reports purposes. Without this method it does not display test name provided by getTestName()
     * ITest interface). For i.e. Intellij IDEA uses name provided by getTestName() .
     *
     * @param result - test results
     */
    @AfterMethod
    public void setResultTestName(ITestResult result) {
        try {
            BaseTestMethod bm = (BaseTestMethod) result.getMethod();
            Field f = bm.getClass().getSuperclass().getDeclaredField("m_methodName");
            f.setAccessible(true);
            f.set(bm, testParams.getTestName());
            if (!result.isSuccess()) {
                ALL_TESTS_SUCCEEDED = false;
            }
        } catch (Exception ex) {
            log.error(ex);
        }
    }
    @AfterSuite
    public  void displaySavedTimesStatistics(){
        savedTimesList.sort(Comparator.comparing(SavedTimes::getDuration).reversed());
        CellStyle cs = new CellStyle(CellStyle.HorizontalAlign.left, CellStyle.AbbreviationStyle.crop,
                CellStyle.NullStyle.emptyString);
        Table t = new Table(3, BorderStyle.DESIGN_TUBES, ShownBorders
                .SURROUND_HEADER_AND_COLUMNS, false, "");
        t.addCell("Test Name", cs);
        t.addCell("Measure Type", cs);
        t.addCell("Duration min:s:ms", cs);
        savedTimesList.forEach(s -> {t.addCell(s.getTestName().trim(), cs)
            ;t.addCell(s.getMeasureType().trim(), cs);t.addCell(s.getFormattedDuration().replace("min:s:ms","").trim(), cs);});
        String stringTable = "Statistics of queries execution (" + savedTimesList.size() + " rows):\r\n" + t.render();
        Printer.addReporterLog(stringTable);

        log.log(Level.OFF, "##teamcity[message '"+stringTable+"']");
        log.info(stringTable);
    }

}
