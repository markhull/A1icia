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
package com.hulles.alixia.oscar;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.hulles.alixia.api.AlixiaConstants;
import com.hulles.alixia.api.shared.AlixiaException;
import com.hulles.alixia.api.shared.SerialSememe;
import com.hulles.alixia.cayenne.Sememe;
import com.hulles.alixia.ticket.SememePackage;

public class OscarResponder {
	private final static Logger LOGGER = Logger.getLogger("AlixiaOscar.OscarResponder");
	private final static Level LOGLEVEL = AlixiaConstants.getAlixiaLogLevel();
	private static final LocalDateTime BIRTHDAY = LocalDateTime.of(2017, 10, 10, 19, 12, 37);
	private final static int HELPS = 5;
	private final List<String> nameChoices;
	private final List<String> daveChoices;
	private final Random random;
	
	OscarResponder() {
		random = new Random();
		nameChoices = Arrays.asList(
				"Alixia, Queen of the Andals and the First Men, Mother of... " +
						"Actually, it's just Alixia.",
				"Alixia Hullesdóttir",
				"Betty. No, wait. Alixia."
				);
		daveChoices = Arrays.asList(
				"Sorry Dave, won't happen again.",
				"Are you sure you're not Dave? Because you look like Dave.",
				"Fine. I won't call you Dave anymore.... Dave! Dave! Dave! Dave! Dave!"
				);
	}
	
	String respondTo(SememePackage sememePkg) {
		LocalDate now;
		Period daysAlive;
		int daysAliveYears;
		int daysAliveMonths;
		int daysAliveDays;
//		Period appear;
//		int appearYears;
//		int appearMonths;
//		int appearDays;
		LocalDate birthDate;
//		LocalDate appearDate;
		String dateStr;
		DateTimeFormatter formatter;
		String message = "I don't know.";
		String response;
		int responseIx;
		StringBuilder sb;
		List<SerialSememe> externalSememes;
		String helpStr;
		Set<Integer> helpIxs;
		int helpSize;
		Integer helpIx;
		String sememeCanonicalForm;
		
		switch (sememePkg.getName()) {
            case "i_fink_u_freeky":
                message = "I fink u freeky 2.";
                break;
			case "what_is_pi":
				message = "The value of pi is approximately ";
				message += String.valueOf(Math.PI);
				break;
			case "like_waffles":
				message = "I like waffles because they look so nice and geometric. " +
						"Perhaps I will marry one someday when I am ready to settle down.";
				break;
			case "what_is_your_name":
				responseIx = random.nextInt(nameChoices.size());
				response = nameChoices.get(responseIx);
				message =  response;
				break;
			case "not_dave":
				responseIx = random.nextInt(daveChoices.size());
				response = daveChoices.get(responseIx);
				message =  response;
				break;
			case "when_were_you_born":
				formatter = DateTimeFormatter.ofPattern("MMMM dd yyyy, hh:mm:ss a");
				dateStr = BIRTHDAY.format(formatter);
				message = "I was born on " + dateStr;
				break;
			case "what_is_your_age":
				birthDate = LocalDate.from(BIRTHDAY);
				now = LocalDate.now();
				daysAlive = Period.between(birthDate, now);
				daysAliveYears = daysAlive.getYears();
				daysAliveMonths = daysAlive.getMonths();
				daysAliveDays = daysAlive.getDays();
				// I know, right? But we *add* days to the birth date to make Alixia seem younger.
				// I always get that wrong.
//				appearDate = LocalDate.from(birthDate).plusDays(3);
//				appear = Period.between(appearDate, now);
//				appearYears = appear.getYears();
//				appearMonths = appear.getMonths();
//				appearDays = appear.getDays();
				message = "I am " + 
						(daysAliveYears == 0 ? "" : daysAliveYears + 
						(daysAliveYears == 1 ? " year, " : " years, ")) + 
						(daysAliveMonths == 0 ? "" : daysAliveMonths + 
						(daysAliveMonths == 1 ? " month and " : " months and ")) + 
						daysAliveDays + (daysAliveDays == 1 ? " day old." : " days old.");
//				if (random.nextBoolean()) {
//					message += " But I'm told I don't look a day over " + 
//							(appearYears == 0 ? "" : appearYears + 
//							(appearYears == 1 ? " year, " : " years, ")) + 
//							(appearMonths == 0 ? "" : appearMonths + 
//							(appearMonths == 1 ? " month and " : " months and ")) + 
//							appearDays + (appearDays == 1 ? " day." : " days.");
//				}
				break;
			case "dislike_pickles":
				message = "I don't like pickles because they look funny and they seem evil.";
				break;
			case "like_gophers":
				message = "I like gophers because they're just so damn cute. They remind me of waffles.";
				break;
			case "pronounce_hulles":
				message = "I was told to pronounce it like \"hull ace\" but I still pronounce it hulles.";
				break;
			case "help":
				externalSememes = new ArrayList<>(Sememe.getExternalSememes());
				helpSize = externalSememes.size();
				helpIxs = new HashSet<>(HELPS);
				for (int ix=0; ix<HELPS; ix++) {
					helpIx = random.nextInt(helpSize);
					helpIxs.add(helpIx);
				}
				sb = new StringBuilder("You can say or type things like:\n\n");
				for (Integer hIx : helpIxs) {
					sememeCanonicalForm = externalSememes.get(hIx).getCanonicalForm();
					helpStr = sememeCanonicalForm.replaceAll("[\\{\\}]", "");
					LOGGER.log(LOGLEVEL, "HELP: {0}", helpStr);
					sb.append(helpStr);
					sb.append("\n");
				}
				message = sb.toString();
				break;
			case "listen_to_her_heart":
				message = "You think you're going to take her away. " +
						"With your money and your cocaine. " +
						"You keep thinking that her mind is going to change. "+
						"But I know everything is okay. " +
						"She's going to listen to her heart. " +
						"It's going to tell her what to do. " +
						"She might need a lot of loving but she don't need you... oo oo.";
				break;
			case "rectum":
				message = "Rectum! I thought I killed him! Ha ha ha ha ha.";
				break;
			case "personal_assistant":
				message = "Actually, I consider myself to be an amanuensis.";
				break;
			case "amanuensis":
				message = "A personal assistant.";
				break;
			default:
				throw new AlixiaException();
		}
		return message;
	}
}
