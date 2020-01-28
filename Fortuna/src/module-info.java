/**
 * 
 */
/**
 * @author hulles
 *
 */
module com.hulles.fortune {
	exports com.hulles.fortune;
	exports com.hulles.fortune.cayenne;
	exports com.hulles.fortune.cayenne.auto;

	requires transitive cayenne.server;
	requires java.json;
	requires java.sql;
    requires org.slf4j;
}