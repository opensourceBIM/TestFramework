package org.bimserver.test.framework.tests;

import java.nio.file.Paths;

import org.bimserver.plugins.OptionsParser;
import org.bimserver.test.framework.FolderWalker;
import org.bimserver.test.framework.RandomBimServerClientFactory;
import org.bimserver.test.framework.TestConfiguration;
import org.bimserver.test.framework.TestFramework;
import org.bimserver.test.framework.RandomBimServerClientFactory.Type;
import org.bimserver.test.framework.actions.AllActionsFactory;

public class TestAllServer {
	public static void main(String[] args) {
		TestConfiguration testConfiguration = new TestConfiguration();
		TestFramework testFramework = new TestFramework(testConfiguration, new OptionsParser(args).getPluginDirectories());

		testConfiguration.setHomeDir(Paths.get("/var/bimservertest"));
		testConfiguration.setActionFactory(new AllActionsFactory(testFramework));
		testConfiguration.setBimServerClientFactory(new RandomBimServerClientFactory(testFramework, Type.JSON));
		testConfiguration.setTestFileProvider(new FolderWalker(Paths.get("/var/ifc"), testFramework));
		testConfiguration.setOutputFolder(Paths.get("/var/bimservertestoutput"));
		testConfiguration.setNrVirtualUsers(8);
		testConfiguration.setStopNoVirtualUsers(false);
		testConfiguration.setStopOnServerException(false);
		testConfiguration.setNrEngineProcesses(8);

		testFramework.start();
	}
}
