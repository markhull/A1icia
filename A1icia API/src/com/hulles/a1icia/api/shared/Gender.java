/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.hulles.a1icia.api.shared;


/**
 *
 * @author hulles
 */
public enum Gender {
    FEMALE('F', "Female"),
    MALE('M', "Male");
    private final Character storeName;
    private final String displayName;

    private Gender(Character storeName, String displayName) {
    	
        this.storeName = storeName;
        this.displayName = displayName;
    }

    public Character getStoreName() {
    	
        return storeName;
    }

    public String getDisplayName() {
    	
        return displayName;
    }

    public static Gender findGender(Character code) {
    	
		SharedUtils.checkNotNull(code);
        if (MALE.storeName.equals(code)) {
            return MALE;
        } else if (FEMALE.storeName.equals(code)) {
            return FEMALE;
        } else {
        	throw new IllegalArgumentException("Bad gender code = " + code.toString());
        }
    }
}
