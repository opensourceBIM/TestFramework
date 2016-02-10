package org.bimserver.testframework;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.GregorianCalendar;

import com.fasterxml.jackson.databind.node.ObjectNode;

public class Run {

	private Connection connection;
	private ObjectNode config;

	public Run(ObjectNode config, Connection connection) {
		this.config = config;
		this.connection = connection;
	}
	
	public void start() {
		try {
			PreparedStatement statement = connection.prepareStatement("INSERT INTO run (datetime) VALUES (?)");
			statement.setTimestamp(1, new Timestamp(new GregorianCalendar().getTimeInMillis()));
			statement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}