package uk.co.objectivity.test.db.comparators.results;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.StringUtils;

public class DBRow {

    private List<String> row;
    private String key;
    private boolean unmatched = true;

    public DBRow(List<String> row, String key) {
        this.row = row;
        this.key = key;
    }

    public List<String> getRow() {
        return row;
    }

    public boolean matchWith(List<Integer> duplicatesArbitratorColumns, List<String> csvRow) {
        if (!unmatched)
            return false;

        AtomicInteger matchesCount = new AtomicInteger();
        duplicatesArbitratorColumns.stream()
                .forEach(i -> {
                    String dbColVal = row.get(i - 1);
                    String csvRowVal = csvRow.get(i - 1);
                    if ((dbColVal == null && csvRowVal == null) || (dbColVal != null && dbColVal.equals(csvRowVal)))
                        matchesCount.getAndIncrement();
                });
        if (matchesCount.get() == duplicatesArbitratorColumns.size()) {
            setAsMatched();
            return true;
        }
        return false;
    }

    public boolean isUnmatched() {
        return unmatched;
    }

    public void setAsMatched() {
        unmatched = false;
    }

    public String getKey() {
        return key;
    }

    public String toString() {
        return StringUtils.join(row) + (unmatched ? " - UNMATCHED" : " - MATCHED");
    }
}
