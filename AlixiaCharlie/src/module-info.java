/**
 * 
 */
/**
 * @author hulles
 *
 */
module com.hulles.alixia.charlie {
	exports com.hulles.alixia.charlie to com.hulles.alixia.central;

	exports com.hulles.alixia.charlie.doccat;
	exports com.hulles.alixia.charlie.language;
	exports com.hulles.alixia.charlie.parse;
	exports com.hulles.alixia.charlie.pos;
	exports com.hulles.alixia.charlie.ner;

	requires com.google.common;
	requires transitive com.hulles.alixia;
	requires com.hulles.alixia.api;
	requires transitive com.hulles.alixia.cayenne;
	requires jedis;
	requires transitive org.apache.opennlp.tools;
    requires org.slf4j;
	// to here
}