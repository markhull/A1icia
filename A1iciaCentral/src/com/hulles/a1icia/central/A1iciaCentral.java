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

package com.hulles.a1icia.central;

import java.io.IOException;

import com.hulles.a1icia.A1icia;
import com.hulles.a1icia.alpha.AlphaRoom;
import com.hulles.a1icia.api.shared.A1iciaException;
import com.hulles.a1icia.bravo.BravoRoom;
import com.hulles.a1icia.charlie.CharlieRoom;
import com.hulles.a1icia.delta.DeltaRoom;
import com.hulles.a1icia.echo.EchoRoom;
import com.hulles.a1icia.foxtrot.FoxtrotRoom;
import com.hulles.a1icia.golf.GolfRoom;
import com.hulles.a1icia.hotel.HotelRoom;
import com.hulles.a1icia.india.IndiaRoom;
import com.hulles.a1icia.juliet.JulietRoom;
import com.hulles.a1icia.kilo.KiloRoom;
import com.hulles.a1icia.lima.LimaRoom;
import com.hulles.a1icia.mike.MikeRoom;
import com.hulles.a1icia.november.NovemberRoom;
import com.hulles.a1icia.oscar.OscarRoom;
import com.hulles.a1icia.overmind.OvermindRoom;
import com.hulles.a1icia.papa.PapaRoom;
import com.hulles.a1icia.quebec.QuebecRoom;
import com.hulles.a1icia.romeo.RomeoRoom;
import com.hulles.a1icia.sierra.SierraRoom;
import com.hulles.a1icia.tracker.TrackerRoom;

/**
 * A1iciaCentral is a simple class with a main method to start up all the 
 * various rooms and run A1icia.
 * <p>
 * N.B. We do it this way because I'm just not smart enough (yet) to know how
 * to make the modules available for A1icia's ServiceLoader to find without
 * instantiating them here.
 * 
 * @author hulles
 */
public class A1iciaCentral {   
	
	private static void waitForKey() {
		
		System.out.println("Hit a key ");
		try {
			System.in.read();
		} catch (IOException e) {
			throw new A1iciaException("A1iciaRunner: IO error reading key", e);
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

		try (A1icia a1icia = new A1icia(noprompt)) {
            
            
			waitForKey();
		}
	}
    
}
