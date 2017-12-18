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
package com.hulles.a1icia.raspi;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.hulles.a1icia.api.object.A1iciaClientObject;
import com.hulles.a1icia.api.remote.WakeUp;
import com.hulles.a1icia.api.shared.SerialSpark;
import com.hulles.a1icia.api.shared.SharedUtils;
import com.hulles.a1icia.cli.A1iciaCLIConsole;

public class PiConsole extends A1iciaCLIConsole implements WakeUppable {
	private final static Logger LOGGER = Logger.getLogger("A1iciaMagicMirror.PiConsole");
	private final static Level LOGLEVEL = Level.FINE;
	private final static int PORT = 12348;
	private final HardwareLayer hardwareLayer;
	
	PiConsole(String host, Integer port, HardwareLayer layer) {
		super(host, port);
		
		SharedUtils.checkNotNull(layer);
		if (SharedUtils.alreadyRunning(PORT)) {
			System.out.println("A1icia Pi Console is already running");
			System.exit(1);
		}
		this.hardwareLayer = layer;
	}
	
	@Override
	protected String getConsoleName() {
	
		return "the A1icia Raspberry Pi command-line interface";
	}

	@Override
	public void motionTriggered() {
		SerialSpark spark;
		
		if (getRemote().serverUp()) {
			spark = new SerialSpark();
			spark.setName("greet");
			getRemote().sendCommand(spark, null);
		}
	}

	@Override
	protected void run() {
		SerialSpark spark;

		spark = new SerialSpark();
		spark.setName("what_is_pi");
		getRemote().sendCommand(spark, null);
		super.run();
	}
	
	@Override
	public boolean receiveCommand(SerialSpark command) {

		SharedUtils.checkNotNull(command);
		LOGGER.log(LOGLEVEL, "PiConsole: in receiveCommand");
		super.receiveCommand(command);
		switch (command.getName()) {
			case "server_startup":
				if (!hardwareLayer.ledIsOn("LeftGreen")) {
					hardwareLayer.setLED("set_green_LED_on");
				}
				LOGGER.log(LOGLEVEL, "PiConsole: server startup command");
				return true;
			case "server_shutdown":
				if (hardwareLayer.ledIsOn("LeftGreen")) {
					hardwareLayer.setLED("set_green_LED_off");
				}
				LOGGER.log(LOGLEVEL, "PiConsole: server shutdown command");
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
	public boolean receiveObject(A1iciaClientObject object) {

		return super.receiveObject(object);
	}

}
