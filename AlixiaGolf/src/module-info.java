/**
 * Alixia Golf
 * 
 * WikiData
 * 
 * @author hulles
 *
 */
module com.hulles.alixia.golf {
	exports com.hulles.alixia.golf to com.hulles.alixia.central;

	requires transitive com.hulles.alixia;
	requires com.hulles.alixia.api;
	requires guava;
	requires java.logging;
	requires javax.json;
	requires jedis;
	// to here
    requires com.hulles.alixia.media;
	
	provides com.hulles.alixia.room.UrRoom with com.hulles.alixia.golf.GolfRoom;
}