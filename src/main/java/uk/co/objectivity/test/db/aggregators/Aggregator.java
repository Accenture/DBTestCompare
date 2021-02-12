package uk.co.objectivity.test.db.aggregators;

import java.util.List;

public interface Aggregator {

    boolean isValid(String aggregatedValue, String columnValue);

    String getAggregated(String aggregatedValue, String columnValue);

    void setParams(List<String> params);

}
