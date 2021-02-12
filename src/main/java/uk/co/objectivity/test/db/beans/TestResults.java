// <copyright file="TestResults.java" company="Objectivity Bespoke Software Specialists">
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

import java.util.List;

public class TestResults {

    private Integer nmbOfRows;

    private Integer nmbOfComparedRows;

    private List<String> columns;

    private List<List<String>> rows;

    private String output = "";

    private String executedQuery;

    public TestResults(String executedQuery, int nmbOfRows) {
        this.executedQuery = executedQuery;
        this.nmbOfRows = nmbOfRows;
    }

    public void appendOutput(String message){
        output += message + "\r\n";
     }

    public String getOutput() {
         return output;
    }

    public Integer getNmbOfComparedRows() {
        return nmbOfComparedRows;
    }

    public void setNmbOfComparedRows(Integer nmbOfComparedRows) {
        this.nmbOfComparedRows = nmbOfComparedRows;
    }

    public Integer getNmbOfRows() {
        return nmbOfRows;
    }

    public void setNmbOfRows(Integer nmbOfRows) {
        this.nmbOfRows = nmbOfRows;
    }

    public List<String> getColumns() {
        return columns;
    }

    public void setColumns(List<String> columns) {
        this.columns = columns;
    }

    public List<List<String>> getRows() {
        return rows;
    }

    public void setRows(List<List<String>> rows) {
        this.rows = rows;
    }

    public String getExecutedQuery() {
        return executedQuery;
    }

    public void setExecutedQuery(String executedQuery) {
        this.executedQuery = executedQuery;
    }
}
