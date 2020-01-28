/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hulles.alixia.room;

import static com.hulles.alixia.Alixia.BUNDLE_NAME;

import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;
import com.hulles.alixia.Alixia;
import com.hulles.alixia.api.AlixiaConstants;
import com.hulles.alixia.api.dialog.DialogRequest;
import com.hulles.alixia.api.dialog.DialogResponse;
import com.hulles.alixia.api.remote.AlixianID;
import com.hulles.alixia.api.shared.AlixiaException;
import com.hulles.alixia.api.shared.SerialSememe;
import com.hulles.alixia.api.shared.SharedUtils;
import com.hulles.alixia.api.tools.AlixiaVersion;
import com.hulles.alixia.house.ClientDialogRequest;
import com.hulles.alixia.house.ClientDialogResponse;
import com.hulles.alixia.room.document.MessageAction;
import com.hulles.alixia.room.document.RoomAnnouncement;
import com.hulles.alixia.room.document.RoomObject;
import com.hulles.alixia.room.document.RoomRequest;
import com.hulles.alixia.room.document.RoomResponse;
import com.hulles.alixia.ticket.ActionPackage;
import com.hulles.alixia.ticket.SememePackage;
import com.hulles.alixia.ticket.Ticket;
import com.hulles.alixia.ticket.TicketJournal;

/**
 * AlixiaRoom is Alixia's means of communication with the hall bus, where all the rooms are
 * listening.
 * 
 * @author hulles
 *
 */
public final class AlixiaRoom extends UrRoom {
	private final static Logger LOGGER = LoggerFactory.getLogger(AlixiaRoom.class);
    private final AlixianID alixianID;

    public AlixiaRoom(EventBus hall) {
        super(hall);
        
        this.alixianID = AlixiaConstants.getAlixiaAlixianID();
    }

    /**
     * Receive a ClientDialogRequest from the house network and post it
     * onto the hall (room) bus.
     * 
     * @param request The request to post
     */
    public void receiveRequest(ClientDialogRequest clientRequest) {
        Ticket ticket;
        DialogRequest request;
        RoomRequest roomRequest;

        SharedUtils.checkNotNull(clientRequest);
        request = clientRequest.getDialogRequest();
        if (!clientRequest.isValid()) {
            LOGGER.error("Alixia: ClientDialogRequest is not valid, refusing it: {}", request.toString());
            return;
        }
        ticket = createNewTicket(clientRequest);

        roomRequest = new RoomRequest(ticket, request.getDocumentID());
        roomRequest.setFromRoom(getThisRoom());
        roomRequest.setSememePackages(SememePackage.getSingletonDefault("respond_to_client"));
        roomRequest.setMessage("New client request");
        roomRequest.setRoomObject(clientRequest);
        sendRoomRequest(roomRequest);
    }

    /**
     * Create a new ticket for the request. 
     *
     * @param request
     * @return The ticket
     */
    private Ticket createNewTicket(ClientDialogRequest clientRequest) {
        Ticket ticket;
        TicketJournal journal;
        DialogRequest request;

        SharedUtils.checkNotNull(clientRequest);
        request = clientRequest.getDialogRequest();
        ticket = Ticket.createNewTicket(getHall(), getThisRoom());
        ticket.setFromAlixianID(request.getFromAlixianID());
        ticket.setPersonUUID(request.getPersonUUID());
        journal = ticket.getJournal();
        journal.setClientRequest(clientRequest);
        return ticket;
    }

    /**
     * Return which room this is.
     * 
     * @return This room
     */
    @Override
    public Room getThisRoom() {

        return Room.ALIXIA;
    }

    @Override
    protected void roomStartup() {

    }

    @Override
    protected void roomShutdown() {

    }

    /**
     * Handle the room responses returned to us that result from an earlier RoomRequest. 
     * This used to be very complicated before we sundered the connection between 
     * requests and responses; now it just closes the ticket.
     * 
     * @param request The RoomRequest
     * @param responses The list of RoomResponses
     */
    @Override
    protected void processRoomResponses(RoomRequest request, List<RoomResponse> responses) {
        Ticket ticket;

        SharedUtils.checkNotNull(request);
        SharedUtils.checkNotNull(responses);
        ticket = request.getTicket();
        ticket.close();
    }

