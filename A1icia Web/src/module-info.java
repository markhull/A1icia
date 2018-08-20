/**
 * A1icia Web
 * 
 * GWT remote
 * 
 * @author hulles
 *
 */
module com.hulles.a1icia.webx {
	exports com.hulles.a1icia.webx.client.services;
	exports com.hulles.a1icia.webx.client;
	exports com.hulles.a1icia.webx.shared;
	exports com.hulles.a1icia.webx.client.content;
	exports com.hulles.a1icia.webx.server;

	requires com.hulles.a1icia.prong;
	requires com.hulles.a1icia.api;
	requires com.hulles.a1icia.media;
	requires guava;
	requires gwt.user;
	requires java.desktop;
	requires java.logging;
	requires jedis;
}