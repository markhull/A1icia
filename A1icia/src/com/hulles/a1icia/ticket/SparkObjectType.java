/*******************************************************************************
 * Copyright © 2017 Hulles Industries LLC
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
 *******************************************************************************/
package com.hulles.a1icia.ticket;

import com.hulles.a1icia.base.A1iciaException;
import com.hulles.a1icia.tools.A1iciaUtils;

public enum SparkObjectType {
	NONE((short)1, "None"),
	ALICIA((short)2, "A1icia"), // spark = "what_is_your_age", spark object = A1icia
	CLIENT((short)3, "Client"), // original question = "how do i look?" sparkObject = client
	CLIENTANDALICIA((short)4, "Client and A1icia"), // original question = "where are we?" sparkObject = client and A1icia
	AUDIOARTIST((short)5, "Audio Artist"), // spark = "play_artist" sparkObject = "Metric"
	AUDIOTITLE((short)6, "Audio Title"), // spark = "play_title" sparkObject = "Sick Muse"
	VIDEOTITLE((short)7, "Video Title"), // spark = "play_video" sparkObject = "Un Chien Andalou"
	PROPERNAME((short)8, "Proper Name"), // spark = "lookup_fact" sparkObject = "Terry Reid"
	FACTQUERY((short)9, "Fact Query"), // spark = "lookup_fact" sparkObject = "climbing paths on Mt. Niitaka"
	ALICIAN((short)10, "A1ician ID"), // spark = "wake_up_a1ician" sparkObject = "90210"
	TIMERNAME((short)11, "Timer Name"), // spark = "named_timer" sparkObject = "Coffee"
	LANGUAGE((short)12, "Language"); // spark = "change_language" sparkObject = Language.GERMAN
    private final Short storeID;
    private final String displayName;

    private SparkObjectType(Short storeID, String displayName) {
    	
        this.displayName = displayName;
        this.storeID = storeID;
    }

    public Short getStoreID() {
    	
    	return storeID;
    }
    
    public String getDisplayName() {
    	
        return displayName;
    }

    public static SparkObjectType findSparkObjectType(Short type) {
    	
		A1iciaUtils.checkNotNull(type);
    	for (SparkObjectType a : SparkObjectType.values()) {
    		if (a.storeID == type) {
    			return a;
    		}
        }
    	throw new A1iciaException("SparkObjectType: can't find type " +  type.toString());
    }
}
