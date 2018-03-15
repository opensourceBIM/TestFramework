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

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Random;

import org.bimserver.interfaces.objects.SProject;
import org.bimserver.interfaces.objects.SRevision;
import org.bimserver.interfaces.objects.SUser;
import org.bimserver.plugins.services.BimServerClientInterface;
import org.bimserver.shared.exceptions.PublicInterfaceNotFoundException;
import org.bimserver.shared.exceptions.ServerException;
import org.bimserver.shared.exceptions.UserException;
import org.bimserver.test.framework.TestFramework.Mode;
import org.bimserver.test.framework.actions.Action;
import org.bimserver.test.framework.actions.ActionResults;
import org.bimserver.test.framework.actions.CheckinAction;
import org.bimserver.test.framework.actions.CheckinSettings;
import org.bimserver.test.framework.actions.CreateProjectAction;
import org.bimserver.test.framework.actions.CreateUserAction;
import org.bimserver.test.framework.actions.LoginAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VirtualUser extends Thread {
	private final Logger LOGGER;
	private final BimServerClientInterface bimServerClient;
	private final TestFramework testFramework;
	private final Random random = new Random();
	private volatile boolean running;
	private final List<String> usernames = new ArrayList<String>();
	private ActionResults actionResults;
	private String username;

	public VirtualUser(TestFramework testFramework, BimServerClientInterface bimServerClient, String name) {
		setName(name);
		this.testFramework = testFramework;
		this.bimServerClient = bimServerClient;
		LOGGER = LoggerFactory.getLogger(name);
		setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
			@Override
			public void uncaughtException(Thread t, Throwable e) {
				getLogger().error("", e);
			}
		});
	}

	public ActionResults getActionResults() {
		return actionResults;
	}
	
	@Override
	public void run() {
		running = true;
		int nrRuns = 0;
		try {
			int nrRunsPerVirtualUser = this.testFramework.getTestConfiguration().getNrRunsPerVirtualUser();
			while (running && (nrRunsPerVirtualUser == -1 || nrRuns < nrRunsPerVirtualUser)) {
				Action action = null;
				GregorianCalendar start = new GregorianCalendar();
				try {
					if (!bimServerClient.getAuthInterface().isLoggedIn()) {
						actionResults = new ActionResults();
						action = new LoginAction(testFramework);
						action.execute(this);
					} else {
						action = testFramework.getTestConfiguration().getActionFactory().createAction();
						actionResults = new ActionResults();
						action.execute(this);
						if (actionResults.getType() == null) {
							actionResults.setType("OKE");
						}
						String text = actionResults.getText();
						LOGGER.info(getName() + " Success: " + (text == null || text.equals("") ? action.getClass().getSimpleName() : text));
						testFramework.getResults().addRow(actionResults, this, action, start);
					}
				} catch (UserException e) {
					if (testFramework.getMode() == Mode.STOPPING) {
						// Ignore
					} else {
						LOGGER.info(getName() + " UserException: " + e.getMessage());
						actionResults.setType("WARN");
						actionResults.setText(e.getMessage());
						testFramework.getResults().addRow(actionResults, this, action, start);
						if (this.testFramework.getTestConfiguration().isStopOnUserException()) {
							break;
						}
					}
				} catch (ServerException e) {
					if (testFramework.getMode() == Mode.STOPPING) {
						// Ignore
					} else {
						LOGGER.info(getName() + " ServerException: " + e.getMessage());
						e.printStackTrace();
						actionResults.setText(e.getMessage() + (e.getCause() != null ? (" (" + e.getCause().getMessage() + ")") : ""));
						actionResults.setType("ERROR");
						testFramework.getResults().addRow(actionResults, this, action, start);
						LOGGER.info(e.getMessage());
						if (this.testFramework.getTestConfiguration().isStopOnServerException()) {
							break;
						}
					}
				} catch (Throwable e) {
					if (testFramework.getMode() == Mode.STOPPING) {
						// Ignore
					} else {
						e.printStackTrace();
						LOGGER.info(e.getMessage());
					}
					break;
				}
				nrRuns++;
			}
		} finally {
			bimServerClient.disconnect();
			testFramework.unsubsribe(this);
		}
	}

	public SUser getRandomUser() throws PublicInterfaceNotFoundException {
		try {
			List<SUser> users = bimServerClient.getServiceInterface().getAllUsers();
			if (users.isEmpty()) {
				CreateUserAction createUserAction = new CreateUserAction(testFramework);
				createUserAction.execute(this);
			}
			users = bimServerClient.getServiceInterface().getAllUsers();
			if (!users.isEmpty()) {
				return users.get(random.nextInt(users.size()));
			}
		} catch (ServerException e) {
			LOGGER.error("", e);
		} catch (UserException e) {
			LOGGER.error("", e);
		}
		return null;
	}

	public SProject getRandomProject() throws UserException, PublicInterfaceNotFoundException {
		try {
			List<SProject> allProjects = bimServerClient.getServiceInterface().getAllProjects(false, true);
			if (allProjects == null || allProjects.isEmpty()) {
				CreateProjectAction createProjectAction = new CreateProjectAction(testFramework);
				createProjectAction.execute(this);
			}
			allProjects = bimServerClient.getServiceInterface().getAllProjects(false, true);
			if (!allProjects.isEmpty()) {
				return allProjects.get(random.nextInt(allProjects.size()));
			}
		} catch (ServerException e) {
			LOGGER.error("", e);
		}
		return null;
	}

	public BimServerClientInterface getBimServerClient() {
		return bimServerClient;
	}

	public Logger getLogger() {
		return LOGGER;
	}

	public void addUsername(String username) {
		usernames.add(username);
	}

	public List<String> getUserNames() {
		return usernames;
	}

	public String getRandomUserName() {
		return usernames.get(random.nextInt(usernames.size()));
	}

	public SRevision getRandomRevision() throws Exception {
		SProject project = getRandomProject();
		if (project.getLastRevisionId() == -1) {
			CheckinAction checkinAction = new CheckinAction(testFramework, new CheckinSettings());
			checkinAction.execute(this);
		}
		project = getBimServerClient().getServiceInterface().getProjectByPoid(project.getOid());
		if (project.getLastRevisionId() != -1) {
			return bimServerClient.getServiceInterface().getRevision(project.getLastRevisionId());
		}
		return null;
	}

	public void shutdown() {
		running = false;
		interrupt();
	}

	public void setUserName(String username) {
		this.username = username;
	}
	
	public String getUsername() {
		return username;
	}
}