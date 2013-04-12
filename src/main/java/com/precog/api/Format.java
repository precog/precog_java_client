package com.precog.api;

import com.precog.api.rest.ContentType;

/**
 * Used to specify the format of some data to be inserted into Precog. This is
 * effectively a closed type hierarchy, supporting JSON ({@link JsonFormat})
 * and CSV ({@link CsvFormat}).
 * 
 * @author Tom Switzer <switzer@precog.com> 
 */
public interface Format {
	
	/** Returns the mime-type of this format's data. */
	public abstract ContentType getContentType();
	
	public abstract <A> A accept(FormatVisitor<A> visitor);
}