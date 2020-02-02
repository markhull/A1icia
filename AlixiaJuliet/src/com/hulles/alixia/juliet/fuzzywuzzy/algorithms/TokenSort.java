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


import java.util.Arrays;
import java.util.List;

import com.hulles.alixia.juliet.fuzzywuzzy.Ratio;
import com.hulles.alixia.juliet.fuzzywuzzy.StringProcessor;

public class TokenSort extends RatioAlgorithm {

    @Override
    public int apply(String s1, String s2, Ratio ratio, StringProcessor stringProcessor) {

        String sorted1 = processAndSort(s1, stringProcessor);
        String sorted2 = processAndSort(s2, stringProcessor);

        return ratio.apply(sorted1, sorted2);

    }

    private static String processAndSort(String in, StringProcessor stringProcessor) {
        String out;
        
        out = stringProcessor.process(in);
        String[] wordsArray = out.split("\\s+");

        List<String> words = Arrays.asList(wordsArray);
        String joined = Utils.sortAndJoin(words, " ");

        return joined.trim();

    }

}
