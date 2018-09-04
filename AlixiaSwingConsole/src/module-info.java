/**
 * Alixia Swing Console
 * 
 * @author hulles
 *
 */
module com.hulles.alixia.swingconsole {
	exports com.hulles.alixia.swingconsole;

	requires transitive com.hulles.alixia.api;
	requires com.hulles.alixia.media;
	requires guava;
	requires java.desktop;
	requires java.logging;
	requires jedis;
    // to here
}