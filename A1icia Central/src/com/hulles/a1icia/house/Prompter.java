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
package com.hulles.a1icia.house;

import java.util.Collections;
import java.util.Set;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.eventbus.EventBus;
import com.hulles.a1icia.api.A1iciaConstants;
import com.hulles.a1icia.api.dialog.DialogRequest;
import com.hulles.a1icia.api.remote.A1icianID;
import com.hulles.a1icia.api.remote.Station;
import com.hulles.a1icia.api.shared.SerialSememe;
import com.hulles.a1icia.api.shared.SerialStation;
import com.hulles.a1icia.api.shared.SerialUUID;
import com.hulles.a1icia.media.Language;
import com.hulles.a1icia.tools.A1iciaUtils;

/**
 * Prompter pushes a prompt of one sort or another to an idle console. 
 * This could be / should be part of Delta Room, probably.
 * 
 * @author hulles
 *
 */
final class Prompter extends TimerTask {
	final static Logger LOGGER = Logger.getLogger("A1icia.Prompter");
	final static Level LOGLEVEL = A1iciaConstants.getA1iciaLogLevel();
	private final static int MAXNAGS = 2;
	private final A1icianID a1icianID;
	private int nagCounter;
	final SerialSememe promptSememe;
	private final EventBus bus;
	private final SerialUUID<SerialStation> stationID;
	private final Language language;
	
	Prompter(A1icianID a1icianID, Language language, EventBus houseBus) {
		Station station;
		
		A1iciaUtils.checkNotNull(a1icianID);
		A1iciaUtils.checkNotNull(language);
		A1iciaUtils.checkNotNull(houseBus);
		station = Station.getInstance();
		this.stationID = station.getStationUUID();
		this.a1icianID = a1icianID;
		this.nagCounter = 0;
		this.bus = houseBus;
		this.language = language;
		this.promptSememe = SerialSememe.find("prompt");
	}

	A1icianID getA1icianID() {
		
		return a1icianID;
	}

	@Override
	public void run() {
		DialogRequest dialogRequest;
		Set<SerialSememe> sememes;
		
		if (nagCounter > MAXNAGS) {
			LOGGER.log(LOGLEVEL, "Cancelling timer");
			this.cancel();
		}
		dialogRequest = new DialogRequest();
		sememes = Collections.singleton(promptSememe);
		dialogRequest.setRequestActions(sememes);
		dialogRequest.setRequestMessage(null);
		dialogRequest.setFromA1icianID(a1icianID);
		dialogRequest.setStationUUID(stationID);
		dialogRequest.setLanguage(language);
		dialogRequest.setToA1icianID(A1iciaConstants.getA1iciaA1icianID());
		bus.post(dialogRequest);
		nagCounter++;
	}
}
