package com.precog.json;

/**
 * Treats strings as literal JSON values and passes them through
 * {@link #serialize(String)} unchanged.
 * 
 * @author Kris Nuttycombe
 */
public class RawStringToJson implements ToJson<String> {
	public String serialize(String value) {
		return value;
	}
}
