package uk.co.objectivity.test.db.transformers;

import org.apache.commons.lang3.StringUtils;
import java.util.List;

public class RemoveLeadingZerosTransformer implements Transformer {

    @Override
    public String transform(String columnValue) {
        String stripped = StringUtils.stripStart(columnValue, "0");
        if (stripped.isEmpty() && !columnValue.isEmpty()) {
            return "0";
        }
        return stripped;
    }

    @Override
    public RemoveLeadingZerosTransformer setParams(List<String> noop) {
        return this;
    }

}
