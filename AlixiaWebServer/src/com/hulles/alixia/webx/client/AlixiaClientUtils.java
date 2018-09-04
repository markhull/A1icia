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
package com.hulles.alixia.webx.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.NotificationMole;
import com.hulles.alixia.api.tools.AlixiaUtils;

final public class AlixiaClientUtils {
	private final static String PRONGERROR = "Bad Prong";
	private final static String COMMERROR = "500 The Bees They're In My Eyes";
	private static AlixiaImageResources imageResources;
	
	public static void prongError(Throwable ex) {
		
		AlixiaUtils.error(PRONGERROR, ex);
	}
	
	
	public static void commError(Throwable ex) {
		
		AlixiaUtils.error(COMMERROR, ex);
	}

    /**
     * Get the standard loading image. A new image is created with each call.
     * 
     * @return The loading image
     */
    public static Image getLoadingImage() {
    	Image loadingImage;
    	
    	if (imageResources == null) {
    		imageResources = GWT.create(AlixiaImageResources.class);
    	}
    	loadingImage = new Image(imageResources.loadingImage());
        return loadingImage;
    }    
   
    private static NotificationMole showNotification(String title, String message) {
    	NotificationMole nm;
    	
    	nm = new NotificationMole();
        nm.setAnimationDuration(2000);
        nm.setTitle(title);
        nm.setHeight("100px");
        nm.setWidth("200px");
        nm.setMessage(message);
        nm.show();
        return nm;
    }
}
