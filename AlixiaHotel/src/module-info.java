/**
 * Alixia Hotel
 * 
 * calendar
 * 
 * @author hulles
 *
 */
module com.hulles.alixia.hotel {
	exports com.hulles.alixia.hotel to com.hulles.alixia.central;

	requires biweekly;
	requires transitive com.hulles.alixia;
	requires com.hulles.alixia.api;
	requires com.hulles.alixia.cayenne;
	requires com.hulles.alixia.media;
	requires guava;
	requires java.logging;
	requires jedis;
    // to here
    requires cayenne.client;
    requires cayenne.di;
    requires cayenne.server;
	
	provides com.hulles.alixia.room.UrRoom with com.hulles.alixia.hotel.HotelRoom;
}