/**
 * A1icia Kilo
 * 
 * weather
 * 
 * @author hulles
 *
 */
module com.hulles.a1icia.kilo {

	requires transitive com.hulles.a1icia;
	requires com.hulles.a1icia.api;
	requires com.hulles.a1icia.cayenne;
	requires com.hulles.a1icia.crypto;
	requires guava;
	requires java.logging;
	requires javax.json;
	// to here
	
	provides com.hulles.a1icia.room.UrRoom with com.hulles.a1icia.kilo.KiloRoom;
}