/**
 * AlixiaIndia
 * 
 * random responses
 * 
 * @author hulles
 *
 */
module com.hulles.alixia.india {
	exports com.hulles.alixia.india to com.hulles.alixia.central;

	requires com.google.common;
	requires transitive com.hulles.alixia;
	requires com.hulles.alixia.api;
    requires com.hulles.fortune;
    requires org.slf4j;
	// to here
}