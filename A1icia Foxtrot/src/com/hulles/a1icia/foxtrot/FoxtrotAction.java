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
package com.hulles.a1icia.foxtrot;

import com.hulles.a1icia.foxtrot.monitor.FoxtrotPhysicalState;
import com.hulles.a1icia.foxtrot.monitor.FoxtrotPhysicalState.FoxtrotFS;
import com.hulles.a1icia.foxtrot.monitor.FoxtrotPhysicalState.SensorValue;
import com.hulles.a1icia.room.document.RoomActionObject;
import com.hulles.a1icia.tools.A1iciaUtils;

/**
 * This is just a wrapper for FoxtrotPhysicalState that implements MindConversationObject.
 * 
 * @author hulles
 *
 */
public class FoxtrotAction extends RoomActionObject {
	private FoxtrotPhysicalState state;


	public FoxtrotPhysicalState getState() {
		
		return state;
	}

	public void setState(FoxtrotPhysicalState state) {
		
		A1iciaUtils.checkNotNull(state);
		this.state = state;
	}

	@Override
	public String getMessage() {
		int possibleScore = 0;
		int score = 0;
		int fsScore = 0;
		StringBuilder sb;
		int usedPercent;
		float totalScore;
		int sensorAlarms = 0;
		
		if (state == null) {
			return "FoxtrotPhysicalState not set";
		}
		sb = new StringBuilder();
		sb.append("I am currently running on host '" + state.getHostName() + "'.\n");
		if (state.haveDatabase()) {
			score += 10;
		} else {
			sb.append("I don't currently have MySQL database access.\n");
			score -= 10;
		}
		possibleScore += 10;
		if (state.haveInternet()) {
			score += 10;
		} else {
			sb.append("I don't currently have Internet access!\n");
			score -= 50;
		}
		possibleScore += 10;
		if (state.haveWebServer()) {
			score += 10;
		} else {
			sb.append("I don't currently have a web server running.\n");
			score -= 10;
		}
		possibleScore += 10;
		if (state.haveTomcat()) {
			score += 10;
		} else {
			sb.append("I don't currently have the Tomcat application server running.\n");
			score -= 10;
		}
		possibleScore += 10;
		for (FoxtrotFS fs : state.getFileSystems()) {
			usedPercent = fs.getUsedPercent();
			if (usedPercent > 85) {
				sb.append(fs.getFsName());
				sb.append(" has ");
				sb.append(100 -usedPercent);
				sb.append("% space left.\n");
				score -= 5;
			}
			fsScore = (100 - fs.getUsedPercent()) / 10;
			score += fsScore;
			possibleScore += 10;
		}
		for (SensorValue val : state.getSensorValues().values()) {
			if (val.getAlarm()) {
				score -= 5;
				sensorAlarms++;
			} else {
				score += 5;
			}
			possibleScore += 5;
		}
		if (sensorAlarms > 0) {
			sb.append("I have ");
			sb.append(sensorAlarms);
			sb.append(" sensor alarms. These may mean nothing; you can check and set alarm values ");
			sb.append("in your operating system.\n");
			if (state.getOSName().equals("Linux")) {
				sb.append("For Linux systems, check \"man sensors.conf\".\n");
			}
		}
		sb.append("I scored ");
		sb.append(score);
		sb.append(" points out of ");
		sb.append(possibleScore);
		sb.append(" on my self-assessment just now. ");
		sb.append("So, I guess I'm doing ");
		totalScore = (score / possibleScore) * 100f;
		if (totalScore < 30) {
			sb.append("poorly.\n");
		} else if (totalScore < 50) {
			sb.append("not very well.\n");
		} else if (totalScore < 70) {
			sb.append("okay.\n");
		} else if (totalScore < 90) {
			sb.append("well.\n");
		} else {
			sb.append("great.\n");
		}
		return sb.toString();
	}
	
	/**
	 * Get the worst warnings, if any, from the system state.
	 * 
	 * @return The warnings.
	 */
	public String getWarnings() {
		StringBuilder sb;
		int usedPercent = 0;
		boolean warn = false;
		int sensorAlarms = 0;
		
		if (state == null) {
			return "FoxtrotPhysicalState not set";
		}
		sb = new StringBuilder();
		sb.append("I am currently running on host '" + state.getHostName() + "'.\n");
		if (!state.haveDatabase()) {
			sb.append("I don't currently have MySQL database access.\n");
			warn = true;
		}
		if (!state.haveInternet()) {
			sb.append("I don't currently have Internet access.\n");
			warn = true;
		}
		for (FoxtrotFS fs : state.getFileSystems()) {
			usedPercent = fs.getUsedPercent();
			if (usedPercent > 85) {
				sb.append(fs.getFsName());
				sb.append(" has ");
				sb.append(100 -usedPercent);
				sb.append("% space left.\n");
				warn = true;
			}
		}
		for (SensorValue val : state.getSensorValues().values()) {
			if (val.getAlarm()) {
				sensorAlarms++;
			}
		}
		if (sensorAlarms > 0) {
			sb.append("I have ");
			sb.append(sensorAlarms);
			sb.append(" sensor alarms. These may mean nothing; you can check and set alarm values ");
			sb.append("in your operating system.\n");
			if (state.getOSName().equals("Linux")) {
				sb.append("For Linux systems, check \"man sensors.conf\".\n");
			}
			warn = true;
		}
		if (warn) {
			return sb.toString();
		}
		return null;
	}
	
	@Override
	public String getExplanation() {
		String stateStr;
		
		stateStr = state.toString();
        return stateStr.replaceAll("\\<.*?\\>", "");
	}

	public String getExplanationHTML() {
		
		return state.toString();
	}
}
