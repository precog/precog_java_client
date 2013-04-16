package com.precog.client;

public class Formats {
	
	/** Valid JSON. Multiple records must be in a JSON array. */
	public final static JsonFormat JSON = JsonFormat.JSON;
	
	/** White-space separated JSON values. */
	public final static JsonFormat JSON_STREAM = JsonFormat.JSON_STREAM;

	/**
	 * A CSV format for comma-separated values. This format uses commas as
	 * separators, double-quotes for quoting values, and double-quotes to
	 * escape values.
	 */
	public final static DelimitedFormat CSV = new DelimitedFormat(',');
	
	/**
	 * A CSV format for tab-separated values. This format uses tabs as
	 * separators, double-quotes for quoting values, and double-quotes to
	 * escape values.
	 */
	public final static DelimitedFormat TSV = new DelimitedFormat('\t');
	
	/** An alias for {@link Formats#TSV}. */
	public final static DelimitedFormat TAB = new DelimitedFormat('\t');
	
	/**
	 * A CSV format for semicolon-separated values. This format uses semicolons
	 * as separators, double-quotes for quoting values, and double-quotes to
	 * escape values.
	 */
	public final static DelimitedFormat SSV = new DelimitedFormat(';');
	
	

}
