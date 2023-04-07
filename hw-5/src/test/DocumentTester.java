package test;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.google.gson.JsonObject;

import hw5.Document;

class DocumentTester {

	/*
	 * Things to consider testing:
	 * 
	 * Parsing embedded documents
	 * Parsing arrays
	 * 
	 * Object to primitive
	 * Object to embedded document
	 * Object to array
	 */
	@Test
	public void testParse() {
		String json = "{ \"key\": \"value\" }";
		JsonObject results = Document.parse(json);
		assertTrue(results.getAsJsonPrimitive("key").getAsString().equals("value"));
	}

}
