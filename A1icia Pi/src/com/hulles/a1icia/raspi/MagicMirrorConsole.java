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
import com.hulles.a1icia.api.shared.SharedUtils.PortCheck;

public class MagicMirrorConsole extends AbstractExecutionThreadService implements A1iciaRemoteDisplay, WakeUppable {
//	private final static Logger logger = Logger.getLogger("A1iciaMagicMirror.MagicMirrorConsole");
//	private final static Level LOGLEVEL = Level.INFO;
	private A1iciaRemote remote;
	private final HardwareLayerMirror hardwareLayer;
	private final String host;
	private final Integer port;
	@SuppressWarnings("unused")
	// the daemon flag is currently unused, since the mirror console is an A1iciaRemoteDisplay, not a console per se
	private final Boolean daemon;
	
	public MagicMirrorConsole(String host, Integer port, Boolean daemon, HardwareLayerMirror hardwareLayer) {

		SharedUtils.checkNotNull(host);
		SharedUtils.checkNotNull(port);
		SharedUtils.checkNotNull(daemon);
		SharedUtils.checkNotNull(hardwareLayer);
		SharedUtils.exitIfAlreadyRunning(PortCheck.A1ICIA_MAGIC_MIRROR);
		this.host = host;
		this.port = port;
		this.daemon = daemon;
		this.hardwareLayer = hardwareLayer;
	}

	@Override
	public void receiveText(String text) {

		if (!remote.useTTS()) {
			throw new A1iciaAPIException("Command line interface not enabled in A1iciaMagicMirror");
		}
	}
	
	@Override
	public void motionTriggered() {
		SerialSpark spark;
		
		if (remote.serverUp()) {
			spark = new SerialSpark();
			spark.setName("greet");
			remote.sendCommand(spark, null);
		}
	}
	
	@Override
	public boolean receiveCommand(SerialSpark command) {

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
		
		remote = new A1iciaRemote(host, port, this);
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
		SerialSpark spark;

		spark = new SerialSpark();
		spark.setName("what_is_pi");
		remote.sendCommand(spark, null);
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

	@Override
	public void receiveRequest(String text) {

		receiveText(text);
	}
	
}
