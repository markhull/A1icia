/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hulles.alixia.house;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hulles.alixia.api.dialog.DialogRequest;
import com.hulles.alixia.api.dialog.DialogResponse;
import com.hulles.alixia.api.shared.AlixiaException;
import com.hulles.alixia.api.shared.SharedUtils;
import com.hulles.alixia.crypto.PurdahKeys;
import com.hulles.alixia.media.Language;
import com.hulles.alixia.tools.ExternalAperture;

/**
 * Translator tranlates DialogRequests and DialogResponses from one language to another.
 * 
 * @author hulles
 */
public class Translator {
	final static Logger LOGGER = LoggerFactory.getLogger(Translator.class);
	
    private Translator() {
        // only static methods
    }
    
	/**
	 * Translate a DialogRequest from another language into American English. We currently provide
	 * a console warning because this is an expensive operation, relatively speaking.
	 * 
	 * NOTE: getting rid of AlixiaGoogleTranslate for the time being, jar issues
	 * 
	 * @param request The request to translate
	 * @param lang The language from which to translate
	 */
	public static void translateRequest(DialogRequest request, Language lang) {
		String translation;
		
		SharedUtils.checkNotNull(request);
		SharedUtils.checkNotNull(lang);
		if ((lang != Language.AMERICAN_ENGLISH) && (lang != Language.BRITISH_ENGLISH)) {
			LOGGER.warn("StationServer: translating request from {} to American English", 
                    lang.getDisplayName());
            translation = translate(lang, Language.AMERICAN_ENGLISH, request.getRequestMessage());
			request.setRequestMessage(translation);
		}
	}

	/**
	 * Translate a DialogResponse into another language from its original language as denoted in the
	 * DialogResponse.
	 * 
	 * @param response The response to translate
	 * @param lang The language into which to translate
	 */
	public static void translateResponse(DialogResponse response, Language lang) {
		String messageTranslation;
		String explanationTranslation;
		String expl;
		Language langIn;
		
		SharedUtils.checkNotNull(lang);
		langIn = response.getLanguage();
		if (langIn == null) {
			throw new AlixiaException("StationServer: translateResponse: null language in response");
		}
		response.setLanguage(lang);
		if (langIn != lang) {
			LOGGER.warn("StationServer: translating response from {} to {}", langIn.getDisplayName(), lang.getDisplayName());
			// also translates American to British and vice versa... TODO change it maybe
            // TODO change this to make only one translate call with 2 texts if explanation exists;
            //    supposedly you can repeat the q parameter in the POST to translate multiple texts
            messageTranslation = translate(langIn, lang, response.getMessage());
			response.setMessage(messageTranslation);
			expl = response.getExplanation();
			if (expl != null && !expl.isEmpty()) {
				explanationTranslation = translate(langIn, lang, expl);
				response.setExplanation(explanationTranslation);
			}
		}
	}
	
    private static String translate(Language from, Language to, String textToTranslate) {
        String result;
        PurdahKeys purdah;
        String key;
        JsonObject resultData;
        JsonObject data;
        JsonArray translations;
        JsonObject translationObject;
        String translation;
        
        SharedUtils.checkNotNull(from);
        SharedUtils.checkNotNull(to);
        SharedUtils.checkNotNull(textToTranslate);
        purdah = PurdahKeys.getInstance();
        key = purdah.getPurdahKey(PurdahKeys.PurdahKey.GOOGLEXLATEKEY);
        result = ExternalAperture.getGoogleTranslation(from, to, textToTranslate, "text", key);
        LOGGER.debug("Translate result: {}", result);        
		try (BufferedReader reader = new BufferedReader(new StringReader(result))) {
			try (JsonReader jsonReader = Json.createReader(reader)) {
                resultData = jsonReader.readObject();
                LOGGER.debug("ResultData: {}", resultData);
                data = resultData.getJsonObject("data");
                LOGGER.debug("Data: {}", data);
                translations = data.getJsonArray("translations");
                LOGGER.debug("Translations: {}", translations);
                if (translations.size() != 1) {
                    LOGGER.error("Invalid translations size = {}");
                    translation = null;
                } else {
                    translationObject = translations.getJsonObject(0);
                    translation = translationObject.getString("translatedText");
                }
            }
		} catch (IOException e) {
            throw new AlixiaException("IO exception in StringReader for some reason", e);
		}
        return translation;
    }
    
}
