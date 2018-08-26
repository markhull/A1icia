/**
 * A1icia Foxtrot
 * 
 * Central hardware
 * 
 * @author hulles
 *
 */
module com.hulles.a1icia.foxtrot {
	exports com.hulles.a1icia.foxtrot to com.hulles.a1icia.central;

	requires transitive com.hulles.a1icia;
	requires com.hulles.a1icia.api;
	requires com.hulles.a1icia.crypto;
	requires guava;
	requires java.logging;
	requires transitive java.sql;
	requires org.mariadb.jdbc;
	// to here
	
	provides com.hulles.a1icia.room.UrRoom with com.hulles.a1icia.foxtrot.FoxtrotRoom;
}