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
package com.hulles.alixia.raspi;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.hulles.alixia.api.object.AlixiaClientObject;
import com.hulles.alixia.api.remote.WakeUp;
import com.hulles.alixia.api.shared.SerialSememe;
import com.hulles.alixia.api.shared.SharedUtils;
import com.hulles.alixia.api.shared.SharedUtils.PortCheck;
import com.hulles.alixia.cli.AlixiaCLIConsole;

public class PiConsole extends AlixiaCLIConsole implements WakeUppable {
	private final static Logger LOGGER = Logger.getLogger("AlixiaPi.PiConsole");
	private final static Level LOGLEVEL = Level.FINE;
	private final HardwareLayer hardwareLayer;
	
	PiConsole(String host, Integer port, ConsoleType console, HardwareLayer layer) {
		super(host, port, console);
		
		SharedUtils.checkNotNull(layer);
		SharedUtils.exitIfAlreadyRunning(PortCheck.ALIXIA_PI_CONSOLE);
		this.hardwareLayer = layer;
	}
	
	@Override
	protected String getConsoleName() {
	
		return "the Alixia Raspberry Pi command-line interface";
	}

	@Override
	public void motionTriggered() {
		SerialSememe sememe;
		
		if (getRemote().serverUp()) {
			sememe = new SerialSememe();
			sememe.setName("greet");
			getRemote().sendCommand(sememe, null);
		}
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
		LOGGER.log(LOGLEVEL, "PiConsole: in receiveCommand");
		super.receiveCommand(command);
		// the valid commands in super.receiveCommand are handled below as well, so
		//    don't just return if super.receiveCommand is true
		switch (command.getName()) {
			case "central_startup":
				if (!hardwareLayer.ledIsOn("LeftGreen")) {
					hardwareLayer.setLED("set_green_LED_on");
				}
				LOGGER.log(LOGLEVEL, "PiConsole: Alixia Central startup command");
				return true;
			case "central_shutdown":
				if (hardwareLayer.ledIsOn("LeftGreen")) {
					hardwareLayer.setLED("set_green_LED_off");
				}
				LOGGER.log(LOGLEVEL, "PiConsole: Alixia Central shutdown command");
				return true;
			// we don't allow the MOTION and ON LEDs to be set by the Central Scrutinizer
			case "set_red_LED_on":
			case "set_red_LED_off":
			case "set_green_LED_on":
			case "set_green_LED_off":
			case "set_yellow_LED_on":
			case "set_yellow_LED_off":
			case "set_white_LED_on":
			case "set_white_LED_off":
			case "blink_red_LED":
			case "blink_green_LED":
			case "blink_yellow_LED":
			case "blink_white_LED":
			case "pulse_red_LED":
			case "pulse_green_LED":
			case "pulse_yellow_LED":
			case "pulse_white_LED":
				LOGGER.log(LOGLEVEL, "PiConsole: LED command");
				hardwareLayer.setLED(command.getName());
				return true;
			case "wake_up_console":
				WakeUp.wakeUpLinux();
				LOGGER.log(LOGLEVEL, "PiConsole: wake up command");
				return true;
			case "pretty_lights_off":
			case "pretty_lights_random":
			case "pretty_lights_spinny":
			case "pretty_lights_color_wipe":
			case "pretty_lights_theater":
			case "pretty_lights_rainbows":
				LOGGER.log(LOGLEVEL, "PiConsole: pretty lights command");
				HardwareLayer.setPrettyLights(command.getName());
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
