// <copyright file="TCMessages.java" company="Objectivity Bespoke Software Specialists">
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


public class TCMessages {

    /**
     * Messages logged via log4j that contains the specified string will be filtered out from logs
     * "##notTeamcity" string will be removed from the message before it is displayed.
     */
    // public static final String NOT_TC = "##notTeamcity";

    static final String TC = "##teamcity";

    private StringBuffer msgBuffer;
    private String testName;

    public TCMessages(String testName) {
        this(testName, false);
    }

    public TCMessages(String testName, boolean appendStarted) {
        msgBuffer = new StringBuffer();
        this.testName = testName;
        if (appendStarted) {
            appendStarted();
        }
    }

    public void appendStarted() {
        msgBuffer.append(TC).append("[testStarted name='").append(testName).append("' " +
                "captureStandardOutput='false']\r\n");
    }

    public void appendFinished(long duration) {
        msgBuffer.append(TC).append("[testFinished name='").append(testName).append("' duration='").append(duration)
                .append("']\r\n");
    }

    public void appendFailed(String failMsg) {
        this.appendFailed(failMsg, null);
    }

    public void appendFailed(String failMsg, String details) {
        msgBuffer.append(TC).append("[testFailed name='").append(testName).append("' message='").append(
                getTCFormattedMsg(failMsg)).append("'");
        if (details != null && !details.isEmpty()) {
            msgBuffer.append(" details='").append(getTCFormattedMsg(details)).append("'");
        }
        msgBuffer.append("]\r\n");
    }

    public void appendIgnored(String ignoreMsg) {
        msgBuffer.append(TC).append("[testIgnored name='").append(testName).append("' message='").append(
                getTCFormattedMsg(ignoreMsg)).append("']\r\n");
    }

    public void appendStdOut(String msg) {
        msgBuffer.append(TC).append("[testStdOut name='").append(testName).append("' out='").append(
                getTCFormattedMsg(msg)).append("']\r\n");
    }

    public void appendStdErr(String msg) {
        msgBuffer.append(TC).append("[testStdErr name='").append(testName).append("' out='").append(
                getTCFormattedMsg(msg)).append("']\r\n");
    }

    public String get() {
        return msgBuffer.toString();
    }

    /**
     * Preparing message to be displayed in Teamcity comment (##teamcity). See:
     * https://confluence.jetbrains.com/display/TCD8/Build+Script+Interaction+with+TeamCity
     *
     * @param msg - message to format
     * @return message in TC format
     */
    private static String getTCFormattedMsg(String msg) {
        if (msg == null) return "";
        msg = msg.replace("|", "||");
        msg = msg.replace("'", "|'");
        msg = msg.replaceAll("\\n", "|n");
        msg = msg.replaceAll("\\r", "|r");
        msg = msg.replace("[", "|[");
        return msg.replace("]", "|]");
    }
}
