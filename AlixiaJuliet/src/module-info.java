/**
 * Alixia Juliet
 * 
 * non-factoid responses
 * 
 * @author hulles
 *
 */
module com.hulles.alixia.juliet {
	exports com.hulles.alixia.juliet to com.hulles.alixia.central;

	requires com.google.common;
	requires transitive com.hulles.alixia;
	requires com.hulles.alixia.api;
	requires com.hulles.alixia.cayenne;
	// to here
}