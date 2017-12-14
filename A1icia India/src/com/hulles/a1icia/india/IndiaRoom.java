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
package com.hulles.a1icia.india;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.eventbus.EventBus;
import com.hulles.a1icia.base.A1iciaException;
import com.hulles.a1icia.cayenne.Spark;
import com.hulles.a1icia.room.Room;
import com.hulles.a1icia.room.UrRoom;
import com.hulles.a1icia.room.document.MessageAction;
import com.hulles.a1icia.room.document.RoomAnnouncement;
import com.hulles.a1icia.room.document.RoomRequest;
import com.hulles.a1icia.room.document.RoomResponse;
import com.hulles.a1icia.ticket.ActionPackage;
import com.hulles.a1icia.ticket.SparkPackage;
import com.hulles.a1icia.tools.A1iciaUtils;
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
	private final static Logger logger = Logger.getLogger("A1iciaIndia.IndiaRoom");
	private final static Level LOGLEVEL = Level.FINE;
	private final static int QUOTE_PROB = 42; // a carefully-chosen integer....
	private ResponseGenerator generator;
	private final Random random;

	public IndiaRoom(EventBus bus) {
		super(bus);
		
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
	protected ActionPackage createActionPackage(SparkPackage sparkPkg, RoomRequest request) {

		A1iciaUtils.checkNotNull(sparkPkg);
		A1iciaUtils.checkNotNull(request);
		logger.log(LOGLEVEL, "IndiaRoom: receiving " + sparkPkg.getName());
		switch (sparkPkg.getName()) {
			case "greet":
			case "youre_welcome":
			case "how_are_you":
			case "exclamation":
			case "praise":
			case "are_you_still_there":
			case "nothing_to_do":
			case "thank_you":
				return createRandomActionPackage(sparkPkg, request);
			case "prompt":
				if (useQuoteForPrompt()) {
					return createQuotationActionPackage(sparkPkg, request);
				}
				return createRandomActionPackage(sparkPkg, request);
			case "random_quotation":
				return createQuotationActionPackage(sparkPkg, request);
			default:
				throw new A1iciaException("Received unknown spark in " + getThisRoom());
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
	
	private ActionPackage createRandomActionPackage(SparkPackage sparkPkg, RoomRequest request) {
		ActionPackage pkg;
		String message;
		@SuppressWarnings("unused")
		String name;
		MessageAction action;
		String msgIn;
		
		A1iciaUtils.checkNotNull(sparkPkg);
		A1iciaUtils.checkNotNull(request);
		logger.log(LOGLEVEL, "IndiaRoom: evaluating " + sparkPkg.getName());
		pkg = new ActionPackage(sparkPkg);
		msgIn = request.getMessage();
		if (msgIn == null) {
			name = null;
		} else {
			name = msgIn.trim();
		}
		message = generator.generateResponse(sparkPkg, "Dave");
		action = new MessageAction();
		action.setMessage(message);
		pkg.setActionObject(action);
		logger.log(LOGLEVEL, "IndiaRoom: returning package for " + sparkPkg.getName());
		return pkg;
	}

	private static ActionPackage createQuotationActionPackage(SparkPackage sparkPkg, RoomRequest request) {
		ActionPackage pkg;
		String message;
		MessageAction action;
		String expl;
		SerialFortune fortune;
		
		A1iciaUtils.checkNotNull(sparkPkg);
		A1iciaUtils.checkNotNull(request);
		logger.log(LOGLEVEL, "IndiaRoom: getting random quotation");
		pkg = new ActionPackage(sparkPkg);
		fortune = Fortuna.getFortune();
//		message = fortune.getText(); 
		expl = "\"" + fortune.getText() + "\" ‒ " + fortune.getSource();
		message = expl;
		action = new MessageAction();
		action.setMessage(message);
		action.setExplanation(expl);
		pkg.setActionObject(action);
		logger.log(LOGLEVEL, "IndiaRoom: returning package for " + sparkPkg.getName());
		return pkg;
	}
	
	@Override
	protected Set<Spark> loadSparks() {
		Set<Spark> sparks;
		
		sparks = new HashSet<>();
		sparks.add(Spark.find("greet"));
		sparks.add(Spark.find("prompt"));
		sparks.add(Spark.find("youre_welcome"));
		sparks.add(Spark.find("how_are_you"));
		sparks.add(Spark.find("exclamation"));
		sparks.add(Spark.find("thank_you"));
		sparks.add(Spark.find("praise"));
		sparks.add(Spark.find("are_you_still_there"));
		sparks.add(Spark.find("nothing_to_do"));
		sparks.add(Spark.find("random_quotation"));
		return sparks;
	}

	@Override
	protected void processRoomAnnouncement(RoomAnnouncement announcement) {
	}


}
