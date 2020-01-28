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
package com.hulles.alixia.mike;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.sound.sampled.AudioFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hulles.alixia.api.AlixiaConstants;
import com.hulles.alixia.api.dialog.DialogResponse;
import com.hulles.alixia.api.jebus.JebusHub;
import com.hulles.alixia.api.object.AlixiaClientObject.ClientObjectType;
import com.hulles.alixia.api.object.AudioObject;
import com.hulles.alixia.api.object.MediaObject;
import com.hulles.alixia.api.remote.AlixianID;
import com.hulles.alixia.api.shared.AlixiaException;
import com.hulles.alixia.api.shared.ApplicationKeys;
import com.hulles.alixia.api.shared.ApplicationKeys.ApplicationKey;
import com.hulles.alixia.api.shared.SerialSememe;
import com.hulles.alixia.api.shared.SharedUtils;
import com.hulles.alixia.cayenne.MediaFile;
import com.hulles.alixia.house.ClientDialogResponse;
import com.hulles.alixia.media.Language;
import com.hulles.alixia.media.MediaFormat;
import com.hulles.alixia.media.MediaUtils;
import com.hulles.alixia.media.audio.SerialAudioFormat;
import com.hulles.alixia.media.audio.TTSPico;
import com.hulles.alixia.mike.MediaLibrary.MediaUpdateStats;
import com.hulles.alixia.room.Room;
import com.hulles.alixia.room.UrRoom;
import com.hulles.alixia.room.document.ClientObjectWrapper;
import com.hulles.alixia.room.document.MediaAnalysis;
import com.hulles.alixia.room.document.MessageAction;
import com.hulles.alixia.room.document.RoomActionObject;
import com.hulles.alixia.room.document.RoomAnnouncement;
import com.hulles.alixia.room.document.RoomRequest;
import com.hulles.alixia.room.document.RoomResponse;
import com.hulles.alixia.ticket.ActionPackage;
import com.hulles.alixia.ticket.SememePackage;
import com.hulles.alixia.ticket.Ticket;
import com.hulles.alixia.tools.FuzzyMatch;
import com.hulles.alixia.tools.FuzzyMatch.Match;

/**
 * Mike Room is our media room. Mike has a library of .wav files that he can broadcast to pretty 
 * much anyone via Redis / Jedis. Clients can also request media files; these are also "sent" via 
 * Redis. What actually happens is that they are stored as byte arrays in Redis and retrieved
 * by (possibly remote) Redis clients. I note in passing that, according to Jedis, the maximum 
 * byte array size is 1GB, but there is a Redis hard output buffer limit for pub/sub clients
 * which, if exceeded, causes the Redis client to be terminated. Ouch. The current limit for Alixia 
 * can be found in JebusHub.
 * 
 * @author hulles
 *
 */
public final class MikeRoom extends UrRoom {
    private final static Logger LOGGER = LoggerFactory.getLogger(MikeRoom.class);
	private final static int MAXHEADROOM = JebusHub.getMaxHardOutputBufferLimit();
//    private final static int POOLSIZE = 2;
	@SuppressWarnings("unused")
	private List<Path> acknowledgments;
	private List<Path> exclamations;
	private List<Path> praise;
	@SuppressWarnings("unused")
	private List<Path> musicClips;
	private List<Path> prompts;
	private List<Path> specialMedia;
	private List<Path> notifications;
	private final Random random;
	private final List<String> artists;
	private final List<String> titles;
	private final ApplicationKeys appKeys;
	private byte[] introBytes = null;
    final MediaLibrary mediaLibrary;
    private ExecutorService updateExecutor;

    
	public MikeRoom() {
		super();
		
		random = new Random();
		artists = new ArrayList<>(5000);
		titles = new ArrayList<>(5000);
		appKeys = ApplicationKeys.getInstance();
        mediaLibrary = new MediaLibrary();
	}

	private Path getRandomPath(List<Path> files) {
		int ix;
		
		ix = random.nextInt(files.size());
		return files.get(ix);
	}
	
	private MediaFile getRandomMediaFile(List<MediaFile> files) {
		int ix;
		
		ix = random.nextInt(files.size());
		return files.get(ix);
	}
	
	public List<Match> matchArtists(String input, int bestNAnswers) {
		List<Match> matches;
		
		SharedUtils.checkNotNull(input);
		SharedUtils.checkNotNull(bestNAnswers);
		matches = FuzzyMatch.getNBestMatches(input, artists, bestNAnswers);
		return matches;
	}
	
	public List<Match> matchTitles(String input, int bestNAnswers) {
		List<Match> matches;
		
		SharedUtils.checkNotNull(input);
		SharedUtils.checkNotNull(bestNAnswers);
		matches = FuzzyMatch.getNBestMatches(input, titles, bestNAnswers);
		return matches;
	}
	
