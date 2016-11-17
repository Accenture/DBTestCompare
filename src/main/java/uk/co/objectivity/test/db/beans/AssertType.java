// <copyright file="AssertType.java" company="Objectivity Bespoke Software Specialists">
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


import org.testng.Assert;

public enum AssertType {
    EQ, NE, LT, LE, GE, GT;

    public void assertByType(Integer actual, Integer expected) throws AssertionError {
        Assert.assertNotNull(actual, "Number of query results is null -");
        Assert.assertNotNull(expected, "Check assert value in test configuration -");
        switch (this) {
            case EQ:
                Assert.assertEquals(actual, expected, "Incorrect number of results -");
                break;
            case NE:
                Assert.assertNotEquals(actual, expected, "Number of results should not be equal " + expected);
                break;
            case LT:
                Assert.assertTrue(actual.compareTo(expected) < 0,
                        "Number of results (" + actual + ") should be less than " + expected + " -");
                break;
            case LE:
                Assert.assertTrue(actual.compareTo(expected) <= 0,
                        "Number of results (" + actual + ") should be less or equal " + expected + " -");
                break;
            case GE:
                Assert.assertTrue(actual.compareTo(expected) >= 0,
                        "Number of results (" + actual + ") should be greater or equal " + expected + " -");
                break;
            case GT:
                Assert.assertTrue(actual.compareTo(expected) > 0,
                        "Number of results (" + actual + ") should be greater than " + expected + " -");
                break;
        }
    }

}
