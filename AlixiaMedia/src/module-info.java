/**
 * AlixiaMedia
 * 
 * multimedia
 * 
 * @author hulles
 *
 */
module com.hulles.alixia.media {
	exports com.hulles.alixia.media;
	exports com.hulles.alixia.media.image;
	exports com.hulles.alixia.media.text;
	exports com.hulles.alixia.media.audio;

	requires transitive java.desktop;
    requires org.slf4j;
	// to here
}