	public static void logAudioFormat(String fileName) {
		
		try {
			LOGGER.debug(MediaUtils.getAudioFormatString(fileName));
		} catch (Exception e) {
			throw new AlixiaException("Can't log audio format", e);
		}
	}

    @Override
	public Room getThisRoom() {

		return Room.MIKE;
	}

	@Override
	public void processRoomResponses(RoomRequest request, List<RoomResponse> responses) {
        // we get a response if we did a push notification
		List<ActionPackage> pkgs;
		RoomActionObject obj;
		MessageAction msgAction;
		Ticket ticket;
		
		SharedUtils.checkNotNull(request);
		SharedUtils.checkNotNull(responses);
		// note that here we're ignoring the fact that we might get more than one response...
		for (RoomResponse rr : responses) {
			if (!rr.hasNoResponse()) {
				// see if we can learn anything....
				pkgs = rr.getActionPackages();
				for (ActionPackage pkg : pkgs) {
					obj = pkg.getActionObject();
					if (obj instanceof MessageAction) {
						msgAction = (MessageAction) obj;
							LOGGER.debug("We got some learning => {} : {}", msgAction.getMessage(), msgAction.getExplanation());
					}
				}
			}
		}
		ticket = request.getTicket();
		ticket.close();
	}

	@Override
	protected void roomStartup() {
		List<MediaFile> media;
		String lib;
		Path path;
		
//		updateExecutor = Executors.newFixedThreadPool(POOLSIZE);
        updateExecutor = Executors.newCachedThreadPool();
		lib = appKeys.getKey(ApplicationKey.MIKELIBRARY) + "/";
		acknowledgments = LibraryLister.listFiles(lib + "acknowledgment");
		exclamations = LibraryLister.listFiles(lib + "exclamation");
		musicClips = LibraryLister.listFiles(lib + "music_clips");
		prompts = LibraryLister.listFiles(lib + "passing_by");
		praise = LibraryLister.listFiles(lib + "praise");
		specialMedia = LibraryLister.listFiles(lib + "other_responses");
		notifications = LibraryLister.listFiles(lib + "notifications");
		updateMediaLibrary(null, false);
        
        // create lists of artists and titles for lookup (yet to be implemented)
		media = MediaFile.getMediaFiles();
		media.stream().forEach(e -> addMediaToLists(e));
        
        // load custom Alixia video intro
		path = findMediaFile("AlixiaVision.mov", specialMedia);
		if (path == null) {
			LOGGER.error("MikeRoom: null intro file");
			return;
		}
		introBytes = MediaUtils.pathToByteArray(path);
	}

	@Override
	protected void roomShutdown() {
		
        shutdownAndAwaitTermination(updateExecutor);
	}
	
	private void addMediaToLists(MediaFile media) {
		String artist;
		String title;
		
		artist = media.getArtist();
		if (artist != null) {
			artists.add(artist);
		}
		title = media.getTitle();
		if (title != null) {
			titles.add(title);
		}
	}

	@Override
	protected ActionPackage createActionPackage(SememePackage sememePkg, RoomRequest request) {

		switch (sememePkg.getName()) {
            case "reload_media_library":
                return reloadMediaLibrary(sememePkg, request);
			case "play_artist":
				return createArtistActionPackage(sememePkg, request);
			case "play_title":
				return createTitleActionPackage(sememePkg, request);
			case "random_music":
				return createRandomMusicActionPackage(sememePkg, request);
			case "play_video":
				return createVideoActionPackage(sememePkg, request);
			case "speak":
				return createSpeakActionPackage(sememePkg, request);
			case "prompt":
			case "exclamation":
			case "nothing_to_do":
			case "praise":
				return createPromptActionPackage(sememePkg, request);
			case "pronounce_linux":
			case "listen_to_her_heart":
			case "pronounce_alicia":
			case "sorry_for_it_all":
			case "dead_sara":
			case "pronounce_hulles":
            case "i_fink_u_freeky":
            case "WAVE_file":
				return createSpecialActionPackage(sememePkg, request);
			case "match_artists_and_titles":
				return createAnalysisActionPackage(sememePkg, request);
			case "notification_medium":
				return createNotificationActionPackage(sememePkg, request);
			default:
				throw new AlixiaException("Received unknown sememe in " + getThisRoom());
		}
	}

