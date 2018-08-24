/**
 * A1icia Hotel
 * 
 * calendar
 * 
 * @author hulles
 *
 */
module com.hulles.a1icia.hotel {

	requires biweekly;
	requires transitive com.hulles.a1icia;
	requires com.hulles.a1icia.api;
	requires com.hulles.a1icia.cayenne;
	requires com.hulles.a1icia.media;
	requires guava;
	requires java.logging;
	requires jedis;
    // to here
	
	provides com.hulles.a1icia.room.UrRoom with com.hulles.a1icia.hotel.HotelRoom;
}