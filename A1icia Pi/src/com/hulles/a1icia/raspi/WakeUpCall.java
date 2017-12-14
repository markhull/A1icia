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

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.hulles.a1icia.api.remote.WakeUp;
import com.hulles.a1icia.api.shared.SharedUtils;

public final class WakeUpCall implements Callable<Void> {
	final static Logger LOGGER = Logger.getLogger("A1iciaPi.WakeUpCall");
	final static Level LOGLEVEL = Level.FINE;
	private final static int INITIAL_DELAY = 60 * 1000; // 1 minute
	private final static int RETRIGGER_DELAY = 4 * 60 * 1000; // 4 minutes
	private final static boolean WAKEUP_DISPLAY = true;
	private final WakeUppable console;
	private final Timer timer;
	volatile boolean allowTrigger = false;
	private TriggerReset triggerReset = null;
	
	public WakeUpCall(WakeUppable console) {
		
		SharedUtils.checkNotNull(console);
		this.console = console;
		this.timer = new Timer();
		// we delay the first wakeup to let startup greeting(s) run, if any
		triggerReset = new TriggerReset();
		timer.schedule(triggerReset, INITIAL_DELAY);
	}
	
	public static int getRetriggerDelay() {
	
		return RETRIGGER_DELAY;
	}
	
	@Override
	public Void call() throws Exception {
		
		LOGGER.log(LOGLEVEL, "WakeUpCall: knock on the door");
		// "debounce" the motion signal
		if (allowTrigger) {
			LOGGER.log(LOGLEVEL, "WakeUpCall: triggered");
			allowTrigger = false;
			console.motionTriggered();
			if (WAKEUP_DISPLAY) {
				WakeUp.wakeUpLinux();
			}
			if (triggerReset != null) {
				triggerReset.cancel();
			}
			triggerReset = new TriggerReset();
			timer.schedule(triggerReset, RETRIGGER_DELAY);
		}
		return null;
	}

	private class TriggerReset extends TimerTask {
		
		TriggerReset() {
		}

		@Override
		public void run() {
			LOGGER.log(LOGLEVEL, "WakeUpCall: reset");
			allowTrigger = true;
		}
	}
}
