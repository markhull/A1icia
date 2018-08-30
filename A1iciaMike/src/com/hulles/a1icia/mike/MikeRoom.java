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
package com.hulles.a1icia.mike;

import com.hulles.a1icia.api.A1iciaConstants;
import com.hulles.a1icia.api.dialog.DialogResponse;
import com.hulles.a1icia.api.jebus.JebusHub;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sound.sampled.AudioFormat;

import com.hulles.a1icia.api.object.A1iciaClientObject.ClientObjectType;
import com.hulles.a1icia.api.object.AudioObject;
import com.hulles.a1icia.api.object.MediaObject;
import com.hulles.a1icia.api.remote.A1icianID;
import com.hulles.a1icia.api.shared.A1iciaException;
import com.hulles.a1icia.api.shared.ApplicationKeys;
import com.hulles.a1icia.api.shared.ApplicationKeys.ApplicationKey;
import com.hulles.a1icia.api.shared.SerialSememe;
import com.hulles.a1icia.api.shared.SharedUtils;
import com.hulles.a1icia.api.tools.A1iciaUtils;
import com.hulles.a1icia.cayenne.MediaFile;
import com.hulles.a1icia.house.ClientDialogResponse;
import com.hulles.a1icia.media.Language;
import com.hulles.a1icia.media.MediaFormat;
import com.hulles.a1icia.media.MediaUtils;
import com.hulles.a1icia.media.audio.SerialAudioFormat;
import com.hulles.a1icia.media.audio.TTSPico;
import com.hulles.a1icia.mike.MediaLibrary.MediaUpdateStats;
import com.hulles.a1icia.room.Room;
import com.hulles.a1icia.room.UrRoom;
import com.hulles.a1icia.room.document.ClientObjectWrapper;
import com.hulles.a1icia.room.document.MediaAnalysis;
import com.hulles.a1icia.room.document.MessageAction;
import com.hulles.a1icia.room.document.RoomActionObject;
import com.hulles.a1icia.room.document.RoomAnnouncement;
import com.hulles.a1icia.room.document.RoomRequest;
import com.hulles.a1icia.room.document.RoomResponse;
import com.hulles.a1icia.ticket.ActionPackage;
import com.hulles.a1icia.ticket.SememePackage;
import com.hulles.a1icia.ticket.Ticket;
import com.hulles.a1icia.tools.FuzzyMatch;
import com.hulles.a1icia.tools.FuzzyMatch.Match;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Mike Room is our media room. Mike has a library of .wav files that he can broadcast to pretty 
 * much anyone via Redis / Jedis. Clients can also request media files; these are also "sent" via 
 * Redis. What actually happens is that they are stored as byte arrays in Redis and retrieved
 * by (possibly remote) Redis clients. I note in passing that, according to Jedis, the maximum 
 * byte array size is 1GB, but there is a Redis hard output buffer limit for pub/sub clients
 * which, if exceeded, causes the Redis client to be terminated. Ouch. The current limit for A1icia 
 * can be found in JebusHub.
 * 
 * @author hulles
 *
 */
