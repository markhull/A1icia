/*******************************************************************************
 * Copyright © 2017, 2018 Hulles Industries LLC
 * All rights reserved
 *  
 * This file is part of A1icia.
 *  
 * A1icia is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *    
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *  
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * SPDX-License-Identifer: GPL-3.0-or-later
 *******************************************************************************/
package com.hulles.a1icia.november;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.eventbus.EventBus;
import com.hulles.a1icia.api.dialog.DialogRequest;
import com.hulles.a1icia.api.object.LoginObject;
import com.hulles.a1icia.api.object.LoginResponseObject;
import com.hulles.a1icia.api.shared.SerialPerson;
import com.hulles.a1icia.api.shared.SerialSememe;
import com.hulles.a1icia.api.shared.SerialUUID;
import com.hulles.a1icia.base.A1iciaException;
import com.hulles.a1icia.cayenne.Person;
import com.hulles.a1icia.crypto.A1iciaCrypto;
import com.hulles.a1icia.house.ClientDialogRequest;
import com.hulles.a1icia.room.Room;
import com.hulles.a1icia.room.UrRoom;
import com.hulles.a1icia.room.document.ClientObjectWrapper;
import com.hulles.a1icia.room.document.LogInOut;
import com.hulles.a1icia.room.document.LogInOut.LogInLogOut;
import com.hulles.a1icia.room.document.RoomAnnouncement;
import com.hulles.a1icia.room.document.RoomDocumentType;
import com.hulles.a1icia.room.document.RoomRequest;
import com.hulles.a1icia.room.document.RoomResponse;
import com.hulles.a1icia.ticket.ActionPackage;
import com.hulles.a1icia.ticket.SememePackage;
import com.hulles.a1icia.tools.A1iciaUtils;

/**
 * November Room is concerned with user data and security. 
 * 
 * @author hulles
 *
 */
public final class NovemberRoom extends UrRoom {
	
	public NovemberRoom(EventBus bus) {
		super(bus);
		
	}
	
	@Override
	public Room getThisRoom() {

		return Room.NOVEMBER;
	}

	@Override
	public void processRoomResponses(RoomRequest request, List<RoomResponse> responses) {
		throw new A1iciaException("Response not implemented in " + 
				getThisRoom().getDisplayName());
	}

	@Override
	protected void roomStartup() {		
	}

	@Override
	protected void roomShutdown() {
	}

	@Override
	protected ActionPackage createActionPackage(SememePackage sememePkg, RoomRequest request) {

		switch (sememePkg.getName()) {
			case "login":
				return createLoginActionPackage(sememePkg, request);
			default:
				throw new A1iciaException("Received unknown sememe in " + getThisRoom());
		}
	}

	private ActionPackage createLoginActionPackage(SememePackage sememePkg, RoomRequest request) {
		ActionPackage pkg;
		ClientDialogRequest cdr;
		DialogRequest dialogRequest;
		LoginObject loginObject;
		LoginResponseObject responseObject;
		String userName;
		String password;
		Person person;
		ClientObjectWrapper clientObject;
		String message;
		
		A1iciaUtils.checkNotNull(sememePkg);
		A1iciaUtils.checkNotNull(request);
		cdr = (ClientDialogRequest) request.getRoomObject();
		dialogRequest = cdr.getDialogRequest();
		loginObject = (LoginObject) dialogRequest.getClientObject();
		pkg = new ActionPackage(sememePkg);
		responseObject = new LoginResponseObject();
		userName = loginObject.getUserName();
		password = loginObject.getPassword();
		if (userName == null || password == null) {
			// this might be valid, if the person is logging out
			responseObject.setPersonUUID(null);
			message = "You are now logged out.";
			if (dialogRequest.getPersonUUID() != null) {
				sendLogAnnouncement(dialogRequest.getPersonUUID(), LogInLogOut.LOGOUT);
			}
		} else {
			person = Person.findPerson(userName);
			if (person == null) {
				responseObject.setPersonUUID(null);
				message = "I received an invalid user name and/or password. You are now logged out.";
			} else {
				if (A1iciaCrypto.checkPassword(password, person.getPassword())) {
					responseObject.setPersonUUID(person.getUUID());
					responseObject.setUserName(userName);
					message = "You are now logged in as " + person.getUsername() + " (" +
							person.getFullNameFL() + ")";
					sendLogAnnouncement(person.getUUID(), LogInLogOut.LOGIN);
				} else {
					responseObject.setPersonUUID(null);
					message = "I received an invalid user name and/or password. You are now logged out.";
				}
			}
		}
		clientObject = new ClientObjectWrapper(responseObject);
		clientObject.setMessage(message);
		pkg.setActionObject(clientObject);
		return pkg;
	}
	
	private void sendLogAnnouncement(SerialUUID<SerialPerson> uuid, LogInLogOut which) {
		RoomAnnouncement loggedIn;
		LogInOut logInOut;
		RoomDocumentType documentType;
		
		A1iciaUtils.checkNotNull(uuid);
		A1iciaUtils.checkNotNull(which);
		switch (which) {
			case LOGIN:
				documentType= RoomDocumentType.LOGGEDIN;
				break;
			case LOGOUT:
				documentType = RoomDocumentType.LOGGEDOUT;
				break;
			default:
				throw new A1iciaException("NovemberRoom: unknown LogInLogOut enum value, "
						+ "which is weird because the name pretty much defines the choices....");
		}
		loggedIn = new RoomAnnouncement(documentType);
		loggedIn.setFromRoom(getThisRoom());
		logInOut = new LogInOut();
		logInOut.setLogInLogOut(which);
		logInOut.setPersonUUID(uuid);
		loggedIn.setRoomObject(logInOut);
		assert(loggedIn.documentIsReady());
		getHall().post(loggedIn);
	}
	
	@Override
	protected Set<SerialSememe> loadSememes() {
		Set<SerialSememe> sememes;
		
		sememes = new HashSet<>();
		sememes.add(SerialSememe.find("login"));
		return sememes;
	}

	@Override
	protected void processRoomAnnouncement(RoomAnnouncement announcement) {
	}
}
