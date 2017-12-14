package com.hulles.a1icia.charlie.pos;

import com.hulles.a1icia.api.A1iciaConstants;
import com.hulles.a1icia.api.shared.ApplicationKeys;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.hulles.a1icia.base.A1iciaException;
import com.hulles.a1icia.tools.A1iciaUtils;
import java.net.MalformedURLException;

import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;

final public class CharliePOS {
	private final static Logger LOGGER = Logger.getLogger("A1iciaCharlie.CharliePOS");
	private final static Level LOGLEVEL = A1iciaConstants.getA1iciaLogLevel();
	private final POSTaggerME posTagger;

	public CharliePOS() {
		URL posModelURL;
		POSModel posModel;
		ApplicationKeys appKeys;
        
        appKeys = ApplicationKeys.getInstance();
        String openNLPPath = appKeys.getOpenNLPPath();
        try {
            posModelURL = new URL(ApplicationKeys.toURL(openNLPPath + "/en-pos-maxent.bin"));
        } catch (MalformedURLException ex) {
            throw new A1iciaException("Can't create POS URL", ex);
        }
		
		try {
			posModel = new POSModel(posModelURL);
		}
		catch (IOException e) {
			throw new A1iciaException("Can't load POS model(s)", e);
		}
		posTagger = new POSTaggerME(posModel);
	}
	
	public String[] generatePOS(String[] tokenizedInput) {
		String[] posStrings;
		
		A1iciaUtils.checkNotNull(tokenizedInput);
		posStrings = posTagger.tag(tokenizedInput);
		for (String s : posStrings) {
			LOGGER.log(LOGLEVEL, "POS: {0}", s);
		}
		return posStrings;
	}
}
