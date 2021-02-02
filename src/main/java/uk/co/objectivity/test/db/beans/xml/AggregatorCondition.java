package uk.co.objectivity.test.db.beans.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

import uk.co.objectivity.test.db.aggregators.condition.AggregatorConditionType;

@XmlAccessorType(XmlAccessType.FIELD)
public class AggregatorCondition {
    @XmlAttribute
    private String column;

    @XmlAttribute
    private String conditionValue;

    @XmlValue
    private AggregatorConditionType type;

    public uk.co.objectivity.test.db.aggregators.condition.AggregatorCondition getAggregatorCondition() {
        return type.getAggregatorCondition(this.conditionValue, Integer.parseInt(column));
    }
}