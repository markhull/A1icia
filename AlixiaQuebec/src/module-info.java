/**
 * AlixiaQuebec
 * 
 * semantic analysis
 * 
 * @author hulles
 *
 */
module com.hulles.alixia.quebec {
	exports com.hulles.alixia.quebec to com.hulles.alixia.central;

	requires com.google.common;
	requires transitive com.hulles.alixia;
	requires com.hulles.alixia.api;
	// to here
}