package uk.co.objectivity.test.db.beans;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

import uk.co.objectivity.test.db.aggregators.Aggregator;
import uk.co.objectivity.test.db.aggregators.AggregatorType;
import uk.co.objectivity.test.db.aggregators.condition.AggregatorCondition;
import uk.co.objectivity.test.db.beans.xml.Aggregators;
import uk.co.objectivity.test.db.beans.xml.Transformer;

public class FileRow {
    private final static Logger log = Logger.getLogger(FileRow.class);

    private List<Transformer> transformers;
    private List<Aggregator> aggregators;
    private List<AggregatorCondition> aggregatorConditions;
    private List<Integer> keyColumns;

    private List<String> aggregatedRow;
    private List<List<String>> excludedRows;
    private List<List<String>> allRows;
    private boolean aggregated;
    private boolean arbitratorOn;

    public FileRow(List<String> row, List<Integer> keyColumns, List<Transformer> transformers, Aggregators aggregators,
            boolean arbitratorOn) {
        this.keyColumns = keyColumns;
        this.aggregators = new ArrayList<>();
        this.excludedRows = new ArrayList<>();
        this.transformers = transformers;
        this.aggregated = false;
        this.arbitratorOn = arbitratorOn;

        setColumnAggregators(row.size(),
                (aggregators == null || aggregators.getAggregators() == null) ? Collections.emptyList()
                        : aggregators.getAggregators());
        this.aggregatorConditions = (aggregators == null || aggregators.getConditions() == null)
                ? Collections.emptyList()
                : aggregators.getConditions();
        this.allRows = new ArrayList<>();
        add(row);
    }

    public void add(List<String> row) {
        transform(row);
        allRows.add(row);
        if (arbitratorOn || !this.shouldAggregate(row)) {
            this.excludedRows.add(row);
        } else if (aggregatedRow == null || aggregatedRow.isEmpty()) {
            this.aggregated = true;
            aggregatedRow = new ArrayList<>(row);
        } else {
            aggregate(row);
        }
    }

    private boolean shouldAggregate(List<String> row) {
        return this.aggregatorConditions.stream().allMatch(ac -> ac.shouldAggregate(row));
    }

    private void aggregate(List<String> row) {
        this.aggregated = true;

        for (int i = 0; i < row.size(); i++) {
            if (keyColumns.contains(i + 1))
                continue;
            Aggregator agg = aggregators.get(i);
            if (agg.isValid(aggregatedRow.get(i), row.get(i)))
                aggregatedRow.set(i, agg.getAggregated(aggregatedRow.get(i), row.get(i)));
            else {
                log.error("Column " + (i + 1) + " can not be aggregated! Invalid values (to be merged): '"
                        + aggregatedRow.get(i) + "' and '" + row.get(i)
                        + "'. Previous/first row will be used to compare with CSV row");
            }
        }
    }

    private void transform(List<String> row) {
        transformers.stream().filter(t -> t.getTransformer() != null).forEach(t -> {
            String transStr = t.getTransformer().transform(row.get(t.getColumn() - 1));
            row.set(t.getColumn() - 1, transStr);
        });
    }

    public List<String> getAggregatedRow() {
        return aggregatedRow;
    }

    private ArrayList<ArrayList<String>> copyList(List<List<String>> listToCopy) {
        ArrayList<ArrayList<String>> newList = new ArrayList<>();
        for (List<String> inArr : listToCopy) {
            ArrayList<String> newStrList = new ArrayList<>();
            for (String inStr : inArr) {
                newStrList.add(inStr);
            }
            newList.add(newStrList);
        }
        return newList;
    }

    public ArrayList<ArrayList<String>> getRows() {
        ArrayList<ArrayList<String>> rowList = copyList(this.excludedRows);
        if (this.aggregated) {
            rowList.add((ArrayList<String>) getAggregatedRow());
        }
        return rowList;
    }

    public List<String> getAggregatedRowKeyVals() {
        List<String> keyVals = new ArrayList<>();
        if (aggregatedRow != null) {
            for (int i = 0; i < aggregatedRow.size(); i++) {
                if (keyColumns.contains(i + 1))
                    keyVals.add(aggregatedRow.get(i));
            }
        } else if (excludedRows != null && excludedRows.size() > 0) {
            List<String> excludedRow = excludedRows.get(0);

            for (int i = 0; i < excludedRow.size(); i++) {
                if (keyColumns.contains(i + 1))
                    keyVals.add(excludedRow.get(i));
            }
        }
        return keyVals;
    }

    public List<List<String>> getAllRows() {
        return allRows;
    }

    public List<List<String>> getExcludedRows() {
        return excludedRows;
    }

    public boolean isAggregated() {
        return this.aggregated && (this.allRows.size() - this.excludedRows.size()) > 1;
    }

    public void setColumnAggregators(int columnsNo,
            List<uk.co.objectivity.test.db.beans.xml.Aggregator> xmlAggregators) {
        for (int i = 1; i <= columnsNo; i++) {
            boolean aggregatorDefined = false;
            for (uk.co.objectivity.test.db.beans.xml.Aggregator aggregator : xmlAggregators) {
                if (aggregator.getColumn() == i) {
                    uk.co.objectivity.test.db.aggregators.Aggregator agg = aggregator.getAggregatorType()
                            .getAggregator();
                    if (aggregator.getParams() != null && !aggregator.getParams().trim().isEmpty())
                        agg.setParams(Arrays.asList(aggregator.getParams().split(";")));
                    aggregators.add(agg);
                    aggregatorDefined = true;
                }
            }
            if (!aggregatorDefined)
                aggregators.add(AggregatorType.OVERRIDE.getAggregator());
        }
    }

}
