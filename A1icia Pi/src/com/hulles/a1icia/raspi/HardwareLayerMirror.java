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
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hulles.a1icia.raspi;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.hulles.a1icia.api.shared.SharedUtils;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.trigger.GpioCallbackTrigger;
import com.pi4j.io.gpio.trigger.GpioPulseStateTrigger;
import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;

/**
 *
 * @author hulles
 */
public final class HardwareLayerMirror implements Closeable {
	final static Logger LOGGER = Logger.getLogger("A1iciaPi.HardwareLayerMirror");
	final static Level LOGLEVEL = Level.FINE;
    public static final int PRETTY_LIGHTS = 0x04;
	private final static int DEBOUNCE_WAIT = 0;
	private final static int WAKEUP_LED_PULSE = 3000;
	private final static int BLINK_RATE = 1000;
	private final static int LED_PULSE = 2000;
	private final static int CYLON_COUNT = 3;
	private final static byte[] ALLOFF = new byte[] {(byte)0xff, (byte)0x00};
	private final static byte[] RANDOM = new byte[] {(byte)0xff, (byte)0x01};
	private final static byte[] SPINNY = new byte[] {(byte)0xff, (byte)0x02};
	private final static byte[] COLORWIPE = new byte[] {(byte)0xff, (byte)0x03};
	private final static byte[] THEATER = new byte[] {(byte)0xff, (byte)0x04};
	private final static byte[] RAINBOWS = new byte[] {(byte)0xff, (byte)0x05};
	private final GpioController gpio;
	private final GpioPinDigitalInput pinPIR; // PIR motion sensor
	private final GpioPinDigitalOutput pinRightGreen; // CONSOLE_ON led
	private final GpioPinDigitalOutput pinBlue; // MOTION led
	// unassigned
	private final GpioPinDigitalOutput pinWhite;
	private final GpioPinDigitalOutput pinYellow;
	private final GpioPinDigitalOutput pinRed;
	private final GpioPinDigitalOutput pinLeftGreen; // SERVER_ON led
	private final List<GpioPinDigitalOutput> pins;
	
	@SuppressWarnings("unused")
	public HardwareLayerMirror() {
		GpioPulseStateTrigger ledTrigger;
		
		gpio = GpioFactory.getInstance();
		
		// PIR pin is MOTION_DETECTOR
		pinPIR = gpio.provisionDigitalInputPin(RaspiPin.GPIO_01, PinPullResistance.PULL_DOWN);
		if (DEBOUNCE_WAIT > 0) {
			pinPIR.setDebounce(DEBOUNCE_WAIT);
		}
		
		// right green LED is CONSOLE_ON
		pinRightGreen = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_29, "RightGreen", PinState.HIGH);
		pinRightGreen.setShutdownOptions(true, PinState.LOW);
		
