package uk.co.objectivity.test.db.transformers;

import java.util.List;

public interface Transformer {

    String transform(String columnValue);

    Transformer setParams(List<String> params);

}
