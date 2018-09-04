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
package com.hulles.alixia.media.audio;

import java.io.Serializable;

import com.hulles.alixia.media.MediaUtils;

public class SerialAudioFormat implements Serializable {
	private static final long serialVersionUID = -5511606801527166819L;
	private String encodingStr;
	private Float sampleRate;
	private Integer sampleSize;
	private Integer channels;
	private Integer frameSize;
	private Float frameRate;
	private Boolean bigEndian;

	public SerialAudioFormat() {
		// needs no-arg constructor
	}
	
	public String getEncodingString() {
		
		return encodingStr;
	}

	public Float getSampleRate() {
		
		return sampleRate;
	}

	public Integer getSampleSize() {
		
		return sampleSize;
	}

	public Integer getChannels() {
		
		return channels;
	}

	public Integer getFrameSize() {
		
		return frameSize;
	}

	public Float getFrameRate() {
		
		return frameRate;
	}

	public Boolean getBigEndian() {
		
		return bigEndian;
	}

	public void setEncodingString(String encoding) {

		MediaUtils.checkNotNull(encoding);
		this.encodingStr = encoding;
	}

	public void setSampleRate(Float rate) {

		MediaUtils.checkNotNull(rate);
		this.sampleRate = rate;
	}

	public void setSampleSize(Integer size) {

		MediaUtils.checkNotNull(size);
		this.sampleSize = size;
	}

	public void setChannels(Integer channels) {

		MediaUtils.checkNotNull(channels);
		this.channels = channels;
	}

	public void setFrameSize(Integer size) {

		MediaUtils.checkNotNull(size);
		this.frameSize = size;
	}

	public void setFrameRate(Float rate) {

		MediaUtils.checkNotNull(rate);
		this.frameRate = rate;
	}
	
	public void setBigEndian(Boolean isBigEndian) {
		
		MediaUtils.checkNotNull(isBigEndian);
		this.bigEndian = isBigEndian;
	}
}
