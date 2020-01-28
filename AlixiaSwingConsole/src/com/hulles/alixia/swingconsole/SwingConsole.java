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
package com.hulles.alixia.swingconsole;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.AbstractExecutionThreadService;
import com.hulles.alixia.api.object.AlixiaClientObject;
import com.hulles.alixia.api.remote.AlixiaRemote;
import com.hulles.alixia.api.remote.AlixiaRemoteDisplay;
import com.hulles.alixia.api.remote.Station;
import com.hulles.alixia.api.shared.AlixiaException;
import com.hulles.alixia.api.shared.SerialSememe;
import com.hulles.alixia.api.shared.SharedUtils;

/**
 * SwingConsole uses good old Java Swing to display a console that has some smarts,
 * like command history.
 * 
 * @author hulles
 */
public class SwingConsole  extends AbstractExecutionThreadService implements AlixiaRemoteDisplay {
	final static Logger LOGGER = LoggerFactory.getLogger(SwingConsole.class);
    private final static String CONSOLENAME = "the Alixia Swing Console command-line interface";
	AlixiaRemote remote;
	private final String host;
	private final Integer port;
	private final Station station;
    static final String NEWLINE = System.getProperty("line.separator");
   	ConsoleWindow window;
	@SuppressWarnings("unused")
	private volatile boolean serverUp;
//	JebusPool jebusPool;

    /**
     * Start up the Swing console.
     * 
     * @param host The Jebus Central host name or IP
     * @param port The Jebus Central port
     */
	public SwingConsole(String host, Integer port) {

		SharedUtils.checkNotNull(host);
		SharedUtils.checkNotNull(port);
		this.host = host;
		this.port = port;
		station = Station.getInstance();
		station.ensureStationExists();
		startSwing();
	}
	
    /**
     * Called when the Guava Executor Service is starting up.
     * 
     */
	@Override
	protected final void startUp() {
		
		remote = new AlixiaRemote(host, port, this);
		remote.startAsync();
		remote.awaitRunning();
		
		remote.setUseTTS(true);
		remote.setPlayAudio(true);
		remote.setShowImage(false);
		remote.setShowText(false);
		remote.setPlayVideo(false);
	}
	
    /**
     * Called when the Guava Executor Service is shutting down
     * 
     */
	@Override
	protected final void shutDown() {
		
		remote.shutdownRemote();
		remote.awaitTerminated();
	}
	
    /**
     * The Guava Executor Service run method. The Service expects this to block
     * until processing is complete. Our implementation just loops.
     * 
     */
	@Override
	protected final void run() {
		
		while (isRunning()) {}
	}

    /**
     * Show some help text.
     * 
     */
	private void showHelp() {
		
		window.displayText("· Type 'quit' or 'exit' to quit the CLI ");
		window.displayTextln("(or just use CTRL+C to quit Java).");
		window.displayTextln("· Type 'test' to check connection to Alixia Central.");
		window.displayTextln("· Type 'login' to log in.");
		window.displayTextln("· Type 'logout' to log out.");
		window.displayTextln("· Type 'text on' to display console log.");
		window.displayTextln("· Type 'text off' to not display console.");
		window.displayTextln("· Type 'images on' to display image content.");
		window.displayTextln("· Type 'images off' to not display images.");
		window.displayTextln("· Type 'TTS on' to enable text-to-speech.");
		window.displayTextln("· Type 'TTS off' to disable text-to-speech.");
		window.displayTextln("· Type 'audio on' to play audio content.");
		window.displayTextln("· Type 'audio off' to disable audio content.");
		window.displayTextln("· Type 'video on' to play video content.");
		window.displayTextln("· Type 'video off' to disable video content.");
		window.displayTextln("· Type 'help console' to repeat these commands.");		
	}

    /**
     * Parse user input to see if it's a command, and if so, execute the command.
     * 
     * @param text The user-typed text
     * @return  True if we consumed a command
     * 
     */
	boolean command(String text) {
		boolean connected;
		
		if (text.equalsIgnoreCase("test")) {
			connected = remote.reachableHost();
			window.displayText("We are " + (connected ? "" : "NOT ") + "connected to Alixia Central ");
			window.displayTextln("at " + host + 
					" on port " + port);
			window.displayTextln("The language is currently " + remote.getCurrentLanguage().getDisplayName());
			window.displayTextln("We currently " + (station.isQuiet() ? "are" : "are not") + 
					" in quiet mode.");
			window.displayTextln("Video is " + (remote.playVideo() ?  "on" : "off"));
			window.displayTextln("Audio is " + (remote.playAudio() ?  "on" : "off"));
			window.displayTextln("TTS is " + (remote.useTTS() ?  "on" : "off"));
			window.displayTextln("Text display is " + (remote.showText() ?  "on" : "off"));
			window.displayTextln("Image display is " + (remote.showImage() ?  "on" : "off"));
			return true;
		}
		if (text.equalsIgnoreCase("tts on")) {
			remote.setUseTTS(true);
			window.displayTextln("Using TTS");
			return true;
		}
		if (text.equalsIgnoreCase("tts off")) {
			remote.setUseTTS(false);
			window.displayTextln("Not using TTS");
			return true;
		}
		if (text.equalsIgnoreCase("audio on")) {
			remote.setPlayAudio(true);
			window.displayTextln("Audio is on");
			return true;
		}
		if (text.equalsIgnoreCase("audio off")) {
			remote.setPlayAudio(false);
			window.displayTextln("Audio is off");
			return true;
		}
		if (text.equalsIgnoreCase("video on")) {
			remote.setPlayVideo(true);
			window.displayTextln("Video is on");
			return true;
		}
		if (text.equalsIgnoreCase("video off")) {
			remote.setPlayVideo(false);
			window.displayTextln("Video is off");
			return true;
		}
		if (text.equalsIgnoreCase("images on")) {
			remote.setShowImage(true);
			window.displayTextln("Images will be displayed");
			return true;
		}
		if (text.equalsIgnoreCase("images off")) {
			remote.setShowImage(false);
			window.displayTextln("Images will not be displayed");
			return true;
		}
		if (text.equalsIgnoreCase("text on")) {
			remote.setShowText(true);
			window.displayTextln("Console log will be displayed");
			return true;
		}
		if (text.equalsIgnoreCase("text off")) {
			remote.setShowText(false);
			window.displayTextln("Console log will not be displayed");
			return true;
		}
		if (text.equalsIgnoreCase("help console")) {
			showHelp();
			return true;
		}
		if (text.equalsIgnoreCase("login")) {
			login();
			return true;
		}
		if (text.equalsIgnoreCase("logout")) {
			logout();
			return true;
		}
		if (text.equalsIgnoreCase("quit")) {
			this.stopAsync();
			return true;
		}
		if (text.equalsIgnoreCase("exit")) {
 			window.dispose();
            remote.shutdownRemote();
			this.stopAsync();
			return true;
		}
		return false;
	}

