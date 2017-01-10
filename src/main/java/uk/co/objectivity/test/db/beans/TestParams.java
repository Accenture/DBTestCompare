// <copyright file="TestParams.java" company="Objectivity Bespoke Software Specialists">
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

package uk.co.objectivity.test.db.beans;

import java.io.File;

import uk.co.objectivity.test.db.beans.xml.CmpSqlResultsConfig;
import uk.co.objectivity.test.db.beans.xml.CmpSqlResultsTest;

public class TestParams {

    private String testName;

    private File testConfigFile;

    private String skipTestMessage;

    private CmpSqlResultsTest cmpSqlResultsTest;

    private CmpSqlResultsConfig cmpSqlResultsConfig;

    public TestParams(String testName) {
        this.testName = testName;
    }

    public String getTestName() {
        return testName;
    }

    public void setTestName(String testName) {
        this.testName = testName;
    }

    public CmpSqlResultsTest getCmpSqlResultsTest() {
        return cmpSqlResultsTest;
    }

    public void setCmpSqlResultsTest(CmpSqlResultsTest cmpSqlResultsTest) {
        this.cmpSqlResultsTest = cmpSqlResultsTest;
    }

    public CmpSqlResultsConfig getCmpSqlResultsConfig() {
        return cmpSqlResultsConfig;
    }

    public void setCmpSqlResultsConfig(CmpSqlResultsConfig cmpSqlResultsConfig) {
        this.cmpSqlResultsConfig = cmpSqlResultsConfig;
    }

    public String getSkipTestMessage() {
        return skipTestMessage;
    }

    public void setSkipTestMessage(String skipTestMessage) {
        this.skipTestMessage = skipTestMessage;
    }

    public void addSkipTestMessage(String skipTestMessage) {
        if (skipTestMessage != null && !skipTestMessage.isEmpty()) {
            if (this.skipTestMessage == null) {
                this.skipTestMessage = "";
            } else {
                this.skipTestMessage += "\r\n";
            }
            this.skipTestMessage += skipTestMessage;
        }
    }

    public boolean isMarkedToSkip() {
        return skipTestMessage != null;
    }

    public File getTestConfigFile() {
        return testConfigFile;
    }

    public void setTestConfigFile(File testConfigFile) {
        this.testConfigFile = testConfigFile;
    }

    @Override
    public String toString() {
        return testName;
    }
}
