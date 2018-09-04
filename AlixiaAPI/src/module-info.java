/**
 * Alixia API
 * 
 * api
 * 
 * @author hulles
 *
 */
module com.hulles.alixia.api {
	exports com.hulles.alixia.api;
	exports com.hulles.alixia.api.jebus;
	exports com.hulles.alixia.api.object;
	exports com.hulles.alixia.api.remote;
	exports com.hulles.alixia.api.dialog;
	exports com.hulles.alixia.api.shared;
    exports com.hulles.alixia.api.tools;

	requires transitive com.hulles.alixia.media;
	requires guava;
	requires java.desktop;
	requires java.logging;
	requires jedis;
    // to here
}