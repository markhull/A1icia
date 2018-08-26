/**
 * A1icia Juliet
 * 
 * non-factoid responses
 * 
 * @author hulles
 *
 */
module com.hulles.a1icia.juliet {
	exports com.hulles.a1icia.juliet to com.hulles.a1icia.central;

	requires transitive com.hulles.a1icia;
	requires com.hulles.a1icia.api;
	requires com.hulles.a1icia.cayenne;
	requires guava;
	// to here
	
	provides com.hulles.a1icia.room.UrRoom with com.hulles.a1icia.juliet.JulietRoom;
}