    private ActionPackage reloadMediaLibrary(SememePackage sememePkg, RoomRequest request) {
        ActionPackage pkg;
        MessageAction action;
        Future<MediaUpdateStats> updateNotificationFuture;
        AlixianID alixianID;
        
        alixianID = request.getTicket().getFromAlixianID();
        // construct an ExecutorService that returns a Future of MediaUpdateStats
        updateNotificationFuture = updateMediaLibrary(alixianID, true);
        updateExecutor.execute(new Runnable() {
            
            @Override
            public void run() {
                MediaUpdateStats stats;
                String message;
                String explanation;
                AlixianID userID;
                StringBuilder sb;
               
                try {
                    // future.get() blocks until the result is ready
                    stats = updateNotificationFuture.get();
                } catch (InterruptedException ex) {
        			LOGGER.error("Mike library update interrupted", ex);
                    return;
                } catch (ExecutionException ex) {
        			LOGGER.error("Mike library update execution exception", ex);
                    return;
                }
                userID = stats.getAlixianID();
                message = "I updated the media library.";
                sb = new StringBuilder();
                sb.append("Media files updated: ");
                sb.append(stats.getMediaFilesUpdated());
                sb.append("\nMedia files deleted: ");
                sb.append(stats.getMediaFilesDeleted());
                sb.append("\n");
                explanation = sb.toString();
                pushNotification(userID, message, explanation);
           }
        });
        pkg = new ActionPackage(sememePkg);
		action = new MessageAction();
		action.setMessage("I am updating the media library now.");
        pkg.setActionObject(action);
        return pkg;
    }

 	private Future<MediaUpdateStats> updateMediaLibrary(final AlixianID alixianToNotifyID, 
            final boolean forceUpdate) {

        SharedUtils.nullsOkay(alixianToNotifyID);
        SharedUtils.checkNotNull(forceUpdate);
        return updateExecutor.submit(new Callable<MediaUpdateStats>() {
			@Override
			public MediaUpdateStats call() {
                MediaUpdateStats stats;
                
				stats = mediaLibrary.updateMediaLibrary(forceUpdate);
                stats.setAlixianID(alixianToNotifyID);
                return stats;
			}
		});
	}
   
	private ActionPackage createArtistActionPackage(SememePackage sememePkg, RoomRequest request) {
		ActionPackage pkg;
		ClientObjectWrapper action = null;
		AudioObject audioObject;
		String fileName;
		List<MediaFile> mediaFiles;
		MediaFile mediaFile;
		String matchTarget;
		AudioFormat audioFormat;
		byte[] mediaBytes;
		byte[][] mediaArrays;
		SerialAudioFormat serialFormat;
        String artist;
        String title;
        MediaFormat mediaFormat;
		
		SharedUtils.checkNotNull(sememePkg);
		SharedUtils.checkNotNull(request);
		pkg = new ActionPackage(sememePkg);
		matchTarget = sememePkg.getSememeObject();
		if (matchTarget == null) {
			LOGGER.error("MikeRoom:createArtistActionPackage: no sememe object");
		}
		mediaFiles = MediaFile.getMediaFiles(matchTarget);
		if (mediaFiles != null) {
			mediaFile = getRandomMediaFile(mediaFiles);
			fileName = mediaFile.getFileName();
			if (fileName == null) {
				LOGGER.error("MikeRoom: file name is null");
				return null;
			}
			audioFormat = MediaUtils.getAudioFormat(fileName);
			audioObject = new AudioObject();
			audioObject.setClientObjectType(ClientObjectType.AUDIOBYTES);
            artist = mediaFile.getArtist();
            title = mediaFile.getTitle();
			audioObject.setMediaTitle(title);
            audioObject.setMediaArtist(artist);
			serialFormat = MediaUtils.audioFormatToSerial(audioFormat);
			audioObject.setAudioFormat(serialFormat);
			mediaBytes = MediaUtils.fileToByteArray(fileName);
			if (mediaBytes.length > MAXHEADROOM) {
				LOGGER.error("MikeRoom: file exceeds Redis limit");
				return null;
			}
			mediaArrays = new byte[][]{mediaBytes};
			audioObject.setMediaBytes(mediaArrays);
            mediaFormat = MediaFormat.getFormatFromFileName(fileName);
			audioObject.setMediaFormat(mediaFormat);
			if (!audioObject.isValid()) {
				LOGGER.error("MikeRoom: invalid media object");
				return null;
			}
			action = wrapMediaObject(audioObject);
		}
		pkg.setActionObject(action);
        pkg.setIsMultiMedia(true);
		return pkg;
	}

