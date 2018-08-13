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
package com.hulles.a1icia.echo.w2v;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.channels.FileChannel;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.hulles.a1icia.echo.EchoWordToVecException;
import com.hulles.a1icia.tools.A1iciaTimer;
import com.hulles.a1icia.tools.A1iciaUtils;

final class WordToVecLoader {
	final static Logger logger = Logger.getLogger("A1iciaEcho.WordToVecLoader");
	private final static Level LOGLEVEL1 = Level.FINE;
	private final static Level LOGLEVEL2 = Level.INFO;
	private final static String DUMPVALUE = "%.8f";
	private long channelStart;
	private int vocabSize = 0;
	private int vectorSize = 0;
	private Map<String, float[]> wordVectors = null;
	
	WordToVecLoader() {
	}
	
	/**
	 * Return the map that has already been loaded
	 * 
	 * @return The map of words and vectors
	 */
	Map<String, float[]> getMap() {
		if (wordVectors == null) {
			System.err.println("You need to call the load method before accessing the map");
			return null;
		}
		return wordVectors;
	}
	
	/**
	 * Return the vector size, obtained from the file that has aleady been loaded
	 * @return
	 */
	Integer getVectorSize() {
		if (wordVectors == null) {
			System.err.println("You need to call the load method before requesting the vector size");
			return null;
		}
		return vectorSize;
	}
	
	/**
	 * Load the word2vec BIN format file using nio
	 * 
	 * @param fileName The BIN format file to load
	 */
	@SuppressWarnings("resource")
	void load(String fileName) {
		FileInputStream fileStream = null;
		FileChannel channel = null;
		ByteBuffer buffer;
		long fileSize;
		int bufferSize;
		ByteOrder byteOrder = ByteOrder.LITTLE_ENDIAN;
		
		A1iciaUtils.checkNotNull(fileName);
		A1iciaTimer.startTimer("LOADER");
		try {
			fileStream = new FileInputStream(fileName);
		} catch (SecurityException e) {
			e.printStackTrace();
			closeResources(fileStream, channel);
			throw new EchoWordToVecException("Unable to open file (security exception)");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			closeResources(fileStream, channel);
			throw new EchoWordToVecException("Unable to open file (file not found exception)");
		}
		
		// get an nio file channel
		channel = fileStream.getChannel();
		try {
			fileSize = channel.size();
		} catch (IOException e) {
			e.printStackTrace();
			closeResources(fileStream, channel);
			throw new EchoWordToVecException("Unable to get file size");
		}
		
		// allocate a (probably large) buffer in direct memory (i.e. not on the heap)
		bufferSize = (int) Math.min(fileSize, Integer.MAX_VALUE);
		buffer = ByteBuffer.allocateDirect(bufferSize);
		buffer.order(byteOrder);
		channelStart = 0;
		try {
			channel.read(buffer);
		} catch (IOException e) {
			e.printStackTrace();
			closeResources(fileStream, channel);
			throw new EchoWordToVecException("Error reading file");
		}
		buffer.rewind();
		// read first line to get vocabulary size and layer size
		getFirstLine(buffer);
		if (!(vocabSize > 0) || !(vectorSize > 0)) {
			closeResources(fileStream, channel);
			throw new EchoWordToVecException("Invalid vocab size and/or vector size");
		}
		
		// load it!
		loadMap(fileStream, channel, buffer);
		A1iciaTimer.stopTimer("LOADER");
	}
	
	/**
	 * Normalize the vector values
	 * 
	 * @param vector The vector (i.e. float[]) to normalize
	 * @return A NEW vector with normalized values
	 */
	static float[] normalize(float[] vector) {
		double len = 0.0;
		float[] newVector;
		
		A1iciaUtils.checkNotNull(vector);
		for (float val : vector) {
			len += val * val;
		}
		len = Math.sqrt(len);
		newVector = new float[vector.length];
		for (int ix = 0; ix < vector.length; ix++) {
			newVector[ix] = (float) (vector[ix] / len);
		}
		return newVector;
	}	
	static float[] normalize(double[] vector) {
		double len = 0.0;
		float[] newVector;
		
		A1iciaUtils.checkNotNull(vector);
		for (double val : vector) {
			len += val * val;
		}
		len = Math.sqrt(len);
		newVector = new float[vector.length];
		for (int ix = 0; ix < vector.length; ix++) {
			newVector[ix] = (float) (vector[ix] / len);
		}
		return newVector;
	}	
	
