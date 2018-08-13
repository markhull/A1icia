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
package com.hulles.a1icia.swingconsole;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import com.google.common.util.concurrent.AbstractExecutionThreadService;
import com.hulles.a1icia.api.A1iciaConstants;
import com.hulles.a1icia.api.jebus.JebusApiBible;
import com.hulles.a1icia.api.jebus.JebusApiHub;
import com.hulles.a1icia.api.jebus.JebusPool;
import com.hulles.a1icia.api.object.A1iciaClientObject;
import com.hulles.a1icia.api.remote.A1iciaRemote;
import com.hulles.a1icia.api.remote.A1iciaRemoteDisplay;
import com.hulles.a1icia.api.remote.Station;
import com.hulles.a1icia.api.shared.SerialSememe;
import com.hulles.a1icia.api.shared.SharedUtils;

import redis.clients.jedis.Jedis;

public class SwingConsole  extends AbstractExecutionThreadService implements A1iciaRemoteDisplay {
	final static Logger LOGGER = Logger.getLogger("A1iciaSwingConsole.SwingConsole");
	final static Level LOGLEVEL = A1iciaConstants.getA1iciaLogLevel();
//	final static Level LOGLEVEL = Level.INFO;
	String historyKey;
	int historyIx;
	final static int HISTORYLIMIT = 255;
	A1iciaRemote remote;
	private final String host;
	private final Integer port;
	private final Station station;
    static final String NEWLINE = System.getProperty("line.separator");
   	private ConsoleWindow window;
	@SuppressWarnings("unused")
	private volatile boolean serverUp;
	JebusPool jebusPool;

	public SwingConsole(String host, Integer port) {

		SharedUtils.checkNotNull(host);
		SharedUtils.checkNotNull(port);
		this.host = host;
		this.port = port;
		station = Station.getInstance();
		station.ensureStationExists();
		jebusPool = JebusApiHub.getJebusLocal();
		historyKey = JebusApiBible.getA1iciaSwingHistoryKey(jebusPool);
		try (Jedis jebus = jebusPool.getResource()) {
			// start fresh
			jebus.del(historyKey);
		}		
		historyIx = 0;
		startSwing();
	}
	
	@Override
	protected void startUp() {
		
		remote = new A1iciaRemote(host, port, this);
		remote.startAsync();
		remote.awaitRunning();
		
		remote.setUseTTS(true);
		remote.setPlayAudio(true);
		remote.setShowImage(false);
		remote.setShowText(false);
		remote.setPlayVideo(false);
	}
	
	@Override
	protected void shutDown() {
		
		remote.stopAsync();
		remote.awaitTerminated();
	}
	
	@Override
	protected void run() {
		
		while (isRunning()) {}
	}

	private void showHelp() {
		
		window.displayText("· Type 'quit' or 'exit' to quit the CLI ");
		window.displayTextln("(or just use CTRL+C to quit Java).");
		window.displayTextln("· Type 'test' to check connection to A1icia Central.");
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
		window.displayTextln("· Type 'help console' to repeat these commands.");		
	}

	boolean command(String text) {
		boolean connected;
		
		if (text.equalsIgnoreCase("test")) {
			connected = remote.reachableHost();
			window.displayText("We are " + (connected ? "" : "NOT ") + "connected to A1icia Central ");
			window.displayTextln("at " + host + 
					" on port " + port);
			window.displayTextln("The language is currently " + remote.getCurrentLanguage().getDisplayName());
			window.displayTextln("We currently " + (station.isQuiet() ? "are" : "are not") + 
					" in quiet mode.");
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
			this.stopAsync();
			return true;
		}
		return false;
	}

	private void login() {
		
	}
	
	private void logout() {
		
	}
	
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

	@Override
	public void receiveExplanation(String text) {

		// we don't do anything with the explanation in the command line client, currently
	}

	@Override
	public boolean receiveObject(A1iciaClientObject object) {

		// we let A1iciaConsole handle the heavy lifting with media objects
		return false;
	}

	/**
	 * Here we have received a request from (presumably) A1icia Central asking for
	 * input.
	 * 
	 */
	@Override
	public void receiveRequest(String text) {
		
		receiveText(text);
	}

	@Override
	public void receiveText(String text) {

		if (!remote.useTTS()) {
			System.out.println("A1icia: " + text);
		}
	}
	
