/**
 * AlixiaCLI
 * 
 * command line interface
 * 
 * @author hulles
 *
 */
module com.hulles.alixia.cli {
	exports com.hulles.alixia.cli;

	requires com.google.common;
	requires transitive com.hulles.alixia.api;
	requires com.hulles.alixia.media;
	requires commons.cli;
    requires org.slf4j;
}