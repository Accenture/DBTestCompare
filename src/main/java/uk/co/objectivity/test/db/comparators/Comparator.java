// <copyright file="Comparator.java" company="Objectivity Bespoke Software Specialists">
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
import java.util.Iterator;
import java.util.List;

import uk.co.objectivity.test.db.beans.TestParams;
import uk.co.objectivity.test.db.beans.TestResults;


public abstract class Comparator {

    /**
     * Compares results of two SQL queries
     *
     * @param testParams
     * @return
     * @throws Exception
     */
    public abstract TestResults compare(TestParams testParams) throws Exception;

    protected File getNewFileBasedOnTestConfigFile(File testFile, String postfix) {
        String fileAbsPath = testFile.getParentFile().getAbsolutePath();
        fileAbsPath += "/" + testFile.getName().replaceFirst("[.][^.]+$", "") + postfix;
        return new File(fileAbsPath);
    }

    protected void writeRowAsCSV(PrintWriter pw, List<String> row) {
        this.writeRowAsCSV(pw, row, false);
    }

    protected void writeRowAsCSV(PrintWriter pw, List<String> row, boolean skipFirst) {
        if (pw == null) return;
        Iterator<String> rowIt = row.iterator();
        if (skipFirst && rowIt.hasNext()) rowIt.next();
        while (rowIt.hasNext()) {
            pw.write("\"" + rowIt.next().replaceAll("\"", "\"\"") + "\"");
            if (rowIt.hasNext()) pw.write(",");
            else pw.write("\r\n");
        }
    }
}
