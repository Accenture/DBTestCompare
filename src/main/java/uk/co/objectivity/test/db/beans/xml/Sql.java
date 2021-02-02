// <copyright file="Sql.java" company="Objectivity Bespoke Software Specialists">
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

import org.testng.TestException;

@XmlAccessorType(XmlAccessType.FIELD)
public class Sql {

    @XmlValue
    private String sql;

    @XmlAttribute
    private String filename;

    @XmlAttribute
    private String datasourceName;

    @XmlAttribute
    private String minusQueryIndicatorOccurence = "1";

    @XmlAttribute
    private int startAtRow = 1;

    @XmlAttribute
    private String minusQueryIndicatorText;

    @XmlAttribute
    private String keyTableName;

    @XmlAttribute
    private String keyTableColumns;

    @XmlAttribute
    private boolean emptyStringToNull = false;

    @XmlAttribute
    private String duplicatesArbitratorColumns;

    @XmlAttribute
    private String dateFormat;

    public String getSql() {
        return sql;
    }

    public int getStartAtRow() {
        return startAtRow;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public String getDatasourceName() {
        return datasourceName;
    }

    public void setDatasourceName(String datasourceName) {
        this.datasourceName = datasourceName;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getMinusQueryIndicatorOccurence() {
        return minusQueryIndicatorOccurence;
    }

    public String getMinusQueryIndicatorText() {
        return minusQueryIndicatorText;
    }

    public void setMinusQueryIndicatorText(String minusQueryIndicatorText) {
        this.minusQueryIndicatorText = minusQueryIndicatorText;
    }

    public String getKeyTableName() {
        return keyTableName;
    }

    public void setKeyTableName(String keyTableName) {
        this.keyTableName = keyTableName;
    }

    public String getKeyTableColumns() {
        return keyTableColumns;
    }

    public void setKeyTableColumns(String keyTableColumns) {
        this.keyTableColumns = keyTableColumns;
    }

    public boolean isEmptyStringToNull() {
        return emptyStringToNull;
    }

    public void setEmptyStringToNull(boolean emptyStringToNull) {
        this.emptyStringToNull = emptyStringToNull;
    }

    public String getDateFormat() {
        return this.dateFormat != null ? this.dateFormat : "yyyy-MM-dd";
    }

    public List<Integer> getDuplicatesArbitratorColumns() {
        if (duplicatesArbitratorColumns == null)
            return new ArrayList<Integer>();
        try {
            return Arrays.stream(duplicatesArbitratorColumns.split(",")).map(s -> s.trim()).map(Integer::valueOf)
                    .collect(Collectors.toList());
        } catch (NumberFormatException nfe) {
            throw new TestException("Incorrect value in the 'duplicatesArbitratorColumns' property");
        }
    }

}
