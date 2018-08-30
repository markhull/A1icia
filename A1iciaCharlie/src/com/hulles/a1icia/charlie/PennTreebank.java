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
package com.hulles.a1icia.charlie;

import com.hulles.a1icia.api.jebus.JebusBible;
import com.hulles.a1icia.api.jebus.JebusBible.JebusKey;
import com.hulles.a1icia.api.jebus.JebusHub;
import com.hulles.a1icia.api.jebus.JebusPool;
import java.util.Set;

import com.hulles.a1icia.api.shared.SharedUtils;

import redis.clients.jedis.Jedis;

/**
 * The Penn Treebank, as implemented by Apache OpenNLP
 *     (The map also contains arbitrary symbols for the CUP parser)
 *     
 * @author hulles
 *
 */
public final class PennTreebank {
	private static JebusPool jebusPool = null;
	
	public static String getTagDefinition(String tag) {
		String result;
		String hashKey;
		
		SharedUtils.checkNotNull(tag);
		if (jebusPool == null) {
			jebusPool = JebusHub.getJebusCentral();
		}
    	try (Jedis jebus = jebusPool.getResource()){
    		hashKey = JebusBible.getPennTreebankHashKey(jebusPool, tag);
    		result = jebus.hget(hashKey, JebusBible.getStringKey(JebusKey.PTTAGDESCRIPTION, jebusPool));
		}
		return result;
	}
	
	public static String getCUPSymbol(String tag) {
		String result;
		String hashKey;
		
		SharedUtils.checkNotNull(tag);
		if (jebusPool == null) {
			jebusPool = JebusHub.getJebusLocal();
		}
    	try (Jedis jebus = jebusPool.getResource()){
    		hashKey = JebusBible.getPennTreebankHashKey(jebusPool, tag);
     		result = jebus.hget(hashKey, JebusBible.getStringKey(JebusKey.PTCUPSYMBOL, jebusPool));
		}
		return result;
	}
	
	public static boolean isValidTag(String tag) {
		boolean result = false;
		String hashKey;
		
		SharedUtils.checkNotNull(tag);
		if (jebusPool == null) {
			jebusPool = JebusHub.getJebusLocal();
		}
    	try (Jedis jebus = jebusPool.getResource()){
    		hashKey = JebusBible.getPennTreebankHashKey(jebusPool, tag);
    		result = jebus.exists(hashKey);
		}
		return result;
	}

	public static void dumpPennTreebank() {
    	String setKey;
    	Set<String> tags;
    	String hashKey;
    	String descFld;
    	String cupFld;
    	
    	setKey = JebusBible.getStringKey(JebusKey.PENNTREEBANKSETKEY, jebusPool);
    	descFld = JebusBible.getStringKey(JebusKey.PTTAGDESCRIPTION, jebusPool);
    	cupFld = JebusBible.getStringKey(JebusKey.PTCUPSYMBOL, jebusPool);
		if (jebusPool == null) {
			jebusPool = JebusHub.getJebusLocal();
		}
    	try (Jedis jebus = jebusPool.getResource()){
    		tags = jebus.zrange(setKey, 0, -1);
    		for (String tag : tags) {
    			System.out.print(tag);
        		hashKey = JebusBible.getPennTreebankHashKey(jebusPool, tag);
        		System.out.print(": ");
        		System.out.print(jebus.hget(hashKey, descFld));
        		System.out.print(": ");
        		System.out.println(jebus.hget(hashKey, cupFld));
    		}
    	    System.out.println("There are " + jebus.zcard(setKey) + " tags.");
		}
	}
}
