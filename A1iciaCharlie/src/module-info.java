/**
 * A1icia Charlie
 * 
 * natural language processing
 * 
 * @author hulles
 *
 */
module com.hulles.a1icia.charlie {
//	exports com.hulles.a1icia.charlie;
//	exports com.hulles.a1icia.charlie.parse;
//	exports com.hulles.a1icia.charlie.ner;
//	exports com.hulles.a1icia.charlie.language;
//	exports com.hulles.a1icia.charlie.pos;
//	exports com.hulles.a1icia.charlie.doccat;

	requires transitive com.hulles.a1icia;
	requires com.hulles.a1icia.api;
	requires transitive com.hulles.a1icia.cayenne;
	requires guava;
	requires java.logging;
	requires jedis;
	requires transitive org.apache.opennlp.tools;
    // to here
	
	provides com.hulles.a1icia.room.UrRoom with com.hulles.a1icia.charlie.CharlieRoom;
}