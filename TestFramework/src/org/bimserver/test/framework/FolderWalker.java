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

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

import org.bimserver.utils.Formatters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FolderWalker implements TestFileProvider {

	private static final Logger LOGGER = LoggerFactory.getLogger(FolderWalker.class);
	private Queue<Path> listFiles;
	private final TestFramework testFramework;
	private int lastPerc;
	private int done;
	private int total;
	private long totalByteSize;

	public FolderWalker(Path folder, TestFramework testFramework) {
		this.testFramework = testFramework;
		try {
			this.listFiles = new ArrayBlockingQueue<>(1000000);
			
			processFolder(folder);
			
			LOGGER.info("Total size of IFC files: " + Formatters.bytesToString(totalByteSize) + " in " + total + " files");
			
			this.total = this.listFiles.size();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void processFolder(Path folder) throws IOException {
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(folder)) {
			for (final Iterator<Path> it = stream.iterator(); it.hasNext();) {
				Path path = it.next();
				if (Files.isDirectory(path)) {
					processFolder(path);
				} else {
					if (path.getFileName().toString().toLowerCase().endsWith(".ifc") || path.getFileName().toString().toLowerCase().endsWith(".ifcxml") || path.getFileName().toString().toLowerCase().endsWith(".ifczip")) {
						listFiles.add(path);
						totalByteSize += Files.size(path);
						total++;
					}
				}
			}
		}		
	}

	@Override
	public synchronized Path getNewFile() {
		Path poll = listFiles.poll();
		if (poll == null) {
			testFramework.stop();
			return null;
		}
		done++;
		int percentage = (int)(100.0 * done / total);
		if (percentage > lastPerc) {
			LOGGER.info("");
			LOGGER.info("");
			LOGGER.info(percentage + "%");
			LOGGER.info("");
			LOGGER.info("");
			lastPerc = percentage;
		}
		return poll;
	}

	@Override
	public synchronized void giveBack(Path randomFile) {
		LOGGER.info("Giving back file " + randomFile);
		done--;
		listFiles.add(randomFile);
	}
}
