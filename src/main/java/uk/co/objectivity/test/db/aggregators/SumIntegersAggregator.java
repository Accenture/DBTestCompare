package uk.co.objectivity.test.db.aggregators;

import java.util.List;

public class SumIntegersAggregator implements Aggregator {

    private static final String INT_REGEX = "-?\\d+";

    @Override public boolean isValid(String aggregatedValue, String columnValue) {
        return columnValue != null && aggregatedValue != null && columnValue.matches(INT_REGEX) && aggregatedValue.matches(INT_REGEX);
    }

    @Override public String getAggregated(String aggregatedValue, String columnValue) {
        Integer aggInt = Integer.parseInt(aggregatedValue);
        Integer colInt = Integer.parseInt(columnValue);
        return String.valueOf(aggInt + colInt);
    }

    @Override public void setParams(List<String> params) {
    }
}