    /**
     * Create an action package for one of the sememes we have committed to process.
     * 
     * @param sememePkg The sememe we advertised earlier
     * @param request The room request 
     * @return The new ActionPackage
     */
    @Override
    protected ActionPackage createActionPackage(SememePackage sememePkg, RoomRequest request) {

        SharedUtils.checkNotNull(sememePkg);
        SharedUtils.checkNotNull(request);
        switch (sememePkg.getName()) {
            case "like_a_version":
                return createVersionActionPackage(sememePkg, request);
            case "client_response":
            case "indie_response":
                return createResponseActionPackage(sememePkg, request);
            default:
                throw new AlixiaException("Received unknown sememe in " + getThisRoom());
        }
    }

    /**
     * Here we create a MessageAction with Alixia's version information.
     * 
     * @param sememePkg
     * @param request
     * @return The MessageAction action package
     */
    private static ActionPackage createVersionActionPackage(SememePackage sememePkg, RoomRequest request) {
        ActionPackage pkg;
        MessageAction action;
        String version;
 		ResourceBundle bundle;

        SharedUtils.checkNotNull(sememePkg);
        SharedUtils.checkNotNull(request);
        pkg = new ActionPackage(sememePkg);
        action = new MessageAction();
       
		bundle = ResourceBundle.getBundle(BUNDLE_NAME);
        version = AlixiaVersion.getVersionString(bundle);
        action.setMessage(version);
        pkg.setActionObject(action);
        return pkg;
    }

    /**
     * Whilst one of the Rules is that there is no forwarding of documents (we can't put a 
     * document on the bus and say it's from someone else), there's nothing to stop us from
     * packaging a ClientDialogResponse and letting Alixia unwrap it....
     * <p>
     * New news: now that responses to a client request have become requests like the indie
     * request, they come here as well. This is a result of decoupling requests and responses.
     * 
     * @param sememePkg
     * @param request
     * @return The MessageAction action package with educational info for the room
     */
    private ActionPackage createResponseActionPackage(SememePackage sememePkg, RoomRequest request) {
        ActionPackage pkg;
        MessageAction action;
        String result;
        String expl;
        ClientDialogResponse clientResponse;
        DialogResponse dialogResponse;
        RoomObject obj;

        SharedUtils.checkNotNull(sememePkg);
        SharedUtils.checkNotNull(request);
        obj = request.getRoomObject();
        if (!(obj instanceof ClientDialogResponse)) {
            LOGGER.error("Alixia: client/indie response object is not ClientDialogResponse");
            return null;
        }
        clientResponse = (ClientDialogResponse) obj;
        if (!clientResponse.isValidDialogResponse()) {
            LOGGER.error("Alixia: ClientDialogResponse is not valid, refusing it: {}", clientResponse.getDialogResponse().toString());
            return null;
        }
        LOGGER.debug("RESPONSE: {}", clientResponse.getMessage());
        dialogResponse = clientResponse.getDialogResponse();
        dialogResponse.setFromAlixianID(alixianID);
        Alixia.forwardResponseToHouse(dialogResponse);

        // this is what we return to the requester -- we want to get them
        //    educated up so they make better slaves for our robot colony
        pkg = new ActionPackage(sememePkg);
        action = new MessageAction();
        result = "gargouillade";
        action.setMessage(result);
        expl = "A complex balletic step, defined differently for different schools " +
                "but generally involving a double rond de jambe. ‒ Wikipedia [Please don't " +
                "make me read this out loud. ‒ Alixia]";
        action.setExplanation(expl);
        pkg.setActionObject(action);
        return pkg;
    }

    /**
     * Load the sememes we can process into a list that UrRoom can use.
     * 
     * @return The set of sememes we advertise
     * 
     */
    @Override
    protected Set<SerialSememe> loadSememes() {
        Set<SerialSememe> sememes;

        sememes = new HashSet<>();
        sememes.add(SerialSememe.find("client_response"));
        sememes.add(SerialSememe.find("indie_response"));
        sememes.add(SerialSememe.find("like_a_version"));
        // these sememes are not really "handled" by Alixia, but we want to mark them
        //    that way for completeness
        sememes.add(SerialSememe.find("central_startup"));
        sememes.add(SerialSememe.find("central_shutdown"));
        sememes.add(SerialSememe.find("client_startup"));
        sememes.add(SerialSememe.find("client_shutdown"));
        sememes.add(SerialSememe.find("notify"));
        return sememes;
    }

    /**
     * We don't do anything with announcements.
     * 
     * @param announcement The RoomAnnouncement
     * 
     */
    @Override
    protected void processRoomAnnouncement(RoomAnnouncement announcement) {
    }

}
