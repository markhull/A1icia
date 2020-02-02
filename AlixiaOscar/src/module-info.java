/**
 * AlixiaOscar
 * 
 * constants
 * 
 * @author hulles
 *
 */
module com.hulles.alixia.oscar {
	exports com.hulles.alixia.oscar to com.hulles.alixia.central;

	requires com.google.common;
	requires transitive com.hulles.alixia;
	requires com.hulles.alixia.api;
	requires com.hulles.alixia.cayenne;
	requires com.hulles.alixia.media;
    requires org.slf4j;
	// to here
}