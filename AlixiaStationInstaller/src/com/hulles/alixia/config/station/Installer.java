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
package com.hulles.alixia.config.station;

import com.hulles.alixia.api.tools.AlixiaVersion;
import java.util.ResourceBundle;

/**
 * Import / export the station values into target. Note that this should probably be compiled with
 * a Java 8 JDK to allow as universal a fit as possible.
 * 
 * @author hulles
 *
 */
public class Installer {
	private static final String BUNDLE_NAME = "com.hulles.alixia.config.station.Version";

	public static void main(String[] args) {
        String version;
 		ResourceBundle bundle;
       
		bundle = ResourceBundle.getBundle(BUNDLE_NAME);
        version = AlixiaVersion.getVersionString(bundle);
		System.out.println(version);
		System.out.println();
		ImportExport.importExportStation();
	}
}
