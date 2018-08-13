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
import com.hulles.a1icia.media.audio.SerialAudioFormat;

/**
 * An audio MediaObject. This just adds the audio format to the MediaObject super-class.
 * 
 * @author hulles
 *
 */
public final class AudioObject extends MediaObject {
	private static final long serialVersionUID = -3047896290125769038L;
	private SerialAudioFormat format;
	
	public AudioObject() {
		super();
	}
	
	public SerialAudioFormat getAudioFormat() {
		
		return format;
	}
	
	public void setAudioFormat(SerialAudioFormat format) {
		
		SharedUtils.checkNotNull(format);
		this.format = format;
	}
	
	@Override
	public boolean isValid() {
		
		if (format == null) {
			return false;
		}
		return super.isValid();
	}
}
