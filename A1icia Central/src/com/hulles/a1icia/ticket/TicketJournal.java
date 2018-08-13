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
package com.hulles.a1icia.ticket;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.hulles.a1icia.house.ClientDialogRequest;
import com.hulles.a1icia.room.document.NLPAnalysis;
import com.hulles.a1icia.tools.A1iciaUtils;

/**
 * TicketJournal is an historical record of what's happened to the ticket. It should be
 * the case that everything in here is the final version; no modifications should be permitted.
 * As an example of a use case, LIMA uses the information to update the AnswerHistory database
 * table.
 * 
 * @author hulles
 *
 */
public class TicketJournal {
	private final String ticketID;
	private ClientDialogRequest clientRequest;
	private final List<String> context;
	private final List<SentencePackage> sentencePackages;
	private NLPAnalysis nlpAnalysis;
	private final List<ActionPackage> actionPackages;
	
	public TicketJournal(String ticketID) {
		
		A1iciaUtils.checkNotNull(ticketID);
		this.ticketID = ticketID;
		context = new ArrayList<>();
		sentencePackages = new ArrayList<>();
		actionPackages = new ArrayList<>();
	}
	
	public String getTicketID() {
	
		return ticketID;
	}

	public List<String> getContext() {
		
		return ImmutableList.copyOf(context);
	}

	public void setContext(List<String> context) {
		
		A1iciaUtils.nullsOkay(context);
		this.context.clear();
		this.context.addAll(context);
	}

	public NLPAnalysis getNlpAnalysis() {
		
		return nlpAnalysis;
	}

	public void setNlpAnalysis(NLPAnalysis analysis) {
		
		A1iciaUtils.checkNotNull(analysis);
		this.nlpAnalysis = analysis;
	}

	public List<SentencePackage> getSentencePackages() {
		
		return ImmutableList.copyOf(sentencePackages);
	}

	public void setSentencePackages(List<SentencePackage> pkgs) {
		
		A1iciaUtils.checkNotNull(pkgs);
		sentencePackages.clear();
		sentencePackages.addAll(pkgs);
	}
	
	public List<ActionPackage> getActionPackages() {
		
		return ImmutableList.copyOf(actionPackages);
	}

	public void setActionPackages(List<ActionPackage> pkgs) {
		
		A1iciaUtils.checkNotNull(pkgs);
		actionPackages.clear();
		actionPackages.addAll(pkgs);
	}

	public void addActionPackage(ActionPackage pkg) {
		
		A1iciaUtils.checkNotNull(pkg);
		actionPackages.add(pkg);
	}
	
	public ClientDialogRequest getClientRequest() {
		
		return clientRequest;
	}

	public void setClientRequest(ClientDialogRequest clientRequest) {
		
		A1iciaUtils.checkNotNull(clientRequest);
		this.clientRequest = clientRequest;
	}
	
	
}
