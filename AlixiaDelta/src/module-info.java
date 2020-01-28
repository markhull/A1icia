/**
 * AlixiaDelta
 * 
 * station hardware
 * 
 * @author hulles
 *
 */
module com.hulles.alixia.delta {
	exports com.hulles.alixia.delta to com.hulles.alixia.central;

	requires com.google.common;
	requires transitive com.hulles.alixia;
	requires transitive com.hulles.alixia.api;
    requires org.slf4j;
 	// to here
}