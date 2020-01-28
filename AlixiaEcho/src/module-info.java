/**
 * AlixiaEcho
 * 
 * word2vec
 * 
 * @author hulles
 *
 */
module com.hulles.alixia.echo {
	exports com.hulles.alixia.echo to com.hulles.alixia.central;

	requires com.google.common;
	requires transitive com.hulles.alixia;
	requires com.hulles.alixia.api;
    requires org.slf4j;
}