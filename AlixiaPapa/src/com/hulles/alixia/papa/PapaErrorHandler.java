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
package com.hulles.alixia.papa;

import java.io.PrintWriter;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.hulles.alixia.api.shared.SharedUtils;

public class PapaErrorHandler implements ErrorHandler {
    private final PrintWriter out;

    PapaErrorHandler(PrintWriter out) {
    	
        this.out = out;
    }

    private static String getParseExceptionInfo(SAXParseException spe) {
    	String systemId;
    	StringBuilder sb;
    	
    	SharedUtils.checkNotNull(spe);
    	sb = new StringBuilder();
        systemId = spe.getSystemId();
        if (systemId == null) {
            systemId = "null";
        }
        sb.append("URI = ");
        sb.append(systemId);
        sb.append(", Line = ");
        sb.append(spe.getLineNumber());
        sb.append(", Column = ");
        sb.append(spe.getColumnNumber());
        sb.append(": ");
        sb.append(spe.getMessage());
        return sb.toString();
    }

    @Override
	public void warning(SAXParseException spe) {
    	
    	SharedUtils.checkNotNull(spe);
        out.println("Warning: " + getParseExceptionInfo(spe));
    }
        
    @Override
	public void error(SAXParseException spe) throws SAXException {
    	String message;
    	
    	SharedUtils.checkNotNull(spe);
        message = "Error: " + getParseExceptionInfo(spe);
        throw new SAXException(message);
    }

    @Override
	public void fatalError(SAXParseException spe) throws SAXException {
    	String message;
    	
    	SharedUtils.checkNotNull(spe);
        message = "Fatal Error: " + getParseExceptionInfo(spe);
        throw new SAXException(message);
    }

}
