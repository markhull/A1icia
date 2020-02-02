/*******************************************************************************
 * Copyright © 2017 Hulles Industries LLC
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
 *******************************************************************************/
package com.hulles.fortune;

import java.io.Serializable;

final public class SerialFortune implements Serializable {
	private static final long serialVersionUID = 465302354035354130L;
	private String text;
	private String source;
	
	public SerialFortune() {
		// need no-arg constructor
	}
	
	public String getText() {
		
		return text;
	}
	
	public void setText(String text) {
		
		SharedUtils.checkNotNull(text);
		this.text = text;
	}
	
	public String getSource() {
		
		return source;
	}
	
	public void setSource(String source) {
		
		SharedUtils.checkNotNull(source);
		this.source = source;
	}

}
