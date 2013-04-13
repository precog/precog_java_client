package com.precog.client;

/**
 * A position in a piece of text.
 * 
 * This is used by {@link QueryResult} to report the position of errors and
 * warnings in a query.
 * 
 * @author Tom Switzer <switzer@precog.com>
 */
public class Position {
	private String text;
	private int column;
	private int line;
	
	public Position() { }
	
	public Position(String text, int column, int line) {
		super();
		this.text = text;
		this.column = column;
		this.line = line;
	}
	
	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public int getColumn() {
		return column;
	}

	public void setColumn(int column) {
		this.column = column;
	}

	public int getLine() {
		return line;
	}

	public void setLine(int line) {
		this.line = line;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + column;
		result = prime * result + line;
		result = prime * result + ((text == null) ? 0 : text.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Position other = (Position) obj;
		if (column != other.column)
			return false;
		if (line != other.line)
			return false;
		if (text == null) {
			if (other.text != null)
				return false;
		} else if (!text.equals(other.text))
			return false;
		return true;
	}
}
