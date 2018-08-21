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


import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.hulles.a1icia.juliet.fuzzywuzzy.StringProcessor;

public class DefaultStringProcessor implements StringProcessor {

    private final static String pattern = "[^\\p{Alnum}]";
    private final static Pattern r = compilePattern();


    /**
     * Substitute non alphanumeric characters.
     *
     * @param in The input string
     * @param sub The string to substitute with
     * @return The replaced string
     */
    public static String subNonAlphaNumeric(String in, String sub) {

        Matcher m = r.matcher(in);

        if(m.find()){
            return m.replaceAll(sub);
        }
		return in;

    }

    /**
     * Performs the default string processing on the input string
     *
     * @param in Input string
     * @return The processed string
     */
    @Override
    public String process(String in) {

        in = subNonAlphaNumeric(in, " ");
        in = in.toLowerCase();
        in = in.trim();

        return in;

    }

    private static Pattern compilePattern(){

        Pattern p;

        try{
            p = Pattern.compile(pattern, Pattern.UNICODE_CHARACTER_CLASS);
        } catch (IllegalArgumentException e) {
            // Even though Android supports the unicode pattern class
            // for some reason it throws an IllegalArgumentException
            // if we pass the flag like on standard Java runtime
            //
            // We catch this and recompile without the flag (unicode should still work)
            p = Pattern.compile(pattern);
        }

        return p;

    }

}
