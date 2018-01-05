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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.eventbus.EventBus;
import com.hulles.a1icia.api.A1iciaConstants;
import com.hulles.a1icia.api.dialog.DialogRequest;
import com.hulles.a1icia.api.dialog.DialogResponse;
import com.hulles.a1icia.api.object.A1iciaClientObject;
import com.hulles.a1icia.api.remote.A1icianID;
import com.hulles.a1icia.api.shared.SerialSpark;
import com.hulles.a1icia.base.A1iciaException;
import com.hulles.a1icia.house.ClientDialogRequest;
import com.hulles.a1icia.house.ClientDialogResponse;
import com.hulles.a1icia.media.Language;
import com.hulles.a1icia.room.Room;
import com.hulles.a1icia.room.UrRoom;
import com.hulles.a1icia.room.document.A1icianAction;
import com.hulles.a1icia.room.document.ClientObjectWrapper;
import com.hulles.a1icia.room.document.HistoryUpdate;
import com.hulles.a1icia.room.document.MessageAction;
import com.hulles.a1icia.room.document.NLPAnalysis;
import com.hulles.a1icia.room.document.RoomActionObject;
import com.hulles.a1icia.room.document.RoomAnnouncement;
import com.hulles.a1icia.room.document.RoomRequest;
import com.hulles.a1icia.room.document.RoomResponse;
import com.hulles.a1icia.room.document.SparkAnalysis;
import com.hulles.a1icia.ticket.ActionPackage;
import com.hulles.a1icia.ticket.SparkPackage;
import com.hulles.a1icia.ticket.Ticket;
import com.hulles.a1icia.ticket.TicketJournal;
import com.hulles.a1icia.tools.A1iciaUtils;

public class OvermindRoom extends UrRoom {
	final static Logger LOGGER = Logger.getLogger("A1iciaOvermind.OvermindRoom");
	private final static Level LOGLEVEL = A1iciaConstants.getA1iciaLogLevel();
	private final Thimk thimk;
//	private final ConcurrentMap<Ticket, Thread> threadMap;
	
	public OvermindRoom(EventBus bus) {
		super(bus);
	
//		threadMap = new ConcurrentHashMap<>();
		thimk = new Thimk(this);
	}

	@Override
	public Room getThisRoom() {

		return Room.OVERMIND;
	}

	@Override
	protected ActionPackage createActionPackage(SparkPackage sparkPackage, RoomRequest request) {

		switch (sparkPackage.getName()) {
			case "respond_to_client":
				return receiveRequest(sparkPackage, request);
			default:
				throw new A1iciaException("Received unknown spark in " + getThisRoom());
		}
	}

	private ActionPackage receiveRequest(SparkPackage sparkPackage, RoomRequest request) {
		TicketJournal journal;
		Ticket ticket;
		Set<SerialSpark> clientSparks;
		ActionPackage pkg;
		ClientDialogRequest clientRequest;
		List<SparkPackage> sparkPackages;
		SparkPackage newSparkPackage;
		MessageAction action;
		String result;
		DialogRequest dialogRequest;
		
		A1iciaUtils.checkNotNull(request);
		LOGGER.log(LOGLEVEL, "Overmind: in receiveRequest");
		if (request.getFromRoom() != Room.ALICIA) {
			A1iciaUtils.warning("Overmind: got room request from a stranger, room is " +
					request.getFromRoom().getDisplayName());
		}
		if (!(request.getRoomObject() instanceof ClientDialogRequest)) {
			A1iciaUtils.error("Overmind: MindObject was not a ClientRequestObject");
			return null;
		}
		LOGGER.log(LOGLEVEL, "Overmind: fromRoom is A1icia, object is ClientDialogRequest");
		ticket = request.getTicket();
		journal = ticket.getJournal();
		clientRequest = (ClientDialogRequest) request.getRoomObject();
		journal.setClientRequest(clientRequest);
		
		// sparkPackage is "respond_to_client"
		LOGGER.log(LOGLEVEL, "Overmind: going to consume spark package");
		sparkPackages = request.getSparkPackages();
		SparkPackage.consumeFinal(sparkPackage.getName(), sparkPackages);
		
		dialogRequest = clientRequest.getDialogRequest();
		clientSparks = dialogRequest.getRequestActions();
		if (clientSparks != null && !clientSparks.isEmpty()) {
			// we got spark(s) up front, so bypass the analysis phase entirely
			LOGGER.log(LOGLEVEL, "Overmind: got client spark(s), bypassing analysis");
			sparkPackages = new ArrayList<>();
			for (SerialSpark s : clientSparks) {
				LOGGER.log(LOGLEVEL, "Client SerialSpark: " + s.getName());
				newSparkPackage = SparkPackage.getDefaultPackage(s);
				if (!newSparkPackage.isValid()) {
					throw new A1iciaException("Overmind: created invalid spark package 1");
				}
				sparkPackages.add(newSparkPackage);
			}
			terminatePipeline(ticket, sparkPackages);
		} else {
			LOGGER.log(LOGLEVEL, "Overmind: no client sparks, on to Thimk");
			thimk.decideClientRequestNextAction(ticket, clientRequest);
		}
		
		//    ....better slaves for our robot colony
		pkg = new ActionPackage(sparkPackage);
		action = new MessageAction();
		result = "plié";
		action.setMessage(result);
		result = "a movement in which a dancer bends the knees and straightens them again, " +
				"usually with the feet turned out and heels firmly on the ground. ‒ Wikipedia";
		action.setExplanation(result);
		pkg.setActionObject(action);
		return pkg;
	}

