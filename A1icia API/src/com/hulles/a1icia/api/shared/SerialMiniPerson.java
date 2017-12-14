package com.hulles.a1icia.api.shared;


/**
 * 
 * @author hulles
 */
final public class SerialMiniPerson extends SerialEntity {
	private static final long serialVersionUID = 1732012700735206589L;
	private String userName;
	private String fullNameLF; // lastname, firstname
	private String fullNameFL; // firstname lastname
	private UserType userType;
	private transient SerialUUID<SerialPerson>  uuid;

	public SerialMiniPerson() {
		// need no-arg constructor
	}

	public SerialUUID<SerialPerson> getUUID() {
		
        return uuid;
    }

    public void setUUID(SerialUUID<SerialPerson> uuid) {
    	
		SharedUtils.checkNotNull(uuid);
        this.uuid = uuid;
    }

	public String getUserName() {
		
		return userName;
	}

	public void setUserName(String userName) {
		
		SharedUtils.checkNotNull(userName);
		this.userName = userName;
	}

	public UserType getUserType() {
		
		return this.userType;
	}

	public void setUserType(UserType userType) {
		
		SharedUtils.checkNotNull(userType);
		this.userType = userType;
	}

	public String getFullNameLF() {
		
		return this.fullNameLF;
	}

	public String getFullNameFL() {
		
		return this.fullNameFL;
	}

	public void setFullNameFL(String name) {
		
		SharedUtils.checkNotNull(name);
		this.fullNameFL = name;
	}

	public void setFullNameLF(String name) {
		
		SharedUtils.checkNotNull(name);
		this.fullNameLF = name;
	}

	@Override
	public SerialUUID<SerialPerson> getKey() {

		return uuid;
	}

}
