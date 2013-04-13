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
public class CsvFormat implements Format {
	
	/**
	 * A CSV format for comma-separated values. This format uses commas as
	 * separators, double-quotes for quoting values, and double-quotes to
	 * escape values.
	 */
	public final static CsvFormat CSV = new CsvFormat(',');
	
	/**
	 * A CSV format for tab-separated values. This format uses tabs as
	 * separators, double-quotes for quoting values, and double-quotes to
	 * escape values.
	 */
	public final static CsvFormat TSV = new CsvFormat('\t');
	
	/** An alias for {@link TSV}. */
	public final static CsvFormat TAB = new CsvFormat('\t');
	
	/**
	 * A CSV format for semicolon-separated values. This format uses semicolons
	 * as separators, double-quotes for quoting values, and double-quotes to
	 * escape values.
	 */
	public final static CsvFormat SSV = new CsvFormat(';');
	
	
	private final char delim;
	private final char quote;
	private final char escape;
	
	/**
	 * Construct a {@link CsvFormat} that uses {@code delim} as delimiter, '"'
	 * for quoted values, and '"' to escape quotes in a quoted value.
	 * 
	 * @param delim the value delimiter to use
	 */
	public CsvFormat(char delim) {
		this(delim, '"', '"');
	}
	
	/**
	 * Constrcuts a {@link CsvFormat} that uses {@code delim} as a delimiter,
	 * {@code quote} to quote values, and {@code escape} to escape quotes
	 * within a quoted value.
	 * 
	 * @param delim the value delimiter to use
	 * @param quote the quotation charater to use (eg. '"' or '\'')
	 * @param escape the escape character to escape quotes in a quoted value
	 */
	public CsvFormat(char delim, char quote, char escape) {
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