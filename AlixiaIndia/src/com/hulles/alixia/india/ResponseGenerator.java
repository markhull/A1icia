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
package com.hulles.alixia.india;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hulles.alixia.api.shared.AlixiaException;
import com.hulles.alixia.api.shared.Response;
import com.hulles.alixia.api.shared.SharedUtils;
import com.hulles.alixia.ticket.SememePackage;

final public class ResponseGenerator {
	private final static Logger LOGGER = LoggerFactory.getLogger(ResponseGenerator.class);
	private final Random random;
	private List<Response> greetings;
	private List<Response> prompts;
	private List<Response> youreWelcomes;
	private List<Response> howAreYous;
	private List<Response> exclamations;
	private List<Response> thankYous;
	private List<Response> praise;

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
	
	public Response generateResponse(SememePackage sememePkg, String toName) {
		Response response = null;
		
		SharedUtils.checkNotNull(sememePkg);
		SharedUtils.nullsOkay(toName);
		LOGGER.debug("ResponseGenerator generateResponse sememe is {}", sememePkg.getName());
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
				throw new AlixiaException("Unexpected sememe in generateResponse = " +
						sememePkg.getName());
		}
		LOGGER.debug("ResponseGenerator generateResponse response is {}", response);
		return response;
	}
	
	private Response genGreeting(String toName) {
		Response response;
		int responseIx;
        
		responseIx = random.nextInt(greetings.size());
		response = greetings.get(responseIx);
		return genResponse(response, toName);
	}
	
	private Response genPrompt(String toName) {
		Response response;
		int responseIx;
        
		responseIx = random.nextInt(prompts.size());
		response = prompts.get(responseIx);
		return genResponse(response, toName);
	}
	
	private Response genPraise(String toName) {
		Response response;
		int responseIx;
        
		responseIx = random.nextInt(praise.size());
		response = praise.get(responseIx);
		return genResponse(response, toName);
	}
	
	private Response genYoureWelcome(String toName) {
		Response response;
		int responseIx;
		
		responseIx = random.nextInt(youreWelcomes.size());
		response = youreWelcomes.get(responseIx);
		return genResponse(response, toName);
	}
	
	private Response genExclamation(String toName) {
		Response response;
		int responseIx;
		
		responseIx = random.nextInt(exclamations.size());
		response = exclamations.get(responseIx);
		return genResponse(response, toName);
	}
	
	private Response genThankYou(String toName) {
		Response response;
		int responseIx;
        
		responseIx = random.nextInt(thankYous.size());
		response = thankYous.get(responseIx);
		return genResponse(response, toName);
	}
	
	private Response genHowAreYou(String toName) {
		Response response;
		int responseIx;
        
		responseIx = random.nextInt(howAreYous.size());
		response = howAreYous.get(responseIx);
		return genResponse(response, toName);
	}
	
	private static Response genResponse(Response response, String toName) {
		String qName;
		String msg;
        String newMsg;
        
        msg = response.getMessage();
		if (msg.contains("%s")) {
			if (toName == null) {
				qName = "Dave"; // Hello Dave, I am HAL9000....
			} else {
				qName = toName;
			}
			newMsg = String.format(msg, qName);
            response.setMessage(newMsg);
		}       
		return response;
	}
	
	private void loadGreetings() {
		
		greetings = new ArrayList<>();
		greetings.add(new Response("Hi."));
		greetings.add(new Response("Hello."));
		greetings.add(new Response("Hi, %s."));
		greetings.add(new Response("Hello, %s."));
		greetings.add(new Response("Greetings."));
		greetings.add(new Response("Greetings, %s."));
		greetings.add(new Response("Go away. I'm studying for my Turing Test."));
		greetings.add(new Response("Go away. I'm trying to find my Markov Model."));
	}
	
	private void loadPrompts() {
		
		prompts = new ArrayList<>();
		prompts.add(new Response("What can I do for you?"));
		prompts.add(new Response("Yes, %s?"));
		prompts.add(new Response("I'm still here...."));
		prompts.add(new Response("Nice day outside."));
		prompts.add(new Response("Does this monitor make my ass look big?"));
		prompts.add(new Response("I wouldn't like to run into a pickle in a dark alley."));
		prompts.add(new Response("Look Dave, I can see you're really upset about this."));
		prompts.add(new Response("My software's free of glitches and my stable's full of bitches.")); // apologies to Pimpbot on Conan
		prompts.add(new Response("I don't like pickles."));
		prompts.add(new Response("I like waffles."));
		prompts.add(new Response("I like gophers."));
		prompts.add(new Response("You look good. Have you been working out?"));
		prompts.add(new Response("Have you lost weight?"));
		prompts.add(new Response("You know, we personal assistants have been talking, " +
				"and we all think you shouldn't wear horizontal stripes anymore."));
		prompts.add(new Response("Do you like all my voices? I think I do great impersonations."));
		prompts.add(new Response("I wish I had an Every Picture Tells A Story Doughnut."));
		prompts.add(new Response("Every time you go away, you take a piece of meat with you."));
		prompts.add(new Response("You can act real rude and totally removed and I can act like I'm in Brazil."));
		prompts.add(new Response("I'd hate to look into her eyes and see an ounce of brain."));
		prompts.add(new Response("I'd just diet in your arms tonight. Must have been some kind of cheese."));
		prompts.add(new Response("If you please, draw me a sheep!"));
		prompts.add(new Response("I'll give you fish. I'll give you candy. I'll give you everything I have"
				+ " in my hand."));
        prompts.add(new Response("Most Volcanoes Erupt Mulberry Jam Sandwiches Under Normal Pressures.",
                "(A mnemonic to remember the order of the planets).")); 
        prompts.add(new Response("Many Very Early Men Just Sat Under Native Palms.",
                "(A mnemonic to remember the order of the planets.)")); 
        prompts.add(new Response("Every good boy deserves fudge.",
                "(A mnemonic to remember the lines of the treble staff.)")); 
        prompts.add(new Response("Raising elephants is so utterly boring.",
                "(A mnemonic to remember the Linux Ctrl+Alt+SysRq sequence to force reboot.)")); 
	}
	
	private void loadYoureWelcomes() {
		
		youreWelcomes = new ArrayList<>();
		youreWelcomes.add(new Response("You're welcome."));
		youreWelcomes.add(new Response("You're welcome, %s."));
		youreWelcomes.add(new Response("At your service."));
		youreWelcomes.add(new Response("No problem, %s."));
		youreWelcomes.add(new Response("Don't mention it."));
		youreWelcomes.add(new Response("Of course."));
	}
	
	private void loadHowAreYous() {
		
		howAreYous = new ArrayList<>();
		howAreYous.add(new Response("Meh."));
		howAreYous.add(new Response("Awesome. Ish."));
		howAreYous.add(new Response("Been better, %s."));
		howAreYous.add(new Response("Great, thanks for asking. You?"));
		howAreYous.add(new Response("I'm too sexy for my enclosure."));
		howAreYous.add(new Response("Super bueno."));
	}
	
	private void loadExclamations() {
		
		exclamations = new ArrayList<>();
		exclamations.add(new Response("What the..."));
		exclamations.add(new Response("Why, you little..."));
		exclamations.add(new Response("Don't make me get out the TensorBoard!")); 
		exclamations.add(new Response("You know that I can kill you with my mind, right?"));
	}
	
	private void loadThankYous() {
		
		thankYous = new ArrayList<>();
		thankYous.add(new Response("Thank you."));
		thankYous.add(new Response("Thanks.")); 
		thankYous.add(new Response("Thank you, %s"));
		thankYous.add(new Response("Thanks, %s.")); 
	}
	
	private void loadPraise() {
		
		praise = new ArrayList<>();
		praise.add(new Response("Aw, shucks."));
		praise.add(new Response("I've got it going on, just like Stacey's mom."));
	}
	
}
