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
import java.util.concurrent.TimeUnit;

public class SavedTimes {
    public String getTestName() {
        return testName;
    }

    private String testName;

    public String getMeasureType() {
        return measureType;
    }

    private String measureType;
    private long startTime;
    private long stopTime;

    public long getDuration() {
        return duration;
    }

    private long duration;
    private String formattedDuration;

    public String getFormattedDuration() {
        return formattedDuration;
    }

    public SavedTimes(String testName)
    {
        this.testName = testName;
    }

    public void SetDuration(long loadTime)
    {
        this.duration = loadTime;
        this.formattedDuration = String.format("%02d:%02d:%03d min:s:ms \n",
                TimeUnit.MILLISECONDS.toMinutes(loadTime),
                TimeUnit.MILLISECONDS.toSeconds(loadTime) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(loadTime))
                ,loadTime - TimeUnit.SECONDS.toMillis(TimeUnit.MILLISECONDS.toSeconds(loadTime)));
    }

    public void StartMeasure(String measureType)
    {
        this.startTime = System.currentTimeMillis();
        this.measureType=measureType;
    }

    public void StopMeasure()
    {
        this.stopTime = System.currentTimeMillis();
        SetDuration(this.stopTime - this.startTime);
    }

}
