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
package com.hulles.a1icia.bravo;

import java.awt.Image;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.eventbus.EventBus;
import com.hulles.a1icia.api.dialog.DialogRequest;
import com.hulles.a1icia.api.object.A1iciaClientObject;
import com.hulles.a1icia.api.object.A1iciaClientObject.ClientObjectType;
import com.hulles.a1icia.api.shared.SerialSememe;
import com.hulles.a1icia.base.A1iciaException;
import com.hulles.a1icia.house.ClientDialogRequest;
import com.hulles.a1icia.media.MediaUtils;
import com.hulles.a1icia.room.Room;
import com.hulles.a1icia.room.UrRoom;
import com.hulles.a1icia.room.document.MessageAction;
import com.hulles.a1icia.room.document.RoomAnnouncement;
import com.hulles.a1icia.room.document.RoomObject;
import com.hulles.a1icia.room.document.RoomObject.RoomObjectType;
import com.hulles.a1icia.room.document.RoomRequest;
import com.hulles.a1icia.room.document.RoomResponse;
import com.hulles.a1icia.room.document.SememeAnalysis;
import com.hulles.a1icia.ticket.ActionPackage;
import com.hulles.a1icia.ticket.SentencePackage;
import com.hulles.a1icia.ticket.SememePackage;
import com.hulles.a1icia.ticket.Ticket;
import com.hulles.a1icia.ticket.TicketJournal;
import com.hulles.a1icia.tools.A1iciaUtils;

/**
 * Bravo Room is an initial implementation of TensorFlow. Right now, it "just" accepts a JPEG
 *     image and tries to classify it with the Inception Engine. If there is no image payload, we
 *     return a null response.
 *     
 *     By the way, this is the first vision-based room.
 * 
 * 	   P.S. The caller must have -Djava.library.path="/home/hulles/DATA/Repository/Jars/jni" in its
 *         runtime configuration. TODO standardize location or at least put it into ApplicationKeys
 *         
 *     N.B. The first time I ran this I didn't quite know what to expect, so I fed it a picture of
 *         my Siamese cat Mimi. I thought if I was lucky the Inception Engine would classify her 
 *         as an animal, and just maybe a cat. It came back and said "Siamese Cat". After recovering
 *         from the shock I just started laughing: in this business you don't often get pleasant 
 *         surprises the first time you run new code.
 *         
 * @author hulles
 *
 */
public final class BravoRoom extends UrRoom {
	private static final int LIKELIHOOD_CUTOFF = 75;
	
	public BravoRoom(EventBus bus) {
		super(bus);
	}

	@Override
	protected ActionPackage createActionPackage(SememePackage sememePkg, RoomRequest request) {

		switch (sememePkg.getName()) {
			case "sememe_analysis":
				return createAnalysisActionPackage(sememePkg, request);
			case "classify_image":
				return createClassifyImageActionPackage(sememePkg, request);
			default:
				throw new A1iciaException("Received unknown sememe in " + getThisRoom());
		}
	}
	
	/**
	 * Create an analysis based on what we know -- if there's an image from the client, it
	 * seems likely that there might be a "classify image" or equivalent among the sentences.
	 * 
	 * @param sememePkg
	 * @param request
	 * @return A SememeAnalysis package
	 */
	private static ActionPackage createAnalysisActionPackage(SememePackage sememePkg, RoomRequest request) {
		ActionPackage actionPkg;
		SememeAnalysis analysis;
		Ticket ticket;
		TicketJournal journal;
		List<SememePackage> sememePackages;
		List<SentencePackage> sentencePackages;
		SememePackage classifyPkg;
		ClientDialogRequest clientObject;
		DialogRequest dialogRequest;
		A1iciaClientObject requestObject;
		int classifyRequestLikelihood;
		int confidence;
//		List<String> context;
		
		A1iciaUtils.checkNotNull(sememePkg);
		A1iciaUtils.checkNotNull(request);
		ticket = request.getTicket();
		journal = ticket.getJournal();
		clientObject = journal.getClientRequest();
		dialogRequest = clientObject.getDialogRequest();
		requestObject = dialogRequest.getClientObject();
		if (requestObject == null || requestObject.getClientObjectType() != ClientObjectType.IMAGEBYTES) {
			// sorry, can't help you
			return null;
		}
		actionPkg = new ActionPackage(sememePkg);
		sememePackages = new ArrayList<>();
		sentencePackages = journal.getSentencePackages();
//		context = journal.getContext();
		analysis = new SememeAnalysis();
		for (SentencePackage sp : sentencePackages) {
//			classifyRequestLikelihood = SentenceAnalyzer.isImageClassification(context, 
//					sp.getAnalysis());
			classifyRequestLikelihood = 75;
			if (classifyRequestLikelihood > LIKELIHOOD_CUTOFF) {
				classifyPkg = SememePackage.getDefaultPackage("classify_image");
				classifyPkg.setSentencePackage(sp);
				confidence = classifyRequestLikelihood + 10; // it did have an image, after all
				if (confidence > 100) {
					confidence = 100;
				}
				classifyPkg.setConfidence(confidence);
				if (!classifyPkg.isValid()) {
					throw new A1iciaException("BravoRoom: created invalid sememe package");
				}
				sememePackages.add(classifyPkg);
			}
		}
		analysis.setSememePackages(sememePackages);
		actionPkg.setActionObject(analysis);
		return actionPkg;
	}

	private static ActionPackage createClassifyImageActionPackage(SememePackage sememePkg, 
			RoomRequest request) {
		RoomObject object;
		Image image;
		byte[] jpegBytes = null;
		MessageAction analysis;
		String classification = null;
		ActionPackage pkg;

		A1iciaUtils.checkNotNull(sememePkg);
		A1iciaUtils.checkNotNull(request);
		pkg = new ActionPackage(sememePkg);
		object = request.getRoomObject();
		if (object.getRoomObjectType() != RoomObjectType.IMAGEINPUT) {
			A1iciaUtils.error("Bad object in BravoRoom");
			return null;
		}
		image = (Image) object;
		try {
			jpegBytes = MediaUtils.imageToByteArray(image, "jpeg");
		} catch (IOException e) {
			throw new A1iciaException("BravoRoom: can't convert image to bytes", e);
		}
		classification = InceptionLabeller.analyzeImage(jpegBytes);
		analysis = new MessageAction();
		analysis.setMessage(classification);
		pkg.setActionObject(analysis);
		return pkg;
	}
	
	@Override
	public Room getThisRoom() {

		return Room.BRAVO;
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
		sememes.add(SerialSememe.find("sememe_analysis"));
		sememes.add(SerialSememe.find("classify_image"));
		return sememes;
	}

	@Override
	protected void processRoomAnnouncement(RoomAnnouncement announcement) {
	}

}