	@Override
	public void processRoomResponses(RoomRequest request, List<RoomResponse> responses) {
		Ticket uberTicket;
		Ticket ticket;
		NLPAnalysis nlpAnalysis;
		SparkAnalysis sparkAnalysis;
		SerialSpark spark;
		ActionPackage pkg;
		List<ActionPackage> responsePackages;
		List<ActionPackage> packageBag;
		List<ActionPackage> actionPackages;
		List<SparkPackage> sparkPackages;
		
		A1iciaUtils.checkNotNull(responses);
		uberTicket = request.getTicket();
		if (uberTicket == null) {
			throw new A1iciaException();
		}
		packageBag = new ArrayList<>();
		for (RoomResponse response : responses) {
			ticket = response.getTicket();
			// quick sanity check
			if (ticket != uberTicket) {
				throw new A1iciaException();
			}
			if (response.hasNoResponse()) {
				continue;
			}
			responsePackages = response.getActionPackages();
			packageBag.addAll(responsePackages); 
		}
		// at this point we're done with the list of RoomResponses, BTW
		if (packageBag.isEmpty()) {
			// no room matched the spark(s)
			A1iciaUtils.error("Empty packageBag -- unhandled spark(s)");
			packageBag = Collections.singletonList(ActionPackage.getProxyActionPackage());
		}
		
		spark = SerialSpark.find("nlp_analysis");
		actionPackages = ActionPackage.consumeActions(spark, packageBag);
		if (!actionPackages.isEmpty()) {
			pkg = Thimk.chooseAction(spark, actionPackages);
			nlpAnalysis = (NLPAnalysis) pkg.getActionObject();
			NLPAnalyzer.processAnalysis(uberTicket, nlpAnalysis);
			thimk.decideNlpAnalysisNextAction(uberTicket);
		}

		spark = SerialSpark.find("spark_analysis");
		actionPackages = ActionPackage.consumeActions(spark, packageBag);
		if (!actionPackages.isEmpty()) {
			pkg = Thimk.chooseAction(spark, actionPackages);
			sparkAnalysis = (SparkAnalysis) pkg.getActionObject();
			sparkPackages = sparkAnalysis.getSparkPackages();
			thimk.decideSparkAnalysisNextAction(uberTicket, sparkPackages);
		}

		spark = SerialSpark.find("client_response");
		actionPackages = ActionPackage.consumeActions(spark, packageBag);
		if (!actionPackages.isEmpty()) {
			// back from our request to A1icia with smartening up info
			if (!packageBag.isEmpty()) {
				throw new A1iciaException("OvermindRoom: got client_response back, but there are more " +
						"packages left for some reason, can't close ticket");
			}
			// we don't currently process this response further
			uberTicket.close();
			return;
		}

		spark = SerialSpark.find("update_history");
		actionPackages = ActionPackage.consumeActions(spark, packageBag);
		if (!actionPackages.isEmpty()) {
			// back from updateHistory
			if (!packageBag.isEmpty()) {
				throw new A1iciaException("OvermindRoom: got updateHistory back, but there are more " +
						"packages left for some reason, can't close ticket");
			}
			// we don't currently process the update response further
			uberTicket.close();
			return;
		}
		
		LOGGER.log(LOGLEVEL, "Overmind: checking packages after analyses, packageBag empty? " + packageBag.isEmpty());
		if (!packageBag.isEmpty()) {
			LOGGER.log(LOGLEVEL, "Overmind: advancing to processRoomActions with packages:");
			for (ActionPackage p : packageBag) {
				LOGGER.log(LOGLEVEL, "Package: " + p.getSpark().getName());
			}
			processRoomActions(uberTicket, packageBag);
		}
	}
	
