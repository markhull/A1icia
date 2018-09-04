/**
 * Fortuna
 * 
 * quotes database
 * 
 * @author hulles
 *
 */
module com.hulles.fortuna {
	exports com.hulles.fortuna.cayenne;
	exports com.hulles.fortuna;
//	exports com.hulles.fortuna.cayenne.auto;

	requires transitive cayenne.server;
	requires java.logging;
	requires javax.json;
	// to here
    requires cayenne.client;
    requires cayenne.di;
}