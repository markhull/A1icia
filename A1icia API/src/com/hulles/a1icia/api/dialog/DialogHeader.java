package com.hulles.a1icia.api.dialog;

import java.io.Serializable;

import com.hulles.a1icia.api.remote.A1icianID;
import com.hulles.a1icia.api.shared.SharedUtils;

/**
 * The concept here is that the DialogHeader contains only enough publicly-available 
 * information to determine if the rest of the dialog is intended for the recipient
 * or not.
 *  
 * @author hulles
 *
 */
public class DialogHeader implements Serializable {
	private static final long serialVersionUID = 1127382284278136055L;
	private A1icianID toA1icianID;
	
	public A1icianID getToA1icianID() {
		
		return toA1icianID;
	}

	public void setToA1icianID(A1icianID a1icianID) {
		
		SharedUtils.checkNotNull(a1icianID);
		this.toA1icianID = a1icianID;
	}

}