	private void processRoomActions(Ticket uberTicket, List<ActionPackage> packageBag) {
		RoomRequest limaRequest;
		HistoryUpdate historyUpdate;
		ClientDialogResponse clientAction;
//		ActionPackage clientPackage;
//		SerialSpark clientSpark;
//		SparkPackage sparkPackage;
		Ticket historyTicket;
		
		A1iciaUtils.checkNotNull(uberTicket);
		A1iciaUtils.checkNotNull(packageBag);
		LOGGER.log(LOGLEVEL, "Overmind: in processRoomActions prior to Thimk");
		clientAction = determineClientAction(uberTicket, packageBag);
		postRequest(uberTicket, clientAction);		
		
		// Here we're updating anything that needs it following the request/response series.
		//    For now, we're just updating history with LIMA but we can do more than one 
		//    in the same request if we need to.
		historyTicket = Ticket.createNewTicket(getHall(), getThisRoom());
		historyTicket.setFromA1icianID(A1iciaConstants.getA1iciaA1icianID());
		limaRequest = new RoomRequest(historyTicket);
		limaRequest.setFromRoom(getThisRoom());
		limaRequest.setSparkPackages(SparkPackage.getSingletonDefault("update_history"));
		historyUpdate = new HistoryUpdate(uberTicket);
		limaRequest.setRoomObject(historyUpdate);
		sendRoomRequest(limaRequest);
	}
	
	private void postRequest(Ticket ticket, ClientDialogResponse response) {
		RoomRequest roomRequest;

		A1iciaUtils.checkNotNull(ticket);
		A1iciaUtils.checkNotNull(response);
		roomRequest = new RoomRequest(ticket);
		roomRequest.setFromRoom(getThisRoom());
		roomRequest.setSparkPackages(SparkPackage.getSingletonDefault("client_response"));
		roomRequest.setMessage("Client response");
		roomRequest.setRoomObject(response);
		sendRoomRequest(roomRequest);
	}
	
