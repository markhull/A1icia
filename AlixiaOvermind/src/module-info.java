/**
 * Alixia Overmind
 * 
 * thimk
 * 
 * @author hulles
 *
 */
module com.hulles.alixia.overmind {
	exports com.hulles.alixia.overmind to com.hulles.alixia.central;

	requires transitive com.hulles.alixia;
	requires com.hulles.alixia.api;
	requires com.hulles.alixia.media;
	requires guava;
	requires java.logging;
	requires jedis;
	// to here
	
	provides com.hulles.alixia.room.UrRoom with com.hulles.alixia.overmind.OvermindRoom;
}