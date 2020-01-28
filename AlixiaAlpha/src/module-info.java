/**
 * AlixiaAlpha
 * 
 * aardvark
 * 
 * @author hulles
 *
 */
module com.hulles.alixia.alpha {
	exports com.hulles.alixia.alpha to com.hulles.alixia.central;

	requires com.google.common;
	requires transitive com.hulles.alixia;
	requires com.hulles.alixia.api;
	requires com.hulles.alixia.media;
    requires org.slf4j;
	// to here
}