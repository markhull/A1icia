/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hulles.alixia.mike;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Iterator;
import java.util.List;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hulles.alixia.api.jebus.JebusBible;
import com.hulles.alixia.api.jebus.JebusBible.JebusKey;
import com.hulles.alixia.api.jebus.JebusHub;
import com.hulles.alixia.api.jebus.JebusPool;
import com.hulles.alixia.api.remote.AlixianID;
import com.hulles.alixia.api.shared.AlixiaException;
import com.hulles.alixia.api.shared.ApplicationKeys;
import com.hulles.alixia.api.shared.SharedUtils;
import com.hulles.alixia.api.tools.AlixiaUtils;
import com.hulles.alixia.cayenne.AlixiaApplication;
import com.hulles.alixia.cayenne.MediaFile;
import com.hulles.alixia.media.MediaFormat;
import com.hulles.alixia.tools.ExternalAperture;

import redis.clients.jedis.Jedis;

/**
 *
 * @author hulles
 */
public class MediaLibrary {
	private final static Logger LOGGER = LoggerFactory.getLogger(MediaLibrary.class);
    private final static int LIBRARYUPDATEDAYS = 7;
	private final ApplicationKeys appKeys;
	private final JebusPool jebusLocal;
	int mediaFilesUpdated;
    int mediaFilesDeleted;
	
    MediaLibrary() {
        
		appKeys = ApplicationKeys.getInstance();
		jebusLocal = JebusHub.getJebusLocal();
    }
    
	MediaUpdateStats updateMediaLibrary(boolean forceUpdate) {
		List<Path> audioList;
		List<Path> videoList;
		List<Path> mikeList;
		String updateKey;
		String instantStr;
		Instant timestamp = null;
		Instant now;
		String musicLib;
		String videoLib;
        String mikeLib;
		List<MediaFile> mediaFiles;
		MediaFile mediaFile;
		Path filePath;
        String fileName;
        boolean oldValue;
        
        SharedUtils.checkNotNull(forceUpdate);
		updateKey = JebusBible.getStringKey(JebusKey.ALIXIAMEDIAFILEUPDATEKEY, jebusLocal);
		now = Instant.now();
		try (Jedis jebus = jebusLocal.getResource()) {
			instantStr = jebus.get(updateKey);
			if (instantStr != null) {
				timestamp = Instant.parse(instantStr);
			}
			if (timestamp == null || timestamp.isBefore(now) || forceUpdate) {
			    oldValue = AlixiaApplication.setErrorOnUncommittedObjects(false);
                mediaFilesUpdated = 0;
                mediaFilesDeleted = 0;
                LOGGER.info("Updating media library");
				jebus.set(updateKey, now.plus(LIBRARYUPDATEDAYS, ChronoUnit.DAYS).toString());
				
				// get list of audio library files and update the database with them
				musicLib = appKeys.getKey(ApplicationKeys.ApplicationKey.MUSICLIBRARY);
				audioList = LibraryLister.listFiles(musicLib, "*.mp3");
				audioList.stream().forEach(e -> updateMediaItem(e));
                
				// get list of video library files and update the database with them
				videoLib = appKeys.getKey(ApplicationKeys.ApplicationKey.VIDEOLIBRARY);
				videoList = LibraryLister.listFiles(videoLib, "*.{mp4,flv}");
				videoList.stream().forEach(e -> updateMediaItem(e));
                
				// get list of our own library files and update the database with them
				mikeLib = appKeys.getKey(ApplicationKeys.ApplicationKey.MIKELIBRARY);
				mikeList = LibraryLister.listFiles(mikeLib);
				mikeList.stream().forEach(e -> updateMediaItem(e));

                // now get rid of any tuples in the database whose files no longer exist in the wild
				mediaFiles = MediaFile.getMediaFiles();
				for (Iterator<MediaFile> iter = mediaFiles.iterator(); iter.hasNext(); ) {
					mediaFile = iter.next();
                    fileName = mediaFile.getFileName();
                    filePath = Paths.get(fileName);
                    if (!Files.exists(filePath)) {
                        LOGGER.debug("updateMediaLibrary: delete file {}", fileName);
                        mediaFilesDeleted++;
						mediaFile.delete();
					}
				}
				AlixiaApplication.commitAll();
				AlixiaApplication.setErrorOnUncommittedObjects(oldValue);
			}
		}
		
        return new MediaUpdateStats();
	}
	
	private void updateMediaItem(Path filePath) {
		MediaFile mediaFile;
        FileTime fTime;
        Instant fileInstant;
        LocalDateTime fileLDT;
        LocalDateTime dbLDT;
        String fileName;
        
		SharedUtils.checkNotNull(filePath);
        LOGGER.debug("updateMediaItem: read {}", filePath);
        fileName = filePath.toString();
//        if (fileName.startsWith("/home/hulles/Media/Music Videos/Pink Floyd")) {
//            // boom?
//            System.out.println("We got to Joe");
//        }
        try {
            fTime = Files.getLastModifiedTime(filePath);
        } catch (IOException ex) {
            throw new AlixiaException("updateMediaItem: IO error getting file modification time", ex);
        }
        fileInstant = fTime.toInstant();
        fileLDT = AlixiaUtils.ldtFromInstant(fileInstant).withNano(0);
		mediaFile = MediaFile.findMediaFile(fileName);
		if (mediaFile == null) {
            LOGGER.debug("Creating new entry");
			mediaFile = MediaFile.createNew();
            mediaFile.setFileName(fileName);
            updateMediaFile(filePath, mediaFile, fileLDT);
		} else {
            LOGGER.debug("Updating existing entry");
            dbLDT = mediaFile.getFileLastModified();
            if ((dbLDT == null) || (!dbLDT.equals(fileLDT))) {
                LOGGER.debug("Modification times differ: {} <--> {}", dbLDT, fileLDT);
                updateMediaFile(filePath, mediaFile, fileLDT);
            }
		}
	}
    
