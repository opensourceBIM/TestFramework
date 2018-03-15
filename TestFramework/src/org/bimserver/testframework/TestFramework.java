package org.bimserver.testframework;

/******************************************************************************
 * Copyright (C) 2009-2018  BIMserver.org
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see {@literal<http://www.gnu.org/licenses/>}.
 *****************************************************************************/

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class TestFramework {
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
	private static final Logger LOGGER = LoggerFactory.getLogger(TestFramework.class);

	public static void main(String[] args) {
		if (args.length == 0) {
			LOGGER.error("Missing configuration argument");
			return;
		}
		new TestFramework().start(args[0]);
	}

	private void start(String configFileLocation) {
		Path configFile = Paths.get(configFileLocation);
		if (!Files.exists(configFile)) {
			LOGGER.error("Config file not found " + configFileLocation);
			return;
		}

		try {
			ObjectNode config = readConfiguration(configFile);
			Connection connection = setupDatabase((ObjectNode)(config.get("database")));
			
			Run run = new Run(config, connection);
			run.start();
		} catch (Exception e) {
			LOGGER.error("", e);
			return;
		}
	}

	private Connection setupDatabase(ObjectNode config) throws Exception {
		Class.forName("com.mysql.jdbc.Driver");
		// Setup the connection with the DB
		return DriverManager.getConnection("jdbc:mysql://" + config.get("host").asText() + "/" + config.get("database").asText() + "?" + "user=" + config.get("username").asText() + "&password=" + config.get("password").asText());
	}

	private ObjectNode readConfiguration(Path configFile) throws Exception {
		return OBJECT_MAPPER.readValue(configFile.toFile(), ObjectNode.class);
	}
}