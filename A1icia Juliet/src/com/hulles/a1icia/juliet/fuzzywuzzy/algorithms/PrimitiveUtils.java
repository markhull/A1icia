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
package com.hulles.a1icia.juliet.fuzzywuzzy.algorithms;

final class PrimitiveUtils {

    static double max(double ... elems) {

        if (elems.length == 0) return 0;

        double best = elems[0];

        for(double t : elems){
            if (t > best) {
                best = t;
            }
        }

        return best;

    }

    static int max(int ... elems) {

        if (elems.length == 0) return 0;

        int best = elems[0];

        for(int t : elems){
            if (t > best) {
                best = t;
            }
        }

        return best;

    }
    
    
    
}
