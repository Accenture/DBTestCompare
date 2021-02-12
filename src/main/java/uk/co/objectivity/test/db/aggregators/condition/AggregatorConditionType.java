package uk.co.objectivity.test.db.aggregators.condition;

public enum AggregatorConditionType {
    EQUALS, NOT_EQUALS;

    public AggregatorCondition getAggregatorCondition(String conditionValue, int column) {
        switch (this) {
            case EQUALS:
                return new Equals(conditionValue, column);
            case NOT_EQUALS:
                return new NotEquals(conditionValue, column);
            default:
                throw new RuntimeException("Unknown Aggregator condition!");
        }
    }

}
