package com.hulles.a1icia.charlie;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.eventbus.EventBus;
import com.hulles.a1icia.base.A1iciaException;
import com.hulles.a1icia.cayenne.Spark;
import com.hulles.a1icia.room.Room;
import com.hulles.a1icia.room.UrRoom;
import com.hulles.a1icia.room.document.NLPAnalysis;
import com.hulles.a1icia.room.document.RoomAnnouncement;
import com.hulles.a1icia.room.document.RoomRequest;
import com.hulles.a1icia.room.document.RoomResponse;
import com.hulles.a1icia.ticket.ActionPackage;
import com.hulles.a1icia.ticket.SparkPackage;
import com.hulles.a1icia.tools.A1iciaUtils;

/**
 * Charlie Room is a busy place. It uses the Apache OpenNLP library to dissect input data from CLIENT,
 * turning them into sentences, lemmata, etc. It works really well; kudos to the OpenNLP team.
 * 
 * @author hulles
 *
 */
public final class CharlieRoom extends UrRoom {
	CharlieDocumentProcessor processor;

	public CharlieRoom(EventBus bus) {
		super(bus);
	}

	@Override
	protected ActionPackage createActionPackage(SparkPackage sparkPkg, RoomRequest request) {

		switch (sparkPkg.getName()) {
			case "nlp_analysis":
				return createNlpActionPackage(sparkPkg, request);
			default:
				throw new A1iciaException("Received unknown spark in " + getThisRoom());
		}
	}

	private ActionPackage createNlpActionPackage(SparkPackage sparkPkg, RoomRequest request) {
		NLPAnalysis analysis;
		ActionPackage pkg;
		
		A1iciaUtils.checkNotNull(sparkPkg);
		A1iciaUtils.checkNotNull(request);
		pkg = new ActionPackage(sparkPkg);
		analysis = processor.processDocument(request);
		pkg.setActionObject(analysis);
		postProcessAnalysis(analysis);
		return pkg;
	}
	
	private static void postProcessAnalysis(NLPAnalysis analysis) {
		Thread postProc;
		
		postProc = new Thread() {
			@Override
			public void run() {
				CharlieDocumentProcessor.postProcessAnalysis(analysis);
			}
		};
		postProc.start();
	}
	
	@Override
	public Room getThisRoom() {

		return Room.CHARLIE;
	}

	@Override
	public void processRoomResponses(RoomRequest request, List<RoomResponse> responses) {
		throw new A1iciaException("Response not implemented in " + 
				getThisRoom().getDisplayName());
	}

	@Override
	protected void roomStartup() {
//		Thread loader;
//		
//		loader = new Thread() {
//			@Override
//			public void run() {
				processor = new CharlieDocumentProcessor();
//			}
//		};
//		loader.start();
	}
	
	@Override
	protected void roomShutdown() {
		
	}

	@Override
	protected Set<Spark> loadSparks() {
		Set<Spark> sparks;
		
		sparks = new HashSet<>();
		sparks.add(Spark.find("nlp_analysis"));
		return sparks;
	}

	@Override
	protected void processRoomAnnouncement(RoomAnnouncement announcement) {
	}

}
