/**
 * AlixiaLima
 * 
 * history
 * 
 * @author hulles
 *
 */
module com.hulles.alixia.lima {
	exports com.hulles.alixia.lima to com.hulles.alixia.central;

	requires com.google.common;
	requires transitive com.hulles.alixia;
	requires com.hulles.alixia.api;
	requires transitive com.hulles.alixia.cayenne;
    requires org.slf4j;
	// to here
}