/**
 * Alixia November
 * 
 * user data
 * 
 * @author hulles
 *
 */
module com.hulles.alixia.november {
	exports com.hulles.alixia.november to com.hulles.alixia.central;

	requires transitive com.hulles.alixia;
	requires com.hulles.alixia.api;
	requires transitive com.hulles.alixia.cayenne;
	requires com.hulles.alixia.crypto;
	requires guava;
	requires java.logging;
	// to here
    requires com.hulles.alixia.media;
    requires cayenne.client;
    requires cayenne.di;
    requires cayenne.server;
	
	provides com.hulles.alixia.room.UrRoom with com.hulles.alixia.november.NovemberRoom;
}