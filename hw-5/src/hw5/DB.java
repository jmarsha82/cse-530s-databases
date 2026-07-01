package hw5;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class DB {
	private final String name;
	private final File directory;

	/**
	 * Creates a database object with the given name.
	 * The name of the database will be used to locate
	 * where the collections for that database are stored.
	 * For example if my database is called "library",
	 * I would expect all collections for that database to
	 * be in a directory called "library".
	 * 
	 * If the given database does not exist, it should be
	 * created.
	 */
	public DB(String name) {
		if (name == null || name.trim().isEmpty()) {
			throw new IllegalArgumentException("Database name is required");
		}
		this.name = name;
		this.directory = new File("testfiles", name);
		if (!directory.exists()) {
			directory.mkdirs();
		}
		if (!directory.isDirectory()) {
			throw new IllegalStateException("Database path is not a directory: " + directory);
		}
	}
	
	/**
	 * Retrieves the collection with the given name
	 * from this database. The collection should be in
	 * a single file in the directory for this database.
	 * 
	 * Note that it is not necessary to read any data from
	 * disk at this time. Those methods are in DBCollection.
	 */
	public DBCollection getCollection(String name) {
		return new DBCollection(this, name);
	}
	
	/**
	 * Drops this database and all collections that it contains
	 */
	public void dropDatabase() {
		deleteRecursively(directory);
	}
	
	String getName() {
		return name;
	}

	File getDirectory() {
		return directory;
	}
	
	private static void deleteRecursively(File file) {
		if (!file.exists()) {
			return;
		}
		if (file.isDirectory()) {
			File[] children = file.listFiles();
			if (children != null) {
				for (File child : children) {
					deleteRecursively(child);
				}
			}
		}
		try {
			Files.deleteIfExists(file.toPath());
		} catch (IOException e) {
			throw new IllegalStateException("Unable to delete " + file, e);
		}
	}
}
