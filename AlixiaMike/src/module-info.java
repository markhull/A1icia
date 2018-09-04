/**
 * Alixia Mike
 * 
 * multimedia
 * 
 * @author hulles
 *
 */
module com.hulles.alixia.mike {
	exports com.hulles.alixia.mike to com.hulles.alixia.central;

	requires transitive com.hulles.alixia;
	requires com.hulles.alixia.api;
	requires com.hulles.alixia.cayenne;
	requires com.hulles.alixia.media;
	requires guava;
	requires java.desktop;
	requires java.logging;
	requires jedis;
    requires javax.json;
    // to here
    requires cayenne.client;
    requires cayenne.di;
    requires cayenne.server;
	
	provides com.hulles.alixia.room.UrRoom with com.hulles.alixia.mike.MikeRoom;
}