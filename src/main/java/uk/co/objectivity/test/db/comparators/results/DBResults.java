package uk.co.objectivity.test.db.comparators.results;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.testng.TestException;

public class DBResults {
    private final static Logger log = Logger.getLogger(DBResults.class);
    private List<Integer> duplicatesArbitratorColumns;
    private Integer maxArbitratorColumn = -1;
    private Map<String, List<DBRow>> dbRowsMap = new TreeMap<>();

    public DBResults(List<Integer> duplicatesArbitratorColumns) {
        this.duplicatesArbitratorColumns = duplicatesArbitratorColumns;
        if (!duplicatesArbitratorColumns.isEmpty())
            maxArbitratorColumn = Collections.max(duplicatesArbitratorColumns);
    }

    /**
     * Method throws exception when adding row with the same key (key columns can
     * not be duplicated). When duplicatesArbitratorColumnNo != null
     * (isArbitratorSet()), DB results with same keys are allowed.
     *
     * @param key
     * @param dbRow
     * @throws TestException
     */
    public void add(String key, List<String> dbRow) throws TestException {
        if (dbRowsMap.containsKey(key)) {
            if (!isArbitratorSet())
                throw new TestException("Multiple DB rows for the same key: " + key
                        + " were returned. Check your test definition SQL!");
            dbRowsMap.get(key).add(new DBRow(dbRow, key));
        } else {
            dbRowsMap.put(key, new ArrayList<>(Arrays.asList(new DBRow(dbRow, key))));
        }
    }

    public Optional<List<String>> getRow(String key, List<String> csvRow) {
        if (!dbRowsMap.containsKey(key)) {
            return Optional.empty();
        }
        if (isArbitratorSet()) {
            List<DBRow> dbKeyRows = dbRowsMap.get(key);
            return dbKeyRows.stream().filter(dbRow -> dbRow.matchWith(duplicatesArbitratorColumns, csvRow)).findFirst()
                    .map(DBRow::getRow);
        }
        DBRow dbRow = dbRowsMap.get(key).get(0);
        dbRow.setAsMatched();
        return Optional.of(dbRow.getRow());
    }

    public List<DBRow> getAll() {
        return dbRowsMap.values().stream().flatMap(Collection::stream).collect(Collectors.toList());
    }

    public List<DBRow> getUnmached() {
        // testers does not want not-matched DB results to be displayd in differences
        if (!isArbitratorSet())
            return new ArrayList<DBRow>();
        return dbRowsMap.values().stream().flatMap(Collection::stream).filter(DBRow::isUnmatched)
                .collect(Collectors.toList());
    }

    public boolean isArbitratorSet() {
        return !duplicatesArbitratorColumns.isEmpty();
    }

    public Integer getMaxArbitratorColumn() {
        return maxArbitratorColumn;
    }
}
