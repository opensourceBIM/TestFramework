package org.bimserver.test.framework.actions;

/******************************************************************************
 * Copyright (C) 2009-2017  BIMserver.org
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

import org.bimserver.interfaces.objects.SActionState;
import org.bimserver.interfaces.objects.SDeserializerPluginConfiguration;
import org.bimserver.interfaces.objects.SLongActionState;
import org.bimserver.interfaces.objects.SProject;
import org.bimserver.plugins.services.Flow;
import org.bimserver.test.framework.TestFramework;
import org.bimserver.test.framework.VirtualUser;

public class CheckinAction extends Action {

	private final CheckinSettings settings;

	public CheckinAction(TestFramework testFramework, CheckinSettings settings) {
		super(testFramework);
		this.settings = settings;
	}

	@Override
	public void execute(VirtualUser virtualUser) throws Exception {
		SProject project = virtualUser.getRandomProject();
		Path randomFile = getTestFramework().getTestFile();
		String fileName = randomFile.getFileName().toString();
		String extension = fileName.substring(fileName.lastIndexOf(".") + 1);
		SDeserializerPluginConfiguration suggestedDeserializerForExtension = virtualUser.getBimServerClient().getServiceInterface().getSuggestedDeserializerForExtension(extension, project.getOid());
		
		if (suggestedDeserializerForExtension == null) {
			virtualUser.getActionResults().setText("No deserializer found for extension " + extension + " in file " + fileName);
			return;
		}
		
		Flow flow = settings.shouldAsync() ? Flow.ASYNC : Flow.SYNC;
		boolean merge = settings.shouldMerge();
		virtualUser.getActionResults().setText("Checking in new revision on project " + project.getName() + " (" + fileName + ") " + "sync: " + flow + ", merge: " + merge);
		long topicId;
		topicId = virtualUser.getBimServerClient().checkin(project.getOid(), randomString(), suggestedDeserializerForExtension.getOid(), merge, flow, randomFile);
		if (flow == Flow.SYNC) {
			SLongActionState longActionState = virtualUser.getBimServerClient().getRegistry().getProgress(topicId);
			if (longActionState.getState() == SActionState.AS_ERROR) {
				virtualUser.getActionResults().setText("" + longActionState.getErrors());
			}
			virtualUser.getBimServerClient().getServiceInterface().cleanupLongAction(topicId);
		} else {
			while (true) {
				SLongActionState checkinState = virtualUser.getBimServerClient().getRegistry().getProgress(topicId);
				if (checkinState.getState() == SActionState.FINISHED || checkinState.getState() == SActionState.UNKNOWN) {
					virtualUser.getBimServerClient().getServiceInterface().cleanupLongAction(topicId);
					break;
				}
				Thread.sleep(1000);
			}
		}
	}
	
	@Override
	public int getWeight() {
		return 10;
	}
}