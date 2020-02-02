/**
 * AlixiaBravo
 * 
 * inception engine
 * 
 * @author hulles
 *
 */
module com.hulles.alixia.bravo {
	exports com.hulles.alixia.bravo to com.hulles.alixia.central;

	requires com.google.common;
	requires transitive com.hulles.alixia;
	requires com.hulles.alixia.api;
	requires com.hulles.alixia.media;
	requires java.desktop;
	requires libtensorflow;
    requires org.slf4j;
	// to here
}