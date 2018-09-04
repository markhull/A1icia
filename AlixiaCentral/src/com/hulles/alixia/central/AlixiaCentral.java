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

package com.hulles.alixia.central;

import java.io.IOException;

import com.hulles.alixia.Alixia;
import com.hulles.alixia.alpha.AlphaRoom;
import com.hulles.alixia.api.shared.AlixiaException;
import com.hulles.alixia.bravo.BravoRoom;
import com.hulles.alixia.charlie.CharlieRoom;
import com.hulles.alixia.delta.DeltaRoom;
import com.hulles.alixia.echo.EchoRoom;
import com.hulles.alixia.foxtrot.FoxtrotRoom;
import com.hulles.alixia.golf.GolfRoom;
import com.hulles.alixia.hotel.HotelRoom;
import com.hulles.alixia.india.IndiaRoom;
import com.hulles.alixia.juliet.JulietRoom;
import com.hulles.alixia.kilo.KiloRoom;
import com.hulles.alixia.lima.LimaRoom;
import com.hulles.alixia.mike.MikeRoom;
import com.hulles.alixia.november.NovemberRoom;
import com.hulles.alixia.oscar.OscarRoom;
import com.hulles.alixia.overmind.OvermindRoom;
import com.hulles.alixia.papa.PapaRoom;
import com.hulles.alixia.quebec.QuebecRoom;
import com.hulles.alixia.romeo.RomeoRoom;
import com.hulles.alixia.sierra.SierraRoom;
import com.hulles.alixia.tracker.TrackerRoom;

/**
 * AlixiaCentral is a simple class with a main method to start up all the 
 * various rooms and run Alixia.
 * <p>
 * N.B. We do it this way because I'm just not smart enough (yet) to know how
 * to make the modules available for Alixia's ServiceLoader to find without
 * instantiating them here.
 * 
 * @author hulles
 */
public class AlixiaCentral {   
	
	private static void waitForKey() {
		
		System.out.println("Hit a key ");
		try {
			System.in.read();
		} catch (IOException e) {
			throw new AlixiaException("AlixiaRunner: IO error reading key", e);
		}
	}

	@SuppressWarnings("unused")
	public static void main(String[] args) {
 		boolean noprompt = false;
		
		if (args.length > 0) {
			if (args[0].equals("--noprompt")) {
				noprompt = true;
			}
		}

        new TrackerRoom();
        new OvermindRoom();
        new AlphaRoom();
        new BravoRoom();
        new CharlieRoom();
        new DeltaRoom();
        new EchoRoom();
        new FoxtrotRoom();
        new GolfRoom();
        new HotelRoom();
        new IndiaRoom();
        new JulietRoom();
        new KiloRoom();
        new LimaRoom();
        new MikeRoom();
        new NovemberRoom();
        new OscarRoom();
        new PapaRoom();
        new QuebecRoom();
        new RomeoRoom();
        new SierraRoom();

		try (Alixia alixia = new Alixia(noprompt)) {
            
            
			waitForKey();
		}
	}
    
}