	// The usual Swing startup stuff
	private void startSwing() {

        try {
            //UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
            //UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
        } catch (UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
        } catch (IllegalAccessException ex) {
            ex.printStackTrace();
        } catch (InstantiationException ex) {
            ex.printStackTrace();
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
        // Turn off metal's use of bold fonts 
        //UIManager.put("swing.boldMetal", Boolean.FALSE);
        
        //Schedule a job for event dispatch thread:
        //creating and showing this application's GUI.
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
        window = new ConsoleWindow("A1icia Swing Console");
        window.addWindowListener(new WindowCloser());
        window.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        
        //Set up the content pane.
        window.addComponentsToPane();
        
        
        //Display the window.
        window.pack();
        window.setVisible(true);
        showStartUp();
    }
	
	private void showStartUp() {
		
		window.displayTextln("Welcome to " + getConsoleName() + ".");
		window.displayTextln("This station connects to A1icia Central at " + host + 
				" on port " + port);
		window.displayTextln("The default language is " + station.getDefaultLanguage().getDisplayName());
		window.displayTextln("We currently " + (station.isQuiet() ? "are" : "are not") + 
				" in quiet mode.");
		showHelp();
		window.displayTextln("Running console SWINGCONSOLE");
		window.displayTextln();
		
	}
	
	private static String getConsoleName() {
	
		return "the A1icia Swing Console command-line interface";
	}

	private class ConsoleWindow extends JFrame implements KeyListener, ActionListener {
		private static final long serialVersionUID = 1L;
		private JTextArea displayArea;
	    private JTextField typingArea;

		ConsoleWindow(String name) {
			super(name);
		}
	    
	    void addComponentsToPane() {
	        
	        JButton button = new JButton("Clear");
	        button.addActionListener(this);
	        
	        typingArea = new JTextField(60);
	        typingArea.addKeyListener(this);
	        
	        //Uncomment this if you wish to turn off focus
	        //traversal.  The focus subsystem consumes
	        //focus traversal keys, such as Tab and Shift Tab.
	        //If you uncomment the following line of code, this
	        //disables focus traversal and the Tab events will
	        //become available to the key event listener.
	        //typingArea.setFocusTraversalKeysEnabled(false);
	        
	        displayArea = new JTextArea();
	        displayArea.setEditable(false);
	        JScrollPane scrollPane = new JScrollPane(displayArea);
	        scrollPane.setPreferredSize(new Dimension(600, 400));
	        
	        getContentPane().add(typingArea, BorderLayout.PAGE_START);
	        getContentPane().add(scrollPane, BorderLayout.CENTER);
	        getContentPane().add(button, BorderLayout.PAGE_END);
	    }

		@Override
		public void keyPressed(KeyEvent e) {
			int keycode;
			String text;
			long maxHistory;
			
			keycode = e.getKeyCode();
			switch (keycode) {
				case 10:
					// enter key
					LOGGER.log(LOGLEVEL, "Received ENTER key");
					text = typingArea.getText();
					handleEnter(text);
					e.consume();
					break;
				case 38:
					// up arrow
					LOGGER.log(LOGLEVEL, "Received UP ARROW");
					try (Jedis jebus = jebusPool.getResource()) {
						maxHistory = jebus.llen(historyKey);
						if (historyIx < maxHistory) {
							text = jebus.lindex(historyKey, historyIx);
							typingArea.setText(text);
							historyIx++;
						} else {
							historyIx = 0;
							typingArea.setText("");
						}
					}
					e.consume();
					break;
				case 40:
					// down arrow
					LOGGER.log(LOGLEVEL, "Received DOWN ARROW");
					try (Jedis jebus = jebusPool.getResource()) {
						if (historyIx > 0) {
							text = jebus.lindex(historyKey, historyIx);
							typingArea.setText(text);
							historyIx--;
						} else {
							historyIx = 0;
							typingArea.setText("");
						}
					}
					e.consume();
					break;
				default:
					break;
			}
		}

		@Override
		public void keyReleased(KeyEvent e) {
		}

		@Override
		public void keyTyped(KeyEvent e) {
		}
		
		private void handleEnter(String text) {

			LOGGER.log(LOGLEVEL, "Handling text line");
			displayTextln(text);
			try (Jedis jebus = jebusPool.getResource()) {
				jebus.lpush(historyKey, text);
				jebus.ltrim(historyKey, 0, HISTORYLIMIT);
			}		
			historyIx = 0;
			if (text == null) {
				return;
			}
			if (command(text)) {
				clearTypingArea();
				return;
			}
			if (!remote.sendText(text)) {
				displayTextln("Can't communicate with A1icia Central");
			}
			clearTypingArea();
			
		}
		
		private void clearTypingArea() {
			
	        typingArea.setText("");
	        typingArea.requestFocusInWindow();
		}
		
		void displayText(String text) {			
			
			SharedUtils.checkNotNull(text);
			displayArea.append(text);
	        displayArea.setCaretPosition(displayArea.getDocument().getLength());
		}
		
		void displayTextln() {
			
			displayArea.append(NEWLINE);
	        displayArea.setCaretPosition(displayArea.getDocument().getLength());
		}
		void displayTextln(String text) {
			
			SharedUtils.checkNotNull(text);
			displayArea.append(text);
			displayArea.append(NEWLINE);
	        displayArea.setCaretPosition(displayArea.getDocument().getLength());
		}
	    
	    /** Handle the button click. */
	    @Override
		public void actionPerformed(ActionEvent e) {
	        displayArea.setText("");
	        typingArea.setText("");
	        typingArea.requestFocusInWindow();
	    }
	}
	
	private class WindowCloser extends WindowAdapter {

		public WindowCloser() {
		}

		@Override
		public void windowClosing(WindowEvent e) {

			LOGGER.log(LOGLEVEL, "Window is closing");
			e.getWindow().dispose();
			SwingConsole.this.stopAsync();
		}
		
	}
}
