package com.hulles.a1icia.cayenne;

import java.util.List;

import org.apache.cayenne.Cayenne;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.query.ObjectSelect;

import com.hulles.a1icia.cayenne.auto._MediaFile;
import com.hulles.a1icia.media.MediaFormat;
import com.hulles.a1icia.tools.A1iciaUtils;

public class MediaFile extends _MediaFile {
    private static final long serialVersionUID = 1L; 
    
    public static MediaFile findMediaFile(String fileName) {
		ObjectContext context;
		MediaFile mediaFile;

		A1iciaUtils.checkNotNull(fileName);
		context = A1iciaApplication.getEntityContext();
		mediaFile = ObjectSelect
				.query(MediaFile.class)
				.where(_MediaFile.FILE_NAME.likeIgnoreCase(fileName))
				.selectOne(context);
		return mediaFile;
    }
    public static MediaFile findMediaFile(Integer mediaFileID) {
		ObjectContext context;
		MediaFile mediaFile;
		
		A1iciaUtils.checkNotNull(mediaFileID);
		context = A1iciaApplication.getEntityContext();
		mediaFile = Cayenne.objectForPK(context, MediaFile.class, mediaFileID);
		return mediaFile;
    }
    
	public static List<MediaFile> getMediaFiles() {
		ObjectContext context;
		List<MediaFile> dbMediaFiles;
		
		context = A1iciaApplication.getEntityContext();
		dbMediaFiles = ObjectSelect
				.query(MediaFile.class)
				.orderBy("artist")
				.orderBy("title")
				.select(context);
		return dbMediaFiles;
    }
    public static List<MediaFile> getMediaFiles(String artist) {
		ObjectContext context;
		List<MediaFile> dbMediaFiles = null;
		
		A1iciaUtils.checkNotNull(artist);
		context = A1iciaApplication.getEntityContext();
		dbMediaFiles = ObjectSelect
				.query(MediaFile.class)
				.where(_MediaFile.ARTIST.likeIgnoreCase(artist))
				.select(context);
		return dbMediaFiles;
    }
    
    public static MediaFile getMediaFile(String title) {
		ObjectContext context;
		MediaFile dbMediaFile = null;
		
		A1iciaUtils.checkNotNull(title);
		context = A1iciaApplication.getEntityContext();
		dbMediaFile = ObjectSelect
				.query(MediaFile.class)
				.where(_MediaFile.TITLE.likeIgnoreCase(title))
				.selectOne(context);
		return dbMediaFile;
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
    	
    	context = A1iciaApplication.getEntityContext();
        dbMediaFile = context.newObject(MediaFile.class);
    	// NOT committed yet
    	return dbMediaFile;
	}
}
