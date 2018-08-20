/**
 * A1icia November
 * 
 * user data
 * 
 * @author hulles
 *
 */
module com.hulles.a1icia.november {
	exports com.hulles.a1icia.november;

	requires transitive com.hulles.a1icia;
	requires com.hulles.a1icia.api;
	requires transitive com.hulles.a1icia.cayenne;
	requires com.hulles.a1icia.crypto;
	requires transitive guava;
	requires java.logging;
}