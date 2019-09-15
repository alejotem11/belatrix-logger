package com.belatrix.joblogger.exception;

public class SQLRuntimeException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public SQLRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}
}
