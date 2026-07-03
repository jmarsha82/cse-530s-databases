package test;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonObject;

import hw5.DB;
import hw5.DBCollection;
import hw5.DBCursor;
import hw5.Document;
import hw5.DocumentStoreCli;

class DocumentStoreBehaviorTest {
	private final DB db = new DB("unit-test-db");

	@AfterEach
	void cleanUp() {
		db.dropDatabase();
	}

	@Test
	void createsAndDropsDatabaseDirectories() {
		assertTrue(new File("testfiles/unit-test-db").isDirectory());
		db.dropDatabase();
		assertFalse(new File("testfiles/unit-test-db").exists());
	}

	@Test
	void insertsCountsAndReadsDocuments() {
		DBCollection collection = db.getCollection("people");
		collection.insert(person("Ada", "engineer"), person("Grace", "admiral"));

		assertEquals(2, collection.count());
		assertEquals("Ada", collection.getDocument(0).get("name").getAsString());
		assertTrue(collection.getDocument(0).has("_id"));
	}

	@Test
	void filtersAndProjectsCursorResults() {
		DBCollection collection = db.getCollection("people");
		collection.insert(person("Ada", "engineer"), person("Grace", "admiral"));

		JsonObject query = new JsonObject();
		query.addProperty("role", "engineer");
		JsonObject projection = new JsonObject();
		projection.addProperty("name", true);

		DBCursor cursor = collection.find(query, projection);

		assertEquals(1, cursor.count());
		JsonObject result = cursor.next();
		assertEquals("Ada", result.get("name").getAsString());
		assertFalse(result.has("role"));
		assertFalse(cursor.hasNext());
	}

	@Test
	void updatesAndRemovesDocuments() {
		DBCollection collection = db.getCollection("people");
		collection.insert(person("Ada", "engineer"), person("Grace", "admiral"));

		JsonObject query = new JsonObject();
		query.addProperty("name", "Ada");
		collection.update(query, person("Ada", "mathematician"), false);

		assertEquals("mathematician", collection.find(query).next().get("role").getAsString());

		collection.remove(query, false);
		assertEquals(1, collection.count());
		assertEquals("Grace", collection.getDocument(0).get("name").getAsString());
	}

	@Test
	void rejectsInvalidDocumentJson() {
		assertThrows(IllegalArgumentException.class, () -> Document.parse("[1, 2, 3]"));
		assertThrows(IllegalArgumentException.class, () -> Document.toJsonString(null));
	}

	@Test
	void validatesNamesAndCollectionInputs() {
		assertThrows(IllegalArgumentException.class, () -> new DB(" "));
		assertThrows(IllegalArgumentException.class, () -> db.getCollection(""));

		DBCollection collection = db.getCollection("people");
		assertThrows(IllegalArgumentException.class, () -> collection.insert((JsonObject) null));
		assertThrows(IllegalArgumentException.class, () -> collection.update(null, null, false));
	}

	@Test
	void dropsCollectionsAndRejectsMissingCursorItems() {
		DBCollection collection = db.getCollection("people");
		collection.insert(person("Ada", "engineer"));
		collection.drop();

		assertEquals(0, collection.count());
		assertThrows(IndexOutOfBoundsException.class, () -> collection.getDocument(0));
		assertThrows(java.util.NoSuchElementException.class, () -> collection.find().next());
	}

	@Test
	void commandLineInterfacePrintsCollectionSummary() {
		PrintStream originalOut = System.out;
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		try {
			System.setOut(new PrintStream(output));
			DocumentStoreCli.main(new String[] {"data", "test"});
		} finally {
			System.setOut(originalOut);
		}

		String text = output.toString();
		assertTrue(text.contains("CSE 530S Document Store"));
		assertTrue(text.contains("Database: data"));
		assertTrue(text.contains("Documents: 3"));
		assertTrue(text.contains("\"key\": \"value\""));
	}

	private JsonObject person(String name, String role) {
		JsonObject document = new JsonObject();
		document.addProperty("name", name);
		document.addProperty("role", role);
		return document;
	}
}
