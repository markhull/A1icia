/**
 * AlixiaSwingConsole
 * 
 * A console written with Java Swing
 * 
 * @author hulles
 *
 */
module com.hulles.alixia.swingconsole {
	exports com.hulles.alixia.swingconsole;

	requires com.google.common;
	requires transitive com.hulles.alixia.api;
	requires com.hulles.alixia.media;
	requires commons.cli;
	requires java.desktop;
	requires jedis;
    requires org.slf4j;
}