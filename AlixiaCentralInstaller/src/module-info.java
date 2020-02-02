/**
 * AliciaCentralInstaller
 *
 * Alicia Central standalone installation utility
 * 
 * @author hulles
 *
 */
module com.hulles.alixia.config.central {
	exports com.hulles.alixia.config.central;

	requires com.hulles.alixia.api;
	requires com.hulles.alixia.crypto;
	requires java.json;
	requires jedis;
}