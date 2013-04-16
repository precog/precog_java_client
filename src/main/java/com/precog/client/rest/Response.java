package com.precog.client.rest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.logging.Logger;

public class Response {
	private static final Logger logger = Logger.getLogger(Response.class.getName());
	
	private int statusCode;
	private String message;
	private InputStream data;
	
	public Response(int statusCode, String message, InputStream data) {
		this.statusCode = statusCode;
		this.message = message;
		this.data = data;
	}
	
	public Response(HttpURLConnection conn) throws IOException {
		this(conn.getResponseCode(), conn.getResponseMessage(), conn.getInputStream());
	}

	public int getStatusCode() {
		return statusCode;
	}

	public String getMessage() {
		return message;
	}

	public InputStream getData() {
		return data;
	}
	
	public String asString() throws HttpException {
		try {
			if (statusCode / 100 != 2) {
				throw HttpException.unexpectedResponse(this);
			}
			
	        StringBuilder sb = new StringBuilder();
	    	BufferedReader buff = new BufferedReader(new InputStreamReader(data, "UTF-8"));
	    	String inputLine;
	        while ((inputLine = buff.readLine()) != null) {
	            sb.append(inputLine);
	        }
	        
	        return sb.toString();
		} catch (IOException ioe) {
			throw new HttpException(ioe);
		} finally {
			try {
				data.close();
			} catch (IOException ioe) {
				logger.warning("IOException thrown on close() from HTTP connection.");
			}
		}
	}
}
