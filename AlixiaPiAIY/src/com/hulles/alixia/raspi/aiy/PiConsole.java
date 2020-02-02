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
package com.hulles.alixia.raspi.aiy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hulles.alixia.api.object.AlixiaClientObject;
import com.hulles.alixia.api.remote.WakeUp;
import com.hulles.alixia.api.shared.SerialSememe;
import com.hulles.alixia.api.shared.SharedUtils;
import com.hulles.alixia.api.shared.SharedUtils.PortCheck;
import com.hulles.alixia.cli.AlixiaCLIConsole;

public class PiConsole extends AlixiaCLIConsole {
	private final static Logger LOGGER = LoggerFactory.getLogger(PiConsole.class);
	private final HardwareLayer hardwareLayer;
	
	PiConsole(String host, Integer port, ConsoleType whichConsole, HardwareLayer layer) {
		super(host, port, whichConsole);
		
		SharedUtils.checkNotNull(layer);
		SharedUtils.exitIfAlreadyRunning(PortCheck.ALIXIA_PI_CONSOLE);
		this.hardwareLayer = layer;
	}
	
	@Override
	protected String getConsoleName() {
	
		return "the Alixia Raspberry Pi command-line interface";
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
		LOGGER.debug("PiConsole: in receiveCommand");
		super.receiveCommand(command);
		switch (command.getName()) {
			case "central_startup":
				LOGGER.debug("PiConsole: Alixia Central startup command");
				return true;
			case "central_shutdown":
				LOGGER.debug("PiConsole: Alixia Central shutdown command");
				return true;
			case "set_blue_LED_on":
			case "set_blue_LED_off":
			case "blink_blue_LED":
			case "pulse_blue_LED":
				LOGGER.debug("PiConsole: LED command");
				hardwareLayer.setLED(command.getName());
				return true;
			case "wake_up_console":
				WakeUp.wakeUpLinux();
				LOGGER.debug("PiConsole: wake up command");
				return true;
			default:
				System.err.println("PiConsole: received unknown command, ignoring it");
				break;
		}
		return false;
	}

	@Override
	public void receiveExplanation(String text) {

		super.receiveExplanation(text);
	}

	@Override
	public boolean receiveObject(AlixiaClientObject object) {

		return super.receiveObject(object);
	}

}
