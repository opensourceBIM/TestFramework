package org.bimserver.test.framework.actions;

import java.util.List;

import org.bimserver.interfaces.objects.SRenderEnginePluginConfiguration;

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

import org.bimserver.interfaces.objects.SUser;
import org.bimserver.interfaces.objects.SUserType;
import org.bimserver.shared.exceptions.PublicInterfaceNotFoundException;
import org.bimserver.shared.exceptions.ServerException;
import org.bimserver.shared.exceptions.UserException;
import org.bimserver.test.framework.TestFramework;
import org.bimserver.test.framework.VirtualUser;

public class CreateUserAction extends Action {

	public CreateUserAction(TestFramework testFramework) {
		super(testFramework);
	}

	@Override
	public void execute(VirtualUser virtualUser) throws ServerException, UserException, PublicInterfaceNotFoundException {
		String username = randomString() + "@bimserver.org";
		virtualUser.getActionResults().setText("Creating new user: " + username);
		SUser user = virtualUser.getBimServerClient().getServiceInterface().addUser(username, randomString(), SUserType.values()[nextInt(SUserType.values().length)], nextBoolean(), "");
		virtualUser.getBimServerClient().getBimServerAuthInterface().changePassword(user.getOid(), "", "test");
		List<SRenderEnginePluginConfiguration> allRenderEngines = virtualUser.getBimServerClient().getPluginInterface().getAllRenderEngines(true);
		for (SRenderEnginePluginConfiguration sRenderEnginePluginConfiguration : allRenderEngines) {
			if (sRenderEnginePluginConfiguration.getName().equals("NOP Render Engine")) {
				virtualUser.getBimServerClient().getPluginInterface().setDefaultRenderEngine(sRenderEnginePluginConfiguration.getOid());
			}
		}
		virtualUser.addUsername(username);
	}
	
	@Override
	public int getWeight() {
		return 10;
	}
}
