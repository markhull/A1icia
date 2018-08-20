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
	requires transitive guava;
	requires java.logging;
	requires javax.json;
	requires jedis;
}