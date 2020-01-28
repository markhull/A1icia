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
package com.hulles.alixia.media;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hulles.alixia.media.audio.SerialAudioFormat;

public class MediaUtils {
	private final static Logger LOGGER = LoggerFactory.getLogger(MediaUtils.class);
    private final static String IMAGE_TYPE = "png";
    private final static String MPVEXEC = "mpv";
    private final static String MPVOPT1 = "--title=AlixiaVision";
    private final static String MPVOPT2 = "--no-terminal";
    
    public static void playMediaBytes(byte[][] bytes, MediaFormat format) {
    	Path path;
    	String[] args;
    	int nameIx = 0;
    	
    	MediaUtils.checkNotNull(bytes);
    	MediaUtils.checkNotNull(format);
    	args = new String[3 + bytes.length];
    	args[nameIx++] = MPVEXEC;
    	args[nameIx++] = MPVOPT1;
    	args[nameIx++] = MPVOPT2;
    	for (byte[] byteArray : bytes) {
        	path = byteArrayToFile(byteArray, format);
        	args[nameIx++] = path.toString();
    	}
		MediaUtils.statCommand(args);
    }

	public static byte[] pathToByteArray(Path path) {
		byte[] bytes = null;
		
		MediaUtils.checkNotNull(path);
		try {
			bytes = Files.readAllBytes(path);
		} catch (IOException e) {
			throw new AlixiaMediaException("MediaUtils: can't convert path to byte array", e);
		}
		return bytes;
	}

	public static byte[] fileToByteArray(String pathName) {
		Path infile;
		
		MediaUtils.checkNotNull(pathName);
		infile = Paths.get(pathName);
		return pathToByteArray(infile);
	}

	public static void byteArrayToFile(byte[] bytes, String path) {
		Path infile;
		
		MediaUtils.checkNotNull(bytes);
		MediaUtils.checkNotNull(path);
		infile = Paths.get(path);
		try {
			Files.write(infile, bytes);
		} catch (IOException e) {
			throw new AlixiaMediaException("MediaUtils: can't convert byte array to file", e);
		}
	}

	public static Path byteArrayToFile(byte[] bytes, MediaFormat format) {
        Path tempFile = null;
        
        MediaUtils.checkNotNull(bytes);
        MediaUtils.checkNotNull(format);
		try {
	    	tempFile = Files.createTempFile(null, format.getFileExtension());
			Files.write(tempFile, bytes);
		} catch (IOException e) {
			throw new AlixiaMediaException("MediaUtils: can't convert byte array to file", e);
		}
        return tempFile;
	}
	
	public static AudioFormat getAudioFormat(String audioFile) {
		AudioFormat	audioFormat = null;
		File file;

		MediaUtils.checkNotNull(audioFile);
		file = new File(audioFile);
		try (AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(file)) {
			audioFormat = audioInputStream.getFormat();
		} catch (IOException e) {
			throw new AlixiaMediaException("MediaUtils: can't get audio format", e);
		} catch (UnsupportedAudioFileException e) {
			throw new AlixiaMediaException("MediaUtils: audio format not supported", e);
		}
		return audioFormat;
	}
	
	public static AudioFormat getAudioFormat(Path audioPath) {
		AudioFormat	audioFormat = null;
		File file;

		MediaUtils.checkNotNull(audioPath);
		file = audioPath.toFile();
		try (AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(file)) {
			audioFormat = audioInputStream.getFormat();
		} catch (IOException e) {
			throw new AlixiaMediaException("MediaUtils: can't get audio format", e);
		} catch (UnsupportedAudioFileException e) {
			throw new AlixiaMediaException("MediaUtils: audio format not supported", e);
		}
		return audioFormat;
	}
	
	public static String getAudioFormatString(String audioFile) {
		AudioFormat	audioFormat = null;
		File file;
		StringBuilder sb;
		
		MediaUtils.checkNotNull(audioFile);
		file = new File(audioFile);
		try (AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(file)) {
			audioFormat = audioInputStream.getFormat();
		} catch (IOException e) {
			throw new AlixiaMediaException("MediaUtils: can't get audio format", e);
		} catch (UnsupportedAudioFileException e) {
			throw new AlixiaMediaException("MediaUtils: audio format not supported", e);
		}
		if (audioFormat == null) {
			LOGGER.error("Can''t find format for audio file {}", audioFile);
			return null;
		}
		sb = new StringBuilder("Audio format for ");
		sb.append(audioFile);
		sb.append(":\nEncoding: ");
		sb.append(audioFormat.getEncoding());
		sb.append("\nSample Rate: ");
		sb.append(audioFormat.getSampleRate());
		sb.append("\nSample Size in Bits: ");
		sb.append(audioFormat.getSampleSizeInBits());
		sb.append("\nChannels: ");
		sb.append(audioFormat.getChannels());
		sb.append("\nFrame Size: ");
		sb.append(audioFormat.getFrameSize());
		sb.append("\nFrame Rate: ");
		sb.append(audioFormat.getFrameRate());
		sb.append("\nBig Endian: ");
		sb.append(audioFormat.isBigEndian());
		sb.append("\n");
		return sb.toString();
	}

