/**
 * Alixia Alpha
 * 
 * aardvark
 * 
 * @author hulles
 *
 */
module com.hulles.alixia.alpha {
	exports com.hulles.alixia.alpha to com.hulles.alixia.central;
	
	requires transitive com.hulles.alixia;
	requires com.hulles.alixia.api;
    requires com.hulles.alixia.media;
	requires guava;
    requires java.logging;
    // to here
    requires cayenne.client;
    requires cayenne.di;
    requires cayenne.server;
    
	provides com.hulles.alixia.room.UrRoom with com.hulles.alixia.alpha.AlphaRoom;	
}