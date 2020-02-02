/**
 * AlixiaPapa
 * 
 * Wolfram|Alpha
 * 
 * @author hulles
 *
 */
module com.hulles.alixia.papa {
	exports com.hulles.alixia.papa to com.hulles.alixia.central;

	requires com.google.common;
	requires transitive com.hulles.alixia;
	requires com.hulles.alixia.api;
	requires com.hulles.alixia.crypto;
	requires com.hulles.alixia.media;
	requires java.desktop;
	requires transitive java.xml;
    requires org.slf4j;
	// to here
}