/**
 * Alixia Charlie
 * 
 * natural language processing
 * 
 * @author hulles
 *
 */
module com.hulles.alixia.charlie {
	exports com.hulles.alixia.charlie to com.hulles.alixia.central;

	requires transitive com.hulles.alixia;
	requires com.hulles.alixia.api;
	requires transitive com.hulles.alixia.cayenne;
	requires guava;
	requires java.logging;
	requires jedis;
	requires transitive org.apache.opennlp.tools;
    // to here
    requires com.hulles.alixia.media;
    requires cayenne.client;
    requires cayenne.di;
    requires cayenne.server;
	
	provides com.hulles.alixia.room.UrRoom with com.hulles.alixia.charlie.CharlieRoom;
}