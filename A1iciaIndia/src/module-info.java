/**
 * A1icia India
 * 
 * random responses
 * 
 * @author hulles
 *
 */
module com.hulles.a1icia.india {
	exports com.hulles.a1icia.india;

	requires com.hulles.fortuna;
	requires transitive com.hulles.a1icia;
	requires com.hulles.a1icia.api;
	requires guava;
	requires java.logging;
	
	provides com.hulles.a1icia.room.UrRoom with com.hulles.a1icia.india.IndiaRoom;
}