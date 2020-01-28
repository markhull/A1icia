/**
 * Alixia Hotel
 * 
 * calendar
 * 
 * @author hulles
 *
 */
module com.hulles.alixia.hotel {
	exports com.hulles.alixia.hotel to com.hulles.alixia.central;

	requires biweekly;
	requires com.google.common;
	requires transitive com.hulles.alixia;
	requires com.hulles.alixia.api;
	requires com.hulles.alixia.cayenne;
	requires com.hulles.alixia.media;
	requires jedis;
    requires org.slf4j;
}