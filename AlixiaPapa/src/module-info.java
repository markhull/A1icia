/**
 * Alixia Papa
 * 
 * Wolfram|Alpha
 * 
 * @author hulles
 *
 */
module com.hulles.alixia.papa {
	exports com.hulles.alixia.papa to com.hulles.alixia.central;

	requires transitive com.hulles.alixia;
	requires com.hulles.alixia.api;
	requires com.hulles.alixia.crypto;
	requires com.hulles.alixia.media;
	requires guava;
	requires java.desktop;
	requires java.xml;
	
	provides com.hulles.alixia.room.UrRoom with com.hulles.alixia.papa.PapaRoom;
}