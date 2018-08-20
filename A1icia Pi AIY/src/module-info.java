/**
 * A1icia Pi AIY
 * 
 * Raspberry Pi with Google AIY hardware
 * 
 * @author hulles
 *
 */
module com.hulles.a1icia.raspi.aiy {
	exports com.hulles.a1icia.raspi.aiy;

	requires transitive com.hulles.a1icia.api;
	requires com.hulles.a1icia.cli;
	requires com.hulles.a1icia.media;
	requires guava;
	requires java.logging;
	requires pi4j.core;
}