	public static SerialAudioFormat audioFormatToSerial(AudioFormat audioFormat) {
		SerialAudioFormat serialFormat;
		Encoding encoding;
		
		MediaUtils.checkNotNull(audioFormat);
		serialFormat = new SerialAudioFormat();
		encoding = audioFormat.getEncoding();
		serialFormat.setEncodingString(encoding.toString());
		serialFormat.setSampleRate(audioFormat.getSampleRate());
		serialFormat.setSampleSize(audioFormat.getSampleSizeInBits());
		serialFormat.setChannels(audioFormat.getChannels());
		serialFormat.setFrameSize(audioFormat.getFrameSize());
		serialFormat.setFrameRate(audioFormat.getFrameRate());
		serialFormat.setBigEndian(audioFormat.isBigEndian());
		return serialFormat;
	}
	
	public static AudioFormat serialToAudioFormat(SerialAudioFormat format) {
		AudioFormat audioFormat;
		Encoding encoding;
		Float sampleRate;
		Integer sampleSize;
		Integer channels;
		Integer frameSize;
		Float frameRate;
		Boolean bigEndian;
		
		MediaUtils.checkNotNull(format);
		encoding = new Encoding(format.getEncodingString());
		sampleRate = format.getSampleRate();
		sampleSize = format.getSampleSize();
		channels = format.getChannels();
		frameSize = format.getFrameSize();
		frameRate = format.getFrameRate();
		bigEndian = format.getBigEndian();
		
		audioFormat = new AudioFormat(encoding, sampleRate, sampleSize, channels, 
				frameSize, frameRate, bigEndian);
		return audioFormat;
	}

    /**
     * Create a byte array from an image for database storage e.g., using the
     * default IMAGE_TYPE.
     *
     * @throws IOException
     * @param image The image to convert.
     * @return The image converted to an array of bytes.
     */
    public static byte[] imageToByteArray(Image image) throws IOException {
    	return imageToByteArray(image, IMAGE_TYPE);
    }
    
    /**
     * Create a byte array from an image for database storage, e.g.
     * 
     * @throws IOException
     * @param image The image to convert.
     * @param format The format for the image ("png" for example)
     * @return The image converted to an array of bytes.
     */
    public static byte[] imageToByteArray(Image image, String format) throws IOException {
        byte[] imageBytes;
        ByteArrayOutputStream imageOut;
        BufferedImage bufImage;
        Graphics g;
        int width;
        int height;

        MediaUtils.checkNotNull(image);
        MediaUtils.checkNotNull(format);
        imageOut = new ByteArrayOutputStream();
        if (image instanceof BufferedImage) {
        	bufImage = (BufferedImage) image;
        } else {
	        width = image.getWidth(null);
	        height = image.getHeight(null);
	        bufImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
	        g = bufImage.getGraphics();
	        g.drawImage(image, 0, 0, null);
	        g.dispose();
        }
        ImageIO.write(bufImage, format, imageOut);
        imageBytes = imageOut.toByteArray();
        return imageBytes;
    }
    
    public static BufferedImage byteArrayToImage(byte[] imageBytes) throws IOException {
		ByteArrayInputStream byteInput;
		BufferedImage image;

		MediaUtils.checkNotNull(imageBytes);
		byteInput = new ByteArrayInputStream(imageBytes);
		image = ImageIO.read(byteInput);
		return image;
    }
	
	public static int statCommand(String[] cmd) {
		ProcessBuilder builder;
		Process proc;
		int retVal;
		StringBuilder stdOut;
		StringBuilder stdErr;
		char[] charBuffer;
		int bufLen;
		
        MediaUtils.checkNotNull(cmd);
		builder = new ProcessBuilder(cmd);
		builder.redirectErrorStream(false); // don't merge stderr w/ stdout
		try {
			proc = builder.start();
		} catch (IOException e) {
			throw new AlixiaMediaException("MediaUtils: can't start statCommand process", e);
		}
		
		stdOut = new StringBuilder();
		charBuffer = new char[1024];
		try (BufferedReader inStream = new BufferedReader(
				new InputStreamReader(proc.getInputStream()))){
			while ((bufLen = inStream.read(charBuffer)) > 0) {
				stdOut.append(charBuffer, 0, bufLen);
			}
		} catch (IOException e) {
			throw new AlixiaMediaException("MediaUtils: can't read runCommand stream", e);
		}
		LOGGER.debug("statCommand: {}", stdOut);
		
		stdErr = new StringBuilder();
		charBuffer = new char[1024];
		try (BufferedReader errStream = new BufferedReader(
				new InputStreamReader(proc.getErrorStream()))){
			while ((bufLen = errStream.read(charBuffer)) > 0) {
				stdErr.append(charBuffer, 0, bufLen);
			}
		} catch (IOException e) {
			throw new AlixiaMediaException("Can't read stderr", e);
		}
		if (stdErr.length() > 0) {
			LOGGER.error("statCommand Error: {}", stdErr.toString());
			throw new AlixiaMediaException("MediaUtils: standard error not empty: " + stdErr.toString());
		}

		try {
			proc.waitFor();
		} catch (InterruptedException e) {
			throw new AlixiaMediaException("MediaUtils: statCommand interrupted", e);
		}
		
		try {
			retVal = proc.exitValue();
		} catch (IllegalThreadStateException e) {
			throw new AlixiaMediaException("MediaUtils: statCommand not terminated when queried", e);
		}
		return retVal;
	}
	
	// see guava SharedUtils, this is just adapted from there
	public static <T> void checkNotNull(T reference) {
		
		if (reference == null) {			
			throw new NullPointerException("Null reference in checkNotNull");
		}
	}

	public static <T> void nullsOkay(T reference) {
		// doesn't do anything, just indicates we don't use checkNotNull
	}
}