	private static ActionPackage createTitleActionPackage(SememePackage sememePkg, RoomRequest request) {
		ActionPackage pkg;
		ClientObjectWrapper action;
		AudioObject audioObject;
		String fileName;
		MediaFile mediaFile;
		String matchTarget;
		AudioFormat audioFormat;
		byte[] mediaBytes;
		byte[][] mediaArrays;
		SerialAudioFormat serialFormat;
        String artist;
        String title;
        MediaFormat mediaFormat;
		
		SharedUtils.checkNotNull(sememePkg);
		SharedUtils.checkNotNull(request);
		pkg = new ActionPackage(sememePkg);
		matchTarget = sememePkg.getSememeObject();
		if (matchTarget == null) {
			LOGGER.error("MikeRoom:createTitleActionPackage: no sememe object");
		}
		mediaFile = MediaFile.getMediaFile(matchTarget);
		if (mediaFile == null) {
			LOGGER.error("Media file is null in Mike room");
			return null;
		}
		fileName = mediaFile.getFileName();
		if (fileName == null) {
			LOGGER.error("File name is null in Mike room");
			return null;
		}
		audioFormat = MediaUtils.getAudioFormat(fileName);
		audioObject = new AudioObject();
		audioObject.setClientObjectType(ClientObjectType.AUDIOBYTES);
        artist = mediaFile.getArtist();
        title = mediaFile.getTitle();
        audioObject.setMediaTitle(title);
        audioObject.setMediaArtist(artist);
		serialFormat = MediaUtils.audioFormatToSerial(audioFormat);
		audioObject.setAudioFormat(serialFormat);
		mediaBytes = MediaUtils.fileToByteArray(fileName);
		if (mediaBytes.length > MAXHEADROOM) {
			LOGGER.error("MikeRoom: file exceeds Redis limit");
			return null;
		}
		mediaArrays = new byte[][]{mediaBytes};
		audioObject.setMediaBytes(mediaArrays);
        mediaFormat = MediaFormat.getFormatFromFileName(fileName);
		audioObject.setMediaFormat(mediaFormat);
		if (!audioObject.isValid()) {
			LOGGER.error("MikeRoom: invalid media object");
			return null;
		}
		action = wrapMediaObject(audioObject);
		pkg.setActionObject(action);
        pkg.setIsMultiMedia(true);
		return pkg;
	}

	private static ActionPackage createRandomMusicActionPackage(SememePackage sememePkg, RoomRequest request) {
		ActionPackage pkg;
		ClientObjectWrapper action;
		AudioObject audioObject;
		String fileName;
		MediaFile mediaFile;
		AudioFormat audioFormat;
		byte[] mediaBytes;
		byte[][] mediaArrays;
		SerialAudioFormat serialFormat;
		String artist;
        String title;
        MediaFormat mediaFormat;
        
		SharedUtils.checkNotNull(sememePkg);
		SharedUtils.checkNotNull(request);
		pkg = new ActionPackage(sememePkg);
		mediaFile = MediaFile.getRandomMediaFile();
		if (mediaFile == null) {
			LOGGER.error("Media file is null in Mike room");
			return null;
		}
		fileName = mediaFile.getFileName();
		if (fileName == null) {
			LOGGER.error("File name is null in Mike room");
			return null;
		}
		audioFormat = MediaUtils.getAudioFormat(fileName);
		audioObject = new AudioObject();
		audioObject.setClientObjectType(ClientObjectType.AUDIOBYTES);
        artist = mediaFile.getArtist();
        title = mediaFile.getTitle();
        audioObject.setMediaTitle(title);
        audioObject.setMediaArtist(artist);
		serialFormat = MediaUtils.audioFormatToSerial(audioFormat);
		audioObject.setAudioFormat(serialFormat);
		mediaBytes = MediaUtils.fileToByteArray(fileName);
		if (mediaBytes.length > MAXHEADROOM) {
			LOGGER.error("MikeRoom: file exceeds Redis limit");
			return null;
		}
		mediaArrays = new byte[][]{mediaBytes};
		audioObject.setMediaBytes(mediaArrays);
        mediaFormat = MediaFormat.getFormatFromFileName(fileName);
		audioObject.setMediaFormat(mediaFormat);
		if (!audioObject.isValid()) {
			LOGGER.error("MikeRoom: invalid media object");
			return null;
		}
		action = wrapMediaObject(audioObject);
		pkg.setActionObject(action);
        pkg.setIsMultiMedia(true);
		return pkg;
	}

