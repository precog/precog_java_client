package com.precog.json.gson;

/**
 * A type wrapper for raw JSON strings to make it easier to embed such strings in 
 * an object being serialized with GSON or other reflection-based JSON serializers. 
 *
 * @author Kris Nuttycombe
 */
public class RawJson {
	private final String json;

	public RawJson(String json) {
		this.json = json;
	}

	public String getJson() {
		return this.json;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((json == null) ? 0 : json.hashCode());
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
		RawJson other = (RawJson) obj;
		if (json == null) {
			if (other.json != null)
				return false;
		} else if (!json.equals(other.json))
			return false;
		return true;
	}
}
