/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hulles.alixia.api.tools;

import java.util.ResourceBundle;

/**
 * Alixia version utility methods
 * 
 * @author hulles
 */
public class AlixiaVersion {
	
    /**
     * Return a formatted version string
     * @param bundle The name of the resource bundle
     * @return The formatted string
     */
	public static String getVersionString(ResourceBundle bundle) {
		StringBuilder sb;
		String value;
		
		sb = new StringBuilder();
		value = bundle.getString("Name");
		sb.append(value);
		sb.append(" \"");
		value = bundle.getString("Build-Title");
		sb.append(value);
		sb.append("\", Version ");
		value = bundle.getString("Build-Version");
		sb.append(value);
		sb.append(", Build ");
		value = bundle.getString("Build-Number");
		sb.append(value);
		sb.append(" ");
		value = bundle.getString("Build-Date");
		sb.append(value);
		return sb.toString();
	}    
}
