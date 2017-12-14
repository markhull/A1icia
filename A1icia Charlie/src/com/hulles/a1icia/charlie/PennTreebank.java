package com.hulles.a1icia.charlie;

import java.util.Set;

import com.hulles.a1icia.jebus.JebusBible;
import com.hulles.a1icia.jebus.JebusHub;
import com.hulles.a1icia.jebus.JebusPool;
import com.hulles.a1icia.tools.A1iciaUtils;

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
		String result = null;
		String hashKey;
		
		A1iciaUtils.checkNotNull(tag);
		if (jebusPool == null) {
			jebusPool = JebusHub.getJebusLocal();
		}
    	try (Jedis jebus = jebusPool.getResource()){
    		hashKey = JebusBible.getPennTreebankHashKey(jebusPool, tag);
    		result = jebus.hget(hashKey, JebusBible.getPTTagDescField(jebusPool));
		}
		return result;
	}
	
	public static String getCUPSymbol(String tag) {
		String result = null;
		String hashKey;
		
		A1iciaUtils.checkNotNull(tag);
		if (jebusPool == null) {
			jebusPool = JebusHub.getJebusLocal();
		}
    	try (Jedis jebus = jebusPool.getResource()){
    		hashKey = JebusBible.getPennTreebankHashKey(jebusPool, tag);
    		result = jebus.hget(hashKey, JebusBible.getPTCupSymbolField(jebusPool));
		}
		return result;
	}
	
	public static boolean isValidTag(String tag) {
		boolean result = false;
		String hashKey;
		
		A1iciaUtils.checkNotNull(tag);
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
    	
    	setKey = JebusBible.getPennTreebankSetKey(jebusPool);
    	descFld = JebusBible.getPTTagDescField(jebusPool);
    	cupFld = JebusBible.getPTCupSymbolField(jebusPool);
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
