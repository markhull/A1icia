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

public enum Language {
    AMERICAN_ENGLISH("en-US", "en", "American English"),    
	BRITISH_ENGLISH("en-GB", "en", "British English"),
    GERMAN("de-DE", "de", "Deutsche"),
	SPANISH("es-ES", "es", "Español"),
    FRENCH("fr-FR", "fr", "Français"),
    ITALIAN("it-IT", "it", "Italiano");
    private final String picoName;
    private final String googleName;
    private final String displayName;
    
    private Language(String picoName, String googleName, String displayName) {
        this.picoName = picoName;
        this.googleName = googleName;
        this.displayName = displayName;
    }

    public String getPicoName() {
        return picoName;
    }

    public String getGoogleName() {
    	return googleName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
