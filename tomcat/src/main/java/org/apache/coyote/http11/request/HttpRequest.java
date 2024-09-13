package org.apache.coyote.http11.request;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.apache.coyote.http11.HttpHeader;
import org.apache.coyote.http11.HttpMethod;

public class HttpRequest {

    private static final String REQUEST_LINE_DELIMITER = " ";
    private static final int REQUEST_LINE_TOKEN_COUNT = 3;
    private static final String HEADER_DELIMITER = ": ";
    private static final int HEADER_TOKEN_COUNT = 2;
    private static final String PARAMETER_DELIMITER = "&";
    private static final String REQUEST_BODY_DELIMITER = "=";
    private static final int REQUEST_BODY_TOKEN_COUNT = 2;

    private HttpRequestLine httpRequestLine;
    private HttpRequestHeader httpRequestHeader;
    private HttpRequestBody httpRequestBody;

    public HttpRequest(BufferedReader bufferedReader) throws IOException {
        this.httpRequestLine = parseRequestLine(bufferedReader);
        this.httpRequestHeader = parseHttpRequestHeader(bufferedReader);
        this.httpRequestBody = parseHttpRequestBody(bufferedReader);
    }

    private HttpRequestLine parseRequestLine(BufferedReader bufferedReader) throws IOException {
        String line = bufferedReader.readLine();
        validateNotNull(line);
        String[] tokens = splitByDelimiter(line, REQUEST_LINE_DELIMITER, REQUEST_LINE_TOKEN_COUNT);
        return new HttpRequestLine(HttpMethod.findByName(tokens[0]), tokens[1], tokens[2].trim());
    }

    private void validateNotNull(String line) {
        if (line == null) {
            throw new IllegalArgumentException("Request Line은 null일 수 없습니다.");
        }
    }

    private String[] splitByDelimiter(String line, String delimiter, int limit) {
        return line.split(delimiter, limit);
    }

    private HttpRequestHeader parseHttpRequestHeader(BufferedReader bufferedReader) throws IOException {
        String line;
        Map<String, String> httpRequestHeaders = new HashMap<>();
        while ((line = bufferedReader.readLine()) != null && !line.isEmpty()) {
            String[] tokens = splitByDelimiter(line, HEADER_DELIMITER, HEADER_TOKEN_COUNT);
            httpRequestHeaders.put(tokens[0], tokens[1]);
        }
        return new HttpRequestHeader(httpRequestHeaders);
    }

    private HttpRequestBody parseHttpRequestBody(BufferedReader bufferedReader) throws IOException {
        HttpRequestBody httpRequestBody = new HttpRequestBody();

        String contentLengthValue = httpRequestHeader.findBy(HttpHeader.CONTENT_LENGTH);
        if (contentLengthValue == null) {
            return httpRequestBody;
        }

        int contentLength = Integer.parseInt(contentLengthValue);
        char[] buffer = new char[contentLength];
        bufferedReader.read(buffer, 0, contentLength);
        String httpRequestBodyLine = new String(buffer);

        parseRequestBody(httpRequestBody, httpRequestBodyLine);
        return httpRequestBody;
    }

    private void parseRequestBody(HttpRequestBody httpRequestBody, String httpRequestBodyLine) {
        for (String line : splitByDelimiter(httpRequestBodyLine, PARAMETER_DELIMITER, 0)) {
            String[] tokens = splitByDelimiter(line, REQUEST_BODY_DELIMITER, REQUEST_BODY_TOKEN_COUNT);
            httpRequestBody.add(tokens[0], tokens[1]);
        }
    }

    public boolean matchesMethod(HttpMethod httpMethod) {
        return this.httpRequestLine.matchesMethod(httpMethod);
    }

    public String findRequestBodyBy(String key) {
        return this.httpRequestBody.findBy(key);
    }

    public String getJSessionId() {
        return this.httpRequestHeader.getJSessionId();
    }

    public String getUrlPath() {
        return this.httpRequestLine.getUrlPath();
    }

    public String getFileExtension() {
        return this.httpRequestLine.getFileExtension();
    }

    public String getHttpVersion() {
        return this.httpRequestLine.getHttpVersion();
    }
}
