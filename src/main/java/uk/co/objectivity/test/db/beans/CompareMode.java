// <copyright file="CompareMode.java" company="Objectivity Bespoke Software Specialists">
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

import uk.co.objectivity.test.db.comparators.Comparator;
import uk.co.objectivity.test.db.comparators.FetchComparator;
import uk.co.objectivity.test.db.comparators.FileComparator;
import uk.co.objectivity.test.db.comparators.MinusComparator;
import uk.co.objectivity.test.db.comparators.NmbOfResultsComparator;

public enum CompareMode {
    MINUS, FETCH, NMB_OF_RESULTS, FILE;

    public Comparator getComparator() {
        switch (this) {
            case MINUS:
                return new MinusComparator();
            case FETCH:
                return new FetchComparator();
            case NMB_OF_RESULTS:
                return new NmbOfResultsComparator();
            case FILE:
                return new FileComparator();
        }
        return null;
    }
}