    /**
     * This is not yet implemented
     * 
     */
	private static void login() {
        
		throw new UnsupportedOperationException("Login/Logout is not yet supported");
	}
	
    /**
     * This is not yet implemented
     * 
     */
	private static void logout() {
        
		throw new UnsupportedOperationException("Login/Logout is not yet supported");
	}
	
    /**
     * Receive a command from AlixiaRemote.
     * 
     * @param command The command sememe
     * @return True if we consumed the command
     */
	@Override
	public boolean receiveCommand(SerialSememe command) {
		
		switch (command.getName()) {
			case "central_startup":
				serverUp = true;
				return true;
			case "central_shutdown":
				serverUp = false;
				return true;
			default:
				break;
		}
		return false;
	}

    /**
     * We don't currently do anything with the explanation in the command line client, currently.
     * 
     * @param text The explanation
     */
	@Override
	public void receiveExplanation(String text) {
	}

    /**
     * Receive a media object from AlixiaRemote. We return 'false' so we can
     * let AlixiaRemote handle the heavy lifting with media objects.
     * 
     * @param object The media object
     * @return False, see above
     */
	@Override
	public boolean receiveObject(AlixiaClientObject object) {

		return false;
	}

	/**
	 * Here we have received a request from (presumably) AlixiaRemote asking for
	 * input. We just ignore the request for now and print it as a message.
	 * 
	 */
	@Override
	public void receiveRequest(String text) {
		
		receiveText(text);
	}

    /**
     * Receive text from AlixiaRemote, and just print it.
     * 
     * @param text The incoming text
     */
	@Override
	public void receiveText(String text) {

        window.displayTextln("Alixia: " + text);
	}

    public boolean sendText(String text) {
        
        return remote.sendText(text);
    }
    
    /**
     * The usual Swing startup boilerplate.
     * 
     */
	private void startSwing() {

        try {
            //UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
            //UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
        } catch (UnsupportedLookAndFeelException | IllegalAccessException | 
                InstantiationException | ClassNotFoundException ex) {
            throw new AlixiaException("SwingConsole: can't set LAF",ex);
        }
        // Turn off metal's use of bold fonts
        //UIManager.put("swing.boldMetal", Boolean.FALSE);
        
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            @Override
			public void run() {
                createAndShowGUI();
            }
        });
	}
    
    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     */
    void createAndShowGUI() {
     	
        //Create and set up the window.
        window = new ConsoleWindow("Alixia Swing Console", this);
        window.addWindowListener(new WindowCloser());
        window.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        
        //Set up the content pane.
        window.addComponentsToPane();
        
        
        //Display the window.
        window.pack();
        window.setVisible(true);
        showStartUp();
    }
	
    /**
     * Display the startup text block.
     * 
     */
	private void showStartUp() {
		
		window.displayTextln("Welcome to " + getConsoleName() + ".");
		window.displayTextln("This station connects to Alixia Central at " + host + 
				" on port " + port);
		window.displayTextln("The default language is " + station.getDefaultLanguage().getDisplayName());
		window.displayTextln("We currently " + (station.isQuiet() ? "are" : "are not") + 
				" in quiet mode.");
		window.displayTextln("Video is " + (remote.playVideo() ?  "on" : "off"));
		window.displayTextln("Audio is " + (remote.playAudio() ?  "on" : "off"));
		window.displayTextln("TTS is " + (remote.useTTS() ?  "on" : "off"));
		window.displayTextln("Text display is " + (remote.showText() ?  "on" : "off"));
		window.displayTextln("Image display is " + (remote.showImage() ?  "on" : "off"));
		showHelp();
		window.displayTextln("Running console SWINGCONSOLE");
		window.displayTextln();
		
	}
	
    /**
     * Get the name of this console.
     * 
     * @return The name
     */
	private static String getConsoleName() {
	
		return CONSOLENAME;
	}

    /**
     * The user closed the text display window, so mark it as closed.
     * 
     */
    @Override
    public void textWindowIsClosing() {
        
        remote.setShowText(false);
    }

	private class WindowCloser extends WindowAdapter {

		WindowCloser() {
		}

		@Override
		public void windowClosing(WindowEvent e) {

			LOGGER.debug("Window is closing");
			window.dispose();
            remote.shutdownRemote();
			SwingConsole.this.stopAsync();
		}
		
	}
}
