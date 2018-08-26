/**
 * A1icia Delta
 * 
 * Station hardware
 * 
 * @author hulles
 *
 */
module com.hulles.a1icia.delta {
	exports com.hulles.a1icia.delta to com.hulles.a1icia.central;

	requires transitive com.hulles.a1icia;
	requires com.hulles.a1icia.api;
	requires guava;
	requires java.logging;
	// to here
	
	provides com.hulles.a1icia.room.UrRoom with com.hulles.a1icia.delta.DeltaRoom;
}