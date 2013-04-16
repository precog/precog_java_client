package com.precog.client;

/**
 * This defines a type-safe way to work with the valid Precog formats. This
 * class is not intended to be used publicly.
 * 
 * @author Tom Switzer <switzer@precog.com>
 *
 * @param <A> the return type of the visitor methods
 */
public interface FormatVisitor<A> {
	public A visitJsonFormat(JsonFormat format);
	public A visitCsvFormat(DelimitedFormat format);
}