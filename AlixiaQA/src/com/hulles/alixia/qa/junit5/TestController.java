package com.hulles.alixia.qa.junit5;

import java.io.Closeable;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.SetMultimap;
import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import com.hulles.alixia.api.AlixiaConstants;
import com.hulles.alixia.api.shared.AlixiaException;
import com.hulles.alixia.api.shared.SerialSememe;
import com.hulles.alixia.api.shared.SharedUtils;
import com.hulles.alixia.room.Room;
import com.hulles.alixia.room.UrRoom;
import com.hulles.alixia.room.document.RoomAnnouncement;
import com.hulles.alixia.room.document.RoomRequest;
import com.hulles.alixia.room.document.RoomResponse;
import com.hulles.alixia.room.document.WhatSememesAction;
import com.hulles.alixia.ticket.ActionPackage;
import com.hulles.alixia.ticket.SememePackage;
import com.hulles.alixia.ticket.Ticket;

public class TestController implements Closeable {
    final static Logger LOGGER = LoggerFactory.getLogger(TestController.class);
    private final EventBus hallBus;
	private final ExecutorService busPool;
    private final ControllerRoom controllerRoom;
    final SetMultimap<SerialSememe, Room> sememeRooms;
    volatile boolean gotWhatSememes = false;
    
    public TestController() {
    	
		busPool = Executors.newCachedThreadPool();
		hallBus = new AsyncEventBus("Hall", busPool);
        controllerRoom = new ControllerRoom(hallBus);
        sememeRooms = MultimapBuilder.hashKeys().enumSetValues(Room.class).build();
    }

    public void startRoom(UrRoom room) {
    	
    	room.setHall(hallBus);
    }
    
    public EventBus getHallBus() {
    	
    	return hallBus;
    }
    
    public void sendWhatSememes() {
        RoomRequest sememesQuery;
        Ticket ticket;

        // send WHAT_SEMEMES request to load sememeRooms
        ticket = Ticket.createNewTicket(hallBus, Room.CONTROLLER);
        ticket.setFromAlixianID(AlixiaConstants.getAlixiaAlixianID());
        sememesQuery = new RoomRequest(ticket);
        sememesQuery.setFromRoom(Room.CONTROLLER);
        sememesQuery.setSememePackages(SememePackage.getSingletonDefault("what_sememes"));
        sememesQuery.setMessage("WHAT_SEMEMES query");
        controllerRoom.sendParentRequest(sememesQuery);
    }
    
    public Set<SerialSememe> getSememesForRoom(UrRoom targetRoom) {
    	Set<SerialSememe> sememes;
    	Room room;
    	
    	room = targetRoom.getThisRoom();
    	sememes = new HashSet<>();
    	Set<Entry<SerialSememe,Room>> sememeSet = sememeRooms.entries();
    	for (Entry<SerialSememe,Room> entry : sememeSet) {
    		if (entry.getValue().equals(room)) {
    			sememes.add(entry.getKey());
    		}
    	}
    	return sememes;
    }
    
    public synchronized boolean gotWhatSememes() {
    
    	return gotWhatSememes;
    }
    
	@Override
	public void close() {
		
		busPool.shutdown();
		LOGGER.debug("TestController: shutting down bus pool");
		try {
			if (!busPool.awaitTermination(5, TimeUnit.SECONDS)) {
				busPool.shutdownNow();
				if (!busPool.awaitTermination(5, TimeUnit.SECONDS)) {
                    throw new RuntimeException("TestController bus pool did not terminate");
                }
			}
		} catch (InterruptedException ie) {
			busPool.shutdownNow();
			Thread.currentThread().interrupt();
		}
		LOGGER.debug("TestController: after shutdown");		
	}

    /**
     * This is the Controller's very own room. Keep out. No girls allowed.
     *
     * @author hulles
     *
     */
    private class ControllerRoom extends UrRoom {
        private final SerialSememe whatSememesSememe;

        ControllerRoom(EventBus hall) {
            super(hall);

            whatSememesSememe = SerialSememe.find("what_sememes");
        }

        /**
         * Send the "what_sememes" request on behalf of the Controller proper.
         *
         * @param request The "what_sememes" request
         */
        void sendParentRequest(RoomRequest request) {

            LOGGER.debug("In sendParentRequest");
            super.sendRoomRequest(request);
        }

        /**
         * Return which room this is.
         *
         */
        @Override
        public Room getThisRoom() {

            return Room.CONTROLLER;
        }

        @Override
        protected void roomStartup() {
        }

        @Override
        protected void roomShutdown() {
        }

        /**
         * Process all the room responses we received from our "what_sememes"
         * request.
         *
         */
        @Override
        public void processRoomResponses(RoomRequest request, List<RoomResponse> responses) {
            List<ActionPackage> pkgs;
            ActionPackage pkg;
            WhatSememesAction action;
            Set<SerialSememe> sememes;
            Room fromRoom;
            Ticket ticket;

            SharedUtils.checkNotNull(responses);
            ticket = request.getTicket();
            if (ticket == null) {
                LOGGER.error("Controller: null ticket");
            }
            for (RoomResponse rr : responses) {
                fromRoom = rr.getFromRoom();
                pkgs = rr.getActionPackages();
                pkg = ActionPackage.has(whatSememesSememe, pkgs);
                if (pkg != null) {
                    action = (WhatSememesAction) pkg.getActionObject();
                    sememes = action.getSememes();
                    LOGGER.debug("In ControllerRoom:processRoomResponse with response from {}, sememes from action = {}", fromRoom, sememes);
                    for (SerialSememe s : sememes) {
                        sememeRooms.put(s, fromRoom);
                    }
                } else {
                    LOGGER.error("Controller: unable to find what_sememes in action packages");
                }
            }
            
            UrRoom.setRoomSememes(sememeRooms);
            
            if (ticket != null) {
                ticket.close();
            }
            
            gotWhatSememes = true;
        }

        /**
         * Load the list of sememes that we can handle (just one,
         * "what_sememes").
         *
         */
        @Override
        protected Set<SerialSememe> loadSememes() {
            Set<SerialSememe> sememes;

            sememes = new HashSet<>();
            sememes.add(SerialSememe.find("what_sememes"));
            return sememes;
        }

        /**
         * We don't respond to room requests. Bear in mind that this method is
         * only called if a request should be handled by us, so if we get one
         * it's a serious error.
         *
         */
        @Override
        public ActionPackage createActionPackage(SememePackage sememePkg, RoomRequest request) {
            throw new AlixiaException("Request not implemented in "
                    + getThisRoom().getDisplayName());
        }

        /**
         * We don't respond to room announcements.
         *
         */
        @Override
        protected void processRoomAnnouncement(RoomAnnouncement announcement) {
        }

    }
}
