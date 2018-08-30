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

import com.hulles.a1icia.api.jebus.JebusHub;
import com.hulles.a1icia.api.shared.SharedUtils;
import com.hulles.a1icia.media.MediaFormat;
import com.hulles.a1icia.media.MediaUtils;

/**
 * A MediaObject is an instance of an image, an audio file, or a video. Actually,
 * there may be multiple segments in the byte[][]. This array array is not intended for 
 * cramming multiple songs from a playlist into one object (although I suppose it
 * might work for a small playlist, now that I mention it); it's more to concatenate a 
 * couple of files into a media package. The causal instance combines the 
 * A1iciaVision intro with a video file so they play seamlessly back-to-back and make us
 * look like a class operation.
 * 
 * @author hulles
 *
 */
public class MediaObject implements A1iciaClientObject {
	private static final long serialVersionUID = -2305398153211376016L;
	private static final int MAXHEADROOM = JebusHub.getMaxHardOutputBufferLimit();
	private byte[][] mediaBytes;
	private MediaFormat format;
	private ClientObjectType type;
	private String mediaTitle;
	private Integer lengthSeconds;
	
	public MediaObject() {
		// needs no-arg constructor
	}
	
	public Integer getLengthSeconds() {
		
		return lengthSeconds;
	}

	public void setLengthSeconds(Integer lengthSeconds) {
		
		SharedUtils.nullsOkay(lengthSeconds);
		this.lengthSeconds = lengthSeconds;
	}

	public MediaFormat getMediaFormat() {
		
		return format;
	}

	public void setMediaFormat(MediaFormat format) {
		
		SharedUtils.checkNotNull(format);
		this.format = format;
	}

	public byte[][] getMediaBytes() {
		
		return mediaBytes;
	}

	public void setMediaBytes(byte[][] bytes) {
		
		SharedUtils.checkNotNull(bytes);
		this.mediaBytes = bytes;
	}

	public String getMediaTitle() {
		
		return mediaTitle;
	}

	public void setMediaTitle(String title) {
		
		MediaUtils.nullsOkay(title);
		this.mediaTitle = title;
	}
	
	@Override
	public ClientObjectType getClientObjectType() {
		return type;
	}

	public void setClientObjectType(ClientObjectType type) {
		
		SharedUtils.checkNotNull(type);
		this.type = type;
	}

	@Override
	public boolean isValid() {
		
		if (mediaBytes == null) {
			return false;
		}
		if (mediaBytes.length > MAXHEADROOM) {
			return false;
		}
		if (format == null) {
			return false;
		}
		if (type == null) {
			return false;
		}
		// title can be null
		return true;
	}
}
