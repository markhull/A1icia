/**
 * AlixiaRomeo
 * 
 * games
 * 
 * @author hulles
 *
 */
module com.hulles.alixia.romeo {
	exports com.hulles.alixia.romeo to com.hulles.alixia.central;

	requires com.google.common;
	requires transitive com.hulles.alixia;
	requires com.hulles.alixia.api;
}