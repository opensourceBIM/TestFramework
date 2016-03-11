package org.bimserver.test.framework;

/******************************************************************************
 * Copyright (C) 2009-2015  BIMserver.org
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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import org.bimserver.BimServer;
import org.bimserver.BimServerConfig;
import org.bimserver.LocalDevPluginLoader;
import org.bimserver.interfaces.objects.SServerSettings;
import org.bimserver.models.store.ServerState;
import org.bimserver.plugins.PluginManager;
import org.bimserver.shared.LocalDevelopmentResourceFetcher;
import org.bimserver.shared.interfaces.AdminInterface;
import org.bimserver.shared.interfaces.SettingsInterface;
import org.bimserver.utils.PathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestFramework {
	private static final Logger LOGGER = LoggerFactory.getLogger(TestFramework.class);
	
	private final Set<VirtualUser> virtualUsers = new HashSet<VirtualUser>();
	private final TestConfiguration testConfiguration;
	private final TestResults testResults = new TestResults();
	private BimServer bimServer;

	private Path[] pluginDirectories;

	public enum Mode {
		RUNNING,
		STOPPING
	}
	
	private volatile Mode mode = Mode.RUNNING;

	public TestFramework(TestConfiguration testConfiguration, Path[] pluginDirectories) {
		this.testConfiguration = testConfiguration;
		this.pluginDirectories = pluginDirectories;
	}
	
	public void start() {
		if (testConfiguration.isStartEmbeddedBimServer()) {
			if (testConfiguration.isCleanEnvironmentFirst()) {
				try {
					if (Files.isDirectory(testConfiguration.getHomeDir())) {
						PathUtils.removeDirectoryWithContent(testConfiguration.getHomeDir());
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			BimServerConfig bimServerConfig = new BimServerConfig();
			bimServerConfig.setStartEmbeddedWebServer(true);
			bimServerConfig.setHomeDir(testConfiguration.getHomeDir());
			bimServerConfig.setPort(8080);
			bimServerConfig.setDevelopmentBaseDir(Paths.get("."));
			bimServerConfig.setResourceFetcher(new LocalDevelopmentResourceFetcher(Paths.get(".")));
			bimServerConfig.setClassPath(System.getProperty("java.class.path"));
			bimServer = new BimServer(bimServerConfig);
			try {
				bimServer.start();
				LocalDevPluginLoader.loadPlugins(bimServer.getPluginManager(), pluginDirectories);
				// Convenience, setup the server to make sure it is in RUNNING state
				if (bimServer.getServerInfo().getServerState() == ServerState.NOT_SETUP) {
					bimServer.getService(AdminInterface.class).setup("http://localhost:8080", "Administrator", "admin@bimserver.org", "admin");
					bimServer.getService(SettingsInterface.class).setGenerateGeometryOnCheckin(false);
					bimServer.getService(SettingsInterface.class).setSendConfirmationEmailAfterRegistration(false);
				}
				
				// Change a setting so normal users can create projects
				bimServer.getService(SettingsInterface.class).setAllowUsersToCreateTopLevelProjects(true);
				SServerSettings serverSettings = bimServer.getService(SettingsInterface.class).getServerSettings();
				serverSettings.setRenderEngineProcesses(testConfiguration.getNrEngineProcesses());
				bimServer.getService(SettingsInterface.class).setServerSettings(serverSettings);
			} catch (Exception e) {
				LOGGER.error("", e);
			}
		}
		if (!Files.exists(testConfiguration.getOutputFolder())) {
			try {
				PathUtils.removeDirectoryWithContent(testConfiguration.getOutputFolder());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		VirtualUserFactory virtualUserFactory = new VirtualUserFactory(this, testConfiguration.getBimServerClientFactory());
		for (int i=0; i<testConfiguration.getNrVirtualUsers(); i++) {
			VirtualUser virtualUser = virtualUserFactory.create("Virtual User " + i);
			virtualUsers.add(virtualUser);
		}
		for (VirtualUser virtualUser : virtualUsers) {
			virtualUser.start();
		}
		org.bimserver.CommandLine commandLine = new org.bimserver.CommandLine(bimServer);
		commandLine.start();
	}

	public synchronized Path getTestFile() {
		return testConfiguration.getTestFileProvider().getNewFile();
	}

	public synchronized void unsubsribe(VirtualUser virtualUser) {
		virtualUsers.remove(virtualUser);
		if (virtualUsers.isEmpty() && testConfiguration.isStopNoVirtualUsers()) {
			if (testConfiguration.isStartEmbeddedBimServer()) {
				bimServer.stop();
			}
		}
	}

	public TestConfiguration getTestConfiguration() {
		return testConfiguration;
	}

	public void stop() {
		if (mode == Mode.STOPPING) {
			return;
		}
		LOGGER.info("Stopping TestFramework");
		mode = Mode.STOPPING;
		for (VirtualUser virtualUser : virtualUsers) {
			virtualUser.shutdown();
		}
		if (testConfiguration.isStartEmbeddedBimServer()) {
			if (testConfiguration.isStopEmbeddedServerAfterTests()) {
				bimServer.stop();
			}
		}
	}

	public void standby() {
		for (VirtualUser virtualUser : virtualUsers) {
			virtualUser.shutdown();
		}
	}

	public TestResults getResults() {
		return testResults;
	}

	public PluginManager getPluginManager() {
		return bimServer.getPluginManager();
	}

	public Mode getMode() {
		return mode;
	}
}