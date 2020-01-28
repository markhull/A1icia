/**
 * AlixiaFoxtrot
 * 
 * Central hardware
 * 
 * @author hulles
 *
 */
module com.hulles.alixia.foxtrot {
	exports com.hulles.alixia.foxtrot to com.hulles.alixia.central;
	exports com.hulles.alixia.foxtrot.monitor;

	requires com.google.common;
	requires transitive com.hulles.alixia;
	requires com.hulles.alixia.api;
	requires com.hulles.alixia.crypto;
	requires transitive java.sql;
	requires org.mariadb.jdbc;
    requires org.slf4j;
	// to here
}