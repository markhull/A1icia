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
package com.hulles.alixia.juliet.fuzzywuzzy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.hulles.alixia.juliet.fuzzywuzzy.algorithms.Utils;
import com.hulles.alixia.juliet.fuzzywuzzy.model.ExtractedResult;

public class Extractor {

    private int cutoff;

    public Extractor() {
        this.cutoff = 0;
    }

    public Extractor(int cutoff) {
        this.cutoff = cutoff;
    }

    public Extractor with(int cutoff1) {
        this.setCutoff(cutoff1);
        return this;
    }

    /**
     * Returns the list of choices with their associated scores of similarity in a list
     * of {@link ExtractedResult}
     *
     * @param query The query string
     * @param choices The list of choices
     * @param func The function to apply
     * @return The list of results
     */
    public List<ExtractedResult> extractWithoutOrder(String query, Collection<String> choices, Applicable func) {

        List<ExtractedResult> yields = new ArrayList<>();

        for (String s : choices) {

            int score = func.apply(query, s);

            if (score >= cutoff) {
                yields.add(new ExtractedResult(s, score));
            }

        }

        return yields;

    }

    /**
     * Find the single best match above a score in a list of choices.
     *
     * @param query  A string to match against
     * @param choice A list of choices
     * @param func   Scoring function
     * @return An object containing the best match and it's score
     */
    public ExtractedResult extractOne(String query, Collection<String> choice, Applicable func) {

        List<ExtractedResult> extracted = extractWithoutOrder(query, choice, func);

        return Collections.max(extracted);

    }

    /**
     * Creates a <b>sorted</b> list of {@link ExtractedResult}  which contain the
     * top @param limit most similar choices
     *
     * @param query   The query string
     * @param choices A list of choices
     * @param func    The scoring function
     * @return A list of the results
     */
    public List<ExtractedResult> extractTop(String query, Collection<String> choices, Applicable func) {

        List<ExtractedResult> best = extractWithoutOrder(query, choices, func);
        Collections.sort(best, Collections.<ExtractedResult>reverseOrder());

        return best;
    }

    /**
     * Creates a <b>sorted</b> list of {@link ExtractedResult} which contain the
     * top @param limit most similar choices
     *
     * @param query   The query string
     * @param choices A list of choices
     * @param limit   Limits the number of results and speeds up
     *                the search (k-top heap sort) is used
     * @return A list of the results
     */
    public List<ExtractedResult> extractTop(String query, Collection<String> choices, Applicable func, int limit) {

        List<ExtractedResult> best = extractWithoutOrder(query, choices, func);

        List<ExtractedResult> results = Utils.findTopKHeap(best, limit);
        Collections.reverse(results);

        return results;
    }

    public int getCutoff() {
        return cutoff;
    }

    public void setCutoff(int cutoff) {
        this.cutoff = cutoff;
    }
}
