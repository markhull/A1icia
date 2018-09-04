/**
 * Alixia
 * 
 * The big kahuna
 * 
 * @author hulles
 *
 */
module com.hulles.alixia {
	exports com.hulles.alixia;
	exports com.hulles.alixia.ticket;
	exports com.hulles.alixia.room;
	exports com.hulles.alixia.room.document;
	exports com.hulles.alixia.tools;
	exports com.hulles.alixia.house;
    requires com.hulles.alixia.api;
	requires com.hulles.alixia.cayenne;
	requires com.hulles.alixia.crypto;
	requires com.hulles.alixia.media;
	requires commons.text;
	requires java.desktop;
	requires java.logging;
	requires jedis;
    requires javax.json;
    // to here
    requires guava;
    requires cayenne.client;
    requires cayenne.di;
    requires cayenne.server;
    	
	uses com.hulles.alixia.room.UrRoom;
}