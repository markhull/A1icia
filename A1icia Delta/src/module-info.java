/**
 * A1icia Delta
 * 
 * Station hardware
 * 
 * @author hulles
 *
 */
module com.hulles.a1icia.delta {
	exports com.hulles.a1icia.delta;

	requires transitive com.hulles.a1icia;
	requires com.hulles.a1icia.api;
	requires transitive guava;
	requires java.logging;
}