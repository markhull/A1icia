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
package com.hulles.a1icia.golf;

import com.hulles.a1icia.api.A1iciaConstants;
import com.hulles.a1icia.api.shared.A1iciaException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.hulles.a1icia.api.shared.SerialSememe;
import com.hulles.a1icia.api.shared.SharedUtils;
import com.hulles.a1icia.api.tools.A1iciaUtils;
import com.hulles.a1icia.room.Room;
import com.hulles.a1icia.room.UrRoom;
import com.hulles.a1icia.room.document.RoomAnnouncement;
import com.hulles.a1icia.room.document.RoomRequest;
import com.hulles.a1icia.room.document.RoomResponse;
import com.hulles.a1icia.ticket.ActionPackage;
import com.hulles.a1icia.ticket.SememePackage;
import com.hulles.a1icia.tools.ExternalAperture;

/**
 * Golf Room performs WikiData queries. So far, its primary function is to look up words
 * or phrases for other analyses or to send them on to the end user.
 * 
 * @author hulles
 *
 */
public final class GolfRoom extends UrRoom {
	private final static Logger LOGGER = Logger.getLogger("A1iciaGolf.GolfRoom");
	private final static Level LOGLEVEL = A1iciaConstants.getA1iciaLogLevel();
	private static final String FOUNDPHRASE = "Here's what I found: ";
	private static final String ISA_FORMAT = "'%s' is a %s";
	private static final String ISAN_FORMAT = "'%s' is an %s";
//	private final Escaper escaper;

	public GolfRoom() {
		super();
		
//		escaper = HtmlEscapers.htmlEscaper();
	}

	@Override
	protected ActionPackage createActionPackage(SememePackage sememePkg, RoomRequest request) {

		switch (sememePkg.getName()) {
			case "define_word_or_phrase":
			case "lookup_fact":
			case "who_is":
				return createWikiActionPackage(sememePkg, request);
			default:
				throw new A1iciaException("Received unknown sememe in " + getThisRoom());
		}
	}

	// I *think* the process should really be:
	//	do aperture searchWikiData
	//	do WikiDataParser parseSearch
	//  select (somehow) a search result
	//	do aperture getWikiDataByTitle or getWikiDataByCode, with title or code from previous step
	//  do wikiDataParser parseEntities
	//	for each entity from previous step, do wikiDataParseEntity
	//  for each claim in entity's claims from previous step, build result
	//  while building result, look up Q values and whatever the hell else there is
	private static ActionPackage createWikiActionPackage(SememePackage sememePkg, RoomRequest request) {
		String searchStr;
		String result = null;
		List<WikiDataSearchResult> searchResults = null;
		StringBuilder sb;
		String sbs;
		String escapedTarget;
		String desc;
		Character initialChar;
		boolean firstResult;
		GolfAnalysis response;
		ActionPackage pkg;
		String lookupTarget;
		
		SharedUtils.checkNotNull(sememePkg);
		SharedUtils.checkNotNull(request);
		pkg = new ActionPackage(sememePkg);
		response = new GolfAnalysis();
		lookupTarget = sememePkg.getSememeObject();
		if (lookupTarget == null || lookupTarget.isEmpty()) {
			A1iciaUtils.error("GolfRoom: no sememe object for sememe " + sememePkg.getName());
			return null;
		}
		try {
			escapedTarget = URLEncoder.encode(lookupTarget, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new A1iciaException("Error encoding query in GolfRoom", e);
		}
		LOGGER.log(LOGLEVEL, "Golf escaped target is [{0}]", escapedTarget);
		searchStr = ExternalAperture.searchWikiData(escapedTarget);
		if (searchStr != null) {
			LOGGER.log(LOGLEVEL, "Golf search string is [{0}]", searchStr);
			searchResults = WikiDataParser.parseSearch(searchStr);
			LOGGER.log(LOGLEVEL, "Golf search results has {0} entries", searchResults.size());
			if (!searchResults.isEmpty()) {
				firstResult = true;
				sb = new StringBuilder();
				for (WikiDataSearchResult sr : searchResults) {
					if (firstResult) {
						sb.append(FOUNDPHRASE);
						firstResult = false;
					} else {
						sb.append(";\n ");
					}
					desc = sr.getDescription().trim();
					initialChar = desc.charAt(0);
					if (A1iciaUtils.isVowel(initialChar)) {
						sbs = String.format(ISAN_FORMAT, sr.getLabel(), desc);
					} else {
						sbs = String.format(ISA_FORMAT, sr.getLabel(), desc);
					}
					sb.append(sbs);
				}
				result = sb.toString();
			}
		}
		response.setMessage(result);
		response.setExplanation("This information is from WikiData via Golf.");
		response.setSearchResults(searchResults);
		pkg.setActionObject(response);
		return pkg;
	}
	
	@Override
	public Room getThisRoom() {

		return Room.GOLF;
	}

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

	@Override
	protected Set<SerialSememe> loadSememes() {
		Set<SerialSememe> sememes;
		
		sememes = new HashSet<>();
		sememes.add(SerialSememe.find("define_word_or_phrase"));
		sememes.add(SerialSememe.find("lookup_fact"));
		sememes.add(SerialSememe.find("who_is"));
		return sememes;
	}

	@Override
	protected void processRoomAnnouncement(RoomAnnouncement announcement) {
	}

}
