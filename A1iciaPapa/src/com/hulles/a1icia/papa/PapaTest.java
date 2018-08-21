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
package com.hulles.a1icia.papa;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.hulles.a1icia.tools.ExternalAperture;

public class PapaTest {
	private final static String QUERY = "Hatsune Miku";
	private final static String KEY = "9PAKPW-77U3JGVUWU";
	
	public static void main(String[] args) {
		String encodedQuery;
		String responseXML = null;
		String wolframKey;
		BufferedImage image;
		
		wolframKey = KEY;
		encodedQuery = PapaRoom.encodeQuery(QUERY);
		
//		responseXML = ExternalAperture.getWolframSpokenQuery(encodedQuery, wolframKey);
//		responseXML = ExternalAperture.getWolframShortQuery(encodedQuery, wolframKey);
		image = ExternalAperture.getWolframSimpleQuery(encodedQuery, wolframKey);
//		responseXML = ExternalAperture.getWolframValidateQuery(encodedQuery, wolframKey);
//		responseXML = ExternalAperture.getWolframQuery(encodedQuery, wolframKey);
		
		if (image == null) {
			System.out.println("RESPONSE:\n");
			System.out.println(responseXML);
			System.out.println();
		} else {
	        javax.swing.SwingUtilities.invokeLater(new Runnable() {
	            @Override
				public void run() {
	                createAndShowGUI(image);
	            }
	        });
		}
	}
    
    /**
     * Create the GUI and show it. For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     */
    static void createAndShowGUI(BufferedImage image) {
        //Create and set up the window.
        JFrame frame = new JFrame("Wolfram|Alpha Result");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
        //Create and set up the content pane.
        JComponent newContentPane = new ImageScroller(image);
        newContentPane.setOpaque(true); //content panes must be opaque
        frame.setContentPane(newContentPane);
 
        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }

    public static class ImageScroller extends JPanel {
		private static final long serialVersionUID = 1L;

		ImageScroller(BufferedImage image) {
    		super(new BorderLayout());
        	ImageIcon icon;
        	JLabel label;
        	int width;
        	int height;
        	
        	width = image.getWidth();
        	height = image.getHeight();
        	System.out.println("Width="+ width);
        	System.out.println("Height=" + height);
            icon=new ImageIcon(image);
            label = new JLabel();
            label.setIcon(icon);
            
            //Put the drawing area in a scroll pane.
            JScrollPane scroller = new JScrollPane(label);
            scroller.setPreferredSize(new Dimension(540, 800));
            add(scroller, BorderLayout.CENTER);
    	}
    }
}
