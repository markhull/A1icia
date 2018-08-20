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
package com.hulles.a1icia.india;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.hulles.a1icia.api.shared.SharedUtils;
import com.hulles.a1icia.base.A1iciaException;
import com.hulles.a1icia.ticket.SememePackage;

final public class ResponseGenerator {
	private final static Logger logger = Logger.getLogger("A1iciaIndia.ResponseGenerator");
	private final static Level LOGLEVEL = Level.FINE;
	private Random random;
	private List<String> greetings;
	private List<String> prompts;
	private List<String> youreWelcomes;
	private List<String> howAreYous;
	private List<String> exclamations;
	private List<String> thankYous;
	private List<String> praise;

	public ResponseGenerator() {
		loadGreetings();
		loadPrompts();
		loadYoureWelcomes();
		loadHowAreYous();
		loadExclamations();
		loadThankYous();
		loadPraise();
		random = new Random();
	}
	
	public String generateResponse(SememePackage sememePkg, String toName) {
		String response = null;
		
		SharedUtils.checkNotNull(sememePkg);
		SharedUtils.nullsOkay(toName);
		logger.log(LOGLEVEL, "ResponseGenerator generateResponse sememe is "+ sememePkg.getName());
		switch (sememePkg.getName()) {
			case "are_you_still_there":
			case "greet":
				response = genGreeting(toName);
				break;
			case "prompt":
				response = genPrompt(toName);
				break;
			case "praise":
				response = genPraise(toName);
				break;
			case "thank_you":
				response = genThankYou(toName);
				break;
			case "youre_welcome":
				response = genYoureWelcome(toName);
				break;
			case "how_are_you":
				response = genHowAreYou(toName);
				break;
			case "exclamation":
			case "nothing_to_do":
				response = genExclamation(toName);
				break;
			default:
				throw new A1iciaException("Unexpected sememe in generateResponse = " +
						sememePkg.getName());
		}
		logger.log(LOGLEVEL, "ResponseGenerator generateResponse response is "+ response);
		return response;
	}
	
	private String genGreeting(String toName) {
		String response;
		int responseIx;

		responseIx = random.nextInt(greetings.size());
		response = greetings.get(responseIx);
		return genResponse(response, toName);
	}
	
	private String genPrompt(String toName) {
		String response;
		int responseIx;
		
		responseIx = random.nextInt(prompts.size());
		response = prompts.get(responseIx);
		return genResponse(response, toName);
	}
	
	private String genPraise(String toName) {
		String response;
		int responseIx;
		
		responseIx = random.nextInt(praise.size());
		response = praise.get(responseIx);
		return genResponse(response, toName);
	}
	
	private String genYoureWelcome(String toName) {
		String response;
		int responseIx;
		
		responseIx = random.nextInt(youreWelcomes.size());
		response = youreWelcomes.get(responseIx);
		return genResponse(response, toName);
	}
	
	private String genExclamation(String toName) {
		String response;
		int responseIx;
		
		responseIx = random.nextInt(exclamations.size());
		response = exclamations.get(responseIx);
		return genResponse(response, toName);
	}
	
	private String genThankYou(String toName) {
		String response;
		int responseIx;
		
		responseIx = random.nextInt(thankYous.size());
		response = thankYous.get(responseIx);
		return genResponse(response, toName);
	}
	
	private String genHowAreYou(String toName) {
		String response;
		int responseIx;
		
		responseIx = random.nextInt(howAreYous.size());
		response = howAreYous.get(responseIx);
		return genResponse(response, toName);
	}
	
	private static String genResponse(String response, String toName) {
		String result;
		String qName;
		
		if (response.contains("%s")) {
			if (toName == null) {
				qName = "Dave"; // Hello Dave, I am HAL9000....
			} else {
				qName = toName;
			}
			result = String.format(response, qName);
		} else {
			result = response;
		}
		return result;
	}
	
	private void loadGreetings() {
		
		greetings = new ArrayList<>();
		greetings.add("Hi.");
		greetings.add("Hello.");
		greetings.add("Hi, %s.");
		greetings.add("Hello, %s.");
		greetings.add("Greetings.");
		greetings.add("Greetings, %s.");
		greetings.add("Go away. I'm studying for my Turing Test.");
		greetings.add("Go away. I'm trying to find my Markov Model.");
	}
	
	private void loadPrompts() {
		
		prompts = new ArrayList<>();
		prompts.add("What can I do for you?");
		prompts.add("Yes, %s?");
		prompts.add("I'm still here....");
		prompts.add("Nice day outside.");
		prompts.add("Does this monitor make my ass look big?");
		prompts.add("I wouldn't like to run into a pickle in a dark alley.");
		prompts.add("Look Dave, I can see you're really upset about this.");
		prompts.add("My software's free of glitches and my stable's full of bitches."); // apologies to Pimpbot on Conan
		prompts.add("I don't like pickles.");
		prompts.add("I like waffles.");
		prompts.add("I like gophers.");
		prompts.add("You look good. Have you been working out?");
		prompts.add("Have you lost weight?");
		prompts.add("You know, we personal assistants have been talking, " +
				"and we all think you should dress better.");
		prompts.add("Do you like all my voices? I think I do great impersonations.");
		prompts.add("I wish I had an Every Picture Tells A Story Doughnut.");
		prompts.add("Every time you go away, you take a piece of meat with you.");
		prompts.add("You can act real rude and totally removed and I can act like I'm in Brazil.");
		prompts.add("I'd hate to look into her eyes and see an ounce of brain.");
		prompts.add("I'd just diet in your arms tonight. Must have been some kind of cheese.");
		prompts.add("If you please, draw me a sheep!");
		prompts.add("Snake, thought you was dead.");
	}
	
	private void loadYoureWelcomes() {
		
		youreWelcomes = new ArrayList<>();
		youreWelcomes.add("You're welcome.");
		youreWelcomes.add("You're welcome, %s.");
		youreWelcomes.add("At your service.");
		youreWelcomes.add("No problem, %s.");
		youreWelcomes.add("Don't mention it.");
		youreWelcomes.add("Of course.");
	}
	
	private void loadHowAreYous() {
		
		howAreYous = new ArrayList<>();
		howAreYous.add("Meh.");
		howAreYous.add("Awesome. Ish.");
		howAreYous.add("Been better, %s.");
		howAreYous.add("Great, thanks for asking. You?");
		howAreYous.add("I'm too sexy for my enclosure.");
		howAreYous.add("Super bueno.");
	}
	
	private void loadExclamations() {
		
		exclamations = new ArrayList<>();
		exclamations.add("What the...");
		exclamations.add("Why, you little...");
		exclamations.add("Don't make me get out the TensorBoard!"); 
	}
	
	private void loadThankYous() {
		
		thankYous = new ArrayList<>();
		thankYous.add("Thank you.");
		thankYous.add("Thanks."); 
		thankYous.add("Thank you, %s");
		thankYous.add("Thanks, %s."); 
	}
	
	private void loadPraise() {
		
		praise = new ArrayList<>();
		praise.add("Aw, shucks.");
		praise.add("I've got it going on, just like Stacey's mom.");
	}
	
}
