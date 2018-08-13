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
package com.hulles.a1icia.juliet.fuzzywuzzy.ratios;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.hulles.a1icia.juliet.fuzzywuzzy.Ratio;
import com.hulles.a1icia.juliet.fuzzywuzzy.StringProcessor;
import com.hulles.a1icia.juliet.fuzzywuzzy.utils.DiffUtils;
import com.hulles.a1icia.juliet.fuzzywuzzy.utils.structs.MatchingBlock;

/**
 * Partial ratio of similarity
 */
public class PartialRatio implements Ratio {

    /**
     * Computes a partial ratio between the strings
     *
     * @param s1 Input string
     * @param s2 Input string
     * @return The partial ratio
     */
    @Override
    public int apply(String s1, String s2) {

        String shorter;
        String longer;

        if (s1.length() < s2.length()){

            shorter = s1;
            longer = s2;

        } else {

            shorter = s2;
            longer = s1;

        }

        MatchingBlock[] matchingBlocks = DiffUtils.getMatchingBlocks(shorter, longer);

        List<Double> scores = new ArrayList<>();

        for (MatchingBlock mb : matchingBlocks) {

            int dist = mb.dpos - mb.spos;

            int long_start = dist > 0 ? dist : 0;
            int long_end = long_start + shorter.length();

            if(long_end > longer.length()) long_end = longer.length();

            String long_substr = longer.substring(long_start, long_end);

            double ratio = DiffUtils.getRatio(shorter, long_substr);

            if (ratio > .995) {
                return 100;
            }
			scores.add(ratio);

        }

        return (int) Math.round(100 * Collections.max(scores));

    }

    @Override
    public int apply(String s1, String s2, StringProcessor sp) {
        return apply(sp.process(s1), sp.process(s2));
    }


}
