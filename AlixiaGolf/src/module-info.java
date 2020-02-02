/**
 * AlixiaGolf
 * 
 * WikiData
 * 
 * @author hulles
 *
 */
module com.hulles.alixia.golf {
	exports com.hulles.alixia.golf to com.hulles.alixia.central;

	requires com.google.common;
	requires transitive com.hulles.alixia;
	requires com.hulles.alixia.api;
	requires java.json;
	requires jedis;
    requires org.slf4j;
	// to here
}