    private void updateMediaFile(Path filePath, MediaFile mediaFile, LocalDateTime fileLDT) {
        MediaFormat format;
		String artist = null;
		String title = null;
		String existingArtist;
		String existingTitle;
		TikaResult tika;
        String fileName;
        boolean usingDefaultTitle = false;
        
        SharedUtils.checkNotNull(filePath);
        SharedUtils.checkNotNull(mediaFile);
        SharedUtils.checkNotNull(fileLDT);
        fileName = filePath.toString();
        // we already know that the fileLDT does not equal the dbLDT so we update it immediately
        mediaFile.setFileLastModified(fileLDT);
        format = mediaFile.getFormat();
        if (format == null) {
            format = MediaFormat.getFormatFromFileName(fileName);
            mediaFile.setFormat(format);
        }
        switch (format) {
            case MP3:
            case MP4:
                tika = getMetaData(filePath);
                if (tika != null) {
                    LOGGER.debug("Tika result is not null");
                    artist = tika.getArtist();
                    title = tika.getTitle();
                }
                break;
            default:
                title = com.google.common.io.Files.getNameWithoutExtension(fileName);
                usingDefaultTitle = true;
                break;
        }
        existingArtist = mediaFile.getArtist();
        if (artist != null) {
            if (existingArtist == null || !existingArtist.equals(artist)) {
                LOGGER.debug("Updating existing artist = {} to {}",existingArtist, artist);
                mediaFile.setArtist(artist);
            }
        }
        existingTitle = mediaFile.getTitle();
        if (title != null) {
            if (existingTitle == null) {
                LOGGER.debug("Updating existing title = {} to {}", existingTitle, title);
                mediaFile.setTitle(title);
            } else if (!usingDefaultTitle && !existingTitle.equals(title)) {
                // if the database already has a title, don't overlay it 
                //    with the default title, i.e. the file name. This lets us edit
                //    the the title in the database record.
                LOGGER.debug("Updating existing title = {} to {}", existingTitle, title);
                mediaFile.setTitle(title);
            }
        }
        mediaFilesUpdated++;
        LOGGER.debug("Committing change(s)");
        AlixiaApplication.commitAll();
    }
    
    TikaResult getMetaData(Path path) {
        String jsonStr;
        TikaResult tikaResult;
        JsonObject jsonResult;
        String artist;
        String title;
        
        SharedUtils.checkNotNull(path);
        jsonStr = ExternalAperture.queryTika(path);
        if (jsonStr == null) {
            return null;
        }
        LOGGER.debug("JSON string is {}", jsonStr);
		try (BufferedReader reader = new BufferedReader(new StringReader(jsonStr))) {
			try (JsonReader jsonReader = Json.createReader(reader)) {
                jsonResult = jsonReader.readObject();
                artist = jsonResult.getString("Author", "unknown");
                LOGGER.debug("JSON artist is {}", artist);
                title = jsonResult.getString("title", "unknown");
                LOGGER.debug("JSON title is {}", title);
            }
 		} catch (IOException e) {
            throw new AlixiaException("MediaLibrary: IO error attempting string read", e);
		}
        if (artist == null || title == null) {
            LOGGER.error("MediaLibrary: artist and/or title missing for file {}", path);
            return null;
        }
        tikaResult = new TikaResult(artist, title);
        LOGGER.info("TIKA: {}", tikaResult.toString());
        return tikaResult;
    }
    
    class MediaUpdateStats {
        private AlixianID alixianID;
        
        AlixianID getAlixianID() {
            return alixianID;
        }
        
        void setAlixianID(AlixianID id) {
            
            SharedUtils.nullsOkay(id);
            alixianID = id;
        }
        
        int getMediaFilesUpdated() {
            
            return mediaFilesUpdated;
        }
        
        int getMediaFilesDeleted() {
            
            return mediaFilesDeleted;
        }

    }
    
    class TikaResult {
        private String artist;
        private String title;

        TikaResult(String artist, String title) {
            
            SharedUtils.checkNotNull(artist);
            SharedUtils.checkNotNull(title);
            this.artist = artist;
            this.title = title;
        }
        
        String getArtist() {
            
            return artist;
        }

        void setArtist(String artist) {
            
            SharedUtils.checkNotNull(artist);
            this.artist = artist;
        }

        String getTitle() {
            
            return title;
        }

        void setTitle(String title) {
            
            SharedUtils.checkNotNull(title);
            this.title = title;
        }

        @Override
        public String toString() {
            String format = "Artist: %s Title: %s";
            
            return String.format(format, getArtist(), getTitle());
        }
        
    }
}
