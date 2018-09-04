/**
 * Alixia Tracker
 * 
 * track hall activity
 * 
 * @author hulles
 *
 */
module com.hulles.alixia.tracker {
	exports com.hulles.alixia.tracker to guava, com.hulles.alixia.central;

	requires transitive com.hulles.alixia;
	requires com.hulles.alixia.api;
	requires guava;
	requires java.logging;
	requires jedis;
	// to here
    requires com.hulles.alixia.media;
	
	provides com.hulles.alixia.room.UrRoom with com.hulles.alixia.tracker.TrackerRoom;
}