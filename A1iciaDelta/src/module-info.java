/**
 * A1icia Delta
 * 
 * Station hardware
 * 
 * @author hulles
 *
 */
module com.hulles.a1icia.delta {
	exports com.hulles.a1icia.delta;

	requires transitive com.hulles.a1icia;
	requires com.hulles.a1icia.api;
	requires guava;
	requires java.logging;
	
	provides com.hulles.a1icia.room.UrRoom with com.hulles.a1icia.delta.DeltaRoom;
}