	private ActionPackage createVideoActionPackage(SememePackage sememePkg, RoomRequest request) {
		ActionPackage pkg;
		ClientObjectWrapper action;
		MediaObject mediaObject;
		String fileName;
		MediaFile mediaFile;
		String matchTarget;
		byte[] mediaBytes;
		byte[][] mediaArrays;
        String artist;
        String title;
        MediaFormat mediaFormat;
		
		SharedUtils.checkNotNull(sememePkg);
		SharedUtils.checkNotNull(request);
		pkg = new ActionPackage(sememePkg);
		matchTarget = sememePkg.getSememeObject();
		if (matchTarget == null) {
			LOGGER.error("MikeRoom:createVideoActionPackage: no sememe object");
		}
		mediaFile = MediaFile.getMediaFile(matchTarget);
		if (mediaFile == null) {
			LOGGER.error("Media file is null in Mike room");
			return null;
		}
		fileName = mediaFile.getFileName();
		if (fileName == null) {
			LOGGER.error("File name is null in Mike room");
			return null;
		}
		mediaObject = new MediaObject();
		mediaObject.setClientObjectType(ClientObjectType.VIDEOBYTES);
        artist = mediaFile.getArtist();
        title = mediaFile.getTitle();
        mediaObject.setMediaTitle(title);
        mediaObject.setMediaArtist(artist);
        mediaFormat = MediaFormat.getFormatFromFileName(fileName);
        mediaObject.setMediaFormat(mediaFormat);
		mediaBytes = MediaUtils.fileToByteArray(fileName);
		if (mediaBytes.length > MAXHEADROOM) {
			LOGGER.error("MikeRoom: file exceeds Redis limit");
			return null;
		}
		if (introBytes == null) {
			mediaArrays = new byte[][]{mediaBytes};
		} else {
			// TODO need to allow for multiple media formats; here we're combining
			//  a .MOV and a .FLV or a .MP4....
			if ((mediaBytes.length + introBytes.length) > MAXHEADROOM) {
				LOGGER.error("MikeRoom: file exceeds Redis limit");
				return null;
			}
			mediaArrays = new byte[][] {introBytes, mediaBytes};
		}
		mediaObject.setMediaBytes(mediaArrays);
		if (!mediaObject.isValid()) {
			LOGGER.error("MikeRoom: invalid media object");
			return null;
		}
		action = wrapMediaObject(mediaObject);
		pkg.setActionObject(action);
        pkg.setIsMultiMedia(true);
		return pkg;
	}

	private static ActionPackage createSpeakActionPackage(SememePackage sememePkg, RoomRequest request) {
		ActionPackage pkg;
		ClientObjectWrapper action;
		AudioObject audioObject;
		String speech;
		File tempFile;
		String fileName;
		AudioFormat audioFormat;
		byte[] mediaBytes;
		byte[][] mediaArrays;
		SerialAudioFormat serialFormat;
        MediaFormat mediaFormat;
		
		SharedUtils.checkNotNull(sememePkg);
		SharedUtils.checkNotNull(request);
		pkg = new ActionPackage(sememePkg);
		speech = request.getMessage().trim();
		tempFile = TTSPico.ttsToFile(speech);
		fileName = tempFile.getAbsolutePath();
		audioObject = new AudioObject();
		audioObject.setClientObjectType(ClientObjectType.AUDIOBYTES);
		audioFormat = MediaUtils.getAudioFormat(fileName);
		audioObject.setMediaTitle(speech);
        mediaFormat = MediaFormat.getFormatFromFileName(fileName);
		audioObject.setMediaFormat(mediaFormat);
		serialFormat = MediaUtils.audioFormatToSerial(audioFormat);
		audioObject.setAudioFormat(serialFormat);
		mediaBytes = MediaUtils.fileToByteArray(fileName);
		if (mediaBytes.length > MAXHEADROOM) {
			LOGGER.error("MikeRoom: file exceeds Redis limit");
			return null;
		}
		mediaArrays = new byte[][]{mediaBytes};
		audioObject.setMediaBytes(mediaArrays);
		if (!audioObject.isValid()) {
			LOGGER.error("MikeRoom: invalid media object");
			return null;
		}
		action = wrapMediaObject(audioObject);
		pkg.setActionObject(action);
        pkg.setIsMultiMedia(true);
		return pkg;
	}

