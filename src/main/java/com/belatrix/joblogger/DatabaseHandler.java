package com.belatrix.joblogger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

import com.belatrix.joblogger.exception.SQLRuntimeException;

public class DatabaseHandler extends Handler {
	private static final String SQL_CREATE_TABLE = "create table if not exists log_values "
			+ "(id integer auto_increment, "
            + " level varchar(255), "
            + " date varchar(255), "
            + " message varchar(255), "
            + " primary key ( id ))";
	private static final String SQL_INSERT_RECORD = "insert into log_values(level, date, message) "
			+ "values ('%s', '%s', '%s')";
	private String url;
	private Properties connectionProperties;
	
	public DatabaseHandler(String url, Properties connectionProperties) {
		this.url = url;
		this.connectionProperties = connectionProperties;
		executeSql(SQL_CREATE_TABLE);
	}
	
	private void executeSql(String sql) {
		try (Connection con = DriverManager.getConnection(url, connectionProperties);
				Statement st = con.createStatement()) {
			st.execute(sql);
		} catch (SQLException ex) {
			throw new SQLRuntimeException("A database access error occurred", ex);
		}
	}

	@Override
	public void publish(LogRecord record) {
		String level = JobLoggerFormatter.getLevel(record.getLevel());
		String date = JobLoggerFormatter.getDate();
		String sql = String.format(SQL_INSERT_RECORD, level, date, record.getMessage());
		executeSql(sql);
	}

	@Override
	public void flush() {
		//
	}

	@Override
	public void close() {
		//
	}

}
