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

import com.google.common.util.concurrent.AbstractExecutionThreadService;
import com.hulles.a1icia.api.object.A1iciaClientObject;
import com.hulles.a1icia.api.remote.A1iciaRemote;
import com.hulles.a1icia.api.remote.A1iciaRemoteDisplay;
import com.hulles.a1icia.api.remote.WakeUp;
import com.hulles.a1icia.api.shared.A1iciaAPIException;
import com.hulles.a1icia.api.shared.SerialSpark;
import com.hulles.a1icia.api.shared.SharedUtils;

public class MagicMirrorConsole extends AbstractExecutionThreadService implements A1iciaRemoteDisplay, WakeUppable {
//	private final static Logger logger = Logger.getLogger("A1iciaMagicMirror.MagicMirrorConsole");
//	private final static Level LOGLEVEL = Level.INFO;
	private final static int PORT = 12347;
	private A1iciaRemote console;
	private final HardwareLayer hardwareLayer;
	
	public MagicMirrorConsole(HardwareLayer hardwareLayer) {

		SharedUtils.checkNotNull(hardwareLayer);
		if (SharedUtils.alreadyRunning(PORT)) {
			System.out.println("A1icia Magic Mirror is already running");
			System.exit(1);
		}
		this.hardwareLayer = hardwareLayer;
	}

	@Override
	public void receiveText(String text) {

		if (!console.useTTS()) {
			throw new A1iciaAPIException("Command line interface not enabled in A1iciaMagicMirror");
		}
	}
	
	@Override
	public void motionTriggered() {
		SerialSpark spark;
		
		if (console.serverUp()) {
			spark = new SerialSpark();
			spark.setName("greet");
			console.sendCommand(spark, null);
		}
	}
	
	@Override
	public boolean receiveCommand(SerialSpark command) {

		SharedUtils.checkNotNull(command);
		switch (command.getName()) {
			case "server_startup":
				if (!hardwareLayer.ledIsOn("LeftGreen")) {
					hardwareLayer.setLED("set_green_LED_on");
				}
				return true;
			case "server_shutdown":
				if (hardwareLayer.ledIsOn("LeftGreen")) {
					hardwareLayer.setLED("set_green_LED_off");
				}
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
				hardwareLayer.setLED(command.getName());
				return true;
			case "wake_up_console":
				WakeUp.wakeUpLinux();
				return true;
			case "pretty_lights_off":
			case "pretty_lights_random":
			case "pretty_lights_spinny":
			case "pretty_lights_color_wipe":
			case "pretty_lights_theater":
			case "pretty_lights_rainbows":
				HardwareLayer.setPrettyLights(command.getName());
				return true;
			default:
				System.err.println("PiConsole: received unknown command, ignoring it");
				break;
		}
		return false;
	}

	@Override
	protected void startUp() throws Exception {
		
		console = new A1iciaRemote(this);
		console.startAsync();
		console.awaitRunning();
		
		console.setShowText(false);
		console.setShowImage(false); // for now, see next comment
		console.setPlayVideo(false); // for now, though we could stick it in a frame in MM...
		console.setPlayAudio(true); // rock on
		console.setUseTTS(true); // talk on
	}

	@Override
	protected void shutDown() throws Exception {
		
		console.stopAsync();
		console.awaitTerminated();
	}

	@Override
	protected void run() throws Exception {
		SerialSpark spark;

		spark = new SerialSpark();
		spark.setName("what_is_pi");
		console.sendCommand(spark, null);
		while(isRunning()) {}
	}

	@Override
	public void receiveExplanation(String text) {

		// we don't do anything with the explanation here, yet
	}

	@Override
	public boolean receiveObject(A1iciaClientObject object) {

		// we also don't currently do anything with the media object
		return false;
	}
	
}