	private ActionPackage createSpecialActionPackage(SememePackage sememePkg, RoomRequest request) {
		ActionPackage pkg;
		ClientObjectWrapper action;
		AudioObject audioObject;
		MediaObject mediaObject;
		String target = null;
		Path path;
        String fileName;
		AudioFormat audioFormat;
		byte[] mediaBytes;
		byte[][] mediaArrays;
		SerialAudioFormat serialFormat;
		String title = "";
        String artist = "";
		MediaFile mediaFile;
        MediaFormat mediaFormat;
        
		SharedUtils.checkNotNull(sememePkg);
		SharedUtils.checkNotNull(request);
		pkg = new ActionPackage(sememePkg);
		if (sememePkg.is("pronounce_linux")) {
			target = "Linus-linux.wav"; 
		} else if (sememePkg.is("pronounce_hulles")) {
			target = "hulles_hulles.wav";
		} else if (sememePkg.is("listen_to_her_heart")) {
			target = "listen_to_her_heart.wav";
		} else if (sememePkg.is("sorry_for_it_all")) {
			target = "sorry_for_it_all.mp4";
		} else if (sememePkg.is("dead_sara")) {
			target = "masse_color1.jpg";
		} else if (sememePkg.is("i_fink_u_freeky")) {
			target = "AlixiaFinkUFreeky.wav";
		} else if (sememePkg.is("pronounce_alicia")) {
			if (random.nextBoolean()) {
				target = "Sv-Alicia_Vikander.wav";
			} else {
				target = "pronounce_alicia.mp4";
			}
		} else if (sememePkg.is("WAVE_file")) {
            target = "WAVEFILE.mp4";
        }
		if (target == null) {
			throw new AlixiaException();
		}
		path = findMediaFile(target, specialMedia);
		if (path == null) {
			LOGGER.error("MikeRoom: null file name");
			return null;
		}
		mediaBytes = MediaUtils.pathToByteArray(path);
		if (mediaBytes.length > MAXHEADROOM) {
			LOGGER.error("MikeRoom: file exceeds Redis limit");
			return null;
		}
        fileName = path.toString();
		mediaFile = MediaFile.findMediaFile(fileName);
        if (mediaFile != null) {
            artist = mediaFile.getArtist();
            title = mediaFile.getTitle();
        }
        mediaFormat = MediaFormat.getFormatFromFileName(fileName);
        if (mediaFormat == null) {
            LOGGER.error("MikeRoom: invalid media format");
            return null;
        }
        switch (mediaFormat) {
            case WAV:
                audioObject = new AudioObject();
                audioObject.setClientObjectType(ClientObjectType.AUDIOBYTES);
                audioFormat = MediaUtils.getAudioFormat(path);
                audioObject.setMediaTitle(title);
                audioObject.setMediaArtist(artist);
                audioObject.setMediaFormat(MediaFormat.WAV);
                serialFormat = MediaUtils.audioFormatToSerial(audioFormat);
                audioObject.setAudioFormat(serialFormat);
                mediaArrays = new byte[][]{mediaBytes};
                audioObject.setMediaBytes(mediaArrays);
                if (!audioObject.isValid()) {
                    LOGGER.error("MikeRoom: invalid media object");
                    return null;
                }
                action = wrapMediaObject(audioObject);
                break;
            case JPG:
                mediaObject = new MediaObject();
                mediaObject.setClientObjectType(ClientObjectType.IMAGEBYTES);
                mediaObject.setMediaFormat(MediaFormat.JPG);
                mediaArrays = new byte[][]{mediaBytes};
                mediaObject.setMediaBytes(mediaArrays);
                mediaObject.setMediaTitle("Dead Sara Poster");
                if (!mediaObject.isValid()) {
                    LOGGER.error("MikeRoom: invalid media object");
                    return null;
                }
                action = wrapMediaObject(mediaObject);
                if (sememePkg.is("dead_sara")) {
                    action.setMessage("Dead Sara is super bueno.");
                }
                break;
            case MOV:
                mediaObject = new MediaObject();
                mediaObject.setClientObjectType(ClientObjectType.VIDEOBYTES);
                mediaObject.setMediaTitle(title);
                mediaObject.setMediaArtist(artist);
                mediaObject.setMediaFormat(MediaFormat.MOV);
                if (introBytes == null) {
                    mediaArrays = new byte[][]{mediaBytes};
                    mediaObject.setMediaBytes(mediaArrays);
                } else {
                    if ((mediaBytes.length + introBytes.length) > MAXHEADROOM) {
                        LOGGER.error("MikeRoom: file exceeds Redis limit");
                        return null;
                    }
                    mediaArrays = new byte[][]{introBytes, mediaBytes};
                    mediaObject.setMediaBytes(mediaArrays);
                }
                if (!mediaObject.isValid()) {
                    LOGGER.error("MikeRoom: invalid media object");
                    return null;
                }
                action = wrapMediaObject(mediaObject);
                break;
            case MP4:
                mediaObject = new MediaObject();
                mediaObject.setClientObjectType(ClientObjectType.VIDEOBYTES);
                mediaObject.setMediaTitle(title);
                mediaObject.setMediaArtist(artist);
                mediaObject.setMediaFormat(MediaFormat.MP4);
                if (introBytes == null) {
                    mediaArrays = new byte[][]{mediaBytes};
                    mediaObject.setMediaBytes(mediaArrays);
                } else {
                    if ((mediaBytes.length + introBytes.length) > MAXHEADROOM) {
                        LOGGER.error("MikeRoom: file exceeds Redis limit");
                        return null;
                    }
                    mediaArrays = new byte[][]{introBytes, mediaBytes};
                    LOGGER.debug("MIKE: intro length = {}", introBytes.length);
                    LOGGER.debug("MIKE: media length = {}", mediaBytes.length);
                    mediaObject.setMediaBytes(mediaArrays);
                }
                if (!mediaObject.isValid()) {
                    LOGGER.error("MikeRoom: invalid media object");
                    return null;
                }
                action = wrapMediaObject(mediaObject);
                if (sememePkg.is("sorry_for_it_all")) {
                    action.setMessage("Dead Sara kicks my ass.");
                }
                break;
            default:
                LOGGER.error("MikeRoom: unsupported media file -- support it");
                return null;
        }
		pkg.setActionObject(action);
        pkg.setIsMultiMedia(true);
		return pkg;
	}

