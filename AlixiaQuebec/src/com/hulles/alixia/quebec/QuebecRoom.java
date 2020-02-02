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
package com.hulles.alixia.quebec;

import com.hulles.alixia.api.shared.AlixiaException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.hulles.alixia.api.shared.SerialSememe;
import com.hulles.alixia.api.shared.SharedUtils;
import com.hulles.alixia.room.Room;
import com.hulles.alixia.room.UrRoom;
import com.hulles.alixia.room.document.RoomAnnouncement;
import com.hulles.alixia.room.document.RoomRequest;
import com.hulles.alixia.room.document.RoomResponse;
import com.hulles.alixia.room.document.SememeAnalysis;
import com.hulles.alixia.room.document.SentenceMeaningAnalysis;
import com.hulles.alixia.room.document.SentenceMeaningAnalysis.SentenceMeaningQuery;
import com.hulles.alixia.room.document.SentenceMeaningRequest;
import com.hulles.alixia.ticket.ActionPackage;
import com.hulles.alixia.ticket.SememePackage;

/**
 * Quebec Room manages semantic extraction and analysis for Alixia. Vive le Quebec!
 * Mais il effectue la tâche en anglais, pas en français.   
 * One Quebec analysis task looks at an input sentence and tries to determine if it 
 * meets certain criteria. For example, AlixiaBravo asks it if a sentence is a request 
 * to classify an image.
 *  <p>
 * This will eventually include some Prolog code (Prolog is a <b>very</b> good tool for this 
 * sort of thing) and possibly some trendy deep-learning gizmo. :O
 * 
 * @author hulles
 *
 */
public final class QuebecRoom extends UrRoom {
	
	public QuebecRoom() {
		super();
	}

	@Override
	protected ActionPackage createActionPackage(SememePackage sememePkg, RoomRequest request) {

		switch (sememePkg.getName()) {
			case "sememe_analysis":
				return createAnalysisActionPackage(sememePkg, request);
			case "sentence_means":
				return createSentenceMeaningActionPackage(sememePkg, request);
			default:
				throw new AlixiaException("Received unknown sememe in " + getThisRoom());
		}
	}
	
	/**
	 * Here we carefully consider each sentence package in the ticket journal and determine
	 * the best sememe package for the essence and nuances of the... just kidding. 
	 * Actually we stick "aardvark" in as the best sememe package for each sentence.
	 * Because we can.
	 * 
	 * @param sememePkg
	 * @param request
	 * @return A SememeAnalysis package
	 */
	private static ActionPackage createAnalysisActionPackage(SememePackage sememePkg, RoomRequest request) {
		ActionPackage actionPkg;
		SememeAnalysis analysis;
//		Ticket ticket;
//		TicketJournal journal;
		List<SememePackage> sememePackages;
//		List<SentencePackage> sentencePackages;
//		SememePackage aardPkg;
		
		SharedUtils.checkNotNull(sememePkg);
		SharedUtils.checkNotNull(request);
//		ticket = request.getTicket();
//		journal = ticket.getJournal();
		actionPkg = new ActionPackage(sememePkg);
		sememePackages = new ArrayList<>();
//		sentencePackages = journal.getSentencePackages();
		analysis = new SememeAnalysis();
//		for (SentencePackage sentencePackage : sentencePackages) {
//			aardPkg = SememePackage.getDefaultPackage("aardvark");
//			aardPkg.setSentencePackage(sentencePackage);
//			aardPkg.setConfidence(5); // hey, it *might* be the best one
//			if (!aardPkg.isValid()) {
//				throw new AlixiaException("QuebecRoom: created invalid sememe package");
//			}
//			sememePackages.add(aardPkg);
//		}
		analysis.setSememePackages(sememePackages);
		actionPkg.setActionObject(analysis);
		return actionPkg;
	}

	private static ActionPackage createSentenceMeaningActionPackage(SememePackage sememePkg, RoomRequest request) {
		ActionPackage pkg;
		SentenceMeaningRequest meaningRequest;
		SentenceMeaningAnalysis  meaningResult;
		SentenceMeaningQuery query;
		
		SharedUtils.checkNotNull(sememePkg);
		SharedUtils.checkNotNull(request);
		pkg = new ActionPackage(sememePkg);
		meaningRequest = (SentenceMeaningRequest) request.getRoomObject();
		query = meaningRequest.getSentenceMeaningQuery();
		meaningResult = new SentenceMeaningAnalysis(query);
		meaningResult.setConfidence(50);
		meaningResult.setResult(false);
		pkg.setActionObject(meaningResult);
		return pkg;
	}
	
	@Override
	public Room getThisRoom() {

		return Room.QUEBEC;
	}

	@Override
	public void processRoomResponses(RoomRequest request, List<RoomResponse> responses) {
		throw new AlixiaException("Response not implemented in " + 
				getThisRoom().getDisplayName());
	}

	@Override
	protected void roomStartup() {
	}

	@Override
	protected void roomShutdown() {
	}

	@Override
	protected Set<SerialSememe> loadSememes() {
		Set<SerialSememe> sememes;
		
		sememes = new HashSet<>();
		sememes.add(SerialSememe.find("sememe_analysis"));
		sememes.add(SerialSememe.find("sentence_means"));
		return sememes;
	}

	@Override
	protected void processRoomAnnouncement(RoomAnnouncement announcement) {
	}

}
