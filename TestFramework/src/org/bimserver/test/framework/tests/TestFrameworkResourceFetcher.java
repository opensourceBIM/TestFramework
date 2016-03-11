package org.bimserver.test.framework.tests;

import java.nio.file.Path;

import org.bimserver.plugins.ResourceFetcher;

public class TestFrameworkResourceFetcher extends ResourceFetcher {
	public TestFrameworkResourceFetcher(Path dir) {
		addPath(dir);
	}
}
