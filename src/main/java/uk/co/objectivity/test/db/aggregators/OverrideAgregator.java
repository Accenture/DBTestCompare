package uk.co.objectivity.test.db.aggregators;

import java.util.List;

public class OverrideAgregator implements Aggregator {

    @Override public boolean isValid(String aggregatedValue, String columnValue) {
        return true;
    }

    @Override public String getAggregated(String aggregatedValue, String columnValue) {
        return columnValue;
    }

    @Override public void setParams(List<String> params) {

    }
}
