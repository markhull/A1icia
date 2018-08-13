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
package com.hulles.a1icia.webx.shared;

import java.io.Serializable;
import java.util.List;

public class SerialConsoleOut implements Serializable {
	private static final long serialVersionUID = 3904580635270447694L;
	private java.util.Date datestamp;
	private String dateString;
	private String text;
	private String explain;
	private String audioURL;
	private List<String> urls;
	private List<String> formats;
	private List<Integer> lengths;
	
	public SerialConsoleOut() {
		// need no-arg constructor
	}

	public List<String> getUrls() {
		
		return urls;
	}

	public void setUrls(List<String> urls) {
		
		SharedUtils.checkNotNull(urls);
		this.urls = urls;
	}

	public List<Integer> getLengths() {
		
		return lengths;
	}

	public void setLengths(List<Integer> lengths) {
		
		SharedUtils.checkNotNull(lengths);
		this.lengths = lengths;
	}

	public List<String> getFormats() {
		
		return formats;
	}

	public void setFormats(List<String> formats) {
		
		SharedUtils.checkNotNull(formats);
		this.formats = formats;
	}

	public String getAudioURL() {
		
		return audioURL;
	}

	public void setAudioURL(String audioURL) {
		
		SharedUtils.nullsOkay(audioURL);
		this.audioURL = audioURL;
	}

	public java.util.Date getDatestamp() {
		
		return datestamp;
	}

	public void setDatestamp(java.util.Date datestamp) {
		
		SharedUtils.checkNotNull(datestamp);
		this.datestamp = datestamp;
	}

	public String getDateString() {
		
		return dateString;
	}

	public void setDateString(String dateString) {
		
		SharedUtils.checkNotNull(dateString);
		this.dateString = dateString;
	}

	public String getText() {
		
		return text;
	}

	public void setText(String text) {
		
		SharedUtils.checkNotNull(text);
		this.text = text;
	}

	public String getExplain() {
		
		return explain;
	}

	public void setExplain(String explain) {
		
		SharedUtils.checkNotNull(text);
		this.explain = explain;
	}

}
