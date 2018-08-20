/**
 * A1icia Hotel
 * 
 * calendar
 * 
 * @author hulles
 *
 */
module com.hulles.a1icia.hotel {
	exports com.hulles.a1icia.hotel.task;
	exports com.hulles.a1icia.hotel;

	requires biweekly;
	requires transitive com.hulles.a1icia;
	requires com.hulles.a1icia.api;
	requires com.hulles.a1icia.cayenne;
	requires com.hulles.a1icia.media;
	requires transitive guava;
	requires java.logging;
	requires jedis;
}