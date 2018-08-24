/**
 * A1icia Papa
 * 
 * Wolfram|Alpha
 * 
 * @author hulles
 *
 */
module com.hulles.a1icia.papa {

	requires transitive com.hulles.a1icia;
	requires com.hulles.a1icia.api;
	requires com.hulles.a1icia.crypto;
	requires com.hulles.a1icia.media;
	requires guava;
	requires java.desktop;
	requires java.xml;
	
	provides com.hulles.a1icia.room.UrRoom with com.hulles.a1icia.papa.PapaRoom;
}