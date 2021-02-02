// <copyright file="Compare.java" company="Objectivity Bespoke Software Specialists">
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

import java.math.BigDecimal;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import uk.co.objectivity.test.db.beans.CompareMode;

@XmlAccessorType(XmlAccessType.FIELD)
public class Compare {

    @XmlAttribute
    private String defaultDatasourceName;

    @XmlAttribute(name = "mode")
    private CompareMode compareMode;

    @XmlAttribute
    private int diffTableSize = 5;

    @XmlAttribute
    private int fetchSize = 100;

    @XmlAttribute
    private int chunk = 0;

    @XmlAttribute
    private BigDecimal delta = BigDecimal.ZERO;

    @XmlAttribute
    private boolean fileOutputOn = false;

    @XmlAttribute
    private String keyColumns = null;

    @XmlAttribute
    private boolean minusQueryIndicatorOn = false;

    @XmlElement(name = "sql")
    private List<Sql> sqls;

    @XmlElement(name = "beforeSqls")
    private List<BeforeSqls> beforeSqls;

    @XmlElement(name = "assert")
    private List<Assert> assertions;

    @XmlElement(name = "file")
    private File file;

    public String getDefaultDatasourceName() {
        return defaultDatasourceName;
    }

    public void setDefaultDatasourceName(String defaultDatasourceName) {
        this.defaultDatasourceName = defaultDatasourceName;
    }

    public CompareMode getCompareMode() {
        return compareMode;
    }

    public void setCompareMode(CompareMode compareMode) {
        this.compareMode = compareMode;
    }

    public List<Sql> getSqls() {
        return sqls;
    }

    public void setSqls(List<Sql> sqls) {
        this.sqls = sqls;
    }

    public List<BeforeSqls> getBeforeSqls() {
        return beforeSqls;
    }

    public int getDiffTableSize() {
        return diffTableSize;
    }

    public void setDiffTableSize(int diffTableSize) {
        this.diffTableSize = diffTableSize;
    }

    public int getFetchSize() {
        return fetchSize;
    }

    public void setFetchSize(int fetchSize) {
        this.fetchSize = fetchSize;
    }

    public int getChunk() {
        return chunk;
    }

    public void setChunk(int chunk) {
        this.chunk = chunk;
    }

    public boolean isFileOutputOn() {
        return fileOutputOn;
    }

    public boolean isMinusQueryIndicatorOn() {
        return minusQueryIndicatorOn;
    }

    public void setFileOutputOn(boolean fileOutputOn) {
        this.fileOutputOn = fileOutputOn;
    }

    public String getKeyColumns() {
        return keyColumns;
    }

    public void setKeyColumns(String keyColumns) {
        this.keyColumns = keyColumns;
    }

    public void setMinusQueryIndicatorOn(boolean minusQueryIndicatorOn) {
        this.minusQueryIndicatorOn = minusQueryIndicatorOn;
    }

    public List<Assert> getAssertions() {
        return assertions;
    }

    public void setAssertions(List<Assert> assertions) {
        this.assertions = assertions;
    }

    public BigDecimal getDelta() {
        return delta;
    }

    public void setDelta(BigDecimal delta) {
        this.delta = delta;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }
}
