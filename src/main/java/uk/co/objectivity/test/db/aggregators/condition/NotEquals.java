package uk.co.objectivity.test.db.aggregators.condition;

import java.util.List;

public class NotEquals implements AggregatorCondition {
    private String conditionValue;
    private int column;

    private NotEquals() {
    }

    public NotEquals(String conditionValue, int column) {
        this();
        this.conditionValue = conditionValue;
        this.column = column;
    }

    public boolean shouldAggregate(List<String> row) {
        return !this.conditionValue.equals(row.get(this.column - 1));
    }
}