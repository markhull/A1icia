/**
 * Alixia Delta
 * 
 * Station hardware
 * 
 * @author hulles
 *
 */
module com.hulles.alixia.delta {
	exports com.hulles.alixia.delta to com.hulles.alixia.central;

	requires transitive com.hulles.alixia;
	requires com.hulles.alixia.api;
	requires guava;
	requires java.logging;
	// to here
    requires com.hulles.alixia.media;
	
	provides com.hulles.alixia.room.UrRoom with com.hulles.alixia.delta.DeltaRoom;
}