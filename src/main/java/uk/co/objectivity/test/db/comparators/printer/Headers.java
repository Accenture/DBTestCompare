package uk.co.objectivity.test.db.comparators.printer;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Headers {

    private List<String> headers, extHeaders;

    public Headers() {
        this(null);
    }

    public Headers(List<String> headers) {
        if (headers == null)
            headers = new ArrayList<>();
        this.headers = headers;
        extHeaders = new ArrayList<>();
    }

    public void addHeader(String header) {
        headers.add(header);
    }

    public void addExtendedHeader(String header) {
        extHeaders.add(header);
    }

    public List<String> getHeaders() {
        return headers;
    }

    public List<String> getExtendedHeaders() {
        return Stream.of(extHeaders, headers)
                .flatMap(x -> x.stream())
                .collect(Collectors.toList());
    }

}
