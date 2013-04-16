package com.precog.client.rest;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;


import static java.net.URLEncoder.encode;
import static javax.xml.bind.DatatypeConverter.printBase64Binary;


/**
 * Class to represent a RESTful service and execute {@link Request}s against
 * it.
 *
 * @author Gabriel Claramunt <gabriel@precog.com>
 * @author Tom Switzer <switzer@precog.com>
 */
public class Rest {
	private static final int CHUNK_SIZE = 16 * 1024;
	

    private final URL service;

    /**
     * Constructor  (only visible to the package)
     *
     * @param service service to connect
     */
    public Rest(URL service) {
      this.service = service;
    }
    
    // Returns true if this wraps an HTTPS end-point.
    private boolean isSecure() {
    	return service.getProtocol().toLowerCase() == "https";
    }

    /**
     * Creates a parameter string for use in url, in the form $key=$value UTF-8
     * encoded.
     *
     * @param key
     * @param value
     * @return a single parameter string
     * @throws UnsupportedEncodingException
     */
    private static String encodeParam(String key, String value) throws UnsupportedEncodingException {
        return key + "=" + encode(value, "UTF-8");
    }

    /**
     * Adds base authentication to a header map
     *
     * @param headers  map with header parameters
     * @param user     user id
     * @param password user password
     */
    public static void addBaseAuth(Map<String, String> headers, String user, String password) {
        headers.put("Authorization", "Basic " + printBase64Binary((user + ":" + password).getBytes()));
    }
    
    public static Response execute(URL service, Request request) throws HttpException {
    	return new Rest(service).execute(request);
    }

    /**
     * Executes a REST {@link Request} against this HTTP end-point and returns
     * the {@link Response}.
     *
     * @param request the REST request
     * @return the server's response
     * @throws HttpException if a network error occurs while executing the REST request
     * @throws IllegalArgumentException if HTTPS is required, but the end-point is HTTP.
     */
    public Response execute(Request request) throws HttpException {
    	if (request.isHttpsRequired() && !isSecure()) {
    		throw new IllegalArgumentException(
    				"Request required HTTPS connection for HTTP end-point.");
    	}
    	
    	try {
	    	StringBuilder params = new StringBuilder();
	        char join = '?';
	        for (Map.Entry<String, String> param : request.getParams().entrySet()) {
	        	params.append(join).append(encodeParam(param.getKey(), param.getValue()));
	        	join = '&';
	        }
	        
	        String path = request.getPath().absolutize().toString();
	        URL serviceURL = new URL(service, path + params.toString());
	        HttpURLConnection conn = (HttpURLConnection) serviceURL.openConnection();
	
	        conn.setRequestMethod(request.getMethod().getValue());
	
	        for (Map.Entry<String, String> e : request.getHeaders().entrySet()) {
	            conn.setRequestProperty(e.getKey(), e.getValue());
	        }
	        conn.setRequestProperty("Content-Type", request.getContentType().getType());
	
	        if (request.getContentLength() != 0) {
	        	InputStream in = request.getBody();
	        	try {
	        		long length = request.getContentLength();
	        		if (length < 0) {
	        			conn.setChunkedStreamingMode(CHUNK_SIZE);
	        		} else {
	        			conn.setRequestProperty("Content-Length", "" + length);
	        			conn.setFixedLengthStreamingMode(length);
	        		}
		        	
		            conn.setDoOutput(true);
		            OutputStream out = conn.getOutputStream();
		            try {
		            	byte[] chunk = new byte[CHUNK_SIZE];
		            	int chunkSize = 0;
		            	while ((chunkSize = in.read(chunk)) >= 0) {
		            		out.write(chunk, 0, chunkSize);
		            	}
		            } finally {
		            	out.close();
		            }
	        	} finally {
	        		in.close();
	        	}
	        }
	        
	        return new Response(conn);
    	} catch (IOException ioe) {
    		throw new HttpException(ioe);
    	}
    }
}
