/**
 * A1icia Swing Console
 * 
 * @author hulles
 *
 */
module com.hulles.a1icia.swingconsole {
	exports com.hulles.a1icia.swingconsole;

	requires transitive com.hulles.a1icia.api;
	requires com.hulles.a1icia.media;
	requires guava;
	requires java.desktop;
	requires java.logging;
	requires jedis;
}