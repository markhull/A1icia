/**
 * AlixiaNovember
 * 
 * user data
 * 
 * @author hulles
 *
 */
module com.hulles.alixia.november {
	exports com.hulles.alixia.november to com.hulles.alixia.central;

	requires com.google.common;
	requires transitive com.hulles.alixia;
	requires com.hulles.alixia.api;
	requires transitive com.hulles.alixia.cayenne;
	requires com.hulles.alixia.crypto;
    requires org.slf4j;
}