/**
 * A1icia Alpha
 * 
 * aardvark
 * 
 * @author hulles
 *
 */
module com.hulles.a1icia.alpha {
	exports com.hulles.a1icia.alpha to com.hulles.a1icia.central;
	
	requires transitive com.hulles.a1icia;
	requires com.hulles.a1icia.api;
	requires guava;
    requires java.logging;
    // to here
    
	provides com.hulles.a1icia.room.UrRoom with com.hulles.a1icia.alpha.AlphaRoom;	
}