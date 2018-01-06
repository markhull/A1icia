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
package com.hulles.a1icia.alpha;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.eventbus.EventBus;
import com.hulles.a1icia.api.shared.SerialSpark;
import com.hulles.a1icia.base.A1iciaException;
import com.hulles.a1icia.room.Room;
import com.hulles.a1icia.room.UrRoom;
import com.hulles.a1icia.room.document.MessageAction;
import com.hulles.a1icia.room.document.RoomAnnouncement;
import com.hulles.a1icia.room.document.RoomRequest;
import com.hulles.a1icia.room.document.RoomResponse;
import com.hulles.a1icia.room.document.SparkAnalysis;
import com.hulles.a1icia.ticket.ActionPackage;
import com.hulles.a1icia.ticket.SentencePackage;
import com.hulles.a1icia.ticket.SparkPackage;
import com.hulles.a1icia.ticket.Ticket;
import com.hulles.a1icia.ticket.TicketJournal;
import com.hulles.a1icia.tools.A1iciaUtils;

/**
 * Alpha Room is an exemplary implementation for rooms. It simply returns a form of "Aardvark" 
 * when queried. I love Alpha.
 * 
 * @author hulles
 *
 */
public final class AlphaRoom extends UrRoom {
	
	public AlphaRoom(EventBus bus) {
		super(bus);
	}

	/**
	 * Here we create an ActionPackage from Alpha, either an analysis or an action, depending 
	 * on the spark that we receive, and return it to UrRoom.
	 * 
	 */
	@Override
	protected ActionPackage createActionPackage(SparkPackage sparkPkg, RoomRequest request) {

		switch (sparkPkg.getName()) {
			case "spark_analysis":
				// Hey, this one's easy! I'm smart like a scientist!
				return createAnalysisActionPackage(sparkPkg, request);
			case "aardvark":
				// I know this one!
				return createAardvarkActionPackage(sparkPkg, request);
			default:
				throw new A1iciaException("Received unknown spark in " + getThisRoom());
		}
	}
	
	/**
	 * Here we carefully consider each sentence package in the ticket journal and determine
	 * the best spark package for the essence and nuances of the... just kidding. 
	 * Actually we stick "aardvark" in as the best spark package for each sentence.
	 * Because we can.
	 * 
	 * @param sparkPkg
	 * @param request
	 * @return A SparkAnalysis package
	 */
	private static ActionPackage createAnalysisActionPackage(SparkPackage sparkPkg, RoomRequest request) {
		ActionPackage actionPkg;
		SparkAnalysis analysis;
		Ticket ticket;
		TicketJournal journal;
		List<SparkPackage> sparkPackages;
		List<SentencePackage> sentencePackages;
		SparkPackage aardPkg;
		
		A1iciaUtils.checkNotNull(sparkPkg);
		A1iciaUtils.checkNotNull(request);
		ticket = request.getTicket();
		journal = ticket.getJournal();
		actionPkg = new ActionPackage(sparkPkg);
		sparkPackages = new ArrayList<>();
		sentencePackages = journal.getSentencePackages();
		analysis = new SparkAnalysis();
		for (SentencePackage sentencePackage : sentencePackages) {
			aardPkg = SparkPackage.getDefaultPackage("aardvark");
			aardPkg.setSentencePackage(sentencePackage);
			aardPkg.setConfidence(5); // hey, it *might* be the best one
			if (!aardPkg.isValid()) {
				throw new A1iciaException("AlphaRoom: created invalid spark package");
			}
			sparkPackages.add(aardPkg);
		}
		analysis.setSparkPackages(sparkPackages);
		actionPkg.setActionObject(analysis);
		return actionPkg;
	}

	/**
	 * Create an action package for the "aardvark" spark, which consists of saying some form
	 * of the word "aardvark".
	 * 
	 * @param sparkPkg
	 * @param request
	 * @return
	 */
	private static ActionPackage createAardvarkActionPackage(SparkPackage sparkPkg, RoomRequest request) {
		ActionPackage pkg;
		MessageAction action;
		String clientMsg;
		String result;
		
		A1iciaUtils.checkNotNull(sparkPkg);
		A1iciaUtils.checkNotNull(request);
		pkg = new ActionPackage(sparkPkg);
		action = new MessageAction();
		clientMsg = request.getMessage().trim();
		if (clientMsg.isEmpty()) {
			result = "Aardvark???";
		} else {
			switch (clientMsg.charAt(clientMsg.length() - 1)) {
				case '?':
					result = "Aardvark!";
					break;
				case '.':
					result = "Aardvark?";
					break;
				case '!':
					result = "Aardvark! Aardvark! Aardvark!";
					break;
				default:
					result = "Aardvark...";
			}
		}
		action.setMessage(result);
		action.setExplanation("I said, \"" + result + "\"");
		pkg.setActionObject(action);
		return pkg;
	}
	
	/**
	 * Return this room name.
	 * 
	 */
	@Override
	public Room getThisRoom() {

		return Room.ALPHA;
	}

	/**
	 * We don't get responses for anything, so if there is one it's an error.
	 * 
	 */
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

	/**
	 * Advertise which sparks we handle.
	 * 
	 */
	@Override
	protected Set<SerialSpark> loadSparks() {
		Set<SerialSpark> sparks;
		
		sparks = new HashSet<>();
		sparks.add(SerialSpark.find("spark_analysis"));
		sparks.add(SerialSpark.find("aardvark"));
		return sparks;
	}

	/**
	 * We don't do anything with RoomAnnouncements but it is legitimate to receive them here,
	 * so no error.
	 * 
	 */
	@Override
	protected void processRoomAnnouncement(RoomAnnouncement announcement) {
	}

}
