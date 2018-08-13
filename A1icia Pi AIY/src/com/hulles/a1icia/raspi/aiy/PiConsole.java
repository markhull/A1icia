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
package com.hulles.a1icia.raspi.aiy;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.hulles.a1icia.api.object.A1iciaClientObject;
import com.hulles.a1icia.api.remote.WakeUp;
import com.hulles.a1icia.api.shared.SerialSememe;
import com.hulles.a1icia.api.shared.SharedUtils;
import com.hulles.a1icia.api.shared.SharedUtils.PortCheck;
import com.hulles.a1icia.cli.A1iciaCLIConsole;

public class PiConsole extends A1iciaCLIConsole {
	private final static Logger LOGGER = Logger.getLogger("A1iciaMagicMirror.Zero2Console");
	private final static Level LOGLEVEL = Level.FINE;
	private final HardwareLayer hardwareLayer;
	
	PiConsole(String host, Integer port, ConsoleType whichConsole, HardwareLayer layer) {
		super(host, port, whichConsole);
		
		SharedUtils.checkNotNull(layer);
		SharedUtils.exitIfAlreadyRunning(PortCheck.A1ICIA_PI_CONSOLE);
		this.hardwareLayer = layer;
	}
	
	@Override
	protected String getConsoleName() {
	
		return "the A1icia Raspberry Pi command-line interface";
	}

	public void sendAudio(byte[] audioBytes) {
	
		SharedUtils.checkNotNull(audioBytes);
		getRemote().sendAudio(audioBytes);
	}
	
	public void sendText(String message) {
	
		SharedUtils.checkNotNull(message);
		getRemote().sendText(message);
	}
	
	@Override
	protected void run() {
		SerialSememe sememe;

		sememe = new SerialSememe();
		sememe.setName("what_is_pi");
		getRemote().sendCommand(sememe, null);
		super.run();
	}
	
	@Override
	public boolean receiveCommand(SerialSememe command) {

		SharedUtils.checkNotNull(command);
		LOGGER.log(LOGLEVEL, "Zero2Console: in receiveCommand");
		super.receiveCommand(command);
		switch (command.getName()) {
			case "central_startup":
				LOGGER.log(LOGLEVEL, "Zero2Console: A1icia Central startup command");
				return true;
			case "central_shutdown":
				LOGGER.log(LOGLEVEL, "Zero2Console: A1icia Central shutdown command");
				return true;
			case "set_blue_LED_on":
			case "set_blue_LED_off":
			case "blink_blue_LED":
			case "pulse_blue_LED":
				LOGGER.log(LOGLEVEL, "Zero2Console: LED command");
				hardwareLayer.setLED(command.getName());
				return true;
			case "wake_up_console":
				WakeUp.wakeUpLinux();
				LOGGER.log(LOGLEVEL, "Zero2Console: wake up command");
				return true;
			default:
				System.err.println("Zero2Console: received unknown command, ignoring it");
				break;
		}
		return false;
	}

	@Override
	public void receiveExplanation(String text) {

		super.receiveExplanation(text);
	}

	@Override
	public boolean receiveObject(A1iciaClientObject object) {

		return super.receiveObject(object);
	}

}
