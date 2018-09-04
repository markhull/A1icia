/**
 * Alixia Central
 * 
 * Run the central server
 * 
 * @author hulles
 *
 */
module com.hulles.alixia.central {
	exports com.hulles.alixia.central;

	requires com.hulles.alixia;
    requires com.hulles.alixia.api;
	requires com.hulles.alixia.alpha;
	requires com.hulles.alixia.bravo;
	requires com.hulles.alixia.charlie;
	requires com.hulles.alixia.delta;
	requires com.hulles.alixia.echo;
	requires com.hulles.alixia.foxtrot;
	requires com.hulles.alixia.golf;
	requires com.hulles.alixia.hotel;
	requires com.hulles.alixia.india;
	requires com.hulles.alixia.juliet;
	requires com.hulles.alixia.kilo;
	requires com.hulles.alixia.lima;
	requires com.hulles.alixia.mike;
	requires com.hulles.alixia.november;
	requires com.hulles.alixia.oscar;
	requires com.hulles.alixia.papa;
	requires com.hulles.alixia.quebec;
	requires com.hulles.alixia.romeo;
	requires com.hulles.alixia.sierra;  
	requires com.hulles.alixia.overmind;
	requires com.hulles.alixia.tracker;
    // to here
//	requires guava;
//    requires opennlp.tools;
//    requires libtensorflow;
//    requires javax.json;
//    requires com.hulles.fortuna
    
//    requires biweekly;
    requires com.hulles.alixia.media;
    requires cayenne.client;
    requires cayenne.di;
    requires cayenne.server;
    requires com.hulles.alixia.cayenne;
    requires com.hulles.alixia.crypto;
    requires commons.pool;
    requires jedis;
    requires commons.text;
    requires org.mariadb.jdbc;
    requires slf4j.api;
    requires slf4j.simple;
}