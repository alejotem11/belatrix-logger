package com.belatrix.logger;

import java.util.logging.Level;

public enum BelatrixLevel {
	MESSAGE(Level.INFO, "message"),
	WARNING(Level.WARNING, "warning"),
	ERROR(Level.SEVERE, "error");
	
	private Level level;
	private String loggerLevel;
	
	BelatrixLevel(Level level, String belatrixLevel) {
		this.level = level;
		this.loggerLevel = belatrixLevel;
	}

	public Level getLevel() {
		return level;
	}
	
	public String getLoggerLevel() {
		return loggerLevel;
	}
}
