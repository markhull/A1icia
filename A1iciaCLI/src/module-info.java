/**
 * A1icia CLI
 * 
 * command line interface
 * 
 * @author hulles
 *
 */
module com.hulles.a1icia.cli {
	exports com.hulles.a1icia.cli;

	requires transitive com.hulles.a1icia.api;
	requires com.hulles.a1icia.media;
	requires guava;
    requires java.logging;
    // to here
}