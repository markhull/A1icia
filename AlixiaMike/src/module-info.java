/**
 * AlixiaMike
 * 
 * multimedia
 * 
 * @author hulles
 *
 */
module com.hulles.alixia.mike {
	exports com.hulles.alixia.mike to com.hulles.alixia.central;

	requires com.google.common;
	requires transitive com.hulles.alixia;
	requires com.hulles.alixia.api;
	requires com.hulles.alixia.cayenne;
	requires com.hulles.alixia.media;
	requires java.desktop;
	requires java.json;
	requires jedis;
    requires org.slf4j;
}