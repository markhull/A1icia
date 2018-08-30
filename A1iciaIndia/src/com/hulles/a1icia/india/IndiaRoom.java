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
package com.hulles.a1icia.india;

import com.hulles.a1icia.api.A1iciaConstants;
import com.hulles.a1icia.api.shared.A1iciaException;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.hulles.a1icia.api.shared.SerialSememe;
import com.hulles.a1icia.api.shared.SharedUtils;
import com.hulles.a1icia.room.Room;
import com.hulles.a1icia.room.UrRoom;
import com.hulles.a1icia.room.document.MessageAction;
import com.hulles.a1icia.room.document.RoomAnnouncement;
import com.hulles.a1icia.room.document.RoomRequest;
import com.hulles.a1icia.room.document.RoomResponse;
import com.hulles.a1icia.ticket.ActionPackage;
import com.hulles.a1icia.ticket.SememePackage;
import com.hulles.fortuna.Fortuna;
import com.hulles.fortuna.SerialFortune;

/**
 * India Room provides what I call "directed random" responses, i.e. random responses from within
 * a response category. Conversationally it actually works better than you might expect. 
 * <p>
 * India Room now also looks up random quotes in the Fortuna database.
 * 
 * @author hulles
 *
 */
public final class IndiaRoom extends UrRoom {
	private final static Logger LOGGER = Logger.getLogger("A1iciaIndia.IndiaRoom");
	private final static Level LOGLEVEL = A1iciaConstants.getA1iciaLogLevel();
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
		throw new A1iciaException("Response not implemented in " + 
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
		LOGGER.log(LOGLEVEL, "IndiaRoom: receiving {0}", sememePkg.getName());
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
				throw new A1iciaException("Received unknown sememe in " + getThisRoom());
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
		String message;
		@SuppressWarnings("unused")
		String name;
		MessageAction action;
		String msgIn;
		
		SharedUtils.checkNotNull(sememePkg);
		SharedUtils.checkNotNull(request);
		LOGGER.log(LOGLEVEL, "IndiaRoom: evaluating {0}", sememePkg.getName());
		pkg = new ActionPackage(sememePkg);
		msgIn = request.getMessage();
		if (msgIn == null) {
			name = null;
		} else {
			name = msgIn.trim();
		}
		message = generator.generateResponse(sememePkg, "Dave");
		action = new MessageAction();
		action.setMessage(message);
		pkg.setActionObject(action);
		LOGGER.log(LOGLEVEL, "IndiaRoom: returning package for {0}", sememePkg.getName());
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
		LOGGER.log(LOGLEVEL, "IndiaRoom: getting random quotation");
		pkg = new ActionPackage(sememePkg);
		fortune = Fortuna.getFortune();
//		message = fortune.getText(); 
		expl = "\"" + fortune.getText() + "\" ‒ " + fortune.getSource();
		message = expl;
		action = new MessageAction();
		action.setMessage(message);
		action.setExplanation(expl);
		pkg.setActionObject(action);
		LOGGER.log(LOGLEVEL, "IndiaRoom: returning package for {0}", sememePkg.getName());
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
