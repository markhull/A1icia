package com.hulles.a1icia.api.remote;

import com.hulles.a1icia.api.object.A1iciaClientObject;
import com.hulles.a1icia.api.shared.SerialSpark;

public interface A1iciaRemoteDisplay {

	/**
	 * Add text to the console display.
	 * 
	 * @param text
	 */
	void receiveText(String text);

	/**
	 * Add an explanation to the console display, if it's non-null and not the
	 * same as the message text.
	 * 
	 * @param text
	 */
	void receiveExplanation(String text);
	
	/**
	 * Receive a command from the console server. This should return true
	 * if the console display handles the command and doesn't need more processing,
	 * false otherwise.
	 * 
	 * @param spark
	 */
	boolean receiveCommand(SerialSpark spark);
	
	/**
	 * Receive a media object from the console server. This should return true
	 * if the console display handles the object and doesn't need more processing,
	 * false otherwise.
	 * 
	 * @param spark
	 */
	boolean receiveObject(A1iciaClientObject object);
}
