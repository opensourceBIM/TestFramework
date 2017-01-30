package org.bimserver.unittests;

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

import static org.junit.Assert.fail;

import java.util.Random;

import org.bimserver.client.protocolbuffers.ProtocolBuffersBimServerClientFactory;
import org.bimserver.emf.IfcModelInterface;
import org.bimserver.interfaces.objects.SProject;
import org.bimserver.plugins.services.BimServerClientInterface;
import org.bimserver.shared.BimServerClientFactory;
import org.bimserver.shared.ChannelConnectionException;
import org.bimserver.shared.UsernamePasswordAuthenticationInfo;
import org.bimserver.shared.exceptions.BimServerClientException;
import org.bimserver.shared.exceptions.PublicInterfaceNotFoundException;
import org.bimserver.shared.exceptions.ServerException;
import org.bimserver.shared.exceptions.ServiceException;
import org.bimserver.shared.exceptions.UserException;
import org.bimserver.shared.meta.SServicesMap;
import org.bimserver.shared.pb.ProtocolBuffersMetaData;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestClientEmfModelRemoteProtocolBuffers {
	private static BimServerClientInterface bimServerClient;

	@BeforeClass
	public static void setup() throws Exception {
		try {
			ProtocolBuffersMetaData protocolBuffersMetaData = new ProtocolBuffersMetaData();
			protocolBuffersMetaData.load(null, ProtocolBuffersBimServerClientFactory.class); // TODO
			try (BimServerClientFactory factory = new ProtocolBuffersBimServerClientFactory("localhost", 8020, 8080, protocolBuffersMetaData, null, new SServicesMap())) {
				UsernamePasswordAuthenticationInfo usernamePasswordAuthenticationInfo = new UsernamePasswordAuthenticationInfo("admin@bimserver.org", "admin");
				bimServerClient = factory.create(usernamePasswordAuthenticationInfo);
			}
		} catch (ChannelConnectionException e1) {
			e1.printStackTrace();
		} catch (ServerException e) {
			e.printStackTrace();
		} catch (UserException e) {
			e.printStackTrace();
		} catch (ServiceException e) {
			e.printStackTrace();
		} catch (BimServerClientException e) {
			e.printStackTrace();
		}
	}

	@AfterClass
	public static void shutdown() {
		bimServerClient.disconnect();
	}

	private SProject createProject() throws PublicInterfaceNotFoundException {
		try {
			SProject project = bimServerClient.getServiceInterface().addProject("Project " + new Random().nextInt(), "ifc4");
			return project;
		} catch (ServiceException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Test
	public void test() {
		try {
			IfcModelInterface model = bimServerClient.newModel(createProject(), false);
			new CreateFromScratch().createIfcProject(model);
			model.commit("tralala");
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
}