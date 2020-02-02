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

import static java.nio.file.FileVisitOption.FOLLOW_LINKS;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hulles.alixia.api.shared.AlixiaException;
import com.hulles.alixia.api.shared.SharedUtils;

public class LibraryLister extends SimpleFileVisitor<Path> {
	private final static Logger LOGGER = LoggerFactory.getLogger(LibraryLister.class);
//	private final static String WAVPATTERN = "*.wav";
	private final static String MEDIAPATTERN = "*.{wav,mp3,mp4,flv,mov,png,jpg,gif}";
	private final PathMatcher matcher;
//	private int numMatches = 0;
	private List<Path> filePaths = null;
	
	LibraryLister(String pattern) {
		filePaths = new ArrayList<>();
		matcher = FileSystems.getDefault().getPathMatcher("glob:" + pattern);
	}

	public static List<Path> listFiles(String logDir) {
		
		return listFiles(logDir, MEDIAPATTERN);
	}
	
	public static List<Path> listFiles(String logDir, String pattern) {
        LibraryLister lister;
        Path logDirPath;
        EnumSet<FileVisitOption> opts;
        
        SharedUtils.checkNotNull(logDir);
        LOGGER.debug("LibraryLister: list files for {}, pattern {}", logDir, pattern);
        lister = new LibraryLister(pattern);
        logDirPath = Paths.get(logDir);
        opts = EnumSet.of(FOLLOW_LINKS);
        try {
			Files.walkFileTree(logDirPath, opts, Integer.MAX_VALUE, lister);
		} catch (IOException e) {
			throw new AlixiaException("IO Exception listing library files", e);
		}
        lister.done();
        return lister.getFilePaths();
	}
	
	// Compares the glob pattern against
	// the file or directory name.
	private void find(Path file) {
		Path filePath;
		
        SharedUtils.checkNotNull(file);
		filePath = file.getFileName();
		if (filePath != null && matcher.matches(filePath)) {
            LOGGER.debug("LibraryLister: find matches {}", filePath);
			try {
				filePath = file.toRealPath();
			} catch (IOException e) {
				throw new AlixiaException("Can't create real path to file " + file.getFileName());
			}
			filePaths.add(filePath);
		} else {
            LOGGER.debug("LibraryLister: find doesn't match {}", filePath);
        }
	}

	private void done() {
	}

	@Override
	public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {

		SharedUtils.checkNotNull(file);
        SharedUtils.checkNotNull(attrs);
        LOGGER.debug("LibraryLister: visit file {}", file);
		find(file);
		return FileVisitResult.CONTINUE;
	}

	@Override
	public FileVisitResult visitFileFailed(Path file, IOException exc) {

		LOGGER.error(exc.getMessage());
		return FileVisitResult.CONTINUE;
	}

	public List<Path> getFilePaths() {
		
		return filePaths;
	}
}



