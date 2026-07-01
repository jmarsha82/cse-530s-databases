package hw5;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class DBCollection {
	private static final String DOCUMENT_SEPARATOR = "\n\t\n";
	private final String name;
	private final File file;

	/**
	 * Constructs a collection for the given database
	 * with the given name. If that collection doesn't exist
	 * it will be created.
	 */
	public DBCollection(DB database, String name) {
		if (database == null) {
			throw new IllegalArgumentException("Database is required");
		}
		if (name == null || name.trim().isEmpty()) {
			throw new IllegalArgumentException("Collection name is required");
		}
		this.name = name;
		this.file = new File(database.getDirectory(), name + ".json");
		try {
			if (!file.exists()) {
				file.createNewFile();
			}
		} catch (IOException e) {
			throw new IllegalStateException("Unable to create collection file " + file, e);
		}
	}
	
	/**
	 * Returns a cursor for all of the documents in
	 * this collection.
	 */
	public DBCursor find() {
		return new DBCursor(this, null, null);
	}

	/**
	 * Finds documents that match the given query parameters.
	 * 
	 * @param query relational select
	 * @return a cursor over matching documents
	 */
	public DBCursor find(JsonObject query) {
		return new DBCursor(this, query, null);
	}
	
	/**
	 * Finds documents that match the given query parameters.
	 * 
	 * @param query relational select
	 * @param projection relational project
	 * @return a cursor over matching projected documents
	 */
	public DBCursor find(JsonObject query, JsonObject projection) {
		return new DBCursor(this, query, projection);
	}
	
	/**
	 * Inserts documents into the collection
	 * Must create and set a proper id before insertion
	 * When this method is completed, the documents
	 * should be permanently stored on disk.
	 * @param documents documents to store
	 */
	public void insert(JsonObject... documents) {
		List<JsonObject> existing = readDocuments();
		for (JsonObject document : documents) {
			if (document == null) {
				throw new IllegalArgumentException("Cannot insert a null document");
			}
			JsonObject copy = document.deepCopy();
			if (!copy.has("_id")) {
				copy.addProperty("_id", nextId(existing));
			}
			existing.add(copy);
		}
		writeDocuments(existing);
	}
	
	/**
	 * Locates one or more documents and replaces them
	 * with the update document.
	 * @param query relational select for documents to be updated
	 * @param update the document to be used for the update
	 * @param multi true if all matching documents should be updated
	 * 				false if only the first matching document should be updated
	 */
	public void update(JsonObject query, JsonObject update, boolean multi) {
		if (update == null) {
			throw new IllegalArgumentException("Update document is required");
		}
		List<JsonObject> documents = readDocuments();
		boolean changed = false;
		for (int i = 0; i < documents.size(); i++) {
			if (matches(documents.get(i), query)) {
				documents.set(i, update.deepCopy());
				changed = true;
				if (!multi) {
					break;
				}
			}
		}
		if (changed) {
			writeDocuments(documents);
		}
	}
	
	/**
	 * Removes one or more documents that match the given
	 * query parameters
	 * @param query relational select for documents to be removed
	 * @param multi true if all matching documents should be updated
	 * 				false if only the first matching document should be updated
	 */
	public void remove(JsonObject query, boolean multi) {
		List<JsonObject> documents = readDocuments();
		for (int i = 0; i < documents.size(); i++) {
			if (matches(documents.get(i), query)) {
				documents.remove(i);
				if (!multi) {
					break;
				}
				i--;
			}
		}
		writeDocuments(documents);
	}
	
	/**
	 * Returns the number of documents in this collection
	 */
	public long count() {
		return readDocuments().size();
	}
	
	public String getName() {
		return name;
	}
	
	/**
	 * Returns the ith document in the collection.
	 * Documents are separated by a line that contains only a single tab (\t)
	 * Use the parse function from the document class to create the document object
	 */
	public JsonObject getDocument(int i) {
		List<JsonObject> documents = readDocuments();
		if (i < 0 || i >= documents.size()) {
			throw new IndexOutOfBoundsException("No document at index " + i);
		}
		return documents.get(i).deepCopy();
	}
	
	/**
	 * Drops this collection, removing all of the documents it contains from the DB
	 */
	public void drop() {
		try {
			Files.deleteIfExists(file.toPath());
		} catch (IOException e) {
			throw new IllegalStateException("Unable to drop collection " + name, e);
		}
	}

	List<JsonObject> readDocuments() {
		try {
			if (!file.exists()) {
				return new ArrayList<>();
			}
			String content = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8).trim();
			List<JsonObject> documents = new ArrayList<>();
			if (content.isEmpty()) {
				return documents;
			}
			for (String chunk : content.split("\\R\\s*\\t\\s*\\R")) {
				String json = chunk.trim();
				if (!json.isEmpty()) {
					documents.add(Document.parse(json));
				}
			}
			return documents;
		} catch (IOException e) {
			throw new IllegalStateException("Unable to read collection " + name, e);
		}
	}

	boolean matches(JsonObject document, JsonObject query) {
		if (query == null || query.size() == 0) {
			return true;
		}
		for (String key : query.keySet()) {
			JsonElement expected = query.get(key);
			JsonElement actual = document.get(key);
			if (actual == null || !actual.equals(expected)) {
				return false;
			}
		}
		return true;
	}

	JsonObject project(JsonObject document, JsonObject projection) {
		if (projection == null || projection.size() == 0) {
			return document.deepCopy();
		}
		JsonObject projected = new JsonObject();
		for (String key : projection.keySet()) {
			if (projection.get(key).getAsBoolean() && document.has(key)) {
				projected.add(key, document.get(key).deepCopy());
			}
		}
		return projected;
	}

	private void writeDocuments(List<JsonObject> documents) {
		List<String> serialized = new ArrayList<>();
		for (JsonObject document : documents) {
			serialized.add(Document.toJsonString(document));
		}
		try {
			Files.write(file.toPath(), String.join(DOCUMENT_SEPARATOR, serialized).getBytes(StandardCharsets.UTF_8));
		} catch (IOException e) {
			throw new IllegalStateException("Unable to write collection " + name, e);
		}
	}
	
	private long nextId(List<JsonObject> documents) {
		long max = 0;
		for (JsonObject document : documents) {
			if (document.has("_id") && document.get("_id").isJsonPrimitive() && document.get("_id").getAsJsonPrimitive().isNumber()) {
				max = Math.max(max, document.get("_id").getAsLong());
			}
		}
		return max + 1;
	}
}
