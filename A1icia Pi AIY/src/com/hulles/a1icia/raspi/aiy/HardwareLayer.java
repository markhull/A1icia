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
 *******************************************************************************/
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hulles.a1icia.raspi.aiy;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.hulles.a1icia.api.shared.SharedUtils;
import com.hulles.a1icia.media.audio.AudioRecorder;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPin;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import com.pi4j.io.gpio.trigger.GpioPulseStateTrigger;

/**
 *
 * @author hulles
 */
public final class HardwareLayer implements Closeable {
	final static Logger LOGGER = Logger.getLogger("A1iciaPiAIY.HardwareLayer");
	final static Level LOGLEVEL = Level.FINE;
	private final static int BLINK_RATE = 1000;
	private final static int LED_PULSE = 2000;
	private final static int SWITCH_DEBOUNCE = 800; // ms
	private final GpioController gpio;
	final GpioPinDigitalOutput pinBlue; // LISTENING led
	private final GpioPinDigitalInput pinSwitch;
	private final List<GpioPin> pins;
	PiConsole console;
	
	@SuppressWarnings("unused")
	public HardwareLayer() {
		GpioPulseStateTrigger ledTrigger;
		
		gpio = GpioFactory.getInstance();
				
		pinSwitch = gpio.provisionDigitalInputPin(RaspiPin.GPIO_04, "Switch", PinPullResistance.PULL_UP);
		pinSwitch.setDebounce(SWITCH_DEBOUNCE);
		pinSwitch.setShutdownOptions(true);
		registerListener();
		
		// blue LED is LISTENING
		pinBlue = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_06, "Blue", PinState.LOW);
		pinBlue.setShutdownOptions(true, PinState.LOW);
		
		pins = new ArrayList<>(2);
		pins.add(pinSwitch);
		pins.add(pinBlue);

		this.console = null;
	}
	
	public void setConsole(PiConsole z2console) {
	
		SharedUtils.checkNotNull(z2console);
		this.console = z2console;
	}
	
	public void setLED(String command) {

		SharedUtils.checkNotNull(command);
		switch (command) {
			// we don't allow the POWER ON LED to be set by the Central Scrutinizer
			case "set_blue_LED_on":
				pinBlue.setState(PinState.HIGH);
				break;
			case "set_blue_LED_off":
				pinBlue.setState(PinState.LOW);
				break;
			case "blink_blue_LED":
				pinBlue.blink(BLINK_RATE);
				break;
			case "pulse_blue_LED":
				pinBlue.pulse(LED_PULSE);
				break;
			default:
				System.err.println("HardwareLayer: received unknown command, ignoring it");
				break;
		}
	}
	
	public boolean ledIsOn(String ledName) {
		GpioPinDigitalOutput pin;
		
		 pin = (GpioPinDigitalOutput) gpio.getProvisionedPin(ledName);
		 return pin.isHigh();
	}
	
    // create and register gpio pin listener
	public void registerListener() {
		
	    pinSwitch.addListener(new GpioPinListenerDigital() {
	        @Override
	        public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
	    		PinState thisState;
	    		byte[] soundBytes;
	    		
	    		thisState = event.getState();
//	            System.out.println(" --> GPIO PIN STATE CHANGE: " + event.getPin() + " = " + thisState);
	            if (thisState == PinState.LOW) {
	            	pinBlue.setState(PinState.LOW);
					pinBlue.setState(PinState.HIGH);
					try {
						soundBytes = AudioRecorder.recordBytes(3);
//						AudioBytePlayer.playAudioFromByteArray(soundBytes, null);
						console.sendAudio(soundBytes);
					} catch (Exception e) {
						System.err.println("HardwareLayer: recording exception");
						e.printStackTrace();
					}
					pinBlue.blink(BLINK_RATE);
	            }
	        }
	
	    });
	}
	
	@Override
	public void close() {
		
		gpio.shutdown();
	}
	
}
