package hw5;

import com.google.gson.JsonObject;

public class DocumentStoreCli {
	public static void main(String[] args) {
		DB db = new DB(args.length > 0 ? args[0] : "data");
		DBCollection collection = db.getCollection(args.length > 1 ? args[1] : "test");

		System.out.println("CSE 530S Document Store");
		System.out.println("Database: " + db.getName());
		System.out.println("Collection: " + collection.getName());
		System.out.println("Documents: " + collection.count());

		DBCursor cursor = collection.find();
		int index = 1;
		while (cursor.hasNext()) {
			JsonObject document = cursor.next();
			System.out.println();
			System.out.println("[" + index + "]");
			System.out.println(Document.toJsonString(document));
			index++;
		}
	}
}
