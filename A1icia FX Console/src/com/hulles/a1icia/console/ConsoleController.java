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
package com.hulles.a1icia.console;

import com.hulles.alicia.api.console.AliciaConsole;
import com.hulles.alicia.api.console.AliciaConsoleDisplay;
import com.hulles.alicia.api.shared.SerialSpark;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public class ConsoleController implements AliciaConsoleDisplay {
	ObservableList<String> consoleData;
	private AliciaConsole console;
	
    @FXML
    private ListView<String> consoleDisplay;

    @FXML
    private TextField consoleCommandText;

    @FXML
    private void initialize() {

    	consoleData = FXCollections.observableArrayList();
    	consoleDisplay.setItems(consoleData);
    	consoleCommandText.setOnKeyPressed(new EventHandler<KeyEvent>() {
    	    @Override
    	    public void handle(KeyEvent keyEvent) {
    	        if (keyEvent.getCode() == KeyCode.ENTER)  {
    	        	sendAction();
    	        }
    	    }
    	});
    }   

    @FXML 
    void sendAction() {
    	String text;
    	
    	text = consoleCommandText.getText();
    	console.sendText(text);
    	addLine("ME: " + text);
    	consoleCommandText.setText("");
    }

    
    @FXML
    private void clearAction() {

    	consoleCommandText.setText("");
    }
	
	private void addLine(String line) {

		consoleData.add(line);
		while (consoleData.size() > 100) {
			consoleData.remove(0);
		}
	}
	
	@Override
	public void receiveText(String text) {

		Platform.runLater(new Runnable() {
		    @Override
		    public void run() {
				consoleData.add(text);
				while (consoleData.size() > 100) {
					consoleData.remove(0);
				}
		    }
		});
	}

	void setConsole(AliciaConsole console) {
    	
    	this.console = console;
//    	consoleData = FXCollections.observableArrayList();
//    	consoleDisplay.setItems(consoleData);
    }

	@Override
	public void receiveCommand(SerialSpark command) {

		// we don't yet implement FX console commands...
	}

}
