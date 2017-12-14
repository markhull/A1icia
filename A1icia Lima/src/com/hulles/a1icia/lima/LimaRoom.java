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
package com.hulles.a1icia.lima;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.eventbus.EventBus;
import com.hulles.a1icia.base.A1iciaException;
import com.hulles.a1icia.cayenne.AnswerHistory;
import com.hulles.a1icia.cayenne.Spark;
import com.hulles.a1icia.room.Room;
import com.hulles.a1icia.room.UrRoom;
import com.hulles.a1icia.room.document.HistoryUpdate;
import com.hulles.a1icia.room.document.MessageAction;
import com.hulles.a1icia.room.document.RoomAnnouncement;
import com.hulles.a1icia.room.document.RoomRequest;
import com.hulles.a1icia.room.document.RoomResponse;
import com.hulles.a1icia.room.document.SparkAnalysis;
import com.hulles.a1icia.ticket.ActionPackage;
import com.hulles.a1icia.ticket.SentencePackage;
import com.hulles.a1icia.ticket.SparkObjectType;
import com.hulles.a1icia.ticket.SparkPackage;
import com.hulles.a1icia.ticket.Ticket;
import com.hulles.a1icia.ticket.TicketJournal;
import com.hulles.a1icia.tools.A1iciaUtils;

/**
 * Lima Room plucks the low-hanging fruit and tries to match the current query to previous query
 * history. She expects the request she is sent to already be lemmatized. After the client 
 * request is finally processed she updates the database with the fulfilled questions.
 * <p>
 * N.B. Back when rooms used to be "lobes", Lima Room was named "Lisa" instead of "Lima", because I
 * really really wanted a module named "Lisa Loeb". Sigh. But that's why Lima
 * is referred to as "she", above. So now I just imagine the room is named after MPB singer Marina
 * Lima. Sigh.
 *  
 * @author hulles
 *
 */
public final class LimaRoom extends UrRoom {
	private final static Logger LOGGER = Logger.getLogger("A1iciaLima.LimaRoom");
	private final static Level LOGLEVEL = Level.FINE;
	private final static int KEEPERSCORE = 95;
	private final LimaHistory limaHistory;

	public LimaRoom(EventBus bus) {
		super(bus);
		
		limaHistory = new LimaHistory();
	}

	private ActionPackage createAnalysisActionPackage(SparkPackage sparkPkg, RoomRequest request) {
		List<ScratchAnswerHistory> historyList;
		ActionPackage pkg;
		Ticket ticket;
		TicketJournal journal;
		ScratchAnswerHistory history;
		List<SentencePackage> sentencePackages;
		AnswerHistory answerHistory;
		List<SparkPackage> sparkPackages;
		SparkAnalysis sparkAnalysis;
		Spark sparkResult;
		SparkObjectType sparkObjectType;
		SparkPackage sparkPackage;
		String sparkObject;
		
		A1iciaUtils.checkNotNull(sparkPkg);
		A1iciaUtils.checkNotNull(request);
		LOGGER.log(LOGLEVEL, "LimaRoom: in createAnalysisActionPackage");
		ticket = request.getTicket();
		journal = ticket.getJournal();
		pkg = new ActionPackage(sparkPkg);
		sparkPackages = new ArrayList<>();
		sentencePackages = journal.getSentencePackages();
		sparkAnalysis = new SparkAnalysis();
		for (SentencePackage sentencePackage : sentencePackages) {
			
			historyList = limaHistory.getMatchingHistory(sentencePackage);
			LOGGER.log(LOGLEVEL, "LimaRoom: got " + historyList.size() + " results from LimaHistory");
			
			if (!historyList.isEmpty()) {
				// for now we just look at the first one (they've been sorted best to worst)
				history = historyList.get(0);
				if (history.getScore() >= KEEPERSCORE) {
					LOGGER.log(LOGLEVEL, "LimaRoom: have keeper");
					answerHistory = history.getHistory();
					sparkResult = answerHistory.getSpark();
					if (sparkResult != null) {
						sparkObject= answerHistory.getSparkObject();
						sparkObjectType = answerHistory.getSparkObjectType();
						sparkPackage = SparkPackage.getNewPackage();
						sparkPackage.setSpark(sparkResult);
						sparkPackage.setSparkObject(sparkObject);
						sparkPackage.setSparkObjectType(sparkObjectType);
						sparkPackage.setSentencePackage(sentencePackage);
						sparkPackage.setConfidence(history.getScore());
						if (!sparkPackage.isValid()) {
							throw new A1iciaException("LimaRoom: created invalid spark package");
						}
						sparkPackages.add(sparkPackage);
					}
				}
			}
		}
		sparkAnalysis.setSparkPackages(sparkPackages);
		pkg.setActionObject(sparkAnalysis);
		LOGGER.log(LOGLEVEL, "LimaRoom: returning " + sparkPackages.size() + " spark packages");
		return pkg;
	}

	private ActionPackage createUpdateActionPackage(SparkPackage sparkPkg, RoomRequest request) {
		HistoryUpdate historyUpdate;
		MessageAction updateResponse;
		ActionPackage pkg;
		
		A1iciaUtils.checkNotNull(sparkPkg);
		A1iciaUtils.checkNotNull(request);
		LOGGER.log(LOGLEVEL, "Lima Room: in createUpdatePackage");
		historyUpdate = (HistoryUpdate) request.getRoomObject();
		limaHistory.addHistory(historyUpdate);
		updateResponse = new MessageAction();
		updateResponse.setMessage("Mischief managed");
		pkg = new ActionPackage(sparkPkg);
		pkg.setActionObject(updateResponse);
		return pkg;
	}	

	@Override
	public Room getThisRoom() {

		return Room.LIMA;
	}

	@Override
	public void processRoomResponses(RoomRequest request, List<RoomResponse> responses) {
		throw new A1iciaException("Response not implemented in " + 
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
	protected ActionPackage createActionPackage(SparkPackage sparkPkg, RoomRequest request) {

		switch (sparkPkg.getName()) {
			case "spark_analysis":
				return createAnalysisActionPackage(sparkPkg, request);
			case "update_history":
				return createUpdateActionPackage(sparkPkg, request);
			default:
				throw new A1iciaException("Received unknown spark in " + getThisRoom());
		}
	}

	@Override
	protected Set<Spark> loadSparks() {
		Set<Spark> sparks;
		
		sparks = new HashSet<>();
		sparks.add(Spark.find("spark_analysis"));
		sparks.add(Spark.find("update_history"));
		return sparks;
	}

	@Override
	protected void processRoomAnnouncement(RoomAnnouncement announcement) {
	}

}
