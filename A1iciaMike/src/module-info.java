/**
 * A1icia Mike
 * 
 * multimedia
 * 
 * @author hulles
 *
 */
module com.hulles.a1icia.mike {

	requires transitive com.hulles.a1icia;
	requires com.hulles.a1icia.api;
	requires com.hulles.a1icia.cayenne;
	requires com.hulles.a1icia.media;
	requires guava;
	requires java.desktop;
	requires java.logging;
	requires jedis;
	requires mp3agic;
	// to here
	
	provides com.hulles.a1icia.room.UrRoom with com.hulles.a1icia.mike.MikeRoom;
}