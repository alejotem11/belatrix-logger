package com.belatrix.joblogger;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.belatrix.logger.ILogger;

public class JobLoggerTests {
	
	private static final Pattern PATTERN_FORMATTER = Pattern.compile("\\[(.+)\\] \\[(.*)\\] (.*)");
	private static final String LEVEL_MESSAGE = "message";
	private static final String LEVEL_WARNING = "warning";
	private static final String LEVEL_ERROR = "error";
	private static final Map<String, String> DB_PARAMS = new HashMap<>();
	private static final String SQL_QUERY = "select level, date, message from log_values";
	private static Connection connection;
	private File logFile;
	private static final ByteArrayOutputStream CONSOLE_OUTPUT = new ByteArrayOutputStream();
	
	@Rule
	public TemporaryFolder testFile = new TemporaryFolder();
	
	@BeforeClass
	public static void setUp() throws SQLException {
		initializeDatabase();
		initializeConsole();
	}
	
	private static void initializeDatabase() throws SQLException {
		String dbUrl = "jdbc:h2:mem:joblogger-integration-test;DB_CLOSE_DELAY=-1";
		String dbUser = "sa";
		String keyUrl = "url";
		String keyUser = "user";
		DB_PARAMS.put(keyUrl, dbUrl);
		DB_PARAMS.put(keyUser, dbUser);
		
		Properties connectionProperties = new Properties();
		connectionProperties.setProperty(keyUrl, dbUrl);
		connectionProperties.setProperty(keyUser, dbUser);
		connection = DriverManager.getConnection(dbUrl, connectionProperties);
	}
	
	private static void initializeConsole() {
		System.setErr(new PrintStream(CONSOLE_OUTPUT));
	}
	
	@AfterClass
	public static void tearDown() throws SQLException {
		connection.close();
	}
	
	@Before
	public void createLogFile() throws IOException {
		logFile = testFile.newFile("test-log.log");
	}
	
	@After
	public void dropAllObjectsFromDatabase() throws SQLException {
		try (Statement st = connection.createStatement()) {
			st.execute("drop all objects");
		}
	}

	@Test
	public void shouldExcludeWarningAndLogInConsole() {
		ILogger logger = JobLogger.getLogger(logFile.getAbsolutePath()).setLogWarnings(false);
		String txtMessage = "message test";
		String txtError = "error test";
		String txtWarning = "warning test";
		logger.message(true, false, false, txtMessage);
		logger.warning(true, false, false, txtWarning);
		logger.error(true, false, false, txtError);
		String output = CONSOLE_OUTPUT.toString();
		String[] lines = output.split("\\n");
		assertTrue("Should log only two messages (excluding warning message)", lines.length == 2);
		String messageLog = lines[0];
		String errorLog = lines[1];
		checkFormattedLine(LEVEL_MESSAGE, txtMessage, messageLog);
		checkFormattedLine(LEVEL_ERROR, txtError, errorLog);
	}
	
	@Test
	public void logWarningInDatabaseAndLogErrorInConsoleAndDatabase() throws IOException, SQLException {
		ILogger logger = JobLogger.getLogger(logFile.getAbsolutePath(), DB_PARAMS);
		String txtWarning = "warning test";
		String txtError = "something wrong happend";
		logger.warning(false, true, true, txtWarning);
		logger.error(true, false, true, txtError);
		try (BufferedReader br = new BufferedReader(new FileReader(logFile))) {
			String[][] recordsExpected = {
					{LEVEL_WARNING, txtWarning},
					{LEVEL_ERROR, txtError}
				};
			checkDatabaseRecord(recordsExpected);
			String line = br.readLine();
			checkFormattedLine(LEVEL_WARNING, txtWarning, line);
		}
	}
	
	@Test
	public void logErrorToDatabase() throws SQLException {
		ILogger logger = JobLogger.getLogger(logFile.getAbsolutePath(), DB_PARAMS);
		String txtMessage = "error test";
		logger.error(false, false, true, txtMessage);
		String[][] recordsExpected = {{LEVEL_ERROR, txtMessage}};
		checkDatabaseRecord(recordsExpected);
	}
	
	private void checkFormattedLine(String levelExpected, String messageExpected, String lineRead) {
		String[] logFields = getLogFields(lineRead);
		String level = logFields[0];
		String date = logFields[1];
		String message = logFields[2];
		assertThat(level, is(levelExpected));
		assertThat(date, startsWith(getCurrentDate()));
		assertThat(message, is(messageExpected));
	}
	
	private void checkDatabaseRecord(String[][] recordsExpected) throws SQLException {
		try (Statement st = connection.createStatement();
				ResultSet rs = st.executeQuery(SQL_QUERY)) {
			int i = 0;
			while (rs.next()) {
				String level = rs.getString(1);
				String date = rs.getString(2);
				String message = rs.getString(3);
				String levelExpected = recordsExpected[i][0];
				String messageExpected = recordsExpected[i][1];
				assertThat(level, is(levelExpected));
				assertThat(date, startsWith(getCurrentDate()));
				assertThat(message, is(messageExpected));
				i++;
			}
		}
	}
	
	private String[] getLogFields(String msg) {
		Matcher matcher = PATTERN_FORMATTER.matcher(msg);
		String[] result = new String[3];
		if (matcher.find()) {
			String level = matcher.group(1).trim();
			String date = matcher.group(2).trim();
			String message = matcher.group(3).trim();
			result[0] = level;
			result[1] = date;
			result[2] = message;
		}
		return result;
	}
	
	private String getCurrentDate() {
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		LocalDateTime now = LocalDateTime.now();
		return dtf.format(now);
	}
}
