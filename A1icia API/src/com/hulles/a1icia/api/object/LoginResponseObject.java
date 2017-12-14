package com.hulles.a1icia.api.object;

import com.hulles.a1icia.api.shared.SerialPerson;
import com.hulles.a1icia.api.shared.SerialUUID;
import com.hulles.a1icia.api.shared.SharedUtils;

public class LoginResponseObject implements A1iciaClientObject {
	private static final long serialVersionUID = 4715884810552504623L;
	private SerialUUID<SerialPerson> personUUID;
	private String userName;
	
	public SerialUUID<SerialPerson> getPersonUUID() {
		
		return personUUID;
	}

	public void setPersonUUID(SerialUUID<SerialPerson> personUUID) {
		
		SharedUtils.nullsOkay(personUUID);
		this.personUUID = personUUID;
	}
	
	public String getUserName() {
		
		return userName;
	}

	public void setUserName(String userName) {
		
		SharedUtils.nullsOkay(userName);
		this.userName = userName;
	}

	@Override
	public ClientObjectType getClientObjectType() {

		return ClientObjectType.LOGIN_RESPONSE;
	}

	@Override
	public boolean isValid() {

		// personUUID can be null if failed login
		return true;
	}

}
