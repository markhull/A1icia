/**
 * Alixia Juliet
 * 
 * non-factoid responses
 * 
 * @author hulles
 *
 */
module com.hulles.alixia.juliet {
	exports com.hulles.alixia.juliet to com.hulles.alixia.central;

	requires transitive com.hulles.alixia;
	requires com.hulles.alixia.api;
	requires com.hulles.alixia.cayenne;
	requires guava;
	// to here
    requires com.hulles.alixia.media;
    requires cayenne.client;
    requires cayenne.di;
    requires cayenne.server;
	
	provides com.hulles.alixia.room.UrRoom with com.hulles.alixia.juliet.JulietRoom;
}