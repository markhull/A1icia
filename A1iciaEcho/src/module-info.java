/**
 * A1icia Echo
 * 
 * word2vec
 * 
 * @author hulles
 *
 */
module com.hulles.a1icia.echo {
	exports com.hulles.a1icia.echo;
	exports com.hulles.a1icia.echo.w2v;

	requires transitive com.hulles.a1icia;
	requires com.hulles.a1icia.api;
	requires java.logging;
	requires guava;
	
	provides com.hulles.a1icia.room.UrRoom with com.hulles.a1icia.echo.EchoRoom;
}