public final class MikeRoom extends UrRoom {
	private final static int MAXHEADROOM = JebusHub.getMaxHardOutputBufferLimit();
    private final static int POOLSIZE = 2;
	private final static Logger LOGGER = Logger.getLogger("A1iciaMike.MikeRoom");
	private final static Level LOGLEVEL = A1iciaConstants.getA1iciaLogLevel();
//	private final static Level LOGLEVEL = Level.INFO;
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
    private final MediaLibrary mediaLibrary;
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
			LOGGER.log(LOGLEVEL, MediaUtils.getAudioFormatString(fileName));
		} catch (Exception e) {
			throw new A1iciaException("Can't log audio format", e);
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
		ClientObjectWrapper cow;
		
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
							LOGGER.log(LOGLEVEL, "We got some learning => {0} : {1}", 
                                    new String[]{msgAction.getMessage(), msgAction.getExplanation()});
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
		
		updateExecutor = Executors.newFixedThreadPool(POOLSIZE);
		lib = appKeys.getKey(ApplicationKey.MIKELIBRARY);
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
        
        // load custom A1icia video intro
		path = findMediaFile("AV.mov", specialMedia);
		if (path == null) {
			A1iciaUtils.error("MikeRoom: null intro file name");
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
				return createSpecialActionPackage(sememePkg, request);
			case "match_artists_and_titles":
				return createAnalysisActionPackage(sememePkg, request);
			case "notification_medium":
				return createNotificationActionPackage(sememePkg, request);
			default:
				throw new A1iciaException("Received unknown sememe in " + getThisRoom());
		}
	}

    private ActionPackage reloadMediaLibrary(SememePackage sememePkg, RoomRequest request) {
        ActionPackage pkg;
        MessageAction action;
        Future<MediaUpdateStats> updateNotification;
        A1icianID a1icianID;
        
        a1icianID = request.getTicket().getFromA1icianID();
        // construct an ExecutorService that returns a Future of MediaUpdateStats
        updateNotification = updateMediaLibrary(a1icianID, true);
        updateExecutor.execute(new Runnable() {
            
            @Override
            public void run() {
                MediaUpdateStats stats;
                String message;
                String explanation;
                A1icianID a1icianID;
                StringBuilder sb;
               
                try {
                    // future.get() blocks until the result is ready
                    stats = updateNotification.get();
                } catch (InterruptedException ex) {
        			A1iciaUtils.error("Mike library update interrupted", ex);
                    return;
                } catch (ExecutionException ex) {
        			A1iciaUtils.error("Mike library update execution exception", ex);
                    return;
                }
                a1icianID = stats.getA1icianID();
                message = "I updated the media library.";
                sb = new StringBuilder();
                sb.append("Media files updated: ");
                sb.append(stats.getMediaFilesUpdated());
                sb.append("\nMedia files deleted: ");
                sb.append(stats.getMediaFilesDeleted());
                sb.append("\n");
                explanation = sb.toString();
                pushNotification(a1icianID, message, explanation);
           }
        });
        pkg = new ActionPackage(sememePkg);
		action = new MessageAction();
		action.setMessage("I am updating the media library now.");
        pkg.setActionObject(action);
        return pkg;
    }

 	private Future<MediaUpdateStats> updateMediaLibrary(final A1icianID a1icianToNotifyID, 
            final boolean forceUpdate) {

        SharedUtils.nullsOkay(a1icianToNotifyID);
        SharedUtils.checkNotNull(forceUpdate);
        return updateExecutor.submit(new Callable<MediaUpdateStats>() {
			@Override
			public MediaUpdateStats call() {
                MediaUpdateStats stats;
                
				stats = mediaLibrary.updateMediaLibrary(forceUpdate);
                stats.setA1icianID(a1icianToNotifyID);
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
		
		SharedUtils.checkNotNull(sememePkg);
		SharedUtils.checkNotNull(request);
		pkg = new ActionPackage(sememePkg);
		matchTarget = sememePkg.getSememeObject();
		if (matchTarget == null) {
			A1iciaUtils.error("MikeRoom:createArtistActionPackage: no sememe object");
		}
		mediaFiles = MediaFile.getMediaFiles(matchTarget);
		if (mediaFiles != null) {
			mediaFile = getRandomMediaFile(mediaFiles);
			fileName = mediaFile.getFileName();
			if (fileName == null) {
				A1iciaUtils.error("MikeRoom: file name is null");
				return null;
			}
			audioFormat = MediaUtils.getAudioFormat(fileName);
			audioObject = new AudioObject();
			audioObject.setClientObjectType(ClientObjectType.AUDIOBYTES);
			audioObject.setMediaTitle(fileName);
			serialFormat = MediaUtils.audioFormatToSerial(audioFormat);
			audioObject.setAudioFormat(serialFormat);
			mediaBytes = MediaUtils.fileToByteArray(fileName);
			if (mediaBytes.length > MAXHEADROOM) {
				A1iciaUtils.error("MikeRoom: file exceeds Redis limit");
				return null;
			}
			mediaArrays = new byte[][]{mediaBytes};
			audioObject.setMediaBytes(mediaArrays);
			audioObject.setMediaFormat(MediaFormat.MP3);
			if (!audioObject.isValid()) {
				A1iciaUtils.error("MikeRoom: invalid media object");
				return null;
			}
			action = new ClientObjectWrapper(audioObject);
		}
		pkg.setActionObject(action);
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
		
		SharedUtils.checkNotNull(sememePkg);
		SharedUtils.checkNotNull(request);
		pkg = new ActionPackage(sememePkg);
		matchTarget = sememePkg.getSememeObject();
		if (matchTarget == null) {
			A1iciaUtils.error("MikeRoom:createTitleActionPackage: no sememe object");
		}
		mediaFile = MediaFile.getMediaFile(matchTarget);
		if (mediaFile == null) {
			A1iciaUtils.error("Media file is null in Mike room");
			return null;
		}
		fileName = mediaFile.getFileName();
		if (fileName == null) {
			A1iciaUtils.error("File name is null in Mike room");
			return null;
		}
		audioFormat = MediaUtils.getAudioFormat(fileName);
		audioObject = new AudioObject();
		audioObject.setClientObjectType(ClientObjectType.AUDIOBYTES);
		audioObject.setMediaTitle(fileName);
		serialFormat = MediaUtils.audioFormatToSerial(audioFormat);
		audioObject.setAudioFormat(serialFormat);
		mediaBytes = MediaUtils.fileToByteArray(fileName);
		if (mediaBytes.length > MAXHEADROOM) {
			A1iciaUtils.error("MikeRoom: file exceeds Redis limit");
			return null;
		}
		mediaArrays = new byte[][]{mediaBytes};
		audioObject.setMediaBytes(mediaArrays);
		audioObject.setMediaFormat(MediaFormat.MP3);
		if (!audioObject.isValid()) {
			A1iciaUtils.error("MikeRoom: invalid media object");
			return null;
		}
		action = new ClientObjectWrapper(audioObject);
		pkg.setActionObject(action);
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
		
		SharedUtils.checkNotNull(sememePkg);
		SharedUtils.checkNotNull(request);
		pkg = new ActionPackage(sememePkg);
		mediaFile = MediaFile.getRandomMediaFile();
		if (mediaFile == null) {
			A1iciaUtils.error("Media file is null in Mike room");
			return null;
		}
		fileName = mediaFile.getFileName();
		if (fileName == null) {
			A1iciaUtils.error("File name is null in Mike room");
			return null;
		}
		audioFormat = MediaUtils.getAudioFormat(fileName);
		audioObject = new AudioObject();
		audioObject.setClientObjectType(ClientObjectType.AUDIOBYTES);
		audioObject.setMediaTitle(fileName);
		serialFormat = MediaUtils.audioFormatToSerial(audioFormat);
		audioObject.setAudioFormat(serialFormat);
		mediaBytes = MediaUtils.fileToByteArray(fileName);
		if (mediaBytes.length > MAXHEADROOM) {
			A1iciaUtils.error("MikeRoom: file exceeds Redis limit");
			return null;
		}
		mediaArrays = new byte[][]{mediaBytes};
		audioObject.setMediaBytes(mediaArrays);
		audioObject.setMediaFormat(MediaFormat.MP3);
		if (!audioObject.isValid()) {
			A1iciaUtils.error("MikeRoom: invalid media object");
			return null;
		}
		action = new ClientObjectWrapper(audioObject);
		pkg.setActionObject(action);
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
		
		SharedUtils.checkNotNull(sememePkg);
		SharedUtils.checkNotNull(request);
		pkg = new ActionPackage(sememePkg);
		matchTarget = sememePkg.getSememeObject();
		if (matchTarget == null) {
			A1iciaUtils.error("MikeRoom:createVideoActionPackage: no sememe object");
		}
		mediaFile = MediaFile.getMediaFile(matchTarget);
		if (mediaFile == null) {
			A1iciaUtils.error("Media file is null in Mike room");
			return null;
		}
		fileName = mediaFile.getFileName();
		if (fileName == null) {
			A1iciaUtils.error("File name is null in Mike room");
			return null;
		}
		mediaObject = new MediaObject();
		mediaObject.setClientObjectType(ClientObjectType.VIDEOBYTES);
		mediaObject.setMediaTitle(fileName);
		if (fileName.endsWith("MP4")) {
			mediaObject.setMediaFormat(MediaFormat.MP4);
		} else if (fileName.endsWith("FLV")) {
			mediaObject.setMediaFormat(MediaFormat.MP4);
		} else {
			A1iciaUtils.error("MikeRoom: unknown video media type");
			return null;
		}
		mediaBytes = MediaUtils.fileToByteArray(fileName);
		if (mediaBytes.length > MAXHEADROOM) {
			A1iciaUtils.error("MikeRoom: file exceeds Redis limit");
			return null;
		}
		if (introBytes == null) {
			mediaArrays = new byte[][]{mediaBytes};
		} else {
			// TODO need to allow for multiple media formats; here we're combining
			//  a .MOV and a .FLV or a .MP4....
			if ((mediaBytes.length + introBytes.length) > MAXHEADROOM) {
				A1iciaUtils.error("MikeRoom: file exceeds Redis limit");
				return null;
			}
			mediaArrays = new byte[][] {introBytes, mediaBytes};
		}
		mediaObject.setMediaBytes(mediaArrays);
		if (!mediaObject.isValid()) {
			A1iciaUtils.error("MikeRoom: invalid media object");
			return null;
		}
		action = new ClientObjectWrapper(mediaObject);
		pkg.setActionObject(action);
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
		
		SharedUtils.checkNotNull(sememePkg);
		SharedUtils.checkNotNull(request);
		pkg = new ActionPackage(sememePkg);
		speech = request.getMessage().trim();
		tempFile = TTSPico.ttsToFile(speech);
		fileName = tempFile.getAbsolutePath();
		audioObject = new AudioObject();
		audioObject.setClientObjectType(ClientObjectType.AUDIOBYTES);
		audioFormat = MediaUtils.getAudioFormat(fileName);
		audioObject.setMediaTitle(fileName);
		audioObject.setMediaFormat(MediaFormat.WAV);
		serialFormat = MediaUtils.audioFormatToSerial(audioFormat);
		audioObject.setAudioFormat(serialFormat);
		mediaBytes = MediaUtils.fileToByteArray(fileName);
		if (mediaBytes.length > MAXHEADROOM) {
			A1iciaUtils.error("MikeRoom: file exceeds Redis limit");
			return null;
		}
		mediaArrays = new byte[][]{mediaBytes};
		audioObject.setMediaBytes(mediaArrays);
		if (!audioObject.isValid()) {
			A1iciaUtils.error("MikeRoom: invalid media object");
			return null;
		}
		action = new ClientObjectWrapper(audioObject);
		pkg.setActionObject(action);
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
			target = "A1iciaFinkUFreeky.wav";
		} else if (sememePkg.is("pronounce_alicia")) {
			if (random.nextBoolean()) {
				target = "Sv-Alicia_Vikander.wav";
			} else {
				target = "pronounce_alicia.mov";
			}
		}
		if (target == null) {
			throw new A1iciaException();
		}
		path = findMediaFile(target, specialMedia);
		if (path == null) {
			A1iciaUtils.error("MikeRoom: null file name");
			return null;
		}
		mediaBytes = MediaUtils.pathToByteArray(path);
		if (mediaBytes.length > MAXHEADROOM) {
			A1iciaUtils.error("MikeRoom: file exceeds Redis limit");
			return null;
		}
        fileName = path.toString();
		if (fileName.endsWith("wav")) {
			audioObject = new AudioObject();
			audioObject.setClientObjectType(ClientObjectType.AUDIOBYTES);
			audioFormat = MediaUtils.getAudioFormat(path);
			audioObject.setMediaTitle(fileName);
			audioObject.setMediaFormat(MediaFormat.WAV);
			serialFormat = MediaUtils.audioFormatToSerial(audioFormat);
			audioObject.setAudioFormat(serialFormat);
			mediaArrays = new byte[][]{mediaBytes};
			audioObject.setMediaBytes(mediaArrays);
			if (!audioObject.isValid()) {
				A1iciaUtils.error("MikeRoom: invalid media object");
				return null;
			}
			action = new ClientObjectWrapper(audioObject);
		} else if (fileName.endsWith("jpg")) {
			mediaObject = new MediaObject();
			mediaObject.setClientObjectType(ClientObjectType.IMAGEBYTES);
			mediaObject.setMediaFormat(MediaFormat.JPG);
			mediaArrays = new byte[][]{mediaBytes};
			mediaObject.setMediaBytes(mediaArrays);
			mediaObject.setMediaTitle("Dead Sara Poster");
			if (!mediaObject.isValid()) {
				A1iciaUtils.error("MikeRoom: invalid media object");
				return null;
			}
			action = new ClientObjectWrapper(mediaObject);
			if (sememePkg.is("dead_sara")) {
				action.setMessage("Dead Sara is super bueno.");
			}
		} else if (fileName.endsWith("mov")) {
			mediaObject = new MediaObject();
			mediaObject.setClientObjectType(ClientObjectType.VIDEOBYTES);
			mediaObject.setMediaTitle(fileName);
			mediaObject.setMediaFormat(MediaFormat.MOV);
			if (introBytes == null) {
				mediaArrays = new byte[][]{mediaBytes};
				mediaObject.setMediaBytes(mediaArrays);
			} else {
				if ((mediaBytes.length + introBytes.length) > MAXHEADROOM) {
					A1iciaUtils.error("MikeRoom: file exceeds Redis limit");
					return null;
				}
				mediaArrays = new byte[][]{introBytes, mediaBytes};
				mediaObject.setMediaBytes(mediaArrays);
			}
			if (!mediaObject.isValid()) {
				A1iciaUtils.error("MikeRoom: invalid media object");
				return null;
			}
			action = new ClientObjectWrapper(mediaObject);
		} else if (fileName.endsWith("mp4")) {
			mediaObject = new MediaObject();
			mediaObject.setClientObjectType(ClientObjectType.VIDEOBYTES);
			mediaObject.setMediaTitle(fileName);
			mediaObject.setMediaFormat(MediaFormat.MP4);
			if (introBytes == null) {
				mediaArrays = new byte[][]{mediaBytes};
				mediaObject.setMediaBytes(mediaArrays);
			} else {
				if ((mediaBytes.length + introBytes.length) > MAXHEADROOM) {
					A1iciaUtils.error("MikeRoom: file exceeds Redis limit");
					return null;
				}
				mediaArrays = new byte[][]{introBytes, mediaBytes};
				System.out.println("MIKE: intro length = " + introBytes.length);
				System.out.println("MIKE: media length = " + mediaBytes.length);
				mediaObject.setMediaBytes(mediaArrays);
			}
			if (!mediaObject.isValid()) {
				A1iciaUtils.error("MikeRoom: invalid media object");
				return null;
			}
			action = new ClientObjectWrapper(mediaObject);
			if (sememePkg.is("sorry_for_it_all")) {
				action.setMessage("Dead Sara kicks my ass.");
			}
		} else {
			A1iciaUtils.error("MikeRoom: unsupported media file -- support it");
			return null;
		}
		pkg.setActionObject(action);
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
		
		SharedUtils.checkNotNull(sememePkg);
		SharedUtils.checkNotNull(request);
		pkg = new ActionPackage(sememePkg);
		// SememeObjectType s/b AUDIOTITLE, btw
		matchTarget = sememePkg.getSememeObject();
		if (matchTarget == null) {
			A1iciaUtils.error("MikeRoom:createNotificationActionPackage: no sememe object");
		}
		path = findMediaFile(matchTarget, notifications);
		if (path == null) {
			A1iciaUtils.error("MikeRoom: null file name");
			return null;
		}
		mediaBytes = MediaUtils.pathToByteArray(path);
		if (mediaBytes.length > MAXHEADROOM) {
			A1iciaUtils.error("MikeRoom: file exceeds Redis limit");
			return null;
		}
		audioObject = new AudioObject();
		audioObject.setClientObjectType(ClientObjectType.AUDIOBYTES);
		audioFormat = MediaUtils.getAudioFormat(path);
		audioObject.setMediaTitle(path.toString());
		audioObject.setMediaFormat(MediaFormat.WAV);
		serialFormat = MediaUtils.audioFormatToSerial(audioFormat);
		audioObject.setAudioFormat(serialFormat);
		mediaArrays = new byte[][]{mediaBytes};
		audioObject.setMediaBytes(mediaArrays);
		if (!audioObject.isValid()) {
			A1iciaUtils.error("MikeRoom: invalid media object");
			return null;
			}
		action = new ClientObjectWrapper(audioObject);
		action.setMessage(request.getMessage()); // this is the kluged long timer id
		pkg.setActionObject(action);
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
		audioFormat = MediaUtils.getAudioFormat(path);
		audioObject = new AudioObject();
		audioObject.setClientObjectType(ClientObjectType.AUDIOBYTES);
		audioObject.setMediaTitle(path.toString());
		serialFormat = MediaUtils.audioFormatToSerial(audioFormat);
		audioObject.setAudioFormat(serialFormat);
		audioObject.setMediaFormat(MediaFormat.WAV);
		mediaBytes = MediaUtils.pathToByteArray(path);
		if (mediaBytes.length > MAXHEADROOM) {
			A1iciaUtils.error("MikeRoom: file exceeds Redis limit");
			return null;
		}
		mediaArrays = new byte[][]{mediaBytes};
		audioObject.setMediaBytes(mediaArrays);
		if (!audioObject.isValid()) {
			A1iciaUtils.error("MikeRoom: invalid media object");
			return null;
		}
		action = new ClientObjectWrapper(audioObject);
		pkg.setActionObject(action);
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
	
    private void pushNotification(A1icianID a1icianID, String message, String explanation) {
		ClientDialogResponse clientResponse;
		SerialSememe sememe;
		DialogResponse response;
        
        SharedUtils.checkNotNull(a1icianID);
        SharedUtils.checkNotNull(message);
        SharedUtils.nullsOkay(explanation);

        clientResponse = new ClientDialogResponse();
		response = clientResponse.getDialogResponse();
		response.setLanguage(Language.AMERICAN_ENGLISH);
		response.setFromA1icianID(A1iciaConstants.getA1iciaA1icianID());
		response.setExplanation(explanation);
		response.setToA1icianID(a1icianID);
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
    private void shutdownAndAwaitTermination(ExecutorService pool) {
        
        pool.shutdown(); // Disable new tasks from being submitted
        try {
            // Wait a while for existing tasks to terminate
            if (!pool.awaitTermination(60, TimeUnit.SECONDS)) {
                pool.shutdownNow(); // Cancel currently executing tasks
                // Wait a while for tasks to respond to being cancelled
                if (!pool.awaitTermination(60, TimeUnit.SECONDS)) {
                    System.err.println("Pool did not terminate");
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
			LOGGER.log(LOGLEVEL, "findMediaFile : {0}", pathStr);
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
		return sememes;
	}

	@Override
	protected void processRoomAnnouncement(RoomAnnouncement announcement) {
	}
}
