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
package com.hulles.a1icia.tools;

import java.util.ArrayList;
import java.util.List;

import com.google.cloud.translate.Translate;
import com.google.cloud.translate.Translate.TranslateOption;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;
import com.hulles.a1icia.base.A1iciaException;
import com.hulles.a1icia.media.Language;

/**
 * This class uses Google Cloud Translate to translate from one language to another.
 * BIG FIXME right now we're relying on the API key being set in our .profile. We need to
 * switch to a named file instead, except that I haven't been able to find it documented or used
 * in an example yet. TODO Switch to a non-cloud translation service of some sort.
 * 
 * @author hulles
 *
 */
public class A1iciaGoogleTranslator {
	private static Translate translateService = null;
	private final static String DEFAULT_FORMAT = "text";
	private final static String AUTH_ENV = "GOOGLE_APPLICATION_CREDENTIALS";
	/**
	 * Provide a list of Google Cloud Translate supported languages.
	 * 
	 * @return The list of languages
	 */
	public static List<String> getSupportedLanguages() {
		List<com.google.cloud.translate.Language> langs;
		List<String> names;
		
		if (translateService == null) {
			if (System.getenv(AUTH_ENV) == null) {
				throw new A1iciaException("You need to set your Google auth credentials environment variable");
			}
			translateService = TranslateOptions.getDefaultInstance().getService();
		}
		langs = translateService.listSupportedLanguages();
		names = new ArrayList<>(langs.size());
		for (com.google.cloud.translate.Language lang : langs) {
			names.add(lang.getName());
		}
		return names;
	}
	
	/**
	 * Translate text from one language to another, using the default format (text). Note that 
	 * arguments are instances of com.hulles.a1icia.media.Language, vs. some Google type.
	 * 
	 * @param from The from Language
	 * @param to The to Language
	 * @param text The text to translate
	 * @return The translated text
	 */
	public static String translate(Language from, Language to, String text) {
		Translation translation;
		TranslateOption source;
		TranslateOption target;
		TranslateOption format;
		
		A1iciaUtils.checkNotNull(from);
		A1iciaUtils.checkNotNull(to);
		A1iciaUtils.nullsOkay(text);
		if (text == null || text.isEmpty()) {
			return null;
		}
		if (translateService == null) {
			if (System.getenv(AUTH_ENV) == null) {
				throw new A1iciaException("You need to set your Google auth credentials environment variable");
			}
			translateService = TranslateOptions.getDefaultInstance().getService();
		}
		format = TranslateOption.format(DEFAULT_FORMAT);
		source = TranslateOption.sourceLanguage(from.getGoogleName());
		target = TranslateOption.targetLanguage(to.getGoogleName());
		translation = translateService.translate(text, source, target, format);
		return translation.getTranslatedText();
	}
	
	
	/**
	 * Translate text from one language to another, using the specified format. Note that 
	 * arguments are instances of com.hulles.a1icia.media.Language, vs. some Google type.
	 * 
	 * @param from The from Language
	 * @param to The to Language
	 * @param text The text to translate
	 * @param fmt The output format ("text" or "html")
	 * @return The translated text
	 */
	public static String translate(Language from, Language to, String text, String fmt) {
		Translation translation;
		TranslateOption source;
		TranslateOption target;
		TranslateOption format;
		
		A1iciaUtils.checkNotNull(from);
		A1iciaUtils.checkNotNull(to);
		A1iciaUtils.checkNotNull(fmt);
		A1iciaUtils.nullsOkay(text);
		if (text == null || text.isEmpty()) {
			return null;
		}
		if (!fmt.equals("html") && !fmt.equals("text")) {
			A1iciaUtils.error("A1iciaGoogleTranslator: format s/b either 'html' or 'text', got " + fmt);
			return null;
		}
		if (translateService == null) {
			if (System.getenv(AUTH_ENV) == null) {
				throw new A1iciaException("You need to set your Google auth credentials environment variable");
			}
			translateService = TranslateOptions.getDefaultInstance().getService();
		}
		format = TranslateOption.format(fmt);
		source = TranslateOption.sourceLanguage(from.getGoogleName());
		target = TranslateOption.targetLanguage(to.getGoogleName());
		translation = translateService.translate(text, source, target, format);
		return translation.getTranslatedText();
	}
}
