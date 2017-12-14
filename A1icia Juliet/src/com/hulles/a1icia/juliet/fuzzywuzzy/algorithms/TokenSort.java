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


import java.util.Arrays;
import java.util.List;

import com.hulles.a1icia.juliet.fuzzywuzzy.Ratio;
import com.hulles.a1icia.juliet.fuzzywuzzy.StringProcessor;

public class TokenSort extends RatioAlgorithm {

    @Override
    public int apply(String s1, String s2, Ratio ratio, StringProcessor stringProcessor) {

        String sorted1 = processAndSort(s1, stringProcessor);
        String sorted2 = processAndSort(s2, stringProcessor);

        return ratio.apply(sorted1, sorted2);

    }

    private static String processAndSort(String in, StringProcessor stringProcessor) {

        in = stringProcessor.process(in);
        String[] wordsArray = in.split("\\s+");

        List<String> words = Arrays.asList(wordsArray);
        String joined = Utils.sortAndJoin(words, " ");

        return joined.trim();

    }

}
