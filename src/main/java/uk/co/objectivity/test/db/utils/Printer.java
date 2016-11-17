// <copyright file="Printer.java" company="Objectivity Bespoke Software Specialists">
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

package uk.co.objectivity.test.db.utils;

import java.util.List;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.spi.Filter;
import org.apache.log4j.spi.LoggingEvent;
import org.nocrala.tools.texttablefmt.BorderStyle;
import org.nocrala.tools.texttablefmt.CellStyle;
import org.nocrala.tools.texttablefmt.ShownBorders;
import org.nocrala.tools.texttablefmt.Table;
import org.testng.Reporter;

import uk.co.objectivity.test.db.beans.TestResults;
import uk.co.objectivity.test.db.beans.xml.CmpSqlResultsConfig;


public class Printer {

    private final static Logger log = Logger.getLogger(Printer.class);

    public static void init(CmpSqlResultsConfig cmpSqlResultsConfig) {
        log.debug("Initializing logger / printer ...");
        ConsoleAppender consoleAppender = (ConsoleAppender) Logger.getRootLogger().getAppender("stdout");
        if (cmpSqlResultsConfig.getLogger() != null && cmpSqlResultsConfig.getLogger().getLogLevel() !=
                null && !cmpSqlResultsConfig.getLogger().getLogLevel().isEmpty()) {
            Logger.getRootLogger().setLevel(Level.toLevel(cmpSqlResultsConfig.getLogger().getLogLevel()));
        }
        if (cmpSqlResultsConfig.getLogger().isTeamcityLogsEnabled()) {
            consoleAppender.setLayout(new PatternLayout("%m%n"));
        }
        consoleAppender.addFilter(new Filter() {
            @Override
            public int decide(LoggingEvent loggingEvent) {
                if(loggingEvent.getMessage() instanceof String == false) return ACCEPT;
                boolean isTCMsg = ((String) loggingEvent.getMessage()).contains(TCMessages.TC);
                // before threads (and buffering messages for TC) we allowed "common" logs
                // boolean isNotTCMsg = ((String) loggingEvent.getMessage()).contains(TCMessages.NOT_TC);
                if (cmpSqlResultsConfig.getLogger().isTeamcityLogsEnabled()) {
                    return isTCMsg ? ACCEPT : DENY;
                    // return isNotTCMsg ? DENY : ACCEPT;
                } else {
                    return isTCMsg ? DENY : ACCEPT;
                }
            }
        });
    }

    /**
     * Prints Text Table based on TestResults. Method uses library:
     * http://grepcode.com/snapshot/repo1.maven.org/maven2/org.ow2.sirocco/sirocco-text-table-formatter/1.0/
     *
     * @param testResults
     */
    public static String getTextTable(TestResults testResults) {
        if (testResults == null || testResults.getColumns() == null || testResults.getRows() == null || testResults.getColumns
                ().isEmpty() || testResults.getRows().isEmpty() || testResults.getNmbOfRows() == null || testResults
                .getRows().get(0).size() != testResults.getColumns().size()) {
            return "Incorrect TestResults. The table of differences can NOT be printed!";
        }
        CellStyle cs = new CellStyle(CellStyle.HorizontalAlign.left, CellStyle.AbbreviationStyle.crop,
                CellStyle.NullStyle.emptyString);
        Table t = new Table(testResults.getColumns().size(), BorderStyle.DESIGN_TUBES, ShownBorders
                .SURROUND_HEADER_AND_COLUMNS, false, "");
        for (String column : testResults.getColumns()) {
            t.addCell(column, cs);
        }
        for (List<String> row : testResults.getRows()) {
            for (String rowVal : row) {
                t.addCell("'" + rowVal + "'", cs);
            }
        }
        String stringTable = "The table of differences (" + testResults.getRows().size() + " rows):\r\n" + t.render();

        // printing to HTML report (testNG) -> generating HTML report is transparent in TestNG -> so is method call
        // below (we do not do this in test class itself)
        addReporterLog(stringTable);
        return stringTable;
    }


    /**
     * Printing to HTML report (testNG). Adds message to test-output (HTML tests report web-page created by
     * TestNG. This log will be visible in "Reporter output" section on web page.
     *
     * @param message
     */
    public static void addReporterLog(String message) {
        Reporter.log(message.replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll("\\u0020", "&nbsp;").replaceAll("(\r\n|\n)", "<br />") + "<br/>");
    }


}
