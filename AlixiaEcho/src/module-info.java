/**
 * Alixia Echo
 * 
 * word2vec
 * 
 * @author hulles
 *
 */
module com.hulles.alixia.echo {
	exports com.hulles.alixia.echo to com.hulles.alixia.central;

	requires transitive com.hulles.alixia;
	requires com.hulles.alixia.api;
	requires java.logging;
	requires guava;
	// to here
    requires com.hulles.alixia.media;
 	
	provides com.hulles.alixia.room.UrRoom with com.hulles.alixia.echo.EchoRoom;
}