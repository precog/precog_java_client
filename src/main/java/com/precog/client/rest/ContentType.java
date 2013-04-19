package com.precog.client.rest;

/**
 * The content type of a REST {@link Request}.
 * 
 * @author Gabriel Claramunt <gabriel@precog.com>
 * @author Tom Switzer <switzer@precog.com>
 */
public enum ContentType {
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
