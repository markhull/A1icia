/**
 * Alixia Prong
 * 
 * proprietary serialization security
 * 
 * @author hulles
 *
 */
module com.hulles.alixia.prong {
	exports com.hulles.alixia.prong.shared;

	requires transitive com.hulles.alixia.api;
    requires org.slf4j;
}