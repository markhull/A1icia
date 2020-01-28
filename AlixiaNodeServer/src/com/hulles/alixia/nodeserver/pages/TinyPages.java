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
package com.hulles.alixia.nodeserver.pages;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.google.common.io.CharStreams;
import com.hulles.alixia.api.shared.AlixiaException;
import com.hulles.alixia.api.shared.SharedUtils;

public class TinyPages {
//	private final static Logger LOGGER = Logger.getLogger("AlixiaNodeServer.TinyPages");
//	private final static Level LOGLEVEL = AlixiaConstants.getAlixiaLogLevel();
	
	public String getWebPage() {
		String page;
		
		page = getResourceAsString("com/hulles/alixia/nodeserver/pages/TinyPage.html");
		return page;
	}
	
	private String getResourceAsString(String resourcePath) {
		String text = null;
		Module module;
		
		SharedUtils.checkNotNull(resourcePath);
        module = this.getClass().getModule();
	    try (InputStream in = module.getResourceAsStream(resourcePath)) {
			if (in == null) {
		    	throw new AlixiaException("TinyPages: resource input stream is null");
			}
			try (InputStreamReader inr = new InputStreamReader(in)) {
				text = CharStreams.toString(inr);
			}
		} catch (IOException e) {
	    	throw new AlixiaException("TinyPages: can't read resource");
		}
        return text;
	}

}
