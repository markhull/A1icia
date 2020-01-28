/** *****************************************************************************
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
 ******************************************************************************
 */
package com.hulles.alixia;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.SetMultimap;
import com.google.common.eventbus.EventBus;
import com.google.common.util.concurrent.AbstractIdleService;
import com.google.common.util.concurrent.Service;
import com.google.common.util.concurrent.ServiceManager;
import com.hulles.alixia.api.AlixiaConstants;
import com.hulles.alixia.api.shared.AlixiaException;
import com.hulles.alixia.api.shared.SerialSememe;
import com.hulles.alixia.api.shared.SharedUtils;
import com.hulles.alixia.api.tools.AlixiaUtils;
import com.hulles.alixia.cayenne.AlixiaApplication;
import com.hulles.alixia.cayenne.Sememe;
import com.hulles.alixia.room.BusMonitor;
import com.hulles.alixia.room.Room;
import com.hulles.alixia.room.UrRoom;
import com.hulles.alixia.room.document.RoomAnnouncement;
import com.hulles.alixia.room.document.RoomRequest;
import com.hulles.alixia.room.document.RoomResponse;
import com.hulles.alixia.room.document.WhatSememesAction;
import com.hulles.alixia.ticket.ActionPackage;
import com.hulles.alixia.ticket.SememePackage;
import com.hulles.alixia.ticket.Ticket;

/**
 * The Controller is a little simpler than its name would suggest -- it
 * basically just starts and stops all the in-house rooms. However, if we ever
 * need dependency injection or some crazy thing like that, this is the guy.
 *
 * @author hulles
 *
 */
final class Controller extends AbstractIdleService {
    final static Logger LOGGER = LoggerFactory.getLogger(Controller.class);
    final SetMultimap<SerialSememe, Room> sememeRooms;
    private ControllerRoom controllerRoom;
    private ServiceManager serviceManager;
    private final EventBus hallBus;
    private final List<UrRoom> rooms;
    final Set<SerialSememe> allSememes;
    final Boolean showOrphans;

    Controller(EventBus hallBus, List<UrRoom> rooms, Boolean showOrphans) {

        SharedUtils.checkNotNull(hallBus);
        SharedUtils.checkNotNull(rooms);
        SharedUtils.checkNotNull(showOrphans);
        this.hallBus = hallBus;
        this.rooms = rooms;
        this.showOrphans = showOrphans;
        AlixiaApplication.setJdbcLogging(false);
        sememeRooms = MultimapBuilder.hashKeys().enumSetValues(Room.class).build();
        // This is probably our first Cayenne access
        allSememes = Sememe.getAllSememes();
        if (allSememes.isEmpty()) {
            throw new AlixiaException("System error: no sememes loaded in Controller");
        }
        SerialSememe.setSememes(allSememes);
    }

    /**
     * Create a ServiceManager and start all the room services. We also send a
     * "what_sememes" request to see what sememes each room advertises that it
     * can handle.
     *
     */
    @Override
    protected void startUp() throws Exception {
        RoomRequest sememesQuery;
        Ticket ticket;
        List<UrRoom> services;
        Set<Entry<Service, Long>> startupTimes;
        Long startupTime;
        Long totalStartupTime = 0L;

        services = loadServices(hallBus, rooms);
        serviceManager = new ServiceManager(services);
        serviceManager.startAsync();
        serviceManager.awaitHealthy();
        startupTimes = serviceManager.startupTimes().entrySet();
        for (Entry<Service, Long> entry : startupTimes) {
            startupTime = entry.getValue();
            totalStartupTime += startupTime;
            LOGGER.info("{} started in {}", entry.getKey(), AlixiaUtils.formatElapsedMillis(startupTime));
        }
        // Note that the total of startup times is not the same as the total 
        //    elapsed startup time thanks to the miracle of parallel processing....
        // The total of startup times IS useful to compare startup speeds for different 
        //    servers, however....
        LOGGER.info("Total of startup times is {}", AlixiaUtils.formatElapsedMillis(totalStartupTime));

        // send WHAT_SEMEMES request to load sememeRooms
        ticket = Ticket.createNewTicket(hallBus, Room.CONTROLLER);
        ticket.setFromAlixianID(AlixiaConstants.getAlixiaAlixianID());
        sememesQuery = new RoomRequest(ticket);
        sememesQuery.setFromRoom(Room.CONTROLLER);
        sememesQuery.setSememePackages(SememePackage.getSingletonDefault("what_sememes"));
        sememesQuery.setMessage("WHAT_SEMEMES query");
        controllerRoom.sendParentRequest(sememesQuery);
    }

    /**
     * Marshal all the rooms and add them to the list of services the
     * ServiceManager will start.
     *
     * @param hall The room bus
     * @return A list of services to start
     */
    private List<UrRoom> loadServices(EventBus hall, List<UrRoom> roomsToLoad) {
        List<UrRoom> services;
        UrRoom busMonitor;
        Set<Room> definedRooms;
        Set<Room> implementedRooms;
        
        SharedUtils.checkNotNull(hall);
        SharedUtils.checkNotNull(roomsToLoad);
        services = new ArrayList<>(roomsToLoad.size() + 2);
        controllerRoom = new ControllerRoom(hall);
        services.add(controllerRoom);
        busMonitor = new BusMonitor(hall);
        services.add(busMonitor);
        for (UrRoom room : roomsToLoad) {
            LOGGER.debug("Loading room {}", room.getThisRoom());
            room.setHall(hall);
            services.add(room);
        }
        // we iterate through the services, compare them to the list 
        //    of defined rooms, and see if any are missing
        definedRooms = EnumSet.allOf(Room.class);
        implementedRooms = EnumSet.noneOf(Room.class);
        for (UrRoom room : services) {
            implementedRooms.add(room.getThisRoom());
            definedRooms.remove(room.getThisRoom());
        }
        UrRoom.setImplementedRooms(implementedRooms);
        for (Room room : definedRooms) {
            LOGGER.warn("{} is not implemented", room.getDisplayName());
        }
        return services;
    }

    /**
     * Shut down the service manager and bus pool.
     *
     */
    @Override
    protected void shutDown() throws Exception {

        serviceManager.stopAsync();
        serviceManager.awaitStopped();
        AlixiaApplication.shutdown();
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

            // we do a couple quick reality checks before we go
            for (SerialSememe s : sememeRooms.keySet()) {
                if (!allSememes.contains(s)) {
                    // Type I error
                    LOGGER.error("ControllerRoom: sememe {} is not a valid sememe", s.getName());
                }
            }
            if (showOrphans) {
	            for (SerialSememe s : allSememes) {
	                if (!sememeRooms.containsKey(s)) {
	                    // Type II error
	                    LOGGER.warn("ControllerRoom: sememe {} not implemented", s.getName());
	                }
	            }
            }
            
            UrRoom.setRoomSememes(sememeRooms);
            
            if (ticket != null) {
                ticket.close();
            }
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
