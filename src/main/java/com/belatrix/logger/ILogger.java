package com.belatrix.logger;

public interface ILogger {
	/**
	 * Log message as message level
	 * 
	 * @param logToConsole	log in console
	 * @param logToFile		log into file
	 * @param logToDatabase	log to database
	 * @param message		message to be logged
	 */
	void message(boolean logToConsole, boolean logToFile, boolean logToDatabase, String message);
	/**
	 * Log message as warning level
	 * 
	 * @param logToConsole	log in console
	 * @param logToFile		log into file
	 * @param logToDatabase	log to database
	 * @param message		message to be logged
	 */
	void warning(boolean logToConsole, boolean logToFile, boolean logToDatabase, String message);
	/**
	 * Log message as error level
	 * 
	 * @param logToConsole	log in console
	 * @param logToFile		log into file
	 * @param logToDatabase	log to database
	 * @param message		message to be logged
	 */
	void error(boolean logToConsole, boolean logToFile, boolean logToDatabase, String message);
}
