package uk.co.objectivity.test.db.aggregators.condition;

import java.util.List;

public interface AggregatorCondition {

    public boolean shouldAggregate(List<String> row);

}
