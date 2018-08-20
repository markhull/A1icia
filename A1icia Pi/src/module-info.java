/**
 * A1icia Pi
 * 
 * Raspbery Pi
 * 
 * @author hulles
 *
 */
module com.hulles.a1icia.raspi {
	exports com.hulles.a1icia.raspi;

	requires transitive com.hulles.a1icia.api;
	requires com.hulles.a1icia.cli;
	requires guava;
	requires java.logging;
	requires pi4j.core;
}