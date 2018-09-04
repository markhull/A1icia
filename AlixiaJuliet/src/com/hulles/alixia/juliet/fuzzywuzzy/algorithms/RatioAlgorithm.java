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

import com.hulles.alixia.juliet.fuzzywuzzy.Ratio;
import com.hulles.alixia.juliet.fuzzywuzzy.StringProcessor;
import com.hulles.alixia.juliet.fuzzywuzzy.ratios.SimpleRatio;

public abstract class RatioAlgorithm extends BasicAlgorithm {

    private Ratio ratio;

    public RatioAlgorithm() {
        super();
        this.ratio = new SimpleRatio();
    }

    public RatioAlgorithm(StringProcessor stringProcessor) {
        super(stringProcessor);
    }

    public RatioAlgorithm(Ratio ratio) {
        super();
        this.ratio = ratio;
    }


    public RatioAlgorithm(StringProcessor stringProcessor, Ratio ratio) {
        super(stringProcessor);
        this.ratio = ratio;
    }

    public abstract int apply(String s1, String s2, Ratio ratio1, StringProcessor stringProcessor);

    public RatioAlgorithm with(Ratio ratio1) {
        setRatio(ratio1);
        return this;
    }

    public int apply(String s1, String s2, Ratio ratio1) {
        return apply(s1, s2, ratio1, getStringProcessor());
    }

    @Override
    public int apply(String s1, String s2, StringProcessor stringProcessor) {
        return apply(s1, s2, getRatio(), stringProcessor);
    }

    public void setRatio(Ratio ratio) {
        this.ratio = ratio;
    }

    public Ratio getRatio() {
        return ratio;
    }
}
