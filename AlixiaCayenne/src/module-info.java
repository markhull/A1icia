/**
 * AlixiaCayenne
 * 
 * Apache Cayenne database access
 * 
 * @author hulles
 *
 */
module com.hulles.alixia.cayenne {
	exports com.hulles.alixia.cayenne;
	exports com.hulles.alixia.cayenne.auto;

	requires cayenne.di;
	requires transitive cayenne.server;
	requires transitive com.hulles.alixia.api;
	requires com.hulles.alixia.crypto;
	requires transitive com.hulles.alixia.media;
	requires java.desktop;
	requires transitive java.sql;
	requires org.mariadb.jdbc;
    requires org.slf4j;
	// to here
}