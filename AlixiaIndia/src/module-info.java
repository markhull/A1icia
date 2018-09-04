/**
 * Alixia India
 * 
 * random responses
 * 
 * @author hulles
 *
 */
module com.hulles.alixia.india {
	exports com.hulles.alixia.india to com.hulles.alixia.central;

	requires com.hulles.fortuna;
	requires transitive com.hulles.alixia;
	requires com.hulles.alixia.api;
	requires guava;
	requires java.logging;
    requires com.hulles.alixia.media;
    // to here
    requires cayenne.client;
    requires cayenne.di;
    requires cayenne.server;
	
	provides com.hulles.alixia.room.UrRoom with com.hulles.alixia.india.IndiaRoom;
}