	/**
	 * The analysis and action phases are over, so here
	 * we figure out what we're going to send to the client.
	 * 
	 * @param ticket The ticket from the client request
	 * @param packageBag A bag of all of the ActionPackages from our RoomResponses
	 * @return The ActionPackage we're sending to the client
	 */
	private static ClientDialogResponse determineClientAction(Ticket ticket, List<ActionPackage> packageBag) {
		Set<SerialSpark> sparks;
		StringBuilder messageSb;
		StringBuilder explanationSb;
		String msg;
		String expl;
		A1iciaClientObject obj;
		ClientDialogResponse clientAction;
		DialogResponse response;
		ActionPackage pkg;
		SerialSpark clientSpark = null;
		A1icianID overrideA1icianID = null;
		List<ActionPackage> pkgs;
		RoomActionObject action;
		TicketJournal journal;
		ClientObjectWrapper objectWrapper;
		A1icianAction remoteObject;
		
		A1iciaUtils.checkNotNull(ticket);
		A1iciaUtils.checkNotNull(packageBag);
		journal = ticket.getJournal();
		sparks = new HashSet<>();
		packageBag.stream().forEach(p -> sparks.add(p.getSpark()));
		
		LOGGER.log(LOGLEVEL, "Overmind: determineClientAction");
		messageSb = new StringBuilder();
		explanationSb = new StringBuilder();
		obj = null;
		for (SerialSpark spark : sparks) {
			if (messageSb.length() > 0) {
				messageSb.append(".\n");
				explanationSb.append("\n\n");
			}
			msg = null;
			expl = null;
			pkgs = ActionPackage.hasActions(spark, packageBag);
			if (pkgs.isEmpty()) {
				A1iciaUtils.error("Unhandled spark in Thimk.processRoomActions = " +
						spark.getName());
				msg = "Ich bin ein Ausländer. Und ich bin die eigentliche A1icia.";
				expl = "Overmind: something went awry in the Overmind.";
			} else {
				pkg = Thimk.chooseAction(spark, pkgs);
				action = pkg.getActionObject();
				msg = action.getMessage();
				expl = action.getExplanation();
				// TODO allow for more than one object to be sent back to client
				if (obj == null) {
                    if (action instanceof ClientObjectWrapper) {
                        objectWrapper = (ClientObjectWrapper) action;
                        obj = objectWrapper.getClientObject();
    					LOGGER.log(LOGLEVEL, "Overmind: using wrapped object for " + pkg);
                    }
				}
				if (action instanceof A1icianAction) {
					LOGGER.log(LOGLEVEL, "Overmind: at A1icianAction statement");
					remoteObject = (A1icianAction) action;
					clientSpark = remoteObject.getClientAction();
					overrideA1icianID = remoteObject.getToA1icianID();
					LOGGER.log(LOGLEVEL, "Overmind: overrideA1icianID = " + overrideA1icianID);
				}
				journal.addActionPackage(pkg);
			}
			if (msg != null) {
				messageSb.append(msg);
			}
			if (expl != null) {
				explanationSb.append(expl);
			}
		}
		
		// at this point, we know that the messageSb, the explanationSb, and optionally the obj
		//    are what we're sending to the client
		clientAction = new ClientDialogResponse();
		response = clientAction.getDialogResponse();
		response.setLanguage(Language.AMERICAN_ENGLISH);
		response.setFromA1icianID(A1iciaConstants.getA1iciaA1icianID());
		if (overrideA1icianID == null) {
			response.setToA1icianID(ticket.getFromA1icianID());
		} else {
			LOGGER.log(LOGLEVEL, "Overmind: overriding ToA1icianID, overrideA1icianID = " + overrideA1icianID);
			response.setToA1icianID(overrideA1icianID);
		}
		response.setMessage(messageSb.toString());
		response.setExplanation(explanationSb.toString());
		if (clientSpark != null) {
			response.setResponseAction(clientSpark);
		}
		if (obj != null) {
			response.setClientObject(obj);
		}
		return clientAction;
	}
	
	/**
	 * This terminates the analysis phase. Now we have all the spark packages, so we'll
	 * initiate the process that assigns actions to the sparks.
	 * 
	 * @param ticket The ticket from HistoryAnalyzer
	 */
	void terminatePipeline(Ticket ticket, List<SparkPackage> sparkPackages) {	
		RoomRequest respondRequest;
		TicketJournal journal;
		ClientDialogRequest clientRequest;
		SparkPackage aardPkg;
		DialogRequest dialogRequest;
		
		A1iciaUtils.checkNotNull(ticket);
		A1iciaUtils.checkNotNull(sparkPackages);
		// Now we're ready to assemble whatever we need to respond to the client.
		//    At this point, the ticket journal should have all of the sentence packages
		//    completed with a spark for each one, and a set of sparks upon which to act (the
		//    journal sparks).
		if (sparkPackages.isEmpty()) {
			A1iciaUtils.error("OvermindRoom: terminatePipeline -- no spark packages for ticket " + 
					ticket, "Using aardvark spark package");
			aardPkg = SparkPackage.getDefaultPackage("aardvark");
			if (!aardPkg.isValid()) {
				throw new A1iciaException("Overmind: created invalid spark package 3");
			}
			sparkPackages.add(aardPkg);
		}
		journal = ticket.getJournal();

		// here we just make one action-related request and let the rooms answer how they see fit
		respondRequest = new RoomRequest(ticket);
		respondRequest.setFromRoom(getThisRoom());
		respondRequest.setSparkPackages(sparkPackages);
		clientRequest = journal.getClientRequest();
		dialogRequest = clientRequest.getDialogRequest();
		respondRequest.setMessage(dialogRequest.getRequestMessage());
		respondRequest.setRoomObject(clientRequest);
		sendRoomRequest(respondRequest);
	}

	@Override
	protected void roomStartup() {
	}

	@Override
	protected void roomShutdown() {
	}

	@Override
	protected Set<SerialSpark> loadSparks() {
		Set<SerialSpark> sparks;
		
		sparks = new HashSet<>();
		sparks.add(SerialSpark.find("respond_to_client"));
		return sparks;
	}

	@Override
	protected void processRoomAnnouncement(RoomAnnouncement announcement) {
	}

}
