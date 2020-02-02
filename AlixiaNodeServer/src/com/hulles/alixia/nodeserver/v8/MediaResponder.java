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

import java.util.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eclipsesource.v8.JavaCallback;
import com.eclipsesource.v8.V8Array;
import com.eclipsesource.v8.V8Object;
import com.hulles.alixia.api.remote.MediaServer;
import com.hulles.alixia.api.shared.SharedUtils;

/**
 * MediaResponder's job is to serve a media file previously saved with MediaServer
 * and return it as a Base64 string, presumably for an AJAX request.
 * 
 * @author hulles
 */
public final class MediaResponder implements JavaCallback {
	private final static Logger LOGGER = LoggerFactory.getLogger(MediaResponder.class);
    private final Base64.Encoder encoder;
    
	public MediaResponder() {
		
        encoder = Base64.getMimeEncoder();
	}

    /**
     * Return the object saved in MediaServer as a Base64 string.
     * 
     * @param receiver Calling program
     * @param parameters Parameters
     * @return A Base64 encoding of the media object
     */
	@Override
	public Object invoke(V8Object receiver, V8Array parameters) {
		String key;
		int paramLen;
        byte[] bytes;
        String result;
                
		SharedUtils.checkNotNull(parameters);
        paramLen = parameters.length();
        LOGGER.debug("In MediaResponder.invoke, params len = {}", paramLen);
		if (paramLen != 1) {
			LOGGER.error("MediaResponder: parameters error");
			return "Parameters error";
		}
		key = parameters.getString(0);
        LOGGER.debug("MediaResponder: key = {}", key);
        // TODO sanitize and validate the string
        bytes = MediaServer.getMediaBytes(key);
        result = encoder.encodeToString(bytes);
        return result;
	}

}
