package org.bimserver.test.framework.actions;

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