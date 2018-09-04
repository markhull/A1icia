/**
 * Alixia Kilo
 * 
 * weather
 * 
 * @author hulles
 *
 */
module com.hulles.alixia.kilo {
	exports com.hulles.alixia.kilo to com.hulles.alixia.central;

	requires transitive com.hulles.alixia;
	requires com.hulles.alixia.api;
	requires com.hulles.alixia.cayenne;
	requires com.hulles.alixia.crypto;
	requires guava;
	requires java.logging;
	requires javax.json;
	// to here
    requires com.hulles.alixia.media;
    requires cayenne.client;
    requires cayenne.di;
    requires cayenne.server;
	
	provides com.hulles.alixia.room.UrRoom with com.hulles.alixia.kilo.KiloRoom;
}