/*******************************************************************************
 * Copyright © 2017 Hulles Industries LLC
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
 *******************************************************************************/
package com.hulles.a1icia.oscar;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.eventbus.EventBus;
import com.hulles.a1icia.api.object.ChangeLanguageObject;
import com.hulles.a1icia.api.shared.SerialSpark;
import com.hulles.a1icia.base.A1iciaException;
import com.hulles.a1icia.media.Language;
import com.hulles.a1icia.room.Room;
import com.hulles.a1icia.room.UrRoom;
import com.hulles.a1icia.room.document.ClientObjectWrapper;
import com.hulles.a1icia.room.document.MessageAction;
import com.hulles.a1icia.room.document.RoomAnnouncement;
import com.hulles.a1icia.room.document.RoomRequest;
import com.hulles.a1icia.room.document.RoomResponse;
import com.hulles.a1icia.ticket.ActionPackage;
import com.hulles.a1icia.ticket.SparkPackage;
import com.hulles.a1icia.tools.A1iciaUtils;

/**
 * Oscar Room handles what are essentially constant values, like A1icia's name. Which is A1icia.
 *
 * @author hulles
 *
 */
public final class OscarRoom extends UrRoom {
	private final OscarResponder chooser;
	
	public OscarRoom(EventBus bus) {
		super(bus);
		
		chooser = new OscarResponder();
	}
	
	@Override
	public Room getThisRoom() {

		return Room.OSCAR;
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
	protected ActionPackage createActionPackage(SparkPackage sparkPkg, RoomRequest request) {

		switch (sparkPkg.getName()) {
			case "change_language":
				return createLanguageActionPackage(sparkPkg, request);
			case "help":
			case "what_is_your_age":
			case "what_is_your_name":
			case "dislike_pickles":
			case "when_were_you_born":
			case "what_is_pi":
			case "like_waffles":
			case "listen_to_her_heart":
			case "rectum":
			case "personal_assistant":
			case "amanuensis":
			case "pronounce_hulles":
			case "like_gophers":
			case "not_dave":
				return createConstantsActionPackage(sparkPkg, request);
			default:
				throw new A1iciaException("Received unknown spark in " + getThisRoom());
		}
	}

	private ActionPackage createConstantsActionPackage(SparkPackage sparkPkg, RoomRequest request) {
		String message;
		ActionPackage pkg;
		MessageAction action;
		
		A1iciaUtils.checkNotNull(sparkPkg);
		A1iciaUtils.checkNotNull(request);
		pkg = new ActionPackage(sparkPkg);
		action = new MessageAction();
		message = chooser.respondTo(sparkPkg);
		action.setMessage(message);
		pkg.setActionObject(action);
		return pkg;
	}

	private static ActionPackage createLanguageActionPackage(SparkPackage sparkPkg, RoomRequest request) {
		ActionPackage pkg;
		Language language;
		String langStr;
		ChangeLanguageObject changeLang;
		ClientObjectWrapper clientChange;
		
		A1iciaUtils.checkNotNull(sparkPkg);
		A1iciaUtils.checkNotNull(request);
		langStr = sparkPkg.getSparkObject();
		language = Language.valueOf(langStr);
		changeLang = new ChangeLanguageObject();
		changeLang.setNewLanguage(language);
		clientChange = new ClientObjectWrapper(changeLang);
		clientChange.setMessage("Changed language to " + language.getDisplayName());
		pkg = new ActionPackage(sparkPkg);
		pkg.setActionObject(clientChange);
		return pkg;
	}
	
	@Override
	protected Set<SerialSpark> loadSparks() {
		Set<SerialSpark> sparks;
		
		sparks = new HashSet<>();
		sparks.add(SerialSpark.find("help"));
		sparks.add(SerialSpark.find("what_is_your_name"));
		sparks.add(SerialSpark.find("dislike_pickles"));
		sparks.add(SerialSpark.find("what_is_your_age"));
		sparks.add(SerialSpark.find("when_were_you_born"));
		sparks.add(SerialSpark.find("what_is_pi"));
		sparks.add(SerialSpark.find("like_waffles"));
		sparks.add(SerialSpark.find("listen_to_her_heart"));
		sparks.add(SerialSpark.find("rectum"));
		sparks.add(SerialSpark.find("personal_assistant"));
		sparks.add(SerialSpark.find("amanuensis"));
		sparks.add(SerialSpark.find("pronounce_hulles"));
		sparks.add(SerialSpark.find("change_language"));
		sparks.add(SerialSpark.find("like_gophers"));
		sparks.add(SerialSpark.find("not_dave"));
		return sparks;
	}

	@Override
	protected void processRoomAnnouncement(RoomAnnouncement announcement) {
	}

}
