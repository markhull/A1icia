package com.hulles.a1icia.api.object;

import java.io.Serializable;

public interface A1iciaClientObject extends Serializable {

	public ClientObjectType getClientObjectType();
	
	public boolean isValid();
	
	public enum ClientObjectType {
		LOGIN,
		LOGIN_RESPONSE,
		AUDIOBYTES,
		VIDEOBYTES,
		IMAGEBYTES,
		CHANGE_LANGUAGE
	}
}
