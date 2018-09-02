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
package com.hulles.a1icia.alpha;

import com.hulles.a1icia.api.A1iciaConstants;
import com.hulles.a1icia.api.dialog.DialogResponse;
import com.hulles.a1icia.api.remote.A1icianID;
import com.hulles.a1icia.api.shared.A1iciaException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.hulles.a1icia.api.shared.SerialSememe;
import com.hulles.a1icia.api.shared.SharedUtils;
import com.hulles.a1icia.api.tools.A1iciaUtils;
import com.hulles.a1icia.house.ClientDialogResponse;
import com.hulles.a1icia.media.Language;
import com.hulles.a1icia.room.Room;
import com.hulles.a1icia.room.UrRoom;
import com.hulles.a1icia.room.document.ClientObjectWrapper;
import com.hulles.a1icia.room.document.MessageAction;
import com.hulles.a1icia.room.document.RoomActionObject;
import com.hulles.a1icia.room.document.RoomAnnouncement;
import com.hulles.a1icia.room.document.RoomRequest;
import com.hulles.a1icia.room.document.RoomResponse;
import com.hulles.a1icia.room.document.SememeAnalysis;
import com.hulles.a1icia.ticket.ActionPackage;
import com.hulles.a1icia.ticket.SememePackage;
import com.hulles.a1icia.ticket.SentencePackage;
import com.hulles.a1icia.ticket.Ticket;
import com.hulles.a1icia.ticket.TicketJournal;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Alpha Room is an exemplary implementation for rooms. It simply returns a form of "Aardvark" 
 * when queried. I love Alpha.
 * <p>
 * At least temporarily, Alpha also tests push notfications. Go Alpha.
 * 
 * @author hulles
 *
 */
public final class AlphaRoom extends UrRoom {
	private final static Logger LOGGER = Logger.getLogger("A1iciaAlpha.AlphaRoom");
	private final static Level LOGLEVEL = A1iciaConstants.getA1iciaLogLevel();
	private final ScheduledExecutorService executor;
	private volatile boolean alreadySentNotification = false;
    
	public AlphaRoom() {
		super();
        
		executor = Executors.newScheduledThreadPool(1);        
		addDelayedShutdownHook(executor);
	}

	/**
	 * Here we create an ActionPackage from Alpha, either an analysis or an action, depending 
	 * on the sememe that we receive, and return it to UrRoom.
	 * <p>
     * Note that we should only receive sememes that we've advertised (see @link{loadSememes}, so
     * if we don't recognize the sememe we receive it's an error.
     * 
     * @param sememePkg The sememe package
     * @param request The room request
     * @return The ActionPackage we've created
	 */
	@Override
	protected ActionPackage createActionPackage(SememePackage sememePkg, RoomRequest request) {

		switch (sememePkg.getName()) {
			case "sememe_analysis":
				// Hey, this one's easy! I'm smart like a scientist!
				return createAnalysisActionPackage(sememePkg, request);
			case "aardvark":
				// I know this one!
				return createAardvarkActionPackage(sememePkg, request);
			default:
				throw new A1iciaException("Received unknown sememe in " + getThisRoom());
		}
	}
	
	/**
	 * Here we carefully consider each sentence package in the ticket journal and determine
	 * the best sememe package for the essence and nuances of the... just kidding. 
	 * Actually we stick "aardvark" in as the best action sememe for each sentence.
	 * Because we can.
	 * 
	 * @param sememePkg The SememePackage
	 * @param request The RoomRequest
	 * @return A SememeAnalysis action package
	 */
	private ActionPackage createAnalysisActionPackage(SememePackage sememePkg, RoomRequest request) {
		ActionPackage actionPkg;
		SememeAnalysis analysis;
		Ticket ticket;
		TicketJournal journal;
		List<SememePackage> sememePackages;
		List<SentencePackage> sentencePackages;
		SememePackage aardPkg;
		A1icianID a1icianID;
        
		SharedUtils.checkNotNull(sememePkg);
		SharedUtils.checkNotNull(request);
        if (!alreadySentNotification) {
            a1icianID = request.getTicket().getFromA1icianID();
            startNotificationService(a1icianID);
            alreadySentNotification = true;
        }
		ticket = request.getTicket();
		journal = ticket.getJournal();
		actionPkg = new ActionPackage(sememePkg);
		sememePackages = new ArrayList<>();
		sentencePackages = journal.getSentencePackages();
		analysis = new SememeAnalysis();
		for (SentencePackage sentencePackage : sentencePackages) {
			aardPkg = SememePackage.getDefaultPackage("aardvark");
			aardPkg.setSentencePackage(sentencePackage);
			aardPkg.setConfidence(5); // hey, it *might* be the best one
			if (!aardPkg.isValid()) {
				throw new A1iciaException("AlphaRoom: created invalid sememe package");
			}
			sememePackages.add(aardPkg);
		}
		analysis.setSememePackages(sememePackages);
		actionPkg.setActionObject(analysis);
		return actionPkg;
	}

