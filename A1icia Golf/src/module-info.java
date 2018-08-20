/**
 * A1icia Golf
 * 
 * WikiData
 * 
 * @author hulles
 *
 */
module com.hulles.a1icia.golf {
	exports com.hulles.a1icia.golf;

	requires transitive com.hulles.a1icia;
	requires com.hulles.a1icia.api;
	requires guava;
	requires java.logging;
	requires javax.json;
	requires jedis;
	
	provides com.hulles.a1icia.room.UrRoom with com.hulles.a1icia.golf.GolfRoom;
}