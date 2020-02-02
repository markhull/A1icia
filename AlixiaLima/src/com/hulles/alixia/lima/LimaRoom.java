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
package com.hulles.alixia.lima;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hulles.alixia.api.shared.AlixiaException;
import com.hulles.alixia.api.shared.SerialSememe;
import com.hulles.alixia.api.shared.SharedUtils;
import com.hulles.alixia.cayenne.AnswerHistory;
import com.hulles.alixia.room.Room;
import com.hulles.alixia.room.UrRoom;
import com.hulles.alixia.room.document.HistoryUpdate;
import com.hulles.alixia.room.document.MessageAction;
import com.hulles.alixia.room.document.RoomAnnouncement;
import com.hulles.alixia.room.document.RoomRequest;
import com.hulles.alixia.room.document.RoomResponse;
import com.hulles.alixia.room.document.SememeAnalysis;
import com.hulles.alixia.ticket.ActionPackage;
import com.hulles.alixia.ticket.SememePackage;
import com.hulles.alixia.ticket.SentencePackage;
import com.hulles.alixia.ticket.Ticket;
import com.hulles.alixia.ticket.TicketJournal;

/**
 * Lima Room plucks the low-hanging fruit and tries to match the current query to previous query
 * history. She expects the request she is sent to already be lemmatized. After the client 
 * request is finally processed she updates the database with the fulfilled questions.
 * <p>
 * N.B. Back when rooms used to be "lobes", Lima Room was named "Lisa" instead of "Lima", because I
 * really really wanted a module named "Lisa Lobe". Sigh. But that's why Lima
 * is referred to as "she", above. So now I just imagine the room is named after MPB singer Marina
 * Lima. Sigh.
 *  
 * @author hulles
 *
 */
public final class LimaRoom extends UrRoom {
	private final static Logger LOGGER = LoggerFactory.getLogger(LimaRoom.class);
	private final static int KEEPERSCORE = 95;
	private final LimaHistory limaHistory;

	public LimaRoom() {
		super();
		
		limaHistory = new LimaHistory();
	}

	private ActionPackage createAnalysisActionPackage(SememePackage sememePkg, RoomRequest request) {
		List<ScratchAnswerHistory> historyList;
		ActionPackage pkg;
		Ticket ticket;
		TicketJournal journal;
		ScratchAnswerHistory history;
		List<SentencePackage> sentencePackages;
		AnswerHistory answerHistory;
		List<SememePackage> sememePackages;
		SememeAnalysis sememeAnalysis;
		SerialSememe sememeResult;
		SememePackage sememePackage;
		String sememeObject;
		
		SharedUtils.checkNotNull(sememePkg);
		SharedUtils.checkNotNull(request);
		LOGGER.debug("LimaRoom: in createAnalysisActionPackage");
		ticket = request.getTicket();
		journal = ticket.getJournal();
		pkg = new ActionPackage(sememePkg);
		sememePackages = new ArrayList<>();
		sentencePackages = journal.getSentencePackages();
		sememeAnalysis = new SememeAnalysis();
		for (SentencePackage sentencePackage : sentencePackages) {
			
			historyList = limaHistory.getMatchingHistory(sentencePackage);
			LOGGER.debug("LimaRoom: got {} results from LimaHistory", historyList.size());
			
			if (!historyList.isEmpty()) {
				// for now we just look at the first one (they've been sorted best to worst)
				history = historyList.get(0);
				if (history.getScore() >= KEEPERSCORE) {
					LOGGER.debug("LimaRoom: have keeper");
					answerHistory = history.getHistory();
					sememeResult = answerHistory.getSememe().toSerial();
					if (sememeResult != null) {
						sememeObject= answerHistory.getSememeObject();
						sememePackage = SememePackage.getNewPackage();
						sememePackage.setSememe(sememeResult);
						sememePackage.setSememeObject(sememeObject);
						sememePackage.setSentencePackage(sentencePackage);
						sememePackage.setConfidence(history.getScore());
						if (!sememePackage.isValid()) {
							throw new AlixiaException("LimaRoom: created invalid sememe package");
						}
						sememePackages.add(sememePackage);
					}
				}
			}
		}
		sememeAnalysis.setSememePackages(sememePackages);
		pkg.setActionObject(sememeAnalysis);
		LOGGER.debug("LimaRoom: returning {} sememe packages", sememePackages.size());
		return pkg;
	}

	private ActionPackage createUpdateActionPackage(SememePackage sememePkg, RoomRequest request) {
		HistoryUpdate historyUpdate;
		MessageAction updateResponse;
		ActionPackage pkg;
		
		SharedUtils.checkNotNull(sememePkg);
		SharedUtils.checkNotNull(request);
		LOGGER.debug("Lima Room: in createUpdatePackage");
		historyUpdate = (HistoryUpdate) request.getRoomObject();
		limaHistory.addHistory(historyUpdate);
		updateResponse = new MessageAction();
		updateResponse.setMessage("Mischief managed");
		pkg = new ActionPackage(sememePkg);
		pkg.setActionObject(updateResponse);
		return pkg;
	}	

	@Override
	public Room getThisRoom() {

		return Room.LIMA;
	}

	@Override
	public void processRoomResponses(RoomRequest request, List<RoomResponse> responses) {
		throw new AlixiaException("Response not implemented in " + 
				getThisRoom().getDisplayName());
	}

	@Override
	protected void roomStartup() {
		
		limaHistory.loadHistory();
	}

	@Override
	protected void roomShutdown() {
		
	}
	
	@Override
	protected ActionPackage createActionPackage(SememePackage sememePkg, RoomRequest request) {

		switch (sememePkg.getName()) {
			case "sememe_analysis":
				return createAnalysisActionPackage(sememePkg, request);
			case "update_history":
				return createUpdateActionPackage(sememePkg, request);
			default:
				throw new AlixiaException("Received unknown sememe in " + getThisRoom());
		}
	}

	@Override
	protected Set<SerialSememe> loadSememes() {
		Set<SerialSememe> sememes;
		
		sememes = new HashSet<>();
		sememes.add(SerialSememe.find("sememe_analysis"));
		sememes.add(SerialSememe.find("update_history"));
		return sememes;
	}

	@Override
	protected void processRoomAnnouncement(RoomAnnouncement announcement) {
	}

}
