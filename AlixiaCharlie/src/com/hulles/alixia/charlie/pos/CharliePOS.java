/*******************************************************************************
 * Copyright © 2017, 2018 Hulles Industries LLC
 * All rights reserved
 *  
 * This file is part of Alixia.
 *  
 * Alixia is free software: you can redistribute it and/or modify
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
package com.hulles.alixia.charlie.pos;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hulles.alixia.api.shared.AlixiaException;
import com.hulles.alixia.api.shared.ApplicationKeys;
import com.hulles.alixia.api.shared.ApplicationKeys.ApplicationKey;
import com.hulles.alixia.api.shared.SharedUtils;

import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;

final public class CharliePOS {
	private final static Logger LOGGER = LoggerFactory.getLogger(CharliePOS.class);
	private final POSTaggerME posTagger;

	public CharliePOS() {
		URL posModelURL;
		POSModel posModel;
		ApplicationKeys appKeys;
        String openNLPPath;
        
        appKeys = ApplicationKeys.getInstance();
        openNLPPath = appKeys.getKey(ApplicationKey.OPENNLPPATH);
        try {
            posModelURL = new URL(ApplicationKeys.toURL(openNLPPath + "/en-pos-maxent.bin"));
        } catch (MalformedURLException ex) {
            throw new AlixiaException("Can't create POS URL", ex);
        }
		
		try {
			posModel = new POSModel(posModelURL);
		}
		catch (IOException e) {
			throw new AlixiaException("Can't load POS model(s)", e);
		}
		posTagger = new POSTaggerME(posModel);
	}
	
	public String[] generatePOS(String[] tokenizedInput) {
		String[] posStrings;
		
		SharedUtils.checkNotNull(tokenizedInput);
		posStrings = posTagger.tag(tokenizedInput);
		for (String s : posStrings) {
			LOGGER.debug("POS: {}", s);
		}
		return posStrings;
	}
}
