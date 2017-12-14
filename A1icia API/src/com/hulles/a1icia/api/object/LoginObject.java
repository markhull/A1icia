package com.hulles.a1icia.api.object;

import com.hulles.a1icia.api.shared.SharedUtils;

public class LoginObject implements A1iciaClientObject {
	private static final long serialVersionUID = 5508127665810763843L;
	private String userName;
	private String password;
		
	public String getUserName() {
		
		return userName;
	}

	public void setUserName(String userName) {
		
		SharedUtils.checkNotNull(userName);
		this.userName = userName;
	}

	public String getPassword() {
		
		return password;
	}

	public void setPassword(String password) {
		
		SharedUtils.checkNotNull(password);
		this.password = password;
	}

	@Override
	public ClientObjectType getClientObjectType() {
		
		return ClientObjectType.LOGIN;
	}

	@Override
	public boolean isValid() {

		// user name and/or password can legitimately be null
		//    if the person is logging out
		return true;
	}

}