	private ActionPackage createNotificationActionPackage(SememePackage sememePkg, RoomRequest request) {
		ActionPackage pkg;
		ClientObjectWrapper action;
		AudioObject audioObject;
		String matchTarget;
        Path path;
		String fileName;
		AudioFormat audioFormat;
		byte[] mediaBytes;
		byte[][] mediaArrays;
		SerialAudioFormat serialFormat;
        MediaFile mediaFile;
        String artist = "";
        String title = "";
        MediaFormat mediaFormat;
		
		SharedUtils.checkNotNull(sememePkg);
		SharedUtils.checkNotNull(request);
		pkg = new ActionPackage(sememePkg);
		// SememeObjectType s/b AUDIOTITLE, btw
		matchTarget = sememePkg.getSememeObject();
		if (matchTarget == null) {
			LOGGER.error("MikeRoom:createNotificationActionPackage: no sememe object");
		}
		path = findMediaFile(matchTarget, notifications);
		if (path == null) {
			LOGGER.error("MikeRoom: null file name");
			return null;
		}
        fileName = path.toString();
		mediaFile = MediaFile.findMediaFile(fileName);
        if (mediaFile != null) {
            artist = mediaFile.getArtist();
            title = mediaFile.getTitle();
        }
		mediaBytes = MediaUtils.pathToByteArray(path);
		if (mediaBytes.length > MAXHEADROOM) {
			LOGGER.error("MikeRoom: file exceeds Redis limit");
			return null;
		}
		audioObject = new AudioObject();
		audioObject.setClientObjectType(ClientObjectType.AUDIOBYTES);
		audioFormat = MediaUtils.getAudioFormat(path);
        audioObject.setMediaTitle(title);
        audioObject.setMediaArtist(artist);
        mediaFormat = MediaFormat.getFormatFromFileName(fileName);
		audioObject.setMediaFormat(mediaFormat);
		serialFormat = MediaUtils.audioFormatToSerial(audioFormat);
		audioObject.setAudioFormat(serialFormat);
		mediaArrays = new byte[][]{mediaBytes};
		audioObject.setMediaBytes(mediaArrays);
		if (!audioObject.isValid()) {
			LOGGER.error("MikeRoom: invalid media object");
			return null;
			}
		action = wrapMediaObject(audioObject);
		action.setMessage(request.getMessage()); // this is the kluged long timer id
		pkg.setActionObject(action);
        pkg.setIsMultiMedia(true);
		return pkg;
	}
	
	private ActionPackage createPromptActionPackage(SememePackage sememePkg, RoomRequest request) {
		ActionPackage pkg;
		ClientObjectWrapper action;
		AudioObject audioObject;
        Path path;
		String fileName;
		AudioFormat audioFormat;
		byte[] mediaBytes;
		byte[][] mediaArrays;
		SerialAudioFormat serialFormat;
        MediaFile mediaFile;
        String artist = "";
        String title = "";
        MediaFormat mediaFormat;
		
		SharedUtils.checkNotNull(sememePkg);
		SharedUtils.checkNotNull(request);
		pkg = new ActionPackage(sememePkg);
		if (sememePkg.is("prompt")) {
			path = getRandomPath(prompts);
		} else if (sememePkg.is("praise")) {
			path = getRandomPath(praise);
		} else {
			path = getRandomPath(exclamations);
		}
        fileName = path.toString();
		mediaFile = MediaFile.findMediaFile(fileName);
        if (mediaFile != null) {
            artist = mediaFile.getArtist();
            title = mediaFile.getTitle();
        }
		audioFormat = MediaUtils.getAudioFormat(path);
		audioObject = new AudioObject();
		audioObject.setClientObjectType(ClientObjectType.AUDIOBYTES);
        audioObject.setMediaTitle(title);
        audioObject.setMediaArtist(artist);
		serialFormat = MediaUtils.audioFormatToSerial(audioFormat);
		audioObject.setAudioFormat(serialFormat);
        mediaFormat = MediaFormat.getFormatFromFileName(fileName);
		audioObject.setMediaFormat(mediaFormat);
		mediaBytes = MediaUtils.pathToByteArray(path);
		if (mediaBytes.length > MAXHEADROOM) {
			LOGGER.error("MikeRoom: file exceeds Redis limit");
			return null;
		}
		mediaArrays = new byte[][]{mediaBytes};
		audioObject.setMediaBytes(mediaArrays);
		if (!audioObject.isValid()) {
			LOGGER.error("MikeRoom: invalid media object");
			return null;
		}
		action = wrapMediaObject(audioObject);
		pkg.setActionObject(action);
        pkg.setIsMultiMedia(true);
		return pkg;
	}

