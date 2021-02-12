// <copyright file="Datasource.java" company="Objectivity Bespoke Software Specialists">
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
import javax.xml.bind.annotation.XmlValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import uk.co.objectivity.test.db.transformers.TransformerType;

@XmlAccessorType(XmlAccessType.FIELD)
public class Transformer {

    @XmlAttribute
    private Integer column;

    @XmlAttribute
    private String params; // comma separated list of aggregator params

    @XmlValue
    private TransformerType transformerType;

    private List<String> convertParams() {
        return this.params != null ? Arrays.asList(this.params.split(";")) : new ArrayList<String>();
    }

    public Integer getColumn() {
        return column;
    }

    public void setColumn(Integer column) {
        this.column = column;
    }

    public String getParams() {
        return params;
    }

    public void setParams(String params) {
        this.params = params;
    }

    public uk.co.objectivity.test.db.transformers.Transformer getTransformer() {
        uk.co.objectivity.test.db.transformers.Transformer transformer = transformerType.getTransformer();
        if (transformer == null)
            return null;
        return transformer.setParams(this.convertParams());
    }

    public void setTransformerType(TransformerType transformerType) {
        this.transformerType = transformerType;
    }

}
