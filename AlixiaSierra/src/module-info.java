/**
 * AlixiaSierra
 * 
 * IoT
 * 
 * @author hulles
 *
 */
module com.hulles.alixia.sierra {
	exports com.hulles.alixia.sierra to com.hulles.alixia.central;

	requires com.google.common;
	requires transitive com.hulles.alixia;
	requires com.hulles.alixia.api;
    requires org.slf4j;
	// to here
}