	/**
	 * Create an action package for the "aardvark" sememe, which consists of saying some form
	 * of the word "aardvark".
	 * 
	 * @param sememePkg
	 * @param request
	 * @return
	 */
	private ActionPackage createAardvarkActionPackage(SememePackage sememePkg, RoomRequest request) {
		ActionPackage pkg;
		MessageAction action;
		String clientMsg;
		String result;
		A1icianID a1icianID;
        
		SharedUtils.checkNotNull(sememePkg);
		SharedUtils.checkNotNull(request);
        if (!alreadySentNotification) {
            a1icianID = request.getTicket().getFromA1icianID();
            startNotificationService(a1icianID);
            alreadySentNotification = true;
        }
		pkg = new ActionPackage(sememePkg);
		action = new MessageAction();
		clientMsg = request.getMessage().trim();
		if (clientMsg.isEmpty()) {
			result = "Aardvark???";
		} else {
			switch (clientMsg.charAt(clientMsg.length() - 1)) {
				case '?':
					result = "Aardvark!";
					break;
				case '.':
					result = "Aardvark?";
					break;
				case '!':
					result = "Aardvark! Aardvark! Aardvark!";
					break;
				default:
					result = "Aardvark...";
			}
		}
		action.setMessage(result);
		action.setExplanation("I said, \"" + result + "\"");
		pkg.setActionObject(action);
		return pkg;
	}
	
	private void startNotificationService(A1icianID a1icianID) {
		
        SharedUtils.checkNotNull(a1icianID);
		final Runnable checker = new Runnable() {
			@Override
			public void run() {
                ClientDialogResponse clientResponse;
                SerialSememe sememe;
                DialogResponse response;
                
                clientResponse = new ClientDialogResponse();
                response = clientResponse.getDialogResponse();
                response.setLanguage(Language.AMERICAN_ENGLISH);
                response.setFromA1icianID(A1iciaConstants.getA1iciaA1icianID());
                response.setMessage("Hello from Alpha! Aardvark!");
                response.setExplanation("I was told to say that.");
                response.setToA1icianID(a1icianID);
                sememe = SerialSememe.find("notify");
                response.setResponseAction(sememe);
                AlphaRoom.super.postPushRequest(clientResponse);
			}
		};
		executor.schedule(checker, 120, TimeUnit.SECONDS);
	}
    
	static void shutdownAndAwaitTermination(ExecutorService pool) {
		
		pool.shutdown(); // Disable new tasks from being submitted
		try {
			// Wait a while for existing tasks to terminate
			if (!pool.awaitTermination(10, TimeUnit.SECONDS)) {
				pool.shutdownNow(); // Cancel currently executing tasks
				// Wait a while for tasks to respond to being cancelled
				if (!pool.awaitTermination(10, TimeUnit.SECONDS)) {
                    A1iciaUtils.error("TimerHandler -- executor did not terminate");
                }
			}
		} catch (InterruptedException ie) {
			// (Re-)Cancel if current thread also interrupted
			pool.shutdownNow();
			// Preserve interrupt status
			Thread.currentThread().interrupt();
		}
	}
	
	private void addDelayedShutdownHook(final ScheduledExecutorService pool) {
		Runnable shutdownHook;
		Thread hook;
		
		shutdownHook = new ShutdownHook(pool);
		hook = new Thread(shutdownHook);
		Runtime.getRuntime().addShutdownHook(hook);
	}
	
	private class ShutdownHook implements Runnable {
		ScheduledExecutorService pool;
		
		ShutdownHook(ScheduledExecutorService pool) {
			this.pool = pool;
		}
		
	    @Override
		public void run() {
	    	
			shutdownAndAwaitTermination(pool);
	    }
	}
	
	/**
	 * Return this room name.
	 * 
     * @return The Room enum for this room.
	 */
	@Override
	public Room getThisRoom() {

		return Room.ALPHA;
	}


	/**
     * This method is executed when we receive a list of responses from our notification request. We
     * don't really care about the responses -- we just send an uber-request to
     * A1icia to forward the notification, and this is what she sends back. Regarding the method body, well, 
	 * while this looks like a lot of nonsense, and on one level that's true of course, it also
	 * tests the complex machinery that generates responses to requests. That's my story and I'm
	 * sticking to it.
	 * 
     * @param request The room request
     * @param responses The responses to the request
	 */
	@Override
	public void processRoomResponses(RoomRequest request, List<RoomResponse> responses) {
		List<ActionPackage> pkgs;
		RoomActionObject obj;
		MessageAction msgAction;
		Ticket ticket;
		ClientObjectWrapper cow;
		
		SharedUtils.checkNotNull(request);
		SharedUtils.checkNotNull(responses);
		// note that here we're ignoring the fact that we might get more than one response, 
		// particularly for the media request -- FIXME
		for (RoomResponse rr : responses) {
			if (!rr.hasNoResponse()) {
				// see if we can learn anything....
				pkgs = rr.getActionPackages();
				for (ActionPackage pkg : pkgs) {
					obj = pkg.getActionObject();
					if (obj instanceof MessageAction) {
						msgAction = (MessageAction) obj;
                        LOGGER.log(Level.INFO, "We got us some learning => {0}, {1}", 
                                new String[]{msgAction.getMessage(), msgAction.getExplanation()});
					}
				}
			}
		}
		ticket = request.getTicket();
		ticket.close();
	}

	@Override
	protected void roomStartup() {

    }

	@Override
	protected void roomShutdown() {
        
		shutdownAndAwaitTermination(executor);
	}

	/**
	 * Advertise which sememes we handle.
	 * 
     * @return The list of sememes we process
	 */
	@Override
	protected Set<SerialSememe> loadSememes() {
		Set<SerialSememe> sememes;
		
		sememes = new HashSet<>();
		sememes.add(SerialSememe.find("sememe_analysis"));
		sememes.add(SerialSememe.find("aardvark"));
		return sememes;
	}

	/**
	 * We don't do anything with RoomAnnouncements but it is legitimate to receive them here,
	 * so no error.
	 * 
     * @param announcement
     * 
	 */
	@Override
	protected void processRoomAnnouncement(RoomAnnouncement announcement) {
	}

}
