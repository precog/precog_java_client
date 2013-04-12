package com.precog.api.rest;

/**
 * The HTTP method used in a REST {@link Request}.
 * 
 * @author Gabriel Claramunt <gabriel@precog.com>
 * @author Tom Switzer <switzer@precog.com>
 */
public enum Method {
    GET("GET"),
    POST("POST"),
    DELETE("DELETE"),
    PUT("PUT");
    
    private String value;
    
    private Method(String value) {
    	this.value = value;
    }
    
    public String getValue() {
    	return value;
    }
}
