package com.precog.api.rest;

/**
 * The content type of a REST {@link Request}.
 * 
 * @author Gabriel Claramunt <gabriel@precog.com>
 * @author Tom Switzer <switzer@precog.com>
 */
public enum ContentType {
    XZIP("application/x-gzip"),
    ZIP("application/zip"),
    JSON("application/json"),
    JSON_STREAM("application/x-json-stream"),
    CSV("text/csv");

    private String type;

    ContentType(String s) {
        type = s;
    }

    public String getType() {
        return type;
    }
}
