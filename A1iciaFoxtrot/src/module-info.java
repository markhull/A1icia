/**
 * A1icia Foxtrot
 * 
 * Central hardware
 * 
 * @author hulles
 *
 */
module com.hulles.a1icia.foxtrot {
	exports com.hulles.a1icia.foxtrot.dummy;
	exports com.hulles.a1icia.foxtrot.monitor;
	exports com.hulles.a1icia.foxtrot;

	requires transitive com.hulles.a1icia;
	requires com.hulles.a1icia.api;
	requires com.hulles.a1icia.crypto;
	requires guava;
	requires java.logging;
	requires transitive java.sql;
	requires org.mariadb.jdbc;
	
	provides com.hulles.a1icia.room.UrRoom with com.hulles.a1icia.foxtrot.FoxtrotRoom;
}