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
package com.hulles.a1icia.juliet.fuzzywuzzy.algorithms;

import java.util.HashSet;
import java.util.Set;

final class SetUtils {

    static <T> Set<T> intersection (Set<T> s1, Set<T> s2){

        Set<T> intersection = new HashSet<>(s1);
        intersection.retainAll(s2);

        return intersection;

    }

    static <T> Set<T> difference (Set<T> s1, Set<T> s2) {

        Set<T> difference = new HashSet<>(s1);
        difference.removeAll(s2);

        return difference;

    }

}
