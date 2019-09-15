package com.belatrix.joblogger;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import com.belatrix.logger.BelatrixLevel;

public class JobLoggerFormatter extends Formatter {
	
	private static final Map<Level, BelatrixLevel> LOGGER_LEVEL = new HashMap<>();
	
	static {
		LOGGER_LEVEL.put(Level.INFO, BelatrixLevel.MESSAGE);
		LOGGER_LEVEL.put(Level.WARNING, BelatrixLevel.WARNING);
		LOGGER_LEVEL.put(Level.SEVERE, BelatrixLevel.ERROR);
	}
	@Override
	public String format(LogRecord record) {
		String date = getDate();
		String level = String.format("%-7s", getLevel(record.getLevel()));
		String message = record.getMessage();
		return "[" + level + "] [" + date + "] " + message + "\n";
	}
	
	static String getLevel(Level level) {
		return LOGGER_LEVEL.get(level).getLoggerLevel();
	}
	
	static String getDate() {
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
		LocalDateTime now = LocalDateTime.now();
		return dtf.format(now);
	}
}
