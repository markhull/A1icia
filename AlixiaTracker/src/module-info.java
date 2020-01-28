/**
 * AlixiaTracker
 * 
 * track hall activity
 * 
 * @author hulles
 *
 */
module com.hulles.alixia.tracker {
	exports com.hulles.alixia.graphviz;
	exports com.hulles.alixia.tracker;

	requires com.google.common;
	requires transitive com.hulles.alixia;
	requires com.hulles.alixia.api;
	requires jedis;
    requires org.slf4j;
	// to here
}