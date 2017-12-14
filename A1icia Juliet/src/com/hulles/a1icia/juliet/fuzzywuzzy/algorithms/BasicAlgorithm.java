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

import com.hulles.a1icia.juliet.fuzzywuzzy.Applicable;
import com.hulles.a1icia.juliet.fuzzywuzzy.StringProcessor;

public abstract class BasicAlgorithm implements Applicable {

    private StringProcessor stringProcessor;

    public BasicAlgorithm() {
        this.stringProcessor = new DefaultStringProcessor();
    }

    public BasicAlgorithm(StringProcessor stringProcessor) {
        this.stringProcessor = stringProcessor;
    }

    public abstract int apply(String s1, String s2, StringProcessor stringProcessor1);

    @Override
	public int apply(String s1, String s2){

        return apply(s1, s2, this.stringProcessor);

    }

    public BasicAlgorithm with(StringProcessor stringProcessor1){
        setStringProcessor(stringProcessor1);
        return this;
    }

    public BasicAlgorithm noProcessor(){
        this.stringProcessor = new NoProcess();
        return this;
    }

    void setStringProcessor(StringProcessor stringProcessor){
        this.stringProcessor = stringProcessor;
    }

    public StringProcessor getStringProcessor() {
        return stringProcessor;
    }
}
