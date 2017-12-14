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
package com.hulles.a1icia.overmind;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.MultimapBuilder;
import com.hulles.a1icia.api.A1iciaConstants;
import com.hulles.a1icia.api.dialog.DialogRequest;
import com.hulles.a1icia.api.object.A1iciaClientObject;
import com.hulles.a1icia.base.A1iciaException;
import com.hulles.a1icia.cayenne.Spark;
import com.hulles.a1icia.house.ClientDialogRequest;
import com.hulles.a1icia.room.Room;
import com.hulles.a1icia.room.document.RoomRequest;
import com.hulles.a1icia.room.document.SparkAnalysis;
import com.hulles.a1icia.ticket.ActionPackage;
import com.hulles.a1icia.ticket.SentencePackage;
import com.hulles.a1icia.ticket.SparkPackage;
import com.hulles.a1icia.ticket.Ticket;
import com.hulles.a1icia.tools.A1iciaUtils;

/**
 * Thimk handles decision points as we choose what to send on to the client. 
 * Its name comes from the "THIMK" signs that IBM used to (and perhaps still does) 
 * have displayed in their offices. Eventually I want A1icia to make the decisions 
 * here. Hello, TensorFlow....
 * 
 * @author hulles
 *
 */
final class Thimk {
	private final static Logger LOGGER = Logger.getLogger("A1iciaOvermind.Thimk");
	private final static Level LOGLEVEL = A1iciaConstants.getA1iciaLogLevel();
	private final static Random RANDOM = new Random();
	private final OvermindRoom overmind;
	
	Thimk(OvermindRoom overmind) {
	
		A1iciaUtils.checkNotNull(overmind);
		this.overmind = overmind;
	}
	
	/**
	 * Select an ActionPackage from among a list of available
	 * ActionPackages. Currently it is a random selection; eventually A1icia
	 * will decide which one is best.
	 * 
	 * @param spark The spark common to the ActionPackages
	 * @param pkgs The list of ActionPackages
	 * @return The chosen ActionPackage
	 */
	static ActionPackage chooseAction(Spark spark, List<ActionPackage> pkgs) {
		ActionPackage pkg;
		int pkgIx;

		A1iciaUtils.checkNotNull(spark);
		A1iciaUtils.checkNotNull(pkgs);
		LOGGER.log(LOGLEVEL, "Thimk: chooseAction");
		if (pkgs.size() == 1) {
			// easy decision
			pkg = pkgs.get(0);
		} else if (spark.is("spark_analysis")) {
			// here, instead of returning an existing action package we're going to create a new one
			pkg = unifySparkAnalyses(pkgs);
		} else {
			// for now, just grab a random package; this is where A1icia will
			//    get to pick, eventually
			pkgIx = RANDOM.nextInt(pkgs.size());
			pkg = pkgs.get(pkgIx);
		}
		return pkg;
	}
	
	/**
	 * This method takes all the spark analyses from the various rooms, assuming there are
	 * some, and ends up with a new action package with a new Spark Analysis that contains 
	 * at most one spark package per sentence. We do this because it seems to make 
	 * conversational sense; we can change it later if seems like we could do better.
	 * 
	 * @param actionPackages
	 * @return
	 */
	private static ActionPackage unifySparkAnalyses(List<ActionPackage> actionPackages) {
		ListMultimap<String, SparkPackage> sentenceSparkPackages;
		SparkAnalysis analysis;
		List<SparkPackage> sparkPackages;
		List<SparkPackage> unifiedSparkPackages;
		SparkAnalysis unifiedAnalysis;
		ActionPackage newActionPackage;
		SparkPackage newSparkPkg;
		SentencePackage sentencePackage;
		
		A1iciaUtils.checkNotNull(actionPackages);
		unifiedSparkPackages = new ArrayList<>();
		sentenceSparkPackages = MultimapBuilder.hashKeys().arrayListValues().build();
		for (ActionPackage pkg : actionPackages) {
			analysis = (SparkAnalysis) pkg.getActionObject();
			sparkPackages = analysis.getSparkPackages();
			for (SparkPackage sparkPkg : sparkPackages) {
				sentencePackage = sparkPkg.getSentencePackage();
				if (sentencePackage == null) {
					A1iciaUtils.error("Thimk: null sentence package for spark " + sparkPkg.getName(),
							"This should not be the case at this point in the analysis.");
				} else {
					sentenceSparkPackages.put(sentencePackage.getSentencePackageID(), sparkPkg);
				}
			}
		}
		for (String sentencePackageID : sentenceSparkPackages.keySet()) {
			LOGGER.log(LOGLEVEL, "Thimk unifySparkAnalyses: sentencePackageID = " + sentencePackageID);
			sparkPackages = sentenceSparkPackages.get(sentencePackageID);
			LOGGER.log(LOGLEVEL, "Thimk unifySparkAnalyses: spark packages count = " + sparkPackages.size());
			for (SparkPackage sp : sparkPackages) {
				LOGGER.log(LOGLEVEL, "Thimk unifySparkAnalyses: spark package " + sp.getName() + 
						" score is " + sp.getConfidence());
			}
			if (sparkPackages.isEmpty()) {
				continue;
			}
			if (sparkPackages.size() == 1) {
				unifiedSparkPackages.add(sparkPackages.get(0));
				continue;
			}
			// fine, we have multiple spark packages for a sentence; we need to pick one
			Collections.sort(sparkPackages, new Comparator<SparkPackage>() {
				@Override
				public int compare(SparkPackage o1, SparkPackage o2) {
					return o2.getConfidence().compareTo(o1.getConfidence());
				}
			});
			unifiedSparkPackages.add(sparkPackages.get(0));
		}
		unifiedAnalysis = new SparkAnalysis();
		unifiedAnalysis.setSparkPackages(unifiedSparkPackages);
		newSparkPkg = SparkPackage.getDefaultPackage("spark_analysis");
		if (!newSparkPkg.isValid()) {
			throw new A1iciaException("Thimk: created invalid spark package");
		}
		newActionPackage = new ActionPackage(newSparkPkg);
		newActionPackage.setActionObject(unifiedAnalysis);
		// whew
		return newActionPackage;
	}
	