	private ActionPackage createAnalysisActionPackage(SememePackage sememePkg, RoomRequest request) {
		MediaAnalysis analysis;
		List<Match> artistMatches;
		List<Match> titleMatches;
		String name;
		ActionPackage pkg;
		
		SharedUtils.checkNotNull(sememePkg);
		SharedUtils.checkNotNull(request);
		pkg = new ActionPackage(sememePkg);
		analysis = new MediaAnalysis();
		name = request.getMessage();
		artistMatches = matchArtists(name, 3);
		titleMatches = matchTitles(name, 3);
		analysis.setArtists(artistMatches);
		analysis.setTitles(titleMatches);
		analysis.setInputToMatch(name);
		pkg.setActionObject(analysis);
		return pkg;
	}
	
    private static ClientObjectWrapper wrapMediaObject(MediaObject obj) {
        ClientObjectWrapper wrapper;
        String title;
        String artist;
        StringBuilder sb;
        String expl;
        
		wrapper = new ClientObjectWrapper(obj);
        title = obj.getMediaTitle();
        artist = obj.getMediaArtist();
        if (title != null && !title.isEmpty()) {
            sb = new StringBuilder(title);
            if (artist != null && !artist.isEmpty()) {
                sb.append(" by ");
                sb.append(artist);
            }
            expl = sb.toString();
        } else {
            expl = artist;
        }
        wrapper.setExplanation(expl);
        return wrapper;
    }
    
    void pushNotification(AlixianID alixianID, String message, String explanation) {
		ClientDialogResponse clientResponse;
		SerialSememe sememe;
		DialogResponse response;
        
        SharedUtils.checkNotNull(alixianID);
        SharedUtils.checkNotNull(message);
        SharedUtils.nullsOkay(explanation);

        clientResponse = new ClientDialogResponse();
		response = clientResponse.getDialogResponse();
		response.setLanguage(Language.AMERICAN_ENGLISH);
		response.setFromAlixianID(AlixiaConstants.getAlixiaAlixianID());
		response.setExplanation(explanation);
		response.setToAlixianID(alixianID);
		sememe = SerialSememe.find("notify");
		response.setResponseAction(sememe);
		response.setMessage(message);
		super.postPushRequest(clientResponse);
        
    }
    
    /**
     * Straight from the @link{ExecutorService} javadoc....
     * 
     * @param pool The updateExecutor
     */
    private static void shutdownAndAwaitTermination(ExecutorService pool) {
        
        pool.shutdown(); // Disable new tasks from being submitted
        try {
            // Wait a while for existing tasks to terminate
            if (!pool.awaitTermination(60, TimeUnit.SECONDS)) {
                pool.shutdownNow(); // Cancel currently executing tasks
                // Wait a while for tasks to respond to being cancelled
                if (!pool.awaitTermination(60, TimeUnit.SECONDS)) {
                    LOGGER.error("Pool did not terminate");
                }
            }
        } catch (InterruptedException ie) {
            // (Re-)Cancel if current thread also interrupted
            pool.shutdownNow();
            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }
    }
    
	private static Path findMediaFile(String name, List<Path> mediaFiles) {
		String pathStr;
        
		for (Path path : mediaFiles) {
            pathStr = path.toString();
			LOGGER.debug("findMediaFile : {}", pathStr);
			if (pathStr.contains(name)) {
				return path;
			}
		}
		return null;
	}
	
	@Override
	protected Set<SerialSememe> loadSememes() {
		Set<SerialSememe> sememes;
		
		sememes = new HashSet<>();
		sememes.add(SerialSememe.find("match_artists_and_titles"));
		sememes.add(SerialSememe.find("play_artist"));
		sememes.add(SerialSememe.find("play_title"));
		sememes.add(SerialSememe.find("play_video"));
		sememes.add(SerialSememe.find("prompt"));
		sememes.add(SerialSememe.find("speak"));
		sememes.add(SerialSememe.find("praise"));
		sememes.add(SerialSememe.find("exclamation"));
		sememes.add(SerialSememe.find("nothing_to_do"));
		sememes.add(SerialSememe.find("pronounce_linux"));
		sememes.add(SerialSememe.find("pronounce_alicia"));
		sememes.add(SerialSememe.find("pronounce_hulles"));
		sememes.add(SerialSememe.find("listen_to_her_heart"));
		sememes.add(SerialSememe.find("sorry_for_it_all"));
		sememes.add(SerialSememe.find("dead_sara"));
		sememes.add(SerialSememe.find("notification_medium"));
		sememes.add(SerialSememe.find("random_music"));
        sememes.add(SerialSememe.find("reload_media_library"));
        sememes.add(SerialSememe.find("i_fink_u_freeky"));
        sememes.add(SerialSememe.find("WAVE_file"));
		return sememes;
	}

	@Override
	protected void processRoomAnnouncement(RoomAnnouncement announcement) {
	}
}
