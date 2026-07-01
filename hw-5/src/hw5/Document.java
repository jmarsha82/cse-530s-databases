package hw5;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class Document {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	
	/**
	 * Parses the given json string and returns a JsonObject
	 * This method should be used to convert text data from
	 * a file into an object that can be manipulated.
	 */
	public static JsonObject parse(String json) {
		JsonElement element = JsonParser.parseString(json);
		if (!element.isJsonObject()) {
			throw new IllegalArgumentException("Document JSON must be an object");
		}
		return element.getAsJsonObject();
	}
	
	/**
	 * Takes the given object and converts it into a
	 * properly formatted json string. This method should
	 * be used to convert JsonObjects to strings
	 * when writing data to disk.
	 */
	public static String toJsonString(JsonObject json) {
		if (json == null) {
			throw new IllegalArgumentException("Document cannot be null");
		}
		return GSON.toJson(json);
	}
}
