/**
 * AlixiaQA
 * 
 * quality assurance
 * 
 * @author hulles
 *
 */
module com.hulles.alixia.qa {
	exports com.hulles.alixia.qa to com.hulles.alixia.central;

	requires com.google.common;
	requires transitive com.hulles.alixia;
	requires com.hulles.alixia.api;
	requires com.hulles.alixia.cayenne;
	requires org.slf4j;
}
