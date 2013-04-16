package com.precog.client.text;

import java.util.Date;

import com.precog.client.QueryResult;

/**
 * A timestamped message attached to a particular position in a piece of text.
 * 
 * This is used by {@link QueryResult} to report query errors, warnings, etc.
 *
 * @author Tom Switzer <switzer@precog.com>
 */
public class TextTag {
	
	private String message;
	private Date timestamp;
	private Position position;
	
	public TextTag() { }
	
	public TextTag(String message, Date timestamp, Position position) {
		super();
		this.message = message;
		this.timestamp = timestamp;
		this.position = position;
	}

	public String getMessage() {
		return message;
	}
	
	public void setMessage(String message) {
		this.message = message;
	}
	
	public Date getTimestamp() {
		return timestamp;
	}
	
	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}
	
	public Position getPosition() {
		return position;
	}
	
	public void setPosition(Position position) {
		this.position = position;
	}
	
	private static String spaces(int num) {
		if (num == 0) {
			return "";
		} else if (num == 1) {
			return " ";
		} else {
			return spaces(num / 2) + spaces(num / 2 + num % 2);
		}
				
	}
	
	@Override
	public String toString() {
		String line = "L" + position.getLine() + ": ";
		StringBuilder sb = new StringBuilder();
		sb.append(line)
		  .append(position.getText())
		  .append('\n')
		  .append(spaces(line.length() + position.getColumn() - 1))
		  .append("^ ")
		  .append(message);
		return sb.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((message == null) ? 0 : message.hashCode());
		result = prime * result
				+ ((position == null) ? 0 : position.hashCode());
		result = prime * result
				+ ((timestamp == null) ? 0 : timestamp.hashCode());
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
		TextTag other = (TextTag) obj;
		if (message == null) {
			if (other.message != null)
				return false;
		} else if (!message.equals(other.message))
			return false;
		if (position == null) {
			if (other.position != null)
				return false;
		} else if (!position.equals(other.position))
			return false;
		if (timestamp == null) {
			if (other.timestamp != null)
				return false;
		} else if (!timestamp.equals(other.timestamp))
			return false;
		return true;
	}

}
