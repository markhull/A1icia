/**
 * Alixia Quebec
 * 
 * semantic analysis
 * 
 * @author hulles
 *
 */
module com.hulles.alixia.quebec {
	exports com.hulles.alixia.quebec to com.hulles.alixia.central;

	requires transitive com.hulles.alixia;
	requires com.hulles.alixia.api;
	requires guava;
	// to here
    requires com.hulles.alixia.media;
	
	provides com.hulles.alixia.room.UrRoom with com.hulles.alixia.quebec.QuebecRoom;
}