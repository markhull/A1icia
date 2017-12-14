package com.hulles.a1icia.api.dialog;

import com.hulles.a1icia.api.object.A1iciaClientObject;
import com.hulles.a1icia.api.remote.A1icianID;

public interface Dialog {

	public A1iciaClientObject getClientObject();
	
	public A1icianID getFromA1icianID();
	
	public A1icianID getToA1icianID();

}
