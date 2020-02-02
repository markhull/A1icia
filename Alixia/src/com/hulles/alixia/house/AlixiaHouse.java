/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hulles.alixia.house;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;
import com.hulles.alixia.Alixia;
import com.hulles.alixia.api.AlixiaConstants;
import com.hulles.alixia.api.dialog.DialogRequest;
import com.hulles.alixia.api.dialog.DialogResponse;
import com.hulles.alixia.api.remote.AlixianID;
import com.hulles.alixia.api.shared.AlixiaException;
import com.hulles.alixia.api.shared.SharedUtils;

/**
 * AlixiaHouse is Alixia's contribution to the "house" network. It is responsible for managing
 * traffic between the street bus and the hall bus.
 * 
 * @author hulles
 *
 */
public final class AlixiaHouse extends UrHouse {
	private final static Logger LOGGER = LoggerFactory.getLogger(AlixiaHouse.class);
    private final AlixianID alixianID;
    
    public AlixiaHouse() {
        super();
        
		alixianID = AlixiaConstants.getAlixiaAlixianID();
    }
    public AlixiaHouse(EventBus street) {
        super(street);

		alixianID = AlixiaConstants.getAlixiaAlixianID();
    }

    /**
     * Receive a response from the room network and post it onto the street (house) bus.
     * 
     * @param response The DialogResponse to post
     */
    public void receiveResponse(DialogResponse response) {

        LOGGER.debug("AlixiaHouse: in receiveResponse");
        SharedUtils.checkNotNull(response);
        getStreet().post(response);
    }

    /**
     * Instantiate the house and start a session for it.
     * 
     */
    @Override
    protected void houseStartup() {
        Session session;

        session = Session.getSession(alixianID);
        super.setSession(session);
    }

    /**
     * Shut down the house's session.
     * 
     */
    @Override
    protected void houseShutdown() {
        Session session;

        session = super.getSession(alixianID);
        if (session != null) {
            super.removeSession(session);
        }
    }

    /**
     * Handle a new DialogRequest on the street for one of the Alixians in
     * our House.
     * 
     * @param request
     * 
     */
    @Override
    protected void newDialogRequest(DialogRequest request) {

        SharedUtils.checkNotNull(request);
        LOGGER.debug("AlixiaHouse: Forwarding dialog request from house to room");
        Alixia.forwardRequestToRoom(request);
    }

    /**
     * Handle a new DialogResponse on the street for one of the Alixians in
     * our house.
     * 
     * @param response
     * 
     */
    @Override
    protected void newDialogResponse(DialogResponse response) {
        throw new AlixiaException("Response not implemented in " + getThisHouse());
    }

    /**
     * Return which House this is.
     * 
     * @return The house enum
     * 
     */
    @Override
    public House getThisHouse() {

        return House.ALIXIA;
    }
}
