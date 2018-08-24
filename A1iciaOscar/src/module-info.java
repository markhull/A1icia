/**
 * A1icia Oscar
 * 
 * constants
 * 
 * @author hulles
 *
 */
module com.hulles.a1icia.oscar {

	requires transitive com.hulles.a1icia;
	requires com.hulles.a1icia.api;
	requires com.hulles.a1icia.cayenne;
	requires com.hulles.a1icia.media;
	requires guava;
	requires java.logging;
	// to here
	
	provides com.hulles.a1icia.room.UrRoom with com.hulles.a1icia.oscar.OscarRoom;
}