/**
 * A1icia Overmind
 * 
 * thimk
 * 
 * @author hulles
 *
 */
module com.hulles.a1icia.overmind {
	exports com.hulles.a1icia.overmind;

	requires transitive com.hulles.a1icia;
	requires com.hulles.a1icia.api;
	requires com.hulles.a1icia.media;
	requires transitive guava;
	requires java.logging;
	requires jedis;
}