/*******************************************************************************
 * Copyright © 2017, 2018 Hulles Industries LLC
 * All rights reserved
 *  
 * This file is part of A1icia.
 *  
 * A1icia is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *    
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *  
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * SPDX-License-Identifer: GPL-3.0-or-later
 *******************************************************************************/
package com.hulles.a1icia.api.object;

import com.hulles.a1icia.api.shared.SharedUtils;
import com.hulles.a1icia.media.Language;

/**
 * This object changes languages passed between A1icia Central and remote station.
 * 
 * @author hulles
 *
 */
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