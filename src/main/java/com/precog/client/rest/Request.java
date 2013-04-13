package com.precog.client.rest;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;


/**
 * Represents a REST request.
 *
 * @author Gabriel Claramunt <gabriel@precog.com>
 * @author Tom Switzer <switzer@precog.com>
 */
public interface Request {
    
    /** Returns the HTTP method to use. */
    public Method getMethod();
    
    /** Returns the query parameters for the request. */
    public Map<String, String> getParams();
    
    /** Returns the HTTP headers required for the request. */
    public Map<String, String> getHeaders();
    
    /** Returns the content-type of the request. */
    public ContentType getContentType();
    
    /**
     * Returns the content length (in bytes) of the body. If this returns a
     * value less than 0, then it is assumed the content length is not known in
     * advance and chunked encoding should be used instead.
     * 
     * @return the length of the content in bytes
     * @throws IOException
     */
    public long getContentLength() throws IOException;
    
    /** Returns an input stream to send for a PUT or POST request. */
    public InputStream getBody() throws IOException;
    
    /** The sub-path of the REST request. */
    public Path getPath();
    
    /** Returns true if HTTPS is required for the request to be executed. */
    public boolean isHttpsRequired();
}
