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
package com.hulles.alixia.media;

public enum MediaFormat {
	MP3(".mp3", "audio/mpeg"),
	WAV(".wav", "audio/wav"),
	
	PNG(".png", "image/png"),
	JPG(".jpg", "image/jpeg"),
	GIF(".gif", "image/gif"),

	MOV(".mov", "video/quicktime"),
	FLV(".flv", "video/x-flv"),
	MP4(".mp4", "video/mp4");
    private final String extension;
    private final String mime;

    private MediaFormat(String extension, String mime) {
    	
        this.extension = extension;
        this.mime = mime;
    }
    
    public String getFileExtension() {
    	
        return extension;
    }

    public String getMimeType() {
        
        return mime;
    }
    
    public Boolean isAudio() {
    	
    	switch (this) {
	    	case MP3:
	    	case WAV:
	    		return true;
    		default:
    			return false;
    	}
    }

    public Boolean isImage() {
    	
    	switch (this) {
	    	case PNG:
	    	case JPG:
	    	case GIF:
	    		return true;
    		default:
    			return false;
    	}
    }

    public Boolean isVideo() {
    	
    	switch (this) {
	    	case MOV:
	    	case MP4:
	    	case FLV:
	    		return true;
    		default:
    			return false;
    	}
    }
    
    public static MediaFormat getFormatFromFileName(String fileName) {
        
        MediaUtils.checkNotNull(fileName);
        for (MediaFormat mf : MediaFormat.values()) {
            if (fileName.endsWith(mf.getFileExtension())) {
                return mf;
            }
        }
        return null;
    }
}
