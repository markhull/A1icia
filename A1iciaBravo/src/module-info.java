/**
 * A1icia Bravo
 * 
 * inception engine
 * 
 * @author hulles
 *
 */
module com.hulles.a1icia.bravo {
	exports com.hulles.a1icia.bravo to com.hulles.a1icia.central;

	requires transitive com.hulles.a1icia;
	requires com.hulles.a1icia.api;
	requires com.hulles.a1icia.media;
	requires guava;
	requires java.desktop;
	requires libtensorflow;
    // to here
	
	provides com.hulles.a1icia.room.UrRoom with com.hulles.a1icia.bravo.BravoRoom;
}