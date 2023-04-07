package hw5;

import java.util.Iterator;

import com.google.gson.JsonObject;

public class DBCursor implements Iterator<JsonObject>{

	public DBCursor(DBCollection collection, JsonObject query, JsonObject fields) {
		
	}
	
	/**
	 * Returns true if there are more documents to be seen
	 */
	public boolean hasNext() {
		return false;
	}

	/**
	 * Returns the next document
	 */
	public JsonObject next() {
		return null;
	}
	
	/**
	 * Returns the total number of documents
	 */
	public long count() {
		return 0;
	}

}
