/**
 * A1icia API
 * 
 * api
 * 
 * @author hulles
 *
 */
module com.hulles.a1icia.api {
	exports com.hulles.a1icia.api;
	exports com.hulles.a1icia.api.jebus;
	exports com.hulles.a1icia.api.object;
	exports com.hulles.a1icia.api.remote;
	exports com.hulles.a1icia.api.dialog;
	exports com.hulles.a1icia.api.shared;

	requires transitive com.hulles.a1icia.media;
	requires guava;
	requires java.desktop;
	requires java.logging;
	requires jedis;
    requires commons.pool;
}