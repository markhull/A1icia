/**
 * A1icia Prong
 * 
 * web validator
 * 
 * @author hulles
 *
 */
module com.hulles.a1icia.prong {
	exports com.hulles.a1icia.prong.client;
	exports com.hulles.a1icia.prong.server;
	exports com.hulles.a1icia.prong.shared;

	requires commons.pool2;
	requires gwt.user;
	requires java.logging;
	requires jedis;
}