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
package com.hulles.alixia.house;

import java.util.Collections;
import java.util.Set;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.eventbus.EventBus;
import com.hulles.alixia.api.AlixiaConstants;
import com.hulles.alixia.api.dialog.DialogRequest;
import com.hulles.alixia.api.remote.AlixianID;
import com.hulles.alixia.api.remote.Station;
import com.hulles.alixia.api.shared.AlixiaException;
import com.hulles.alixia.api.shared.SerialSememe;
import com.hulles.alixia.api.shared.SerialStation;
import com.hulles.alixia.api.shared.SerialUUID;
import com.hulles.alixia.api.shared.SessionType;
import com.hulles.alixia.api.shared.SharedUtils;
import com.hulles.alixia.media.Language;

/**
 * Prompter pushes a prompt of one sort or another to an idle console. 
 * This could be / should be part of Delta Room, probably.
 * 
 * @author hulles
 *
 */
final class Prompter extends TimerTask {
	final static Logger LOGGER = Logger.getLogger("Alixia.Prompter");
	final static Level LOGLEVEL = AlixiaConstants.getAlixiaLogLevel();
//	final static Level LOGLEVEL = Level.INFO;
	private final static int MAXNAGS = 2;
	private final AlixianID alixianID;
	private int nagCounter;
	final SerialSememe promptSememe;
	private final EventBus bus;
	private final SerialUUID<SerialStation> stationID;
	private final Language language;
    private final SessionType sessionType;
	private final Boolean isQuiet;
    
	Prompter(AlixianID alixianID, SessionType sessionType, Language language, Boolean isQuiet, EventBus houseBus) {
		Station station;
		
		SharedUtils.checkNotNull(alixianID);
		SharedUtils.checkNotNull(sessionType);
		SharedUtils.checkNotNull(language);
		SharedUtils.checkNotNull(houseBus);
        LOGGER.log(LOGLEVEL, "Prompter: creating prompter for {0}", alixianID);
		station = Station.getInstance();
		this.stationID = station.getStationUUID(); // our station, not the promptee's
		this.alixianID = alixianID;
		this.nagCounter = 0;
		this.bus = houseBus;
		this.language = language;
        this.sessionType = sessionType;
        this.isQuiet = isQuiet;
		this.promptSememe = SerialSememe.find("prompt");
	}

	AlixianID getAlixianID() {
		
		return alixianID;
	}

	@Override
	public void run() {
		DialogRequest dialogRequest;
		Set<SerialSememe> sememes;
		
		if (nagCounter > MAXNAGS) {
			LOGGER.log(LOGLEVEL, "Cancelling timer");
			this.cancel();
		}
        LOGGER.log(LOGLEVEL, "Prompter: creating new prompt DialogRequest for {0}", alixianID);
		dialogRequest = new DialogRequest();
		sememes = Collections.singleton(promptSememe);
		dialogRequest.setRequestActions(sememes);
		dialogRequest.setRequestMessage(null);
		dialogRequest.setFromAlixianID(alixianID);
		dialogRequest.setStationUUID(stationID);
		dialogRequest.setLanguage(language);
        dialogRequest.setSessionType(sessionType);
        dialogRequest.setIsQuiet(isQuiet);
		dialogRequest.setToAlixianID(AlixiaConstants.getAlixiaAlixianID());
        if (!dialogRequest.isValid()) {
            throw new AlixiaException("Prompter: created invalid DialogRequest");
        }
		bus.post(dialogRequest);
		nagCounter++;
	}
}
