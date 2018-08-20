/**
 * A1icia Sierra
 * 
 * IoT
 * 
 * @author hulles
 *
 */
module com.hulles.a1icia.sierra {
	exports com.hulles.a1icia.sierra;

	requires transitive com.hulles.a1icia;
	requires com.hulles.a1icia.api;
	requires guava;
	requires java.logging;
	
	provides com.hulles.a1icia.room.UrRoom with com.hulles.a1icia.sierra.SierraRoom;
}