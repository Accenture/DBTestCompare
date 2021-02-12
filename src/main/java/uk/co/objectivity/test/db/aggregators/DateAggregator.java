package uk.co.objectivity.test.db.aggregators;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

public class DateAggregator implements Aggregator {

    private final static Logger log = Logger.getLogger(DateAggregator.class);

    private SimpleDateFormat dateFormat;
    private int greaterThan;

    @Override public boolean isValid(String aggregatedValue, String columnValue) {
        try {
            dateFormat.parse(columnValue);
            dateFormat.parse(aggregatedValue);
            return true;
        } catch (ParseException e) {
            log.warn(e);
            return false;
        }
    }

    @Override public String getAggregated(String aggregatedValue, String columnValue) {
        Date newDate, oldDate;
        try {
            newDate = dateFormat.parse(columnValue);
            oldDate = dateFormat.parse(aggregatedValue);
        } catch (ParseException e) {
            log.error(e);
            return null;
        }
        return (greaterThan * newDate.compareTo(oldDate)) > 0 ? columnValue : aggregatedValue;
    }

    @Override public void setParams(List<String> params) {
        // TODO kontrola liczebnosci i poprawnosci params
        dateFormat = new SimpleDateFormat(params.get(0));
        greaterThan = "gt".equals(params.get(1)) ? 1 : -1;
    }
}
