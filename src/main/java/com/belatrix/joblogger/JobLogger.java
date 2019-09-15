package com.belatrix.joblogger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import com.belatrix.logger.ILogger;
import com.belatrix.logger.BelatrixLevel;

public class JobLogger implements ILogger {
	private static final String DEFAULT_LOG_FILE = "./log/joblogger.log";
	private static final Formatter DEFAULT_FORMATTER = new JobLoggerFormatter();
	private static final String DEFAULT_DB_URL = "jdbc:h2:~/test";
	private static final String DEFAULT_DB_USER = "sa";
	private static final String DB_KEY_URL = "url";
	private static final String DB_KEY_USER = "user";
	private static final Map<String, String> DEFAULT_DB_PARAMS = new HashMap<>();
	
	private Logger logger;
	private Handler consoleHandler;
	private Handler fileHandler;
	private Handler databaseHandler;
	private boolean logErrors = true;
	private boolean logWarnings = true;
	private boolean logMessages = true;
	
	static {
		DEFAULT_DB_PARAMS.put(DB_KEY_URL, DEFAULT_DB_URL);
		DEFAULT_DB_PARAMS.put(DB_KEY_USER, DEFAULT_DB_USER);
	}
	
	private JobLogger(String logFile, Map<String, String> dbParams) {
		initializeLogger();
		initializeConsoleHandler();
		initializeFileHandler(logFile);
		initializeDbHandler(dbParams);
	}
	
	private void initializeLogger() {
		logger = Logger.getLogger(JobLogger.class.getName());
		logger.setUseParentHandlers(false);
	}
	
	private void initializeConsoleHandler() {
		consoleHandler = new ConsoleHandler();
		consoleHandler.setFormatter(DEFAULT_FORMATTER);
	}
	
	private void initializeDbHandler(Map<String, String> dbParams) {
		String url = getRequiredDbParam(dbParams, DB_KEY_URL);
		getRequiredDbParam(dbParams, DB_KEY_USER);
		Properties connectionProperties = new Properties();
		dbParams.entrySet().forEach(entry -> connectionProperties.setProperty(entry.getKey(), entry.getValue()));
		databaseHandler = new DatabaseHandler(url, connectionProperties);
	}
	
	private void initializeFileHandler(String logFile) {
		try {
			fileHandler = new FileHandler(logFile, true);
			fileHandler.setFormatter(DEFAULT_FORMATTER);
		} catch (SecurityException e) {
			logError("There is an access permission problem for the file " + logFile, e);
		} catch (IOException e) {
			logError("There is a problem accesing the file " + logFile, e);
		}
	}
	
	private String getRequiredDbParam(Map<String, String> dbParams, String key) {
		return Optional.ofNullable(dbParams.get(key))
				.orElseThrow(() -> new IllegalArgumentException("The database key [" + key + "] must be provided"));
	}
	
	private void logError(String message, Throwable t) {
		Handler handler = new ConsoleHandler();
		handler.setFormatter(new SimpleFormatter());
		logger.addHandler(handler);
		logger.log(Level.SEVERE, message, t);
		logger.removeHandler(handler);
	}
	
	/**
	 * Create a JobLogger instance with default log file {@value #DEFAULT_LOG_FILE}
	 * and default db (url={@value #DEFAULT_DB_URL} user={@value #DEFAULT_DB_USER})
	 * 
	 * @return JobLogger instance
	 */
	public static JobLogger getLogger() {
		return new JobLogger(DEFAULT_LOG_FILE, DEFAULT_DB_PARAMS);
	}
	
	/**
	 * Create a JobLogger instance with the specified log file and default
	 * db (url={@value #DEFAULT_DB_URL} user={@value #DEFAULT_DB_USER}).
	 * i.e.
	 * <pre>
	 * {@code ILogger logger = JobLogger.getLogger("./myDir/myLog.log");}
	 * </pre>
	 *
	 * @param logFile the log file that is going to be used when logging into file
	 * @return JobLogger instance
	 */
	public static JobLogger getLogger(String logFile) {
		return new JobLogger(logFile, DEFAULT_DB_PARAMS);
	}
	
