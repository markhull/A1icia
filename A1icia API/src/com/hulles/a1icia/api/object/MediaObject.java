package com.hulles.a1icia.api.object;

import com.hulles.a1icia.api.jebus.JebusApiHub;
import com.hulles.a1icia.api.shared.SharedUtils;
import com.hulles.a1icia.media.MediaFormat;
import com.hulles.a1icia.media.MediaUtils;

/**
 * A MediaObject is an instance of an image, an audio file, or a video. Actually,
 * there may be multiple segments in the byte[][]. This is an array array 
 * not to cram multiple songs from a playlist into one object (although I suppose it
 * might work for a small playlist, now that I mention it); it's more to concatenate a 
 * couple of files into a media package. The causal instance combines the 
 * A1iciaVision intro with a video file so they play seamlessly back-to-back and make us
 * look like a class operation.
 * 
 * @author hulles
 *
 */
public class MediaObject implements A1iciaClientObject {
	private static final long serialVersionUID = -2305398153211376016L;
	private static final int MAXHEADROOM = JebusApiHub.getMaxHardOutputBufferLimit();
	private byte[][] mediaBytes;
	private MediaFormat format;
	private ClientObjectType type;
	private String mediaTitle;
	
	public MediaObject() {
		// needs no-arg constructor
	}
	
	public MediaFormat getMediaFormat() {
		
		return format;
	}

	public void setMediaFormat(MediaFormat format) {
		
		SharedUtils.checkNotNull(format);
		this.format = format;
	}

	public byte[][] getMediaBytes() {
		
		return mediaBytes;
	}

	public void setMediaBytes(byte[][] bytes) {
		
		SharedUtils.checkNotNull(bytes);
		this.mediaBytes = bytes;
	}

	public String getMediaTitle() {
		
		return mediaTitle;
	}

	public void setMediaTitle(String title) {
		
		MediaUtils.nullsOkay(title);
		this.mediaTitle = title;
	}
	
	@Override
	public ClientObjectType getClientObjectType() {
		return type;
	}

	public void setClientObjectType(ClientObjectType type) {
		
		SharedUtils.checkNotNull(type);
		this.type = type;
	}

	@Override
	public boolean isValid() {
		
		if (mediaBytes == null) {
			return false;
		}
		if (mediaBytes.length > MAXHEADROOM) {
			return false;
		}
		if (format == null) {
			return false;
		}
		if (type == null) {
			return false;
		}
		// title can be null
		return true;
	}
}
