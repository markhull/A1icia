/**
 * A1icia Oscar
 * 
 * constants
 * 
 * @author hulles
 *
 */
module com.hulles.a1icia.oscar {
	exports com.hulles.a1icia.oscar;

	requires transitive com.hulles.a1icia;
	requires com.hulles.a1icia.api;
	requires com.hulles.a1icia.cayenne;
	requires com.hulles.a1icia.media;
	requires transitive guava;
	requires java.logging;
}