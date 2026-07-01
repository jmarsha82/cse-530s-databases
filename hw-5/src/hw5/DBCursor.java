package hw5;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import com.google.gson.JsonObject;

public class DBCursor implements Iterator<JsonObject>{
	private final List<JsonObject> results;
	private int index;

	public DBCursor(DBCollection collection, JsonObject query, JsonObject fields) {
		if (collection == null) {
			throw new IllegalArgumentException("Collection is required");
		}
		this.results = new ArrayList<>();
		for (JsonObject document : collection.readDocuments()) {
			if (collection.matches(document, query)) {
				results.add(collection.project(document, fields));
			}
		}
	}
	
	/**
	 * Returns true if there are more documents to be seen
	 */
	public boolean hasNext() {
		return index < results.size();
	}

	/**
	 * Returns the next document
	 */
	public JsonObject next() {
		if (!hasNext()) {
			throw new NoSuchElementException("No more documents");
		}
		return results.get(index++).deepCopy();
	}
	
	/**
	 * Returns the total number of documents
	 */
	public long count() {
		return results.size();
	}
}