	/**
	 * Create a JobLogger instance with default log file {@value #DEFAULT_LOG_FILE}
	 * and the specified details for the database connection
	 * i.e.
	 * <pre>
	 * {@code
	 * Map<String, String> dbParams = new HasMap<>();
	 * dbParams.put("url", "jdbc:h2:mem:joblogger-integration-test;DB_CLOSE_DELAY=-1");
	 * dbParams.put("user", "sa");
	 * dbParams.put("password", "mypass");
	 * ILogger logger = JobLogger.getLogger(dbParams);
	 * }
	 * </pre>
	 *
	 * @param dbParams info of the database that is going to be used when logging to the database
	 * @return JobLogger instance
	 */
	public static JobLogger getLogger(Map<String, String> dbParams) {
		return new JobLogger(DEFAULT_LOG_FILE, dbParams);
	}
	
	/**
	 * Create a JobLogger instance with the specified log file and the details for the database connection
	 * i.e.
	 * <pre>
	 * {@code
	 * Map<String, String> dbParams = new HasMap<>();
	 * dbParams.put("url", "jdbc:h2:mem:joblogger-integration-test;DB_CLOSE_DELAY=-1");
	 * dbParams.put("user", "sa");
	 * dbParams.put("password", "mypass");
	 * ILogger logger = JobLogger.getLogger("./myDir/myLog.log", dbParams);
	 * }
	 * </pre>
	 * @param logFile the log file that is going to be used when logging into file
	 * @param dbParams info of the database that is going to be used when logging to the database
	 * @return JobLogger instance
	 */
	public static JobLogger getLogger(String logFile, Map<String, String> dbParams) {
		return new JobLogger(logFile, dbParams);
	}
	
	public boolean isLogErrors() {
		return logErrors;
	}

	/**
	 * Configure if the logger should log errors
	 * 
	 * @param logErrors default is {@code true}
	 * @return this instance
	 */
	public JobLogger setLogErrors(boolean logErrors) {
		this.logErrors = logErrors;
		return this;
	}

	public boolean isLogWarnings() {
		return logWarnings;
	}

	/**
	 * Configure if the logger should log warnings
	 * 
	 * @param logWarnings default is {@code true}
	 * @return this instance
	 */
	public JobLogger setLogWarnings(boolean logWarnings) {
		this.logWarnings = logWarnings;
		return this;
	}

	public boolean isLogMessages() {
		return logMessages;
	}

	/**
	 * Configure if the logger should log messages
	 * 
	 * @param logMessages default is {@code true}
	 * @return this instance
	 */
	public JobLogger setLogMessages(boolean logMessages) {
		this.logMessages = logMessages;
		return this;
	}
	
	@Override
	public void message(boolean logToConsole, boolean logToFile, boolean logToDatabase, String message) {
		if (!logMessages) return;
		logMessage(BelatrixLevel.MESSAGE.getLevel(), logToConsole, logToFile, logToDatabase, message);
	}
	
	@Override
	public void warning(boolean logToConsole, boolean logToFile, boolean logToDatabase, String message) {
		if (!logWarnings) return;
		logMessage(BelatrixLevel.WARNING.getLevel(), logToConsole, logToFile, logToDatabase, message);
	}
	
	@Override
	public void error(boolean logToConsole, boolean logToFile, boolean logToDatabase, String message) {
		if (!logErrors) return;
		logMessage(BelatrixLevel.ERROR.getLevel(), logToConsole, logToFile, logToDatabase, message);
	}
	
	private void logMessage(Level level, boolean logToConsole, boolean logToFile, boolean logToDatabase, String message) {
		List<Handler> handlers = getHandlers(logToConsole, logToFile, logToDatabase);
		handlers.forEach(logger::addHandler);
		logger.log(level, message);
		handlers.forEach(logger::removeHandler);
	}
	
	private List<Handler> getHandlers(boolean logToConsole, boolean logToFile, boolean logToDatabase) {
		List<Handler> handlers = new ArrayList<>();
		if (logToConsole) {
			handlers.add(consoleHandler);
		}
		if (logToFile) {
			Optional.ofNullable(fileHandler).ifPresent(handlers::add);
		}
		if (logToDatabase) {
			handlers.add(databaseHandler);
		}
		return handlers;
	}
}
