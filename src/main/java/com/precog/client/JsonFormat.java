package com.precog.client;

import com.precog.client.rest.ContentType;

/**
 * This class represents JSON formatted data. There are 2 instances,
 * {@link #JSON} and {@link #JSON_STREAM} to match the 2 formats supported by
 * Precog.
 * 
 * The first, {@link #JSON}, expects data to be either single JSON object, or a
 * JSON array of multiple objects, where each element in the array represents
 * an event/record in Precog. For example:
 * 
 * <pre>
 * {@code
 * [ { "id": 1, "name": "Boulder"},
 *   { "id": 2, "name": "Toronto" },
 *   { "id": 3, "name": "New York" } ]
 * }
 * </pre>
 * 
 * The second, {@link #JSON_STREAM}, is a stream of valid JSON values, each
 * separated by whitespace. Each value is valid JSON, but the values are simply
 * concatenated with newlines (or spaces or tabs). For example:
 * 
 * <pre>
 * {@code
 * { "id": 1, "name": "Boulder" }
 * { "id": 2, "name": "Toronto" }
 * { "id": 3, "name": "New York" }
 * }
 * </pre>
 *  
 * @author Tom Switzer <switzer@precog.com>
 */
public enum JsonFormat implements Format {
	
	/** Valid JSON. Multiple records must be in a JSON array. */
	JSON(ContentType.JSON),
	
	/** White-space separated JSON values. */
	JSON_STREAM(ContentType.JSON_STREAM);
	
	private ContentType contentType;
	
	private JsonFormat(ContentType contentType) {
		this.contentType = contentType;
	}

	public ContentType getContentType() {
		return contentType;
	}

	public <A> A accept(FormatVisitor<A> visitor) {
		return visitor.visitJsonFormat(this);
	}
}