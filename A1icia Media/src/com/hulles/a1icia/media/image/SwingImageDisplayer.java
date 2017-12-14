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
package com.hulles.a1icia.media.image;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.WindowConstants;

import com.hulles.a1icia.media.MediaUtils;

public class SwingImageDisplayer {

	public SwingImageDisplayer() {
		
	}
	
	public void displayImage(BufferedImage image, String title) {

		MediaUtils.checkNotNull(image);
		MediaUtils.nullsOkay(title);
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            @Override
			public void run() {
                createAndShowGUI(image, title);
            }
        });
	}

    /**
     * Create the GUI and show it. For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     */
    void createAndShowGUI(BufferedImage image, String title) {
    	JFrame frame;
    	JComponent contentPane;
    	
    	MediaUtils.checkNotNull(image);
    	MediaUtils.nullsOkay(title);
    	frame = new JFrame();
    	if (title == null) {
    		frame.setTitle(title);
    	}
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        contentPane = new ImageScroller(image);
        contentPane.setOpaque(true); //content panes must be opaque
        frame.setContentPane(contentPane);
 
        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }

    private class ImageScroller extends JPanel {
		private static final long serialVersionUID = 1L;

		ImageScroller(BufferedImage image) {
    		super(new BorderLayout());
        	ImageIcon icon;
        	JLabel label;
        	JScrollPane scroller;
        	
            icon=new ImageIcon(image);
            label = new JLabel();
            label.setIcon(icon);
            scroller = new JScrollPane(label);
            scroller.setPreferredSize(new Dimension(600, 800));
            add(scroller, BorderLayout.CENTER);
    	}
    }
    
}
