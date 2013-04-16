package com.precog.client;

import com.precog.client.rest.ContentType;

/**
 * A CSV format represents CSV files as values separated by a delimiter
 * ({link getDelimiter()}), a quotation character ({@link getQuote()})
 * used to quote values, and a character to escape quotes within a quoted
 * value.
 * 
 * The most common formats are available as static members ({@link CSV},
 * {@link TSV}, and {@link SSV}).
 * 
 * @author Tom Switzer <switzer@precog.com>
 */
public class DelimitedFormat implements Format {
	
	private final char delim;
	private final char quote;
	private final char escape;
	
	/**
	 * Construct a {@link DelimitedFormat} that uses {@code delim} as delimiter, '"'
	 * for quoted values, and '"' to escape quotes in a quoted value.
	 * 
	 * @param delim the value delimiter to use
	 */
	public DelimitedFormat(char delim) {
		this(delim, '"', '"');
	}
	
	/**
	 * Constrcuts a {@link DelimitedFormat} that uses {@code delim} as a delimiter,
	 * {@code quote} to quote values, and {@code escape} to escape quotes
	 * within a quoted value.
	 * 
	 * @param delim the value delimiter to use
	 * @param quote the quotation charater to use (eg. '"' or '\'')
	 * @param escape the escape character to escape quotes in a quoted value
	 */
	public DelimitedFormat(char delim, char quote, char escape) {
		super();
		this.delim = delim;
		this.quote = quote;
		this.escape = escape;
	}

	/** Returns the value delimiter of the CSV format. */
	public char getDelimiter() {
		return delim;
	}

	/** Returns the quotation character of the CSV format. */
	public char getQuote() {
		return quote;
	}
	
	/** Returns the character used to escapte quotes within a quoted value. */
	public char getEscape() {
		return escape;
	}
	
	public ContentType getContentType() {
		return ContentType.CSV;
	}

	public <A> A accept(FormatVisitor<A> visitor) {
		return visitor.visitCsvFormat(this);
	}
}