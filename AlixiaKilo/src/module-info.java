/**
 * AlixiaKilo
 * 
 * weather
 * 
 * @author hulles
 *
 */
module com.hulles.alixia.kilo {
	exports com.hulles.alixia.kilo to com.hulles.alixia.central;

	requires com.google.common;
	requires transitive com.hulles.alixia;
	requires com.hulles.alixia.api;
	requires com.hulles.alixia.cayenne;
	requires com.hulles.alixia.crypto;
	requires java.json;
    requires org.slf4j;
	// to here
}