	/**
	 * Load the java util collections map from the ByteBuffer. I ran times on the Guava ImmutableMap and the
	 *   times were close to the same as using the POJ map, so I used the latter and avoided the external library.
	 *   
	 * @param fileStream The file stream in case we need to close it
	 * @param channel The nio file channel
	 * @param buffer The nio ByteBuffer
	 */
	private void loadMap(FileInputStream fileStream, FileChannel channel, ByteBuffer buffer) {
		String word;
		float[] vector;
		float[] newVector;
		int dupeCount;
		float[] oldVector;
		
		A1iciaUtils.checkNotNull(fileStream);
		A1iciaUtils.checkNotNull(channel);
		A1iciaUtils.checkNotNull(buffer);
		
		// we use a LinkedHashMap to get decent performance from sequential access
		wordVectors = new LinkedHashMap<>(vocabSize);
		dupeCount = 0;
		logger.log(LOGLEVEL2, "Loading map for " + vocabSize + " word vectors, dimension " + vectorSize);
		
		// we already know the number of words from reading the first line
		// we use the convenience of nio buffer.mark and buffer.reset to make sure that we don't split 
		//  across a buffer boundary
		for (int lineIx = 0; lineIx < vocabSize; lineIx++) { 
			buffer.mark();
			// read word
			try {
				word = getWord(buffer);
			} catch (BufferUnderflowException U) {
				refillBuffer(fileStream, channel, buffer);
				lineIx--;
				continue;
			}
			// read vectors
			try {
				vector = getVector(buffer);
			} catch (BufferUnderflowException U) {
				refillBuffer(fileStream, channel, buffer);
				lineIx--;
				continue;
			}
			
			// if the word is valid, put the word and associated vector into the map
			if (goodWord(word)) {
//				System.out.println("BEFORE:");
//				dumpArray(vector);
				newVector = normalize(vector);
//				System.out.println("AFTER:");
//				dumpArray(newVector);
				oldVector =  wordVectors.put(word, newVector);
				if (oldVector != null) {
					dupeCount++;
				}
			}
		}
		closeResources(fileStream, channel);
		logger.log(LOGLEVEL2, "Loaded map with " + dupeCount + " duplicates ignored");
	}
	
	/**
	 * This could be reinserted as in-line code; I left it this way in case there may be more criteria
	 * 
	 * @param word The word to validate
	 * @return True if the word is valid, false otherwise
	 */
	private static boolean goodWord(String word) {
		
		A1iciaUtils.checkNotNull(word);
		return (word.length() > 1);
	}
	
	/**
	 * We refill the buffer after resetting to the earlier mark
	 * 
	 * @param fileStream The original file stream, in case we need to close it
	 * @param channel The nio file channel
	 * @param buffer The nio ByteBuffer, which we like lots...
	 */
	private void refillBuffer(FileInputStream fileStream, FileChannel channel, ByteBuffer buffer) {
		long bytes;
		
		A1iciaUtils.checkNotNull(fileStream);
		A1iciaUtils.checkNotNull(channel);
		A1iciaUtils.checkNotNull(buffer);
		buffer.reset();
		logger.log(LOGLEVEL1, "WordToVecLoader: refilling");
		logger.log(LOGLEVEL1, "WordToVecLoader: channelStart = " + channelStart);
		channelStart = channelStart + buffer.position();
		logger.log(LOGLEVEL1, "WordToVecLoader: buffer position = " + buffer.position());
		logger.log(LOGLEVEL1, "WordToVecLoader: new channelStart = " + channelStart);
		try {
			buffer.clear();
			channel.position(channelStart);
			bytes = channel.read(buffer);
			buffer.rewind();
			logger.log(LOGLEVEL1, "WordToVecLoader: after read, bytes read = " + bytes);
			logger.log(LOGLEVEL2, "WordToVecLoader: after read, channel position = " + channel.position());
		} catch (IOException e) {
			e.printStackTrace();
			closeResources(fileStream, channel);
			throw new EchoWordToVecException("Error refilling file");
		}
	}
	
