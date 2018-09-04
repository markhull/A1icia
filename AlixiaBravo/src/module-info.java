/**
 * Alixia Bravo
 * 
 * inception engine
 * 
 * @author hulles
 *
 */
module com.hulles.alixia.bravo {
	exports com.hulles.alixia.bravo to com.hulles.alixia.central;

	requires transitive com.hulles.alixia;
	requires com.hulles.alixia.api;
	requires com.hulles.alixia.media;
	requires guava;
	requires java.desktop;
	requires libtensorflow;
    // to here
	
	provides com.hulles.alixia.room.UrRoom with com.hulles.alixia.bravo.BravoRoom;
}