/*******************************************************************************
 * Copyright © 2017 Hulles Industries LLC
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
 *******************************************************************************/
package com.hulles.a1icia.media;

public enum MediaFormat {
	MP3(".mp3"),
	WAV(".wav"),
	
	PNG(".png"),
	JPG(".jpg"),
	GIF(".gif"),

	MOV(".mov"),
	FLV(".flv"),
	MP4(".mp4");
    private final String extension;

    private MediaFormat(String extension) {
    	
        this.extension = extension;
    }
    
    public String getFileExtension() {
    	
        return extension;
    }

}
