/**
 * A1icia Overmind
 * 
 * thimk
 * 
 * @author hulles
 *
 */
module com.hulles.a1icia.overmind {
	exports com.hulles.a1icia.overmind to com.hulles.a1icia.central;

	requires transitive com.hulles.a1icia;
	requires com.hulles.a1icia.api;
	requires com.hulles.a1icia.media;
	requires guava;
	requires java.logging;
	requires jedis;
	// to here
	
	provides com.hulles.a1icia.room.UrRoom with com.hulles.a1icia.overmind.OvermindRoom;
}