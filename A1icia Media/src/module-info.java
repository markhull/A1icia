/**
 * A1icia Media
 * 
 * multimedia
 * 
 * @author hulles
 *
 */
module com.hulles.a1icia.media {
	exports com.hulles.a1icia.media.text;
	exports com.hulles.a1icia.media;
	exports com.hulles.a1icia.media.image;
	exports com.hulles.a1icia.media.audio;

	requires transitive java.desktop;
	requires java.logging;
}