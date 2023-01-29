/*
 * Copyright (c) 2023, Draque Thompson, draquemail@gmail.com
 * All rights reserved.
 *
 * Licensed under: MIT License
 * See LICENSE.TXT included with this code to read the full license agreement.

 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package org.darisadesigns.polyglotlina.ManagersCollections;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.UUID;

import org.darisadesigns.polyglotlina.DictCore;
import org.darisadesigns.polyglotlina.Nodes.PhraseNode;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author pe1uca
 */
public class TranslationManager implements AutoCloseable {

	public enum SourceLang {
		CONLANG,
		LOCALLANG
	}

	public enum ResultColumn {
		HIGHLIGHT,
		CONLANG,
		LOCALLANG,
		ID,
		PRIORITY,
		RANK
	}

	public static final String ZIP_FILE_NAME = "translation.db";

	public static final String MANAGER_XID = "translations";
	public static final String TMP_FILENAME_XID = "TmpFileName";

	private String tmpFileName = "";
	private final DictCore core;
	private Connection connection;
	private boolean isInitialized = false;
	private boolean newFile = true;

	public TranslationManager(DictCore core) {
		this.core = core;
	}

	public void setTmpFileName(String fileName) {
		this.tmpFileName = fileName;
	}

	public String getTmpFileName() {
		if (this.tmpFileName.isEmpty()) {
			UUID uuid = UUID.randomUUID();
			this.tmpFileName = uuid.toString() + ".db";
		}
		return this.tmpFileName;
	}

	public DictCore getCore() {
		return this.core;
	}

	public boolean isDBOpen() {
		return null != this.connection;
	}

	public void setIsInitialized(boolean isInitialized) {
		this.isInitialized = isInitialized;
	}

	public boolean isInitialized() {
		return this.isInitialized;
	}

	public Connection openConnection() throws SQLException, IOException {
		return this.openConnection(this.getTmpFileName());
	}

	public Connection openConnection(String dbName) throws SQLException, IOException {
		if (null != this.connection) return this.connection;

		dbName = dbName.replaceFirst("(?i)\\.db$", "") + ".db";
		File dir = core.getOSHandler().getWorkingDirectory();
		String dbPath = dir.getCanonicalPath() + File.separator + dbName;
		File dbFile = new File(dbPath);
		// If the file doesn't exist a new file will be created when connecting
		newFile = !dbFile.exists();
		String connectionString = "jdbc:sqlite:%s".formatted(dbPath);
		this.connection = DriverManager.getConnection(connectionString);
		if(null == this.connection) throw new IOException("Unable to connect to translation DB");
		
		return this.connection;
	}

	@Override
	public void close() throws Exception {
		if (null == this.connection) return;

		try {
			this.connection.close();
		} catch (SQLException e) {
			// Ignore errors and set to null
		}
		this.connection = null;
	}

	public boolean deleteTmpFile() throws IOException {
		File dir = core.getOSHandler().getWorkingDirectory();
		String dbPath = dir.getCanonicalPath() + File.separator + this.getTmpFileName();
		File tmpFile = new File(dbPath);
		return tmpFile.delete();
	}

	public void initializeTmpTranslationDB(String tokenChars, String separators) {
		this.initializeTranslationDB(this.getTmpFileName(), tokenChars, separators);
	}
	
	public void initializeTranslationDB(String dbName, String tokenChars, String separators) {
		try {
			// Normalize database file extension
			openConnection(dbName);

			if (newFile) {
				createDBSchema(tokenChars, separators);
				prefillTranslations();
			}
			this.isInitialized = true;
		} catch (IOException | SQLException e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
		}
	}

	private void createDBSchema(String tokenChars, String separators) throws SQLException {
		try {
			String fts5Create = "CREATE VIRTUAL TABLE translations USING fts5(conlang, locallang, id UNINDEXED, priority UNINDEXED, tokenize = \"unicode61 remove_diacritics 0 tokenchars '%s' separators '%s'\")";
			this.connection.createStatement().execute(fts5Create.formatted(tokenChars, separators));
			Statement statement = this.connection.createStatement();
			// If the table was just created we recreate an SQLite intermediate table to include some constraints
			statement.addBatch("DROP TABLE translations_content;");
			statement.addBatch("""
				CREATE TABLE translations_content (
					id INTEGER PRIMARY KEY, -- Internal ID of SQLite
					c0         NOT NULL,    -- conlang
					c1         NOT NULL,    -- locallang
					c2         UNIQUE,      -- Polyglot phrase ID
					c3         DEFAULT (0), -- Priority to prefer one result over other
					UNIQUE(c0, c1)
				);
				""");
			statement.executeBatch();
		} catch (SQLException e) {
			// We ignore the error if the table already existed. We throw other exceptions
			if(!e.getMessage().contains("table translations already exists")) {
				throw e;
			}
		}
	}

