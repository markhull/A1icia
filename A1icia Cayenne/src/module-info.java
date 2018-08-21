/**
 * A1icia Cayenne
 * 
 * Apache Cayenne database access
 * 
 * @author hulles
 *
 */
module com.hulles.a1icia.cayenne {
	exports com.hulles.a1icia.cayenne;
	exports com.hulles.a1icia.cayenne.auto;

	requires transitive cayenne.di;
	requires transitive cayenne.server;
	requires transitive com.hulles.a1icia.api;
	requires com.hulles.a1icia.crypto;
	requires transitive com.hulles.a1icia.media;
	requires java.desktop;
	requires transitive java.sql;
	requires org.mariadb.jdbc;
    requires org.slf4j;
    requires cayenne.client;
}