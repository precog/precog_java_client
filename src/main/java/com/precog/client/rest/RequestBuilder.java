package com.precog.client.rest;

import static javax.xml.bind.DatatypeConverter.printBase64Binary;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


/**
 * A builder for a REST {@link Request}.
 * 
 * @author Tom Switzer <switzer@precog.com>
 */
public class RequestBuilder {
	private static interface Body {
		public InputStream asInputStream() throws IOException;
		public long getContentLength() throws IOException;
	}
	
	private static Body EMPTY_BODY = new ByteArrayBody(new byte[0]);
	
	
	private Method method = Method.GET;
	private Map<String, String> params = new HashMap<String, String>();
    private Map<String, String> header = new HashMap<String, String>();
    private Body body = EMPTY_BODY;
    private ContentType contentType = ContentType.JSON;
    private Path path = new Path("");
    private boolean httpsRequired = false;
    
    public RequestBuilder(Request request) {
    	this.method = request.getMethod();
    	this.params = new HashMap<String, String>(request.getParams());
    	this.header = new HashMap<String, String>(request.getHeaders());
    	this.contentType = request.getContentType();
    	this.path = request.getPath();
    	this.body = new RequestBody(request);
    	this.httpsRequired = request.isHttpsRequired();
    }
    
    public RequestBuilder(Method method, Path path) {
    	this.method = method;
    	this.path = path;
    }
    
    public RequestBuilder(Method method) {
    	this.method = method;
    }
    
    public RequestBuilder(Path path) {
    	this.path = path;
    }
    
    public RequestBuilder() {
    }
    
    /**
     * Set the path to use. If the path is relative, it'll be appended to the
     * current path. If it is absolute, then it will replace the path.
     * 
     * @param path the request path
     * @return this request builder
     */
    public RequestBuilder setPath(Path path) {
    	if (path.isRelative()) {
    		this.path = this.path.append(path);
    	} else {
    		this.path = path;
    	}
    	return this;
    }

    /**
     * Add an HTTP header to the request.
     * 
     * @param key the header
     * @param value the value of the header
     * @return this request builder
     */
    public RequestBuilder addHeader(String key, String value) {
    	header.put(key, value);
    	return this;
    }
    
    /**
     * Add a URL parameter to the request.
     * 
     * @param key the parameter key
     * @param value the value of the parameter
     * @return this request builder
     */
    public RequestBuilder addParam(String key, String value) {
    	params.put(key, value);
    	return this;
    }
    
    /**
     * Add many URL parameters to the request.
     * 
     * @param params the parameters to add to the request
     * @return this request builder
     */
    public RequestBuilder addAllParams(Map<String,String> params) {
    	params.putAll(params);
    	return this;
    }
    
    /**
     * Set the content type of the request to {@code contentType}.
     * 
     * @param contentType the content type of the request body
     * @return this request builder
     */
    public RequestBuilder setContentType(ContentType contentType) {
        this.contentType = contentType;
        return this;
    }
    
    /**
     * Set the body as the UTF-8 encoding of the string {@code body}. This also
     * sets the content length to be equal to the number of bytes in the UTF-8
     * encoding of {@code body}.
     * 
     * @param body the body of the request
     * @return this request builder
     * @throws UnsupportedEncodingException if UTF-8 isn't support on the platform
     */
    public RequestBuilder setBody(String body) {
    	this.body = new StringBody(body);
    	return this;
    }
    
    /**
     * Set the body of the request to {@code bytes}.
     * 
     * @param bytes the bytes to use as the request body
     * @return this request builder
     */
    public RequestBuilder setBody(byte[] bytes) {
    	this.body = new ByteArrayBody(bytes);
    	return this;
    }
    
    /**
     * Set the body of the request to use the contents of {@code file}.
     * 
     * @param file the file to use as the body of the request
     * @return this request builder
     */
    public RequestBuilder setBody(File file) {
    	this.body = new FileBody(file);
    	return this;
    }

