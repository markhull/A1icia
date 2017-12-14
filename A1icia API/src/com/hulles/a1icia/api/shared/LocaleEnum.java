package com.hulles.a1icia.api.shared;


/**
 *
 * @author hulles
 */
public enum LocaleEnum {
    ENGLISH("en"),
    SPANISH("es");
    private final String storeName;

    private LocaleEnum(String storeName) {
    	
        this.storeName = storeName;
     }

    public String getStoreName() {
    	
        return storeName;
    }
    
    public static LocaleEnum findLocale(String name) {
    	
		SharedUtils.checkNotNull(name);
        if (ENGLISH.storeName.equals(name)) {
            return ENGLISH;
        } else if (SPANISH.storeName.equals(name)) {
            return SPANISH;
        } else {
        	throw new IllegalArgumentException("Bad locale = " + name);
        }
    }
}
