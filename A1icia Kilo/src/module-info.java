/**
 * A1icia Kilo
 * 
 * weather
 * 
 * @author hulles
 *
 */
module com.hulles.a1icia.kilo {
	exports com.hulles.a1icia.kilo;

	requires transitive com.hulles.a1icia;
	requires com.hulles.a1icia.api;
	requires com.hulles.a1icia.cayenne;
	requires com.hulles.a1icia.crypto;
	requires transitive guava;
	requires java.logging;
	requires javax.json;
}