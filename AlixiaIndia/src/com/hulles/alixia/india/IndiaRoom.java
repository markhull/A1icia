/*******************************************************************************
 * Copyright © 2017, 2018 Hulles Industries LLC
 * All rights reserved
 *  
 * This file is part of Alixia.
 *  
 * Alixia is free software: you can redistribute it and/or modify
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
package com.hulles.alixia.india;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hulles.alixia.api.shared.AlixiaException;
import com.hulles.alixia.api.shared.Response;
import com.hulles.alixia.api.shared.SerialSememe;
import com.hulles.alixia.api.shared.SharedUtils;
import com.hulles.alixia.room.Room;
import com.hulles.alixia.room.UrRoom;
import com.hulles.alixia.room.document.MessageAction;
import com.hulles.alixia.room.document.RoomAnnouncement;
import com.hulles.alixia.room.document.RoomRequest;
import com.hulles.alixia.room.document.RoomResponse;
import com.hulles.alixia.ticket.ActionPackage;
import com.hulles.alixia.ticket.SememePackage;
import com.hulles.fortune.Fortunes;
import com.hulles.fortune.SerialFortune;

/**
 * India Room provides what I call "directed random" responses, i.e. random responses from within
 * a response category. Conversationally it actually works better than you might expect. 
 * <p>
 * India Room now also looks up random quotes in the Fortune database.
 * 
 * @author hulles
 *
 */
public final class IndiaRoom extends UrRoom {
	private final static Logger LOGGER = LoggerFactory.getLogger(IndiaRoom.class);
	private final static int QUOTE_PROB = 42; // a carefully-chosen integer....
	private ResponseGenerator generator;
	private final Random random;

	public IndiaRoom() {
		super();
		
		random = new Random();
	}

	@Override
	public Room getThisRoom() {

		return Room.INDIA;
	}

	@Override
	public void processRoomResponses(RoomRequest request, List<RoomResponse> responses) {
		throw new AlixiaException("Response not implemented in " + 
				getThisRoom().getDisplayName());
	}

	@Override
	protected void roomStartup() {
		
		generator = new ResponseGenerator();
	}

	@Override
	protected void roomShutdown() {
		
	}
	
	@Override
	protected ActionPackage createActionPackage(SememePackage sememePkg, RoomRequest request) {

		SharedUtils.checkNotNull(sememePkg);
		SharedUtils.checkNotNull(request);
		LOGGER.debug("IndiaRoom: receiving {}", sememePkg.getName());
		switch (sememePkg.getName()) {
			case "greet":
			case "youre_welcome":
			case "how_are_you":
			case "exclamation":
			case "praise":
			case "are_you_still_there":
			case "nothing_to_do":
			case "thank_you":
				return createRandomActionPackage(sememePkg, request);
			case "prompt":
				if (useQuoteForPrompt()) {
					return createQuotationActionPackage(sememePkg, request);
				}
				return createRandomActionPackage(sememePkg, request);
			case "random_quotation":
				return createQuotationActionPackage(sememePkg, request);
			default:
				throw new AlixiaException("Received unknown sememe in " + getThisRoom());
		}
	}

	private boolean useQuoteForPrompt() {
		int prob;
		
		prob = random.nextInt(100);
		if (prob < QUOTE_PROB) {
			return true;
		}
		return false;
	}
	
	private ActionPackage createRandomActionPackage(SememePackage sememePkg, RoomRequest request) {
		ActionPackage pkg;
		Response response;
		@SuppressWarnings("unused")
		String name;
		MessageAction action;
		String msgIn;
		
		SharedUtils.checkNotNull(sememePkg);
		SharedUtils.checkNotNull(request);
		LOGGER.debug("IndiaRoom: evaluating {}", sememePkg.getName());
		pkg = new ActionPackage(sememePkg);
		msgIn = request.getMessage();
		if (msgIn == null) {
			name = null;
		} else {
			name = msgIn.trim();
		}
		response = generator.generateResponse(sememePkg, "Dave");
		action = new MessageAction();
		action.setMessage(response.getMessage());
        action.setExplanation(response.getExplanation());
		pkg.setActionObject(action);
		LOGGER.debug("IndiaRoom: returning package for {}", sememePkg.getName());
		return pkg;
	}

	private static ActionPackage createQuotationActionPackage(SememePackage sememePkg, RoomRequest request) {
		ActionPackage pkg;
		String message;
		MessageAction action;
		String expl;
		SerialFortune fortune;
		
		SharedUtils.checkNotNull(sememePkg);
		SharedUtils.checkNotNull(request);
		LOGGER.debug("IndiaRoom: getting random quotation");
		pkg = new ActionPackage(sememePkg);
		fortune = Fortunes.getFortune();
		expl = "\"" + fortune.getText() + "\" ‒ " + fortune.getSource();
		message = expl;
		action = new MessageAction();
		action.setMessage(message);
		action.setExplanation(expl);
		pkg.setActionObject(action);
		LOGGER.debug("IndiaRoom: returning package for {}", sememePkg.getName());
		return pkg;
	}
	
	@Override
	protected Set<SerialSememe> loadSememes() {
		Set<SerialSememe> sememes;
		
		sememes = new HashSet<>();
		sememes.add(SerialSememe.find("greet"));
		sememes.add(SerialSememe.find("prompt"));
		sememes.add(SerialSememe.find("youre_welcome"));
		sememes.add(SerialSememe.find("how_are_you"));
		sememes.add(SerialSememe.find("exclamation"));
		sememes.add(SerialSememe.find("thank_you"));
		sememes.add(SerialSememe.find("praise"));
		sememes.add(SerialSememe.find("are_you_still_there"));
		sememes.add(SerialSememe.find("nothing_to_do"));
		sememes.add(SerialSememe.find("random_quotation"));
		return sememes;
	}

	@Override
	protected void processRoomAnnouncement(RoomAnnouncement announcement) {
	}
}
