package com.hulles.a1icia.api.object;

import com.hulles.a1icia.api.shared.SharedUtils;
import com.hulles.a1icia.media.Language;

public class ChangeLanguageObject implements A1iciaClientObject {
	private static final long serialVersionUID = 6987368151783107511L;
	private Language language;
	
	public Language getNewLanguage() {
		
		return language;
	}
	
	public void setNewLanguage(Language lang) {
		
		SharedUtils.checkNotNull(lang);
		this.language = lang;
	}

	@Override
	public ClientObjectType getClientObjectType() {

		return ClientObjectType.CHANGE_LANGUAGE;
	}

	@Override
	public boolean isValid() {

		return (language != null);
	}
}
