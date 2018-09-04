/**
 * Alixia CLI
 * 
 * command line interface
 * 
 * @author hulles
 *
 */
module com.hulles.alixia.cli {
	exports com.hulles.alixia.cli;

	requires transitive com.hulles.alixia.api;
	requires com.hulles.alixia.media;
	requires guava;
    requires java.logging;
    // to here
    requires jedis;
    requires commons.pool;
}