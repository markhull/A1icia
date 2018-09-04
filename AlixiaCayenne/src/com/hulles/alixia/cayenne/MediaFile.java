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
package com.hulles.alixia.cayenne;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.cayenne.Cayenne;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.query.ObjectSelect;
import org.apache.cayenne.query.Query;

import com.hulles.alixia.api.shared.SharedUtils;
import com.hulles.alixia.cayenne.auto._MediaFile;
import com.hulles.alixia.media.MediaFormat;

public class MediaFile extends _MediaFile {
    private static final long serialVersionUID = 1L; 
	private static final Random RANDOM = new Random();
    
    
    public static MediaFile findMediaFile(String fileName) {
		ObjectContext context;
		MediaFile mediaFile;

		SharedUtils.checkNotNull(fileName);
		context = AlixiaApplication.getEntityContext();
		mediaFile = ObjectSelect
				.query(MediaFile.class)
				.where(_MediaFile.FILE_NAME.likeIgnoreCase(fileName))
				.selectOne(context);
		return mediaFile;
    }
    public static MediaFile findMediaFile(Integer mediaFileID) {
		ObjectContext context;
		MediaFile mediaFile;
		
		SharedUtils.checkNotNull(mediaFileID);
		context = AlixiaApplication.getEntityContext();
		mediaFile = Cayenne.objectForPK(context, MediaFile.class, mediaFileID);
		return mediaFile;
    }
    
	public static List<MediaFile> getMediaFiles() {
		ObjectContext context;
		List<MediaFile> dbMediaFiles;
		
		context = AlixiaApplication.getEntityContext();
		dbMediaFiles = ObjectSelect
				.query(MediaFile.class)
//				.orderBy("artist")
//				.orderBy("title")
				.select(context);
		return dbMediaFiles;
    }
    public static List<MediaFile> getMediaFiles(String artist) {
		ObjectContext context;
		List<MediaFile> dbMediaFiles;
		
		SharedUtils.checkNotNull(artist);
		context = AlixiaApplication.getEntityContext();
		dbMediaFiles = ObjectSelect
				.query(MediaFile.class)
				.where(_MediaFile.ARTIST.likeIgnoreCase(artist))
				.select(context);
		return dbMediaFiles;
    }
    public static List<MediaFile> getMediaFiles(MediaFormat format) {
		ObjectContext context;
		List<MediaFile> dbMediaFiles;
		
		SharedUtils.checkNotNull(format);
		context = AlixiaApplication.getEntityContext();
		dbMediaFiles = ObjectSelect
				.query(MediaFile.class)
				.where(_MediaFile.FORMAT_CODE.eq(format.name()))
				.select(context);
		return dbMediaFiles;
    }
    
    public static List<MediaFile> getAudioFiles() {
		ObjectContext context;
		List<MediaFile> dbMediaFiles;
		List<MediaFile> mediaFiles;
		MediaFormat format;
		
		context = AlixiaApplication.getEntityContext();
		dbMediaFiles = ObjectSelect
				.query(MediaFile.class)
				.select(context);
		mediaFiles = new ArrayList<>(dbMediaFiles.size());
		for (MediaFile mf : dbMediaFiles) {
			format = mf.getFormat();
			if (format.isAudio()) {
				mediaFiles.add(mf);
			}
		}
		return mediaFiles;
    }
    
    public static List<MediaFile> getVideoFiles() {
		ObjectContext context;
		List<MediaFile> dbMediaFiles;
		List<MediaFile> mediaFiles;
		MediaFormat format;
		
		context = AlixiaApplication.getEntityContext();
		dbMediaFiles = ObjectSelect
				.query(MediaFile.class)
				.select(context);
		mediaFiles = new ArrayList<>(dbMediaFiles.size());
		for (MediaFile mf : dbMediaFiles) {
			format = mf.getFormat();
			if (format.isVideo()) {
				mediaFiles.add(mf);
			}
		}
		return mediaFiles;
    }
    
    public static List<MediaFile> getImageFiles() {
		ObjectContext context;
		List<MediaFile> dbMediaFiles;
		List<MediaFile> mediaFiles;
		MediaFormat format;
		
		context = AlixiaApplication.getEntityContext();
		dbMediaFiles = ObjectSelect
				.query(MediaFile.class)
				.select(context);
		mediaFiles = new ArrayList<>(dbMediaFiles.size());
		for (MediaFile mf : dbMediaFiles) {
			format = mf.getFormat();
			if (format.isImage()) {
				mediaFiles.add(mf);
			}
		}
		return mediaFiles;
    }
    
    public static MediaFile getMediaFile(String title) {
		ObjectContext context;
		MediaFile dbMediaFile;
		
		SharedUtils.checkNotNull(title);
		context = AlixiaApplication.getEntityContext();
		dbMediaFile = ObjectSelect
				.query(MediaFile.class)
				.where(_MediaFile.TITLE.likeIgnoreCase(title))
				.selectOne(context);
		return dbMediaFile;
    }
    
    public static MediaFile getRandomMediaFile() {
		int recCount;
		int recPos;
		Query query;
		ObjectContext context;

		context = AlixiaApplication.getEntityContext();
		recCount = getRecordCount();
		recPos = RANDOM.nextInt(recCount);
		query = ObjectSelect
				.query(MediaFile.class)
				.limit(1)
				.offset(recPos);
		return (MediaFile) Cayenne.objectForQuery(context, query);
    }
    
	private static int getRecordCount() {
     	ObjectContext context;
     	int count;
    	
    	context = AlixiaApplication.getEntityContext();
    	count = (int) ObjectSelect
    			.query(MediaFile.class)
    			.selectCount(context);
     	return count;
    }
    
    public MediaFormat getFormat() {
    	
    	return MediaFormat.valueOf(getFormatCode());
    }
    
    public void setFormat(MediaFormat format) {
    	
    	this.setFormatCode(format.name());
    }
    
    public void commit() {
    	ObjectContext context;
    	
    	context = this.getObjectContext();
    	context.commitChanges();
    }
    
    public void rollback() {
    	ObjectContext context;
    	
    	context = this.getObjectContext();
    	context.rollbackChanges();
    }

	public void delete() {
    	ObjectContext context;
    	
    	context = this.getObjectContext();
     	context.deleteObjects(this);
    	context.commitChanges();
	}

	public static MediaFile createNew() {
    	ObjectContext context;
    	MediaFile dbMediaFile;
    	
    	context = AlixiaApplication.getEntityContext();
        dbMediaFile = context.newObject(MediaFile.class);
    	// NOT committed yet
    	return dbMediaFile;
	}
}
