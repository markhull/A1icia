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
package com.hulles.a1icia.media.audio;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;

import com.hulles.a1icia.media.A1iciaMediaException;
import com.hulles.a1icia.media.MediaUtils;

public class AudioBytePlayer {
//	private final static int FILETTL = 60 * 60 * 8; // files live in Redis for 8 hours 
	
	/**
	 * playAudioFromByteArray will only play Java Sound (javax.sound) supported formats,
	 * notably .wav files.
	 *  
	 * @param audio The audio stream as an array of bytes
	 * @param length The length to play, in seconds; null if entire stream
	 * 
	 * @throws Exception
	 */
	public static void playAudioFromByteArray(byte[] audio, Integer length) throws Exception {
		final AudioFormat format;
		
		MediaUtils.checkNotNull(audio);
		// this is the format used by pico2wave
		format = new AudioFormat(16000.0f, // sampleRate
			      16, // sampleSizeInBits
			      1, // channels
			      true, // signed
			      false); // bigEndian
		playAudioFromByteArray(format, audio, length);
	}
	
	/**
	 * playAudioFromByteArray will only play Java Sound (javax.sound) supported formats,
	 * notably .wav files.
	 *  
	 * @param format The AudioFormat of the audio stream
	 * @param audio The audio stream as an array of bytes
	 * @param length The length to play, in seconds; null if entire stream
	 * 
	 * @throws Exception
	 */
	@SuppressWarnings("resource")
	public static void playAudioFromByteArray(AudioFormat format, byte[] audio, Integer length) throws Exception {
		InputStream input;
		final AudioInputStream ais;
		DataLine.Info info;
		final SourceDataLine line;
		Thread playThread;
		long lengthInMicroseconds;
		
		MediaUtils.checkNotNull(format);
		MediaUtils.checkNotNull(audio);
		MediaUtils.nullsOkay(length);
		if (length == null) {
			lengthInMicroseconds = Long.MAX_VALUE;
		} else {
			lengthInMicroseconds = length * 1000000;
		}
		input = new ByteArrayInputStream(audio);
		ais = new AudioInputStream(input, format, audio.length / format.getFrameSize());
		info = new DataLine.Info(SourceDataLine.class, format);
		try {
			line = (SourceDataLine) AudioSystem.getLine(info);
		} catch (IllegalArgumentException ex) {
			throw new A1iciaMediaException("AudioBytePlayer: can't get line", ex);
		}
		line.open(format);
		line.start();

		Runnable runner = new Runnable() {

			@Override
			public void run() {
				int bufferSize;
				byte[] buffer;
				int count;
				
				bufferSize = (int) format.getSampleRate() * format.getFrameSize();
				buffer = new byte[bufferSize];
				try {
					while ((count = ais.read(buffer, 0, buffer.length)) != -1) {
						if (count > 0) {
							line.write(buffer, 0, count);
						}
						if (line.getMicrosecondPosition() > lengthInMicroseconds) {
							break;
						}
					}
					line.drain();
					line.close();
				} catch (Exception e) {
					line.close();
					throw new A1iciaMediaException("AudioBytePlayer: IO exception in byte player", e);
				}
			}
		};
		playThread = new Thread(runner);
		playThread.start();
	}

	/**
	 * Get an audio file, convert it to a byte array, and play it.
	 * 
	 * @see playAudioFromByteArray
	 * 
	 * @param filename The file name of the audio file
	 * @param length The length to play, in seconds; null if entire stream
	 * 
	 * @throws Exception
	 */
	public static void playAudioFromFile(String filename, Integer length) throws Exception {
		AudioFormat format;
		byte[] audioBytes;
		
		MediaUtils.checkNotNull(filename);
		format = MediaUtils.getAudioFormat(filename);
		audioBytes = MediaUtils.fileToByteArray(filename);
		playAudioFromByteArray(format, audioBytes, length);
	}
	

	/**
	 * Get an audio file, convert it to a byte array, and play it using the specified AudioFormat.
	 * 
	 * @see playAudioFromByteArray
	 * 
	 * @param filename The file name of the audio file
	 * @param format The AudioFormat
	 * @param length The length to play, in seconds; null if entire stream
	 * 
	 * @throws Exception
	 */
	public static void playAudioFromFile(String filename, AudioFormat format, Integer length) throws Exception {
		byte[] audioBytes;
		
		MediaUtils.checkNotNull(filename);
		MediaUtils.checkNotNull(format);
		audioBytes = MediaUtils.fileToByteArray(filename);
		playAudioFromByteArray(format, audioBytes, length);
	}
	
}