	/**
	 * Decide what to do after receiving a client request. Currently there isn't really
	 * a decision to make, we just throw it at the NLP analyzers (just Charlie, for now).
	 * 
	 * @param ticket
	 * @param request
	 */
	void decideClientRequestNextAction(Ticket ticket, ClientDialogRequest request) {
		RoomRequest newRequest;
		String msg;
		A1iciaClientObject obj;
		List<SparkPackage> sparkPackages;
		DialogRequest dialogRequest;
		
		A1iciaUtils.checkNotNull(ticket);
		A1iciaUtils.checkNotNull(request);
		LOGGER.log(LOGLEVEL, "Thimk: decideClientRequestNextAction entry");
		dialogRequest = request.getDialogRequest();
		msg = dialogRequest.getRequestMessage();
		obj = dialogRequest.getClientObject();
		if ((msg == null || msg.isEmpty()) && obj == null) {
			// nothing to do
			LOGGER.log(LOGLEVEL, "Thimk: nothing to do");
			sparkPackages = SparkPackage.getSingletonDefault("nothing_to_do");
			overmind.terminatePipeline(ticket, sparkPackages);
			return;
		}
		
		// okay, we need to figure out what's wanted, so first we start with awesome Charlie
		//    for some NLP action
		LOGGER.log(LOGLEVEL, "Thimk: decideClientRequestNextAction, msg = " + msg);
		newRequest = new RoomRequest(ticket);
		newRequest.setFromRoom(Room.OVERMIND);
		sparkPackages = SparkPackage.getSingletonDefault("nlp_analysis");
		newRequest.setSparkPackages(sparkPackages);
		newRequest.setMessage(msg);
		newRequest.setRoomObject(null);
		overmind.sendRoomRequest(newRequest);
	}
	
	/**
	 * Decide what to do after receiving the NLP analysis.
	 * 
	 * @param ticket The associated ticket (the sentence packages are now in the TicketJournal
	 */
	void decideNlpAnalysisNextAction(Ticket ticket) {
		RoomRequest newRequest;
		List<SparkPackage> sparkPackages;

		A1iciaUtils.checkNotNull(ticket);
		LOGGER.log(LOGLEVEL, "Thimk: decideNlpAnalysisNextAction");
		
		// Next, we ask for a spark analysis, to match up sparks with our input sentences
		newRequest = new RoomRequest(ticket);
		// we proxy the From room back to Overmind (we're not a full-fledged Room (yet))
		newRequest.setFromRoom(Room.OVERMIND);
		sparkPackages = SparkPackage.getSingletonDefault("spark_analysis");
		newRequest.setSparkPackages(sparkPackages);
		newRequest.setMessage("Asking for spark analysis");
		overmind.sendRoomRequest(newRequest);		
	}
	
	/**
	 * Decide what to do after receiving the spark analysis.
	 * This is currently the end of the pipeline, so we proceed in a calm and 
	 * orderly fashion to the exit.
	 * 
	 * @param ticket The associated ticket
	 * @param sparkPackages The accumulated spark packages
	 */
	void decideSparkAnalysisNextAction(Ticket ticket, List<SparkPackage> sparkPackages) {
		
		A1iciaUtils.checkNotNull(ticket);
		LOGGER.log(LOGLEVEL, "Thimk: decideSparkAnalysisNextAction");
		overmind.terminatePipeline(ticket, sparkPackages);
	}

}
