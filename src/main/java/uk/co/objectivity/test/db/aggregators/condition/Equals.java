package uk.co.objectivity.test.db.aggregators.condition;

import java.util.List;

public class Equals implements AggregatorCondition {
    private String conditionValue;
    private int column;

    private Equals() {
    }

    public Equals(String conditionValue, int column) {
        this();
        this.conditionValue = conditionValue;
        this.column = column;
    }

    public boolean shouldAggregate(List<String> row) {
        return this.conditionValue.equals(row.get(this.column - 1));
    }
}