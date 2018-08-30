/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hulles.a1icia.mike;

import com.hulles.a1icia.api.A1iciaConstants;
import com.hulles.a1icia.api.jebus.JebusBible;
import com.hulles.a1icia.api.jebus.JebusBible.JebusKey;
import com.hulles.a1icia.api.jebus.JebusHub;
import com.hulles.a1icia.api.jebus.JebusPool;
import com.hulles.a1icia.api.remote.A1icianID;
import com.hulles.a1icia.api.shared.A1iciaException;
import com.hulles.a1icia.api.shared.ApplicationKeys;
import com.hulles.a1icia.api.shared.SharedUtils;
import com.hulles.a1icia.api.tools.A1iciaUtils;
import com.hulles.a1icia.cayenne.MediaFile;
import com.hulles.a1icia.media.MediaFormat;
import com.hulles.a1icia.tools.ExternalAperture;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import redis.clients.jedis.Jedis;

/**
 *
 * @author hulles
 */
public class MediaLibrary {
	private final static Logger LOGGER = Logger.getLogger("A1iciaMike.MediaLibrary");
	private final static Level LOGLEVEL = A1iciaConstants.getA1iciaLogLevel();
//	private final static Level LOGLEVEL = Level.INFO;
    private final static int LIBRARYUPDATEDAYS = 7;
	private final ApplicationKeys appKeys;
	private final JebusPool jebusLocal;
	private int mediaFilesUpdated;
    private int mediaFilesDeleted;
	
    MediaLibrary() {
        
		appKeys = ApplicationKeys.getInstance();
		jebusLocal = JebusHub.getJebusLocal();
    }
    
	MediaUpdateStats updateMediaLibrary(boolean forceUpdate) {
		List<Path> audioList;
		List<Path> videoList;
		String updateKey;
		String instantStr;
		Instant timestamp = null;
		Instant now;
		String musicLib;
		String videoLib;
		List<MediaFile> mediaFiles;
		MediaFile mediaFile;
		Path filePath;
        String fileName;
        boolean commit;
        
        SharedUtils.checkNotNull(forceUpdate);
		updateKey = JebusBible.getStringKey(JebusKey.ALICIAMEDIAFILEUPDATEKEY, jebusLocal);
		now = Instant.now();
		try (Jedis jebus = jebusLocal.getResource()) {
			instantStr = jebus.get(updateKey);
			if (instantStr != null) {
				timestamp = Instant.parse(instantStr);
			}
			if (timestamp == null || timestamp.isBefore(now) || forceUpdate) {
                mediaFilesUpdated = 0;
                mediaFilesDeleted = 0;
                LOGGER.log(Level.INFO, "Updating media library");
				jebus.set(updateKey, now.plus(LIBRARYUPDATEDAYS, ChronoUnit.DAYS).toString());
				
				// get list of audio library files and update the database with them
				musicLib = appKeys.getKey(ApplicationKeys.ApplicationKey.MUSICLIBRARY);
				audioList = LibraryLister.listFiles(musicLib, "*.mp3");
				audioList.stream().forEach(e -> updateMediaItem(e));
				
				// get list of video library files and update the database with them
				videoLib = appKeys.getKey(ApplicationKeys.ApplicationKey.VIDEOLIBRARY);
				videoList = LibraryLister.listFiles(videoLib, "*.{mp4,flv}");
				videoList.stream().forEach(e -> updateMediaItem(e));

                // now get rid of any tuples in the database whose files no longer exist in the wild
				mediaFiles = MediaFile.getMediaFiles();
				for (Iterator<MediaFile> iter = mediaFiles.iterator(); iter.hasNext(); ) {
					mediaFile = iter.next();
                    fileName = mediaFile.getFileName();
                    filePath = Paths.get(fileName);
                    if (!Files.exists(filePath)) {
                        LOGGER.log(LOGLEVEL, "updateMediaLibrary: delete file {0}", fileName);
                        mediaFilesDeleted++;
						mediaFile.delete();
					}
				}
			}
		}
        return new MediaUpdateStats();
	}
	
	private void updateMediaItem(Path filePath) {
		String artist;
		String title;
		MediaFile mediaFile;
		boolean updating = false;
		String existingArtist;
		String existingTitle;
		TikaResult tika;
        String fileName;
        
		SharedUtils.checkNotNull(filePath);
        LOGGER.log(LOGLEVEL, "updateMediaItem: read {0}", filePath);
        tika = getMetaData(filePath);
        artist = tika.getArtist();
        title = tika.getTitle();
        fileName = filePath.toString();
		mediaFile = MediaFile.findMediaFile(fileName);
		if (mediaFile == null) {
			mediaFile = MediaFile.createNew();
			mediaFile.setArtist(artist);
			mediaFile.setTitle(title);
			if (fileName.endsWith("mp4")) {
				mediaFile.setFormat(MediaFormat.MP4);
			} else if (fileName.endsWith("flv")) {
				mediaFile.setFormat(MediaFormat.FLV);
			} else if (fileName.endsWith("mp3")) {
				mediaFile.setFormat(MediaFormat.MP3);
			} else {
				A1iciaUtils.error("MikeRoom:updateMediaItem bad media file extension");
				return;
			}
			mediaFile.setFileName(fileName);
            LOGGER.log(LOGLEVEL, "updateMediaItem: add {0}", fileName);
			updating = true;
		} else {
			existingArtist = mediaFile.getArtist();
			if (artist != null) {
				if (existingArtist == null || !existingArtist.equals(artist)) {
					LOGGER.log(LOGLEVEL, "Updating existing artist = {0} to {1}", new Object[]{existingArtist, artist});
					mediaFile.setArtist(artist);
					updating = true;
				}
			}
			existingTitle = mediaFile.getTitle();
			if (title != null) {
				if (existingTitle == null || !existingTitle.equals(title)) {
					LOGGER.log(LOGLEVEL, "Updating existing title = {0} to {1}", new Object[]{existingTitle, title});
					mediaFile.setTitle(title);
					updating = true;
				}
			}
			if (mediaFile.getFormat() != MediaFormat.MP3) {
				LOGGER.log(LOGLEVEL, "Updating media format from {0} to MP3", mediaFile.getFormat());
				mediaFile.setFormat(MediaFormat.MP3);
				updating = true;
			}
		}
		if (updating) {
            mediaFilesUpdated++;
			LOGGER.log(LOGLEVEL, "Committing change(s)");
			mediaFile.commit();
		}
	}
    
    TikaResult getMetaData(Path path) {
        String jsonStr;
        TikaResult tikaResult;
        JsonObject jsonResult;
        String artist;
        String title;
        
        SharedUtils.checkNotNull(path);
        jsonStr = ExternalAperture.queryTika(path);
		try (BufferedReader reader = new BufferedReader(new StringReader(jsonStr))) {
			try (JsonReader jsonReader = Json.createReader(reader)) {
                jsonResult = jsonReader.readObject();
                artist = jsonResult.getString("Author");
                title = jsonResult.getString("title");
            }
 		} catch (IOException e) {
            throw new A1iciaException("MediaLibrary: IO error attempting string read", e);
		}
        if (artist == null || title == null) {
            A1iciaUtils.error("MediaLibrary: artist and/or title missing for file " + path);
            return null;
        }
        tikaResult = new TikaResult(artist, title);
        System.out.println(tikaResult);
        return tikaResult;
    }
    
    class MediaUpdateStats {
        A1icianID a1icianID;
        
        A1icianID getA1icianID() {
            return a1icianID;
        }
        
        void setA1icianID(A1icianID id) {
            
            SharedUtils.nullsOkay(id);
            a1icianID = id;
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