	/**
	 * Winkle the first text line out of the buffer, which has the word and vector counts, and set global
	 *   variables to those values
	 * 
	 * @param buffer The nio ByteBuffer with the data
	 */
	private void getFirstLine(ByteBuffer buffer) {
		StringBuilder sb;
		char c;
		String[] tokens;
		
		A1iciaUtils.checkNotNull(buffer);
		sb = new StringBuilder();
		c = (char) buffer.get();
		while (c != '\n') {
			sb.append(c);
			c = (char) buffer.get();
		}
		String firstLine = sb.toString();
		tokens = firstLine.split(" ");
		vocabSize = Integer.parseInt(tokens[0]);
		vectorSize = Integer.parseInt(tokens[1]);
	}
	
	/**
	 * Get the vocabulary word from the buffer
	 * 
	 * @param buffer The nio ByteBuffer
	 * @return The vocabulary word as a String
	 * @throws BufferUnderflowException
	 */
	private static String getWord(ByteBuffer buffer) throws BufferUnderflowException {
		StringBuilder sb;
		char c;
		
		A1iciaUtils.checkNotNull(buffer);
		sb = new StringBuilder();
		c = (char) buffer.get();
		while (c != ' ') {
			// ignore newlines in front of words (some binary files have newline,
			// some don't) [per Medallia Word2VecModel]
			if (c != '\n') {
				sb.append(c);
			}
			c = (char) buffer.get();
		}
		return sb.toString().trim();
	}
	
	/**
	 * Get a vector (i.e. a float[] whose length is equal to vectorSize). I decided to not convert it to
	 *   a collection just for simplicity and convenience; I'm not sure it would buy us anything to do so
	 * 
	 * @param buffer
	 * @return The newly-retrieved vector (float[])
	 * @throws BufferUnderflowException
	 */
	private float[] getVector(ByteBuffer buffer) throws BufferUnderflowException {
		FloatBuffer floatBuffer;
		float[] vector;
		
		A1iciaUtils.checkNotNull(buffer);
		vector = new float[vectorSize];
		
		// this next line is why I like the nio ByteBuffer...
		floatBuffer = buffer.asFloatBuffer();
		floatBuffer.get(vector);
		// floats are 4 bytes
		buffer.position(buffer.position() + (floatBuffer.position() * 4));
		return vector;
	}
	
	/**
	 * Close the resources we might have open
	 * 
	 * @param stream The original file input stream
	 * @param channel The nio file channel
	 */
	private static void closeResources(FileInputStream stream, FileChannel channel) {
		
		A1iciaUtils.nullsOkay(stream);
		A1iciaUtils.nullsOkay(channel);
		if (channel != null && channel.isOpen()) {
			try {
				channel.close();
			} catch (IOException e) {
				System.err.println("Error closing channel");
				e.printStackTrace();
			}
		}
		if (stream != null) {
			try {
				stream.close();
			} catch (IOException e) {
				System.err.println("Error closing file stream");
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * A convenience method to dump vector values to System.out; see commented-out lines in loadMap method
	 *   above for usage example
	 *   
	 * @param array The vector to dump
	 */
	public static void dumpArray(float[] array) {
		int ix;
		int colIx;
		String valStr;
		
		A1iciaUtils.checkNotNull(array);
		for (int rowIx = 0; rowIx < array.length; rowIx = rowIx + 10) {
			for (colIx = 0; colIx < 9; colIx++) {
				ix = rowIx + colIx;
				valStr = String.format(DUMPVALUE, array[ix]);
				System.out.print("[" + ix + "]" + valStr + ", ");
			}
			ix = rowIx + colIx;
			valStr = String.format(DUMPVALUE, array[ix]);
			System.out.println("[" + ix + "]" + valStr);
		}
	}
}
