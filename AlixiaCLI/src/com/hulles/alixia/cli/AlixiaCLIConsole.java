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
package com.hulles.alixia.cli;

import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.InputStreamReader;

import com.google.common.util.concurrent.AbstractExecutionThreadService;
import com.hulles.alixia.api.AlixiaConstants;
import com.hulles.alixia.api.object.AlixiaClientObject;
import com.hulles.alixia.api.object.LoginObject;
import com.hulles.alixia.api.remote.AlixiaRemote;
import com.hulles.alixia.api.remote.AlixiaRemoteDisplay;
import com.hulles.alixia.api.remote.Station;
import com.hulles.alixia.api.shared.AlixiaException;
import com.hulles.alixia.api.shared.SerialSememe;
import com.hulles.alixia.api.shared.SharedUtils;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This is a moderately sophisticated <b>C</b>ommand-<b>L</b>ine <b>I</b>nterface REPL package. 
 * AlixiaRemote does most of the work; this implements AlixiaRemoteDisplay to display the results.
 * 
 * @author hulles
 */
public class AlixiaCLIConsole extends AbstractExecutionThreadService implements AlixiaRemoteDisplay {
	private final static Logger LOGGER = Logger.getLogger("AlixiaCLI.AlixiaCLIConsole");
	private final static Level LOGLEVEL = AlixiaConstants.getAlixiaLogLevel();
    private final static String CONSOLENAME = "the Alixia command-line interface";
	private AlixiaRemote remote;
	private final Console javaConsole;
	private ConsoleType whichConsole;
	@SuppressWarnings("unused")
	private volatile boolean serverUp;
	private final String host;
	private final Integer port;
	private final Station station;
	
    /**
     * Build the CLI.
     * 
     * @param host The Jebus Central host name or IP address
     * @param port THe Jebus Central port
     * @param console The ConsoleType we're running
     */
	public AlixiaCLIConsole(String host, Integer port, ConsoleType console) {

		SharedUtils.checkNotNull(host);
		SharedUtils.checkNotNull(port);
		SharedUtils.checkNotNull(console);
		this.host = host;
		this.port = port;
		this.whichConsole = console;
		station = Station.getInstance();
		station.ensureStationExists();
		switch (whichConsole) {
			case DAEMON:
				javaConsole = null;
				break;
			case DEFAULT:
				javaConsole = System.console();
		        if (javaConsole != null) {
		        	whichConsole = ConsoleType.JAVACONSOLE; 
		        } else {
		        	whichConsole = ConsoleType.STANDARDIO;
		        }
		        break;
			case JAVACONSOLE:
				javaConsole = System.console();
				if (javaConsole == null) {
					LOGGER.log(Level.SEVERE, "Unable to allocate java console, aborting");
					System.exit(1);
				}
				break;
			case STANDARDIO:
				javaConsole = null;
				break;
			default:
				throw new AlixiaException("Invalid console type");
		}
		consolePrintln("Welcome to " + CONSOLENAME + ".");
		consolePrintln("This station connects to Alixia Central at " + host + 
				" on port " + port);
		consolePrintln("The default language is " + station.getDefaultLanguage().getDisplayName());
		consolePrintln("We currently " + (station.isQuiet() ? "are" : "are not") + 
				" in quiet mode.");
		consolePrintln("Running console " + whichConsole);
		showHelp();
		consolePrintln();
	}
	
    /**
     * Return the name of this console.
     * 
     * @return The console name
     */
	@SuppressWarnings("static-method")
	protected String getConsoleName() {
	
		return CONSOLENAME;
	}
	
    /**
     * Start up the Guava Execution Service
     */
	@Override
	protected void startUp() {
		
		remote = new AlixiaRemote(host, port, this);
		remote.startAsync();
		remote.awaitRunning();
		
		remote.setUseTTS(true);
		remote.setPlayAudio(true);
		remote.setShowImage(false);
		remote.setShowText(false);
		remote.setPlayVideo(false);

		showRemoteStatus();
	}
	
    /**
     * Shut down the Guava Execution Service
     */
	@Override
	protected void shutDown() {
		
		remote.shutdownRemote();
		remote.awaitTerminated();
        this.stopAsync();
	}
	
    /**
     * Run method for the Guava Execution Service. The Service expects this to block
     * until the Service is ready to be terminated.
     * 
     */
	@Override
	protected void run() {
		
		switch (whichConsole) {
			case JAVACONSOLE:
				runJavaConsole();
				break;
			case STANDARDIO:
				runStandardIn();
				break;
			case DAEMON:
				runDaemon();
				break;
			default:
				LOGGER.log(Level.SEVERE, "System error: bad console type, exiting");
				this.stopAsync();
		}
	}
	