    /**
     * Set the body of the request to use the contents of {@code file}.
     * 
     * @param body the InputStream to use as the body of the request
     * @return this request builder
     */
    public RequestBuilder setBody(InputStream body) {
    	this.body = new InputStreamBody(body);
    	return this;
    }
    
    /**
     * Set the HTTP method to use for the request.
     * 
     * @param method the HTTP method used to make this request
     * @return this request builder
     */
    public RequestBuilder setMethod(Method method) {
    	this.method = method;
    	return this;
    }

    /**
     * Adds base authentication to a header map
     *
     * @param user     user id
     * @param password user password
     */
    public RequestBuilder addBasicAuth(String user, String password) {
        return addHeader("Authorization",
        		"Basic " + printBase64Binary((user + ":" + password).getBytes()));
    }
    
    /**
     * If set to {@code true}, then that means the request should not be
     * executed unless the service is access via HTTPS (rather than HTTP). This
     * may be desired when, eg, sending authentication information via basic
     * auth.
     * 
     * @param req true if this request requires HTTPS (default is false)
     * @return this request builder
     */
    public RequestBuilder setHttpsRequired(boolean req) {
    	this.httpsRequired = req;
    	return this;
    }
    
    public Request build() {
    	return new Request() {
    		private Method method = RequestBuilder.this.method;
    		private Map<String, String> params =
    				Collections.unmodifiableMap(RequestBuilder.this.params);
    	    private Map<String, String> header =
    				Collections.unmodifiableMap(RequestBuilder.this.header);
    	    private Body body = RequestBuilder.this.body;
    	    private ContentType contentType = RequestBuilder.this.contentType;
    	    private Path path = RequestBuilder.this.path;
    	    private boolean httpRequired = RequestBuilder.this.httpsRequired;

			public Method getMethod() {
				return method;
			}

			public Map<String, String> getParams() {
				return params;
			}

			public Map<String, String> getHeaders() {
				return header;
			}

			public ContentType getContentType() {
				return contentType;
			}

			public long getContentLength() throws IOException {
				return body.getContentLength();
			}

			public InputStream getBody() throws IOException {
				return body.asInputStream();
			}

			public Path getPath() {
				return path;
			}

			public boolean isHttpsRequired() {
				return httpRequired;
			}
    	};
    }

	private static class FileBody implements Body {
		private File file;
		
		FileBody(File file) {
			this.file = file;
		}

		public InputStream asInputStream() throws IOException {
			return new FileInputStream(file);
		}

		public long getContentLength() throws IOException {
			return file.length();
		}
	}
	
	private static class RequestBody implements Body {
		private Request req;
		
		RequestBody(Request request) {
			this.req = request;
		}

		public InputStream asInputStream() throws IOException {
			return req.getBody();
		}

		public long getContentLength() throws IOException {
			return req.getContentLength();
		}
	}
	
	private static class ByteArrayBody implements Body {
		private byte[] bytes;
		
		ByteArrayBody(byte[] bytes) {
			this.bytes = bytes;
		}

		public InputStream asInputStream() throws IOException {
			return new ByteArrayInputStream(bytes);
		}
		
		public long getContentLength() throws IOException {
			return bytes.length;
		}
	}
	
	private static class StringBody implements Body {
		private String body;
		private byte[] bytes;
		
		StringBody(String body) {
			this.body = body;
		}
		
		public InputStream asInputStream() throws IOException {
			if (bytes == null) {
				bytes = body.getBytes("UTF-8");
			}
			return new ByteArrayInputStream(bytes);
		}
		
		public long getContentLength() throws IOException {
			if (bytes == null)
				asInputStream();
			return bytes.length;
		}
	}
	
	private static class InputStreamBody implements Body {
		private InputStream body;
		
		InputStreamBody(InputStream body) {
			this.body = body;
		}

		@Override
		public InputStream asInputStream() throws IOException {
			return body;
		}

		public long getContentLength() throws IOException {
			return -1L;
		}
	}
}
