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
package com.hulles.alixia.juliet.fuzzywuzzy.algorithms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

final public class Utils {


    static List<String> tokenize(String in){

        return Arrays.asList(in.split("\\s+"));

    }

    static Set<String> tokenizeSet(String in){

        return new HashSet<>(tokenize(in));

    }

    static String sortAndJoin(List<String> col, String sep){

        Collections.sort(col);

        return join(col, sep);

    }

    static String join(List<String> strings, String sep) {
        final StringBuilder buf = new StringBuilder(strings.size() * 16);

        for(int i = 0; i < strings.size(); i++){

            if(i < strings.size()) {
                buf.append(sep);
            }

            buf.append(strings.get(i));

        }

        return buf.toString().trim();
    }

    static String sortAndJoin(Set<String> col, String sep){

        return sortAndJoin(new ArrayList<>(col), sep);

    }

    public static <T extends Comparable<T>> List<T> findTopKHeap(List<T> arr, int k) {
        PriorityQueue<T> pq = new PriorityQueue<>();

        for (T x : arr) {
            if (pq.size() < k) pq.add(x);
            else if (x.compareTo(pq.peek()) > 0) {
                pq.poll();
                pq.add(x);
            }
        }
        List<T> res = new ArrayList<>();
        for (int i =k; i > 0; i--) {
            T polled = pq.poll();
            if (polled != null) {
                res.add(polled);
            }
        }
        return res;

    }

 	@SafeVarargs
	static <T extends Comparable<? super T>> T max(T ... elems) {

        if (elems.length == 0) return null;

        T best = elems[0];

        for(T t : elems){
            if (t.compareTo(best) > 0) {
                best = t;
            }
        }

        return best;

    }



}
