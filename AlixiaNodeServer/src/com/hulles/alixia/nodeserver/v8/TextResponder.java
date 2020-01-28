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

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eclipsesource.v8.JavaCallback;
import com.eclipsesource.v8.V8Array;
import com.eclipsesource.v8.V8Object;
import com.hulles.alixia.api.shared.SerialProng;
import com.hulles.alixia.api.shared.SharedUtils;
import com.hulles.alixia.nodeserver.NodeConsole;

/**
 * Respond to a client request with a simple text string.
 * 
 * @author hulles
 */
public final class TextResponder implements JavaCallback {
	private final static Logger LOGGER = LoggerFactory.getLogger(TextResponder.class);
	private final Registrar registrar;
    
	public TextResponder(Registrar registrar) {
		
        SharedUtils.checkNotNull(registrar);
        this.registrar = registrar;
	}

    /**
     * TextResponder can be legitimately invoked with 1 or 2 arguments.
     * If 2 arguments, from a POST e.g., we expect a session ID (prong string) 
     * and a text message to transmit to Alixia. If 1 argument, from a GET e.g., 
     * we expect just a session ID (prong string). In the latter case we just check
     * if there are incoming messages for the client.
     * 
     * @param receiver The calling program
     * @param parameters The parameter list
     * @return A String containing the accumulated messages and explanations since
     * the last check
     */
	@Override
	public Object invoke(V8Object receiver, V8Array parameters) {
		String prongStr;
        String text;
		SerialProng prong;
		int paramLen;
        List<String> msgs;
        List<String> expls;
        StringBuilder sb;
        String msgIn;
        
		SharedUtils.checkNotNull(parameters);
        paramLen = parameters.length();
        LOGGER.debug("In TextResponder.invoke, params len = {}", paramLen);
		if (paramLen < 1 || paramLen > 2) {
			LOGGER.error("TextResponder: parameters error");
			return "Parameters error";
		}
		prongStr = parameters.getString(0);
        LOGGER.debug("Prong string = {}", prongStr);
        // TODO sanitize and validate the string
		prong = new SerialProng(prongStr);
        LOGGER.debug("Prong is {}", prong);
		try (NodeConsole miniConsole = registrar.getConsole(prong)) {
            LOGGER.debug("MiniConsole is {}", miniConsole == null ? "null" : "instantiated");
            if (miniConsole == null) {
    			LOGGER.error("TextResponder: no console error");
                return "Parameters error";
            }
            if (paramLen == 2) {
                LOGGER.debug("Text in is {}", parameters.get(1));
                msgIn = parameters.getString(1);
                LOGGER.debug("Message text is {}", msgIn);
                if ((msgIn != null) && (!msgIn.isEmpty())) {
                    // TODO sanitize the msg
                    miniConsole.sendText(msgIn);
                }
            }
            sb = new StringBuilder();
            msgs = miniConsole.getMessages();
            if (!msgs.isEmpty()) {
                for (String msg : msgs) {
                    sb.append("<p>");
                    sb.append("Alixia: ");
                    sb.append(msg);
                    sb.append("</p>");
                }
            }
            expls = miniConsole.getExplanations();
            if (!expls.isEmpty()) {
                for (String expl : expls) {
                    sb.append("<p>");
                    sb.append("Alixia: ");
                    sb.append(expl);
                    sb.append("</p>");
                }
            }
		}
        text = sb.toString();
        LOGGER.debug("Text from queue is {}", text);
        return text;
	}

}
