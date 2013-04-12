package com.precog.json.gson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonSerializationContext;
import java.lang.reflect.Type;
import com.precog.json.ToJson;

/**
 * A simple ToJson instance that uses Gson default serialization.
 *
 * @author knuttycombe
 */
public class GsonToJson implements ToJson<Object> {
	private Gson gson;
	
	public GsonToJson(Gson gson) {
		this.gson = gson;
	}
	
	public GsonToJson() {
		this(new GsonBuilder().registerTypeAdapter(RawJson.class, new RawJsonSerializer()).create());
	}
	
	public String serialize(Object value) {
		return gson.toJson(value);
	}

	private static class RawJsonSerializer implements JsonSerializer<RawJson> {
		public JsonElement serialize(RawJson src, Type typeOfSrc, JsonSerializationContext context) {
			return new JsonParser().parse(src.getJson());
		}
	}
}
