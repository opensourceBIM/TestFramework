package org.bimserver.test.framework;

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

import java.nio.file.Path;
import java.nio.file.Paths;

import org.bimserver.shared.BimServerClientFactory;
import org.bimserver.test.framework.actions.ActionFactory;

public class TestConfiguration {
	private int nrVirtualUsers = 1;
	private int nrRunsPerVirtualUser = -1;
	private boolean startEmbeddedBimServer = true;
	private boolean cleanEnvironmentFirst = true;
	private boolean stopOnUserException = false;
	private boolean stopOnServerException = true;
	private boolean stopNoVirtualUsers = true;
	private ActionFactory actionFactory;
	private Path outputFolder;
	private BimServerClientFactory bimServerClientFactory;
	private Path homeDir = Paths.get("home");
	private TestFileProvider testFileProvider;
	private boolean stopEmbeddedServerAfterTests = false;
	private int nrEngineProcesses;
	private String host = "localhost";
	private int port = 8080;

	public void setHost(String host) {
		this.host = host;
	}
	
	public void setPort(int port) {
		this.port = port;
	}
	
	public int getNrVirtualUsers() {
		return nrVirtualUsers;
	}

	public void setNrVirtualUsers(int nrVirtualUsers) {
		this.nrVirtualUsers = nrVirtualUsers;
	}

	public int getNrRunsPerVirtualUser() {
		return nrRunsPerVirtualUser;
	}

	public void setNrRunsPerVirtualUser(int nrRunsPerVirtualUser) {
		this.nrRunsPerVirtualUser = nrRunsPerVirtualUser;
	}

	public boolean isStartEmbeddedBimServer() {
		return startEmbeddedBimServer;
	}

	public void setStartEmbeddedBimServer(boolean startEmbeddedBimServer) {
		this.startEmbeddedBimServer = startEmbeddedBimServer;
	}

	public boolean isCleanEnvironmentFirst() {
		return cleanEnvironmentFirst;
	}

	public void setCleanEnvironmentFirst(boolean cleanEnvironmentFirst) {
		this.cleanEnvironmentFirst = cleanEnvironmentFirst;
	}

	public boolean isStopOnUserException() {
		return stopOnUserException;
	}

	public void setStopOnUserException(boolean stopOnUserException) {
		this.stopOnUserException = stopOnUserException;
	}

	public boolean isStopOnServerException() {
		return stopOnServerException;
	}

	public void setStopOnServerException(boolean stopOnServerException) {
		this.stopOnServerException = stopOnServerException;
	}

	public ActionFactory getActionFactory() {
		return actionFactory;
	}

	public void setActionFactory(ActionFactory actionFactory) {
		this.actionFactory = actionFactory;
	}

	public Path getOutputFolder() {
		return outputFolder;
	}

	public void setOutputFolder(Path outputFolder) {
		this.outputFolder = outputFolder;
	}

	public BimServerClientFactory getBimServerClientFactory() {
		return bimServerClientFactory;
	}

	public void setBimServerClientFactory(BimServerClientFactory bimServerClientFactory) {
		this.bimServerClientFactory = bimServerClientFactory;
	}
	
	public Path getHomeDir() {
		return homeDir;
	}

	public void setHomeDir(Path homeDir) {
		this.homeDir = homeDir;
	}

	public void setTestFileProvider(TestFileProvider testFileProvider) {
		this.testFileProvider = testFileProvider;
	}
	
	public TestFileProvider getTestFileProvider() {
		return testFileProvider;
	}

	public boolean isStopNoVirtualUsers() {
		return stopNoVirtualUsers;
	}

	public void setStopNoVirtualUsers(boolean stopNoVirtualUsers) {
		this.stopNoVirtualUsers = stopNoVirtualUsers;
	}

	public boolean isStopEmbeddedServerAfterTests() {
		return stopEmbeddedServerAfterTests;
	}
	
	public void setStopEmbeddedServerAfterTests(boolean stopEmbeddedServerAfterTests) {
		this.stopEmbeddedServerAfterTests = stopEmbeddedServerAfterTests;
	}

	public void setNrEngineProcesses(int nrEngineProcesses) {
		this.nrEngineProcesses = nrEngineProcesses;
	}
	
	public int getNrEngineProcesses() {
		return nrEngineProcesses;
	}

	public int getPort() {
		return port;
	}

	public String getHost() {
		return host;
	}
}