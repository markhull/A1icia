/**
 * Alixia
 * 
 * The big kahuna
 * 
 * @author hulles
 *
 */
module com.hulles.alixia {
	exports com.hulles.alixia;
	exports com.hulles.alixia.tools;
	exports com.hulles.alixia.room.document;
	exports com.hulles.alixia.ticket;
	exports com.hulles.alixia.house;
	exports com.hulles.alixia.room;

	requires transitive com.google.common;
	requires transitive com.hulles.alixia.api;
	requires com.hulles.alixia.cayenne;
	requires com.hulles.alixia.crypto;
	requires transitive com.hulles.alixia.media;
	requires java.desktop;
	requires java.json;
	requires jedis;
	requires org.apache.commons.text;
    requires org.slf4j;
}