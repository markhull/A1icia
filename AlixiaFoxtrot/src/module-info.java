/**
 * Alixia Foxtrot
 * 
 * Central hardware
 * 
 * @author hulles
 *
 */
module com.hulles.alixia.foxtrot {
	exports com.hulles.alixia.foxtrot to com.hulles.alixia.central;

	requires transitive com.hulles.alixia;
	requires com.hulles.alixia.api;
	requires com.hulles.alixia.crypto;
	requires guava;
	requires java.logging;
	requires transitive java.sql;
	requires org.mariadb.jdbc;
	// to here
    requires com.hulles.alixia.media;
	
	provides com.hulles.alixia.room.UrRoom with com.hulles.alixia.foxtrot.FoxtrotRoom;
}