		// blue LED is MOTION
		pinBlue = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_27, "Blue", PinState.LOW);
		pinBlue.setShutdownOptions(true, PinState.LOW);
		ledTrigger = new GpioPulseStateTrigger(PinState.HIGH, pinBlue, WAKEUP_LED_PULSE);
		pinPIR.addTrigger(ledTrigger);
		
		// left green LED is SERVER_ON
		pinLeftGreen = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_06, "LeftGreen", PinState.LOW);
		pinLeftGreen.setShutdownOptions(true, PinState.LOW);

		// unassigned LEDs
		pinWhite = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_24, "White", PinState.LOW);
		pinWhite.setShutdownOptions(true, PinState.LOW);
		pinYellow = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_23, "Yellow", PinState.LOW);
		pinYellow.setShutdownOptions(true, PinState.LOW);
		pinRed = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_26, "Red", PinState.LOW);
		pinRed.setShutdownOptions(true, PinState.LOW);
		
		// order is left-to-right
		pins = new ArrayList<>(6);
		pins.add(pinLeftGreen);
		pins.add(pinYellow);
		pins.add(pinRed);
		pins.add(pinWhite);
		pins.add(pinBlue);
		pins.add(pinRightGreen);
		
		try {
			cylon();
		} catch (Exception e) {
			System.err.println("Cylon or Pretty Lights exception");
		}
	}

	public void setWakeUpCall(WakeUpCall caller) {
		GpioCallbackTrigger callbackTrigger;
		
		SharedUtils.checkNotNull(caller);
		LOGGER.log(LOGLEVEL, "HardwareLayer: set wakeup call");
//		pinPIR.setDebounce(WakeUpCall.getRetriggerDelay());
		callbackTrigger = new GpioCallbackTrigger(PinState.HIGH, caller);
		pinPIR.addTrigger(callbackTrigger);
	}
	
	public void setLED(String command) {

		SharedUtils.checkNotNull(command);
		switch (command) {
			// we don't allow the MOTION and ON LEDs to be set by the Central Scrutinizer
			case "set_red_LED_on":
				pinRed.setState(PinState.HIGH);
				break;
			case "set_red_LED_off":
				pinRed.setState(PinState.LOW);
				break;
			case "set_green_LED_on":
				pinLeftGreen.setState(PinState.HIGH);
				break;
			case "set_green_LED_off":
				pinLeftGreen.setState(PinState.LOW);
				break;
			case "set_yellow_LED_on":
				pinYellow.setState(PinState.HIGH);
				break;
			case "set_yellow_LED_off":
				pinYellow.setState(PinState.LOW);
				break;
			case "set_white_LED_on":
				pinWhite.setState(PinState.HIGH);
				break;
			case "set_white_LED_off":
				pinWhite.setState(PinState.LOW);
				break;
			case "blink_red_LED":
				pinRed.blink(BLINK_RATE);
				break;
			case "blink_green_LED":
				pinLeftGreen.blink(BLINK_RATE);
				break;
			case "blink_yellow_LED":
				pinYellow.blink(BLINK_RATE);
				break;
			case "blink_white_LED":
				pinWhite.blink(BLINK_RATE);
				break;
			case "pulse_red_LED":
				pinRed.pulse(LED_PULSE);
				break;
			case "pulse_green_LED":
				pinLeftGreen.pulse(LED_PULSE);
				break;
			case "pulse_yellow_LED":
				pinYellow.pulse(LED_PULSE);
				break;
			case "pulse_white_LED":
				pinWhite.pulse(LED_PULSE);
				break;
			default:
				System.err.println("HardwareLayer: received unknown command, ignoring it");
				break;
		}
	}

	public static void setPrettyLights(String command) {
        I2CBus i2c = null;
        I2CDevice device = null;
        
		SharedUtils.checkNotNull(command);
		LOGGER.log(LOGLEVEL, "HardwareLayer: Pretty Lights command = " + command);
		try {
			i2c = I2CFactory.getInstance(I2CBus.BUS_1);
			device = i2c.getDevice(PRETTY_LIGHTS);
			switch (command) {
				case "pretty_lights_off":
					device.write(ALLOFF);
					break;
				case "pretty_lights_random":
					device.write(RANDOM);
					break;
				case "pretty_lights_spinny":
					device.write(SPINNY);
					break;
				case "pretty_lights_color_wipe":
					device.write(COLORWIPE);
					break;
				case "pretty_lights_theater":
					device.write(THEATER);
					break;
				case "pretty_lights_rainbows":
					device.write(RAINBOWS);
					break;
				default:
					System.err.println("HardwareLayer: received unknown pretty lights command, ignoring it");
					break;
			}
		} catch (UnsupportedBusNumberException | IOException e) {
			System.err.println("HardwareLayer: I/O exception executing pretty lights command");
		}
	}
	
	public boolean ledIsOn(String ledName) {
		GpioPinDigitalOutput pin;
		
		 pin = (GpioPinDigitalOutput) gpio.getProvisionedPin(ledName);
		 return pin.isHigh();
	}
	
	public void cylon() throws Exception {
		int ix;
		
		for (int iy=0; iy <CYLON_COUNT; iy++) {
	        for(ix=0; ix<pins.size(); ix++) {
	            pins.get(ix).pulse(50);
	            Thread.sleep(50);
	        }
	        // ix is at 6
	        for(ix-- ; ix>=0; ix--) {
	            pins.get(ix).pulse(50);
	            Thread.sleep(50);
	        }
		}
		// turn ON led back on
		pinRightGreen.setState(PinState.HIGH);
	}
	
	public static void powerOnPrettyLights() throws Exception {		
        I2CBus i2c = null;
        I2CDevice device = null;
        
		i2c = I2CFactory.getInstance(I2CBus.BUS_1);
		device = i2c.getDevice(PRETTY_LIGHTS);
        
		device.write(SPINNY);
        
		Thread.sleep(1000*2);
        
		device.write(RAINBOWS);
        
		Thread.sleep(1000*2);

		device.write(ALLOFF);		
	}
	
	public static void initializePrettyLights() throws Exception {
        I2CBus i2c = null;
        I2CDevice device = null;
        
		i2c = I2CFactory.getInstance(I2CBus.BUS_1);
		device = i2c.getDevice(PRETTY_LIGHTS);

		device.write(ALLOFF);		
	}
	
	@Override
	public void close() {
		
		gpio.shutdown();
	}
}
