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

import java.io.StringWriter;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eclipsesource.v8.JavaCallback;
import com.eclipsesource.v8.V8Array;
import com.eclipsesource.v8.V8Object;
import com.hulles.alixia.api.shared.SerialProng;
import com.hulles.alixia.api.shared.SharedUtils;
import com.hulles.alixia.nodeserver.NodeConsole;

public final class JsonResponder implements JavaCallback {
	private final static Logger LOGGER = LoggerFactory.getLogger(JsonResponder.class);
	private final Registrar registrar;
    
	public JsonResponder(Registrar registrar) {
		
        SharedUtils.checkNotNull(registrar);
        this.registrar = registrar;
	}

    /**
     * JsonResponder can only be invoked with 2 arguments, vs. {@link TextResponder}.
     * 
     * @param receiver
     * @param parameters
     * @return A stringified JSON object
     */
	@Override
	public Object invoke(V8Object receiver, V8Array parameters) {
		String prongStr;
		SerialProng prong;
		int paramLen;
        String msgIn;
        JsonObject jsonObj;
        String messageQueue;
        
		SharedUtils.checkNotNull(parameters);
        paramLen = parameters.length();
        LOGGER.debug("JsonResponder: invoke");
		if (paramLen != 2) {
			LOGGER.error("JsonResponder: paramenters error");
            jsonObj = getErrorJSON("Parameters error");
            return jsonToString(jsonObj);
		}
		prongStr = parameters.getString(0);
        LOGGER.debug("JsonResponder: Prong string = {}", prongStr);
        // TODO sanitize and validate the string
		prong = new SerialProng(prongStr);
		try (NodeConsole miniConsole = registrar.getConsole(prong)) {
            LOGGER.debug("JsonResponder: MiniConsole is {}", miniConsole == null ? "null" : "instantiated");
            if (miniConsole == null) {
    			LOGGER.error("JsonResponder: no console error");
                jsonObj = getErrorJSON("Console error");
                return jsonToString(jsonObj);
            }
            msgIn = parameters.getString(1);
            LOGGER.debug("JsonResponder: Message text is {}", msgIn);
            if ((msgIn != null) && (!msgIn.isEmpty())) {
                // TODO sanitize the msg
                miniConsole.sendText(msgIn);
            }
            jsonObj = miniConsole.getJSON(prong);
		}
        messageQueue = jsonToString(jsonObj);
        LOGGER.debug("JsonResponder: Message queue is {}", messageQueue);
        return messageQueue;
	}

    private static String jsonToString(JsonObject jsonObj) {
        StringWriter writer;
        String result;
        
        SharedUtils.checkNotNull(jsonObj);
        writer = new StringWriter();
        try (JsonWriter jsonWriter = Json.createWriter(writer)) {
        	jsonWriter.writeObject(jsonObj);
        }
        result = writer.toString();
        return result;
    }
    
    // FIX ME
    private static JsonObject getErrorJSON(String message) {
        JsonArrayBuilder messageBuilder;
        JsonArrayBuilder explanationBuilder;
        JsonObjectBuilder resultBuilder;

        SharedUtils.checkNotNull(message);
        resultBuilder = Json.createObjectBuilder();
        messageBuilder = Json.createArrayBuilder();
        messageBuilder.add(message);
        resultBuilder.add("messages", messageBuilder);
        
        explanationBuilder = Json.createArrayBuilder();
        resultBuilder.add("explanations", explanationBuilder);
        
        resultBuilder.add("errors", true);
        
        return resultBuilder.build();
    }

}
