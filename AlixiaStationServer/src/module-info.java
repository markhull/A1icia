/**
 * AlixiaStationServer
 * 
 * house that serves AlixiaCentral requests
 * 
 * @author hulles
 *
 */
module com.hulles.alixia.stationserver {
	exports com.hulles.alixia.stationserver to com.hulles.alixia.central;

	requires transitive com.google.common;
	requires transitive com.hulles.alixia;
	requires com.hulles.alixia.api;
	requires com.hulles.alixia.media;
	requires jedis;
    requires org.slf4j;
	// to here
}