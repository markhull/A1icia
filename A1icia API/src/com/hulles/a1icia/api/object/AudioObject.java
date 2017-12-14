package com.hulles.a1icia.api.object;

import com.hulles.a1icia.api.shared.SharedUtils;
import com.hulles.a1icia.media.audio.SerialAudioFormat;

public final class AudioObject extends MediaObject {
	private static final long serialVersionUID = -3047896290125769038L;
	private SerialAudioFormat format;
	
	public AudioObject() {
		super();
	}
	
	public SerialAudioFormat getAudioFormat() {
		
		return format;
	}
	
	public void setAudioFormat(SerialAudioFormat format) {
		
		SharedUtils.checkNotNull(format);
		this.format = format;
	}
	
	@Override
	public boolean isValid() {
		
		if (format == null) {
			return false;
		}
		return super.isValid();
	}
}
