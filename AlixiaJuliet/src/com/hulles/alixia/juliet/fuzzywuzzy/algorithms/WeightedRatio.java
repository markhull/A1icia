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

import static java.lang.Math.round;

import com.hulles.alixia.juliet.fuzzywuzzy.FuzzySearch;
import com.hulles.alixia.juliet.fuzzywuzzy.StringProcessor;


public class WeightedRatio extends BasicAlgorithm  {

    public static final double UNBASE_SCALE = .95;
    public static final double PARTIAL_SCALE = .90;
    public static final boolean TRY_PARTIALS = true;


    @Override
    public int apply(String s1, String s2, StringProcessor stringProcessor) {
        String p1;
        String p2;
        
        p1 = stringProcessor.process(s1);
        p2 = stringProcessor.process(s2);

        int len1 = p1.length();
        int len2 = p2.length();

        if (len1 == 0 || len2 == 0) { return 0; }

        boolean tryPartials = TRY_PARTIALS;
        double unbaseScale = UNBASE_SCALE;
        double partialScale = PARTIAL_SCALE;

        int base = FuzzySearch.ratio(p1, p2);
        double lenRatio = ((double) Math.max(len1, len2)) / Math.min(len1, len2);

        // if strings are similar length don't use partials
        if (lenRatio < 1.5) tryPartials = false;

        // if one string is much shorter than the other
        if (lenRatio > 8) partialScale = .6;

        if (tryPartials) {

            double partial = FuzzySearch.partialRatio(p1, p2) * partialScale;
            double partialSor = FuzzySearch.tokenSortPartialRatio(p1, p2) * unbaseScale * partialScale;
            double partialSet = FuzzySearch.tokenSetPartialRatio(p1, p2) * unbaseScale * partialScale;

            return (int) round(PrimitiveUtils.max(base, partial, partialSor, partialSet));

        }
		double tokenSort = FuzzySearch.tokenSortRatio(p1, p2) * unbaseScale;
		double tokenSet = FuzzySearch.tokenSetRatio(p1, p2) * unbaseScale;

		return (int) round(PrimitiveUtils.max(base, tokenSort, tokenSet));

    }

}