	/**
	 * Retrieves all the data from the phrasebook and inserts in the DB.  
	 * This is intended to be ran in an empty DB.  
	 * 
	 * @throws SQLException
	 */
	private void prefillTranslations() throws SQLException {
		try {
			// Manually control commit and rollback of the transaction
			this.connection.setAutoCommit(false);

			List<PhraseNode> phrases = this.core.getPhraseManager().getAllValues();
			String sql = "INSERT INTO translations(conlang, locallang, id, priority) VALUES(?, ?, ?, 0)";
			for (PhraseNode phraseNode : phrases) {
				PreparedStatement statement = this.connection.prepareStatement(sql);

				statement.setString(1, phraseNode.getConPhrase());
				statement.setString(2, phraseNode.getLocalPhrase());
				statement.setInt(3, phraseNode.getId());

				statement.executeUpdate();
			}

			this.connection.commit();
		} catch(SQLException e) {
			this.connection.rollback();
			throw e;
		} finally {
			// Revert to default behavior to commit each statement
			this.connection.setAutoCommit(true);
		}
	}

	/**
	 * Adds a dedicated translation (not registered in phrasebook).  
	 * Helps with small translations that user might not want to save as a full phrase.
	 * 
	 * @param conlang Phrase in the conlang
	 * @param localLang Phrase in the local language for translation
	 * @throws SQLException
	 */
	public void addTranslation(String conlang, String localLang) throws SQLException {
		this.addTranslation(-1, conlang, localLang);
	}

	/**
	 * Adds a translation from a node in the phrasebook.
	 * 
	 * @param phrase Phrasebook entry from which to extract the data
	 * @throws SQLException
	 */
	public void addTranslation(PhraseNode phrase) throws SQLException {
		this.addTranslation(phrase.getId(), phrase.getConPhrase(), phrase.getLocalPhrase());
	}

	/**
	 * Adds a unique translation to the DB.
	 * The unique constraint comes from the ID in the phrasebook.  
	 * An id of `-1` is treated as null by the DB which allows to have multiple rows with that id.  
	 * 
	 * @param id ID to link this translation in the phrasebook (-1 if the translation isn't present in the phrasebook)
	 * @param conlang Phrase in the conlang
	 * @param localLang Phrase in the local language for translation
	 * @throws SQLException
	 */
	private void addTranslation(int id, String conlang, String localLang) throws SQLException {
		if (null == this.connection) throw new SQLException("Connection hasn't been opened");

		String sql = "INSERT INTO translations(conlang, locallang, id, priority) VALUES(?, ?, ?, 0)";
		PreparedStatement statement = this.connection.prepareStatement(sql);
		statement.setString(1, conlang);
		statement.setString(2, localLang);
		if (-1 == id) {
			statement.setNull(3, java.sql.Types.INTEGER);
		}
		else {
			statement.setInt(3, id);
		}

		statement.executeUpdate();
	}

	public List<EnumMap<ResultColumn, String>> searchTranslationsFor(SourceLang source, String phrase) throws SQLException {
		if (null == this.connection) throw new SQLException("Connection hasn't been opened");
		if (phrase.isBlank()) return new ArrayList<>();

		int columnIdx = 0;
		String column = "";
		switch (source) {
			case CONLANG:
				columnIdx = 0;
				column = "conlang";
				break;
			case LOCALLANG:
				columnIdx = 1;
				column = "locallang";
				break;
		}

		// Try to sanitize string for the query
		phrase = phrase.replaceAll("[-'\"\\?!\\.\\(\\)\\[\\]\\\\]", " ");
		
		String sql = """
			SELECT highlight(translations, %d, '<b>', '</b>') as conlang_highlight, conlang, locallang, id, priority, rank 
			FROM translations 
			WHERE %s MATCH ? order by rank
			""".formatted(columnIdx, column);
		// find matches with any of the words in the phrase
		// OR the exact phrase
		String whereMatch = "(%s) OR \"%s\"".formatted(phrase.replaceAll(" ", " OR "), phrase);
		PreparedStatement statement = this.connection.prepareStatement(sql);
		statement.setString(1, whereMatch);

		ResultSet res = statement.executeQuery();

		ArrayList<EnumMap<ResultColumn, String>> result = new ArrayList<>();
		
		while(res.next()) {
			EnumMap<ResultColumn, String> row = new EnumMap<>(ResultColumn.class);

			row.put(ResultColumn.HIGHLIGHT, res.getString("conlang_highlight"));
			row.put(ResultColumn.CONLANG, res.getString("conlang"));
			row.put(ResultColumn.LOCALLANG, res.getString("locallang"));
			row.put(ResultColumn.ID, res.getString("id"));
			row.put(ResultColumn.PRIORITY, res.getString("priority"));
			row.put(ResultColumn.RANK, res.getString("rank"));

			result.add(row);
		}
		return result;
	}

	/**
     * Writes manager information to XML document
	 * 
     * @param doc Document to write to
     * @param rootElement root element of document
     */
    public void writeXML(Document doc, Element rootElement) {
        Element translationManager = doc.createElement(TranslationManager.MANAGER_XID);
        
        Element currentElement = doc.createElement(TranslationManager.TMP_FILENAME_XID);
        currentElement.appendChild(doc.createTextNode(this.getTmpFileName()));
        translationManager.appendChild(currentElement);
        
        rootElement.appendChild(translationManager);
    }
}
