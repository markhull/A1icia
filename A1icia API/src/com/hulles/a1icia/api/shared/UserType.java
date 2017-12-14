package com.hulles.a1icia.api.shared;


/**
 *
 * @author hulles
 */
public enum UserType {
    NORMAL(1, "Normal"),
    ADMIN(2, "Administrator"),
    SYSADMIN(3, "System Admin");
    private final Integer storeID;
    private final String displayName;

    private UserType(Integer storeID, String displayName) {
    	
        this.storeID = storeID;
        this.displayName = displayName;
    }

    public Integer getStoreID() {
    	
        return storeID;
    }

    public String getDisplayName() {
    	
        return displayName;
    }

    public static UserType findUserType(Integer type) {
    	
		SharedUtils.checkNotNull(type);
    	for (UserType a : UserType.values()) {
    		if (a.storeID == type) {
    			return a;
    		}
        }
    	throw new IllegalArgumentException("Bad user type = " + type.toString());
    }
}
