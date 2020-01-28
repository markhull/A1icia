/**
 * AlixiaOvermind
 * 
 * thimk
 * 
 * @author hulles
 *
 */
module com.hulles.alixia.overmind {
	exports com.hulles.alixia.overmind to com.hulles.alixia.central;

	requires com.google.common;
	requires transitive com.hulles.alixia;
	requires com.hulles.alixia.api;
	requires com.hulles.alixia.media;
	requires jedis;
	requires com.hulles.alixia.cayenne;
    requires org.slf4j;
	// to here
}