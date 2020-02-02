package com.hulles.alixia.qa;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.SetMultimap;
import com.hulles.alixia.api.shared.AlixiaException;
import com.hulles.alixia.api.shared.SerialSememe;
import com.hulles.alixia.api.shared.SharedUtils;
import com.hulles.alixia.room.Room;
import com.hulles.alixia.room.document.RoomRequest;
import com.hulles.alixia.ticket.SememePackage;

class Requester {
    private final SetMultimap<SerialSememe, Room> sememeRooms;
	
	Requester() {
		
        sememeRooms = MultimapBuilder.hashKeys().enumSetValues(Room.class).build();
	}
	
	void updateRoomRequest(RoomRequest request, SerialSememe sememe, Set<Room> rooms) {
		SememePackage sememePackage;
		List<SememePackage> sememePackages;
		
		SharedUtils.checkNotNull(request);
		SharedUtils.checkNotNull(sememe);
		SharedUtils.checkNotNull(rooms);
		sememePackage = SememePackage.getNewPackage();
		sememePackage.setSememe(sememe);
		sememePackage.setSememeObject(null);
		sememePackage.setSentencePackage(null);
		sememePackage.setConfidence(100);
		if (!sememePackage.isValid()) {
			throw new AlixiaException("Requester: created invalid sememe package");
		}
		sememePackages = Collections.singletonList(sememePackage);
        request.setSememePackages(sememePackages);
        for (Room room : rooms) {
        	sememeRooms.put(sememe, room);
        }
	}
	
}
