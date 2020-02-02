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
package com.hulles.alixia.juliet;

import com.hulles.alixia.api.shared.AlixiaException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.hulles.alixia.api.shared.SerialSememe;
import com.hulles.alixia.api.shared.SharedUtils;
import com.hulles.alixia.api.tools.AlixiaUtils;
import com.hulles.alixia.room.Room;
import com.hulles.alixia.room.UrRoom;
import com.hulles.alixia.room.document.RoomAnnouncement;
import com.hulles.alixia.room.document.RoomRequest;
import com.hulles.alixia.room.document.RoomResponse;
import com.hulles.alixia.ticket.ActionPackage;
import com.hulles.alixia.ticket.SememePackage;

/**
 * Juliet Room works with the nfL6 dataset, a dataset of non-factoid questions and answers (a *lot* of
 * them) culled from Yahoo. TODO Remember where I got this. 
 * 
 * @author hulles
 *
 */
public final class JulietRoom extends UrRoom {
//	private final static Logger logger = Logger.getLogger("AlixiaJuliet.JulietRoom");
//	private final static Level LOGLEVEL = Level.INFO;
	JulietResponder responder;

	public JulietRoom() {
		super();
	}

	@Override
	public Room getThisRoom() {

		return Room.JULIET;
	}

	@Override
	public void processRoomResponses(RoomRequest request, List<RoomResponse> responses) {
		throw new AlixiaException("Response not implemented in " + 
				getThisRoom().getDisplayName());
	}

	@Override
	protected void roomStartup() {
//		Thread loader;
//		
//		loader = new Thread() {
//			@Override
//			public void run() {
				responder = new JulietResponder();
//			}
//		};
//		loader.start();
	}

	@Override
	protected void roomShutdown() {
		
	}
	
	@Override
	protected ActionPackage createActionPackage(SememePackage sememePkg, RoomRequest request) {

		switch (sememePkg.getName()) {
			case "what_about":
				return createWhatAboutActionPackage(sememePkg, request);
			default:
				throw new AlixiaException("Received unknown sememe in " + getThisRoom());
		}
	}

	private ActionPackage createWhatAboutActionPackage(SememePackage sememePkg, RoomRequest request) {
		String result = null;
		String clientMsg;
		List<ScratchNfl6Question> questions;
		JulietAnalysis response;
		ScratchNfl6Question question;
		ActionPackage pkg;
		
		SharedUtils.checkNotNull(sememePkg);
		SharedUtils.checkNotNull(request);
		pkg = new ActionPackage(sememePkg);
		response = new JulietAnalysis();
		clientMsg = request.getMessage().trim();
		if (!clientMsg.isEmpty()) {
			responder.getFWMatchingQuestions(clientMsg, 5, response);
			questions = response.getFWQuestionList();
			if (!questions.isEmpty()) {
				question = questions.get(0);
				result = "Closest FW matching question = '" + question.getBestAnswer() + 
						"' with a score of " + question.getScore() + "; ET = "  +
						AlixiaUtils.formatElapsedMillis(response.getFWElapsed());
			}
			responder.getFMMatchingQuestions(clientMsg, 5, response);
			questions = response.getFMQuestionList();
			if (!questions.isEmpty()) {
				question = questions.get(0);
				result += "<br />\nClosest FM matching question = '" + question.getBestAnswer() + 
						"' with a score of " + question.getScore() + "; ET = "  +
						AlixiaUtils.formatElapsedMillis(response.getFMElapsed());
			}
		}
		response.setMessage(result);
		pkg.setActionObject(response);
		return pkg;
	}
	
	@Override
	protected Set<SerialSememe> loadSememes() {
		Set<SerialSememe> sememes;
		
		sememes = new HashSet<>();
		sememes.add(SerialSememe.find("what_about"));
		return sememes;
	}

	@Override
	protected void processRoomAnnouncement(RoomAnnouncement announcement) {
	}

}
