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
package com.hulles.alixia.nodeserver.v8;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eclipsesource.v8.JavaCallback;
import com.eclipsesource.v8.V8Array;
import com.eclipsesource.v8.V8Object;
import com.hulles.alixia.api.shared.SharedUtils;
import com.hulles.alixia.nodeserver.pages.TinyPages;

/**
 * Respond to an HTML request for a server page.
 * 
 * @author hulles
 */
public final class HtmlResponder implements JavaCallback {
	private final static Logger LOGGER = LoggerFactory.getLogger(HtmlResponder.class);
    private final TinyPages tinyPages;
    
	public HtmlResponder() {
	
        tinyPages = new TinyPages();
	}

    /**
     * Return an HTML page as a String to the caller.
     * 
     * @param receiver The calling program
     * @param parameters The parameters
     * @return The HTML page as a String
     */
	@Override
	public Object invoke(V8Object receiver, V8Array parameters) {
        int paramLen;
        
		SharedUtils.checkNotNull(parameters);
        paramLen = parameters.length();
        LOGGER.debug("In HtmlResponder.invoke, params len = {}", paramLen);
        return tinyPages.getWebPage();
	}
	
}
