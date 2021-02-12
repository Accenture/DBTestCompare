package uk.co.objectivity.test.db.transformers;

import java.util.List;
import org.apache.log4j.Logger;

public class SwapTransformer implements Transformer {
    private final static Logger log = Logger.getLogger(Transformer.class);

    private String from = "";
    private String to = "";

    @Override
    public String transform(String columnValue) {
        return columnValue.equals(from) ? to : columnValue;
    }

    @Override
    public SwapTransformer setParams(List<String> params) {
        if (params.size() == 2) {
            this.from = params.get(0);
            this.to = params.get(1);
        } else {
            log.warn("Incorrect number of params. Expected 2, found " + params.size());
            log.warn("The SWAP transformer will not work correctly");
        }

        return this;
    }
}
