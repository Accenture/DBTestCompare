// <copyright file="File.java" company="Objectivity Bespoke Software Specialists">
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

package uk.co.objectivity.test.db.beans.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import uk.co.objectivity.test.db.beans.CheckPresenceAlgorithm;

@XmlAccessorType(XmlAccessType.FIELD)
public class File {

    @XmlAttribute
    private String filename;

    @XmlAttribute
    private String separator = ",";

    @XmlAttribute
    private int startAtRow = 1;

    @XmlAttribute
    private Integer testSetColumn;

    @XmlAttribute
    private String checkPresenceColumn;

    @XmlAttribute
    private CheckPresenceAlgorithm presenceAlgorithm;

    @XmlAttribute
    private String presenceColumnHeader;

    @XmlAttribute
    private String absentIndicator;

    @XmlAttribute
    private String ignoredColumns;

    @XmlElement(name = "transformers")
    private Transformers transformers;

    @XmlElement(name = "aggregators")
    private Aggregators aggregators;

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getSeparator() {
        return separator;
    }

    public void setSeparator(String separator) {
        this.separator = separator;
    }

    public int getStartAtRow() {
        return startAtRow;
    }

    public String getIgnoredColumns() {
        return ignoredColumns;
    }

    public Transformers getTransformers() {
        return transformers;
    }

    public Aggregators getAggregators() {
        return aggregators;
    }

    public Integer getTestSetColumn() {
        return testSetColumn;
    }

    public String getCheckPresenceHeader() {
        return presenceColumnHeader;
    }

    public String getAbsentIndicator() {
        if (absentIndicator != null)
            return absentIndicator.toUpperCase();
        return "C";
    }

}