    /**
     * Get the AlixiaRemote for this CLI.
     * 
     * @return The remote
     */
	protected AlixiaRemote getRemote() {
	
		return remote;
	}
	
    /**
     * Run the daemon. We don't want any I/O so the method just loops.
     * 
     */
	private void runDaemon() {
	
		while(isRunning()) {}
	}
	
    /**
     * Run the System.console, which we determined was implemented earlier.
     * This blocks until we're ready to terminate the console.
     * 
     */
	private void runJavaConsole() {
		String input;
		
		while (isRunning()) {
            consolePrintPrompt();
			input = javaConsole.readLine();
			if (input == null) {
				continue;
			}
			if (command(input)) {
				continue;
			}
			if (!remote.sendText(input)) {
				LOGGER.log(Level.SEVERE, "Can't communicate with Alixia Central");
			}
		}
	}
	
    /**
     * Run a simple System.in/System.out console. This blocks until we're ready to
     * terminate the service.
     * 
     */
	private void runStandardIn() {
		InputStreamReader stdIn;
		String input;
		
		stdIn = new InputStreamReader(System.in);
		try (BufferedReader reader = new BufferedReader(stdIn)) {
			while (isRunning()) {
				consolePrintPrompt();
				input = reader.readLine();
				if (input == null) {
					continue;
				}
				if (command(input)) {
					continue;
				}
				if (!remote.sendText(input)) {
					LOGGER.log(Level.SEVERE, "Can't communicate with Alixia Central");
				}
			}
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, "System error: IO error, exiting");
			this.stopAsync();
		}
	}

    /**
     * We encapsulate console printing.
     * 
     * @param text The text to print
     */
    private void consolePrint(String text) {
        
        System.out.print(text);
    }
    
    /**
     * We encapsulate console printing.
     * 
     * @param text The text to print
     */
    private void consolePrintln() {
        
        System.out.println();
    }
    private void consolePrintln(String text) {
        
        System.out.println(text);
    }
    
    /**
     * Print a "Me:" prompt, possibly including a user name if a user has logged in.
     * 
     */
    private void consolePrintPrompt() {
        String userName;
        String prompt;
        
        userName = remote.getUserName();
        if (userName == null) {
            prompt = "Me: ";
        } else {
            prompt = "Me (" + userName + "): ";
        }
        consolePrint(prompt);
    }
    
    /**
     * Receive text from AlixiaRemote, and just print it.
     * 
     * @param text The incoming text
     */
	@Override
	public void receiveText(String text) {

        consolePrintln();
        consolePrintln("Alixia: " + text);
        consolePrintPrompt();
	}

    /**
     * Show a simple help prompt.
     * 
     */
	private void showHelp() {
		
		consolePrint("· Type 'quit' or 'exit' to quit the CLI ");
		consolePrintln("(or just use CTRL+C to quit Java).");
		consolePrintln("· Type 'test' to check connection to Alixia Central.");
    	if (whichConsole == ConsoleType.JAVACONSOLE) { 
			consolePrintln("· Type 'login' to log in.");
			consolePrintln("· Type 'logout' to log out.");
    	}
		consolePrintln("· Type 'text on' to display console log.");
		consolePrintln("· Type 'text off' to not display console.");
		consolePrintln("· Type 'images on' to display image content.");
		consolePrintln("· Type 'images off' to not display images.");
		consolePrintln("· Type 'TTS on' to enable text-to-speech.");
		consolePrintln("· Type 'TTS off' to disable text-to-speech.");
		consolePrintln("· Type 'audio on' to play audio content.");
		consolePrintln("· Type 'audio off' to disable audio content.");
		consolePrintln("· Type 'video on' to play video content.");
		consolePrintln("· Type 'video off' to disable video content.");
		consolePrintln("· Type 'help console' to repeat these commands.");		
	}

    /**
     * Show the status of the logical peripherals.
     * 
     */
	private  void showRemoteStatus() {
		
		consolePrintln("Video is " + (remote.playVideo() ?  "on" : "off"));
		consolePrintln("Audio is " + (remote.playAudio() ?  "on" : "off"));
		consolePrintln("TTS is " + (remote.useTTS() ?  "on" : "off"));
		consolePrintln("Text display is " + (remote.showText() ?  "on" : "off"));
		consolePrintln("Image display is " + (remote.showImage() ?  "on" : "off"));
	}
	
    /**
     * We parse the input text from the user to see if it's a command, 
     * and if so we try and process it.
     * 
     * @param text The command
     * @return True if we can process it here
     */
	private boolean command(String text) {
		boolean connected;
		
		if (text.equalsIgnoreCase("test")) {
			connected = remote.reachableHost();
			consolePrint("We are " + (connected ? "" : "NOT ") + "connected to Alixia Central ");
			consolePrintln("at " + host + 
					" on port " + port);
			consolePrintln("The language is currently " + remote.getCurrentLanguage().getDisplayName());
			consolePrintln("We currently " + (station.isQuiet() ? "are" : "are not") + 
					" in quiet mode.");
			showRemoteStatus();
			return true;
		}
		if (text.equalsIgnoreCase("tts on")) {
			remote.setUseTTS(true);
			consolePrintln("Using TTS");
			return true;
		}
		if (text.equalsIgnoreCase("tts off")) {
			remote.setUseTTS(false);
			consolePrintln("Not using TTS");
			return true;
		}
		if (text.equalsIgnoreCase("audio on")) {
			remote.setPlayAudio(true);
			consolePrintln("Audio is on");
			return true;
		}
		if (text.equalsIgnoreCase("audio off")) {
			remote.setPlayAudio(false);
			consolePrintln("Audio is off");
			return true;
		}
		if (text.equalsIgnoreCase("video on")) {
			remote.setPlayVideo(true);
			consolePrintln("Video is on");
			return true;
		}
		if (text.equalsIgnoreCase("video off")) {
			remote.setPlayVideo(false);
			consolePrintln("Video is off");
			return true;
		}
		if (text.equalsIgnoreCase("images on")) {
			remote.setShowImage(true);
			consolePrintln("Images will be displayed");
			return true;
		}
		if (text.equalsIgnoreCase("images off")) {
			remote.setShowImage(false);
			consolePrintln("Images will not be displayed");
			return true;
		}
		if (text.equalsIgnoreCase("text on")) {
			remote.setShowText(true);
			consolePrintln("Console log will be displayed");
			return true;
		}
		if (text.equalsIgnoreCase("text off")) {
			remote.setShowText(false);
			consolePrintln("Console log will not be displayed");
			return true;
		}
		if (text.equalsIgnoreCase("help console")) {
			showHelp();
			return true;
		}
    	if (whichConsole == ConsoleType.JAVACONSOLE) { 
			if (text.equalsIgnoreCase("login")) {
				login();
				return true;
			}
			if (text.equalsIgnoreCase("logout")) {
				logout();
				return true;
			}
    	}
		if (text.equalsIgnoreCase("quit")) {
            shutDown();
			return true;
		}
		if (text.equalsIgnoreCase("exit")) {
            shutDown();
			return true;
		}
		return false;
	}

    /**
     * Get a login from the user if we're running System.console, which supports
     * password input. This is obviously not very secure, so use it with care.
     * 
     */
	private void login() {
		LoginObject obj;
		String userName = null;
		char[] passwordChars;
		String password = null;
		
		switch (whichConsole) {
			case JAVACONSOLE:
				userName = javaConsole.readLine("Enter user name: ");
				passwordChars = javaConsole.readPassword("Enter password: ");
				password = String.copyValueOf(passwordChars);
				break;
			case STANDARDIO:
			case DAEMON:
				LOGGER.log(Level.SEVERE, "System error: login is not supported for {0}", whichConsole);
				break;
			default:
				LOGGER.log(Level.SEVERE, "System error: bad console type, exiting");
				System.exit(2);
		}
		obj = new LoginObject();
		obj.setUserName(userName);
		obj.setPassword(password);
		if (!remote.sendLogin(obj)) {
			LOGGER.log(Level.SEVERE, "Can't communicate with Alixia Central");
		}
	}
	
    /**
     * Log out the user if he/she is logged in.
     * 
     */
	private void logout() {
		LoginObject obj;
		
		obj = new LoginObject();
		obj.setUserName(null);
		obj.setPassword(null);
		if (!remote.sendLogin(obj)) {
			LOGGER.log(Level.SEVERE, "Can't communicate with Alixia Central");
		}
	}
	
    /**
     * We received a command from AlixiaRemote, so we try and execute it here.
     * 
     * @param command The command sememe to process
     * @return True if we were able to process the sememe
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
     * This is a notification from the Swing text window that it was closed.
     * 
     */
    @Override
    public void textWindowIsClosing() {
        
        remote.setShowText(false);
    }

    /**
     * The type of console we are.
     * 
     */
	public enum ConsoleType {
		DEFAULT, 		// converts to JAVACONSOLE if one is available, otherwise STANDARDIO
		JAVACONSOLE,
		STANDARDIO,
		DAEMON
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
	 * input. We just ignore the request for now and pass it along as a message.
	 * 
     * @param text The text from AlixiaRemote
	 */
	@Override
	public void receiveRequest(String text) {
		
		receiveText(text);
	}
}
