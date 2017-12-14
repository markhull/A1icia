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

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;


public class Main extends Application {
	@Override
	public void start(Stage primaryStage) {
		AnchorPane pane;
		Scene scene;
		ConsoleController controller;
		AliciaConsole console;
		FXMLLoader loader;
		
		try {
			loader = new FXMLLoader();
			loader.setLocation(getClass().getResource("Console.fxml"));
			pane = (AnchorPane)loader.load();
			
			scene = new Scene(pane);
			scene.getStylesheets().add(getClass().getResource("Console.css").toExternalForm());
            
			controller = loader.getController();
			console = new AliciaConsole(controller);
            controller.setConsole(console);
            console.startListening();
            
		    primaryStage.setTitle("Alicia Console");
			primaryStage.setScene(scene);
			primaryStage.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
