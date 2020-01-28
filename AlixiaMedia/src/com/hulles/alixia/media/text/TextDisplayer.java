/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hulles.alixia.media.text;

/**
 *
 * @author hulles
 */
public interface TextDisplayer {
	
	/**
	 * This allows us to send a notification of the text window closing, so
	 * the implementer can act accordingly.
	 * 
	 */
	void textWindowIsClosing();
}
