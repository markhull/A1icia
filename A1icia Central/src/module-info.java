/**
 * A1icia Central
 * 
 * central processor
 * 
 * @author hulles
 *
 */
module com.hulles.a1icia {
	exports com.hulles.a1icia;
	exports com.hulles.a1icia.jebus;
	exports com.hulles.a1icia.ticket;
	exports com.hulles.a1icia.room;
	exports com.hulles.a1icia.base;
	exports com.hulles.a1icia.room.document;
	exports com.hulles.a1icia.tools;
	exports com.hulles.a1icia.house;

	requires transitive com.hulles.a1icia.api;
	requires com.hulles.a1icia.cayenne;
	requires com.hulles.a1icia.crypto;
	requires com.hulles.a1icia.media;
	requires commons.pool2;
	requires commons.text;
	requires transitive guava;
	requires java.desktop;
	requires java.logging;
	requires jedis;
}