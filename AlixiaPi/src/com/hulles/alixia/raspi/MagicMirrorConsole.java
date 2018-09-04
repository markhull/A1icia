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

import com.google.common.util.concurrent.AbstractExecutionThreadService;
import com.hulles.alixia.api.object.AlixiaClientObject;
import com.hulles.alixia.api.remote.AlixiaRemote;
import com.hulles.alixia.api.remote.AlixiaRemoteDisplay;
import com.hulles.alixia.api.remote.WakeUp;
import com.hulles.alixia.api.shared.AlixiaException;
import com.hulles.alixia.api.shared.SerialSememe;
import com.hulles.alixia.api.shared.SharedUtils;
import com.hulles.alixia.api.shared.SharedUtils.PortCheck;

public class MagicMirrorConsole extends AbstractExecutionThreadService implements AlixiaRemoteDisplay, WakeUppable {
//	private final static Logger logger = Logger.getLogger("AlixiaMagicMirror.MagicMirrorConsole");
//	private final static Level LOGLEVEL = Level.INFO;
	private AlixiaRemote remote;
	private final HardwareLayerMirror hardwareLayer;
	private final String host;
	private final Integer port;
	
	public MagicMirrorConsole(String host, Integer port, HardwareLayerMirror hardwareLayer) {

		SharedUtils.checkNotNull(host);
		SharedUtils.checkNotNull(port);
		SharedUtils.checkNotNull(hardwareLayer);
		SharedUtils.exitIfAlreadyRunning(PortCheck.ALIXIA_MAGIC_MIRROR);
		this.host = host;
		this.port = port;
		this.hardwareLayer = hardwareLayer;
	}

	@Override
	public void receiveText(String text) {

		if (!remote.useTTS()) {
			throw new AlixiaException("Command line interface not enabled in AlixiaMagicMirror");
		}
	}
	
	@Override
	public void motionTriggered() {
		SerialSememe sememe;
		
		if (remote.serverUp()) {
			sememe = new SerialSememe();
			sememe.setName("greet");
			remote.sendCommand(sememe, null);
		}
	}
	
	@Override
	public boolean receiveCommand(SerialSememe command) {

		SharedUtils.checkNotNull(command);
		switch (command.getName()) {
			case "central_startup":
				if (!hardwareLayer.ledIsOn("LeftGreen")) {
					hardwareLayer.setLED("set_green_LED_on");
				}
				return true;
			case "central_shutdown":
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
		
		remote = new AlixiaRemote(host, port, this);
		remote.startAsync();
		remote.awaitRunning();
		
		remote.setShowText(false);
		remote.setShowImage(false); // for now, see next comment
		remote.setPlayVideo(false); // for now, though we could stick it in a frame in MM...
		remote.setPlayAudio(true); // rock on
		remote.setUseTTS(true); // talk on
	}

	@Override
	protected void shutDown() throws Exception {
		
		remote.stopAsync();
		remote.awaitTerminated();
	}

	@Override
	protected void run() throws Exception {
		SerialSememe sememe;

		sememe = new SerialSememe();
		sememe.setName("what_is_pi");
		remote.sendCommand(sememe, null);
		while(isRunning()) {}
	}

	@Override
	public void receiveExplanation(String text) {

		// we don't do anything with the explanation here, yet
	}

	@Override
	public boolean receiveObject(AlixiaClientObject object) {

		// we also don't currently do anything with the media object
		return false;
	}

	@Override
	public void receiveRequest(String text) {

		receiveText(text);
	}
	
}
