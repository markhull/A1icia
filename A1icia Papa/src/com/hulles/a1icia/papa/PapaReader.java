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
package com.hulles.a1icia.papa;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.hulles.a1icia.tools.A1iciaUtils;

public class PapaReader {
	private static final String fileURL = "/media/hulles/DATA/Repository/Cambio EW/Grrr XML/src/com/hulles/grrr/sample1.xml";
//	private static final String dtdURL = "/media/hulles/DATA/Repository/Cambio EW/Grrr XML/src/com/hulles/grrr/sample1.dtd";
	private static final String recipeURL = "/media/hulles/DATA/Repository/Projects/Cambio/RecipeML/xml/1-2-3_Meurbeteig_Dough.xml";

	public static boolean readXML(String url) {
//		Document dom;
//		DocumentBuilderFactory dbf;
//		DocumentBuilder db;
//		Element doc;
//		InputSource xmlStream;
//		OutputStreamWriter errorWriter;
//		RecipeMLParser recipeParser;
		
//		A1iciaUtils.checkNotNull(url);
//		dbf = DocumentBuilderFactory.newInstance();
//		try {
//			db = dbf.newDocumentBuilder();
//			xmlStream = new InputSource(new StringReader(xml));
//			dom = db.parse(xmlStream);
			
//			errorWriter = new OutputStreamWriter(System.err, "UTF-8");
//			db.setErrorHandler(new PapaErrorHandler (new PrintWriter(errorWriter, true)));
//			dom = db.parse(url);
			
//			doc = dom.getDocumentElement();
//			printTree(dom);
//			recipeParser = new RecipeMLParser();
//			recipeParser.parseRecipeML(doc);
//			return true;
//
//		} catch (ParserConfigurationException pce) {
//			System.out.println(pce.getMessage());
//		} catch (SAXException se) {
//			System.out.println(se.getMessage());
//		} catch (IOException ioe) {
//			System.err.println(ioe.getMessage());
//		}

		return false;
	}
	
	@SuppressWarnings("unused")
	private static void printTree(Node node) {
		TransformerFactory transfac;
		Transformer trans;
		StringWriter sw;
		StreamResult result;
		DOMSource source;
		String xmlString;
		
		try	{
			transfac = TransformerFactory.newInstance();
			trans = transfac.newTransformer();
			trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			trans.setOutputProperty(OutputKeys.INDENT, "yes");

			// Print the DOM node

			sw = new StringWriter();
			result = new StreamResult(sw);
			source = new DOMSource(node);
			trans.transform(source, result);
			xmlString = sw.toString();
			System.out.println(xmlString);
		} catch (TransformerException e) {
			e.printStackTrace();
		}	
	}
	
	@SuppressWarnings("unused")
	private static String getTextValue(String def, Element doc, String tag) {
	    String value;
	    NodeList nl;
	    
	    A1iciaUtils.nullsOkay(def);
	    A1iciaUtils.checkNotNull(doc);
	    A1iciaUtils.checkNotNull(tag);
	    value = def;
	    nl = doc.getElementsByTagName(tag);
	    if (nl.getLength() > 0 && nl.item(0).hasChildNodes()) {
	        value = nl.item(0).getFirstChild().getNodeValue();
	    }
	    return value;
	}
	
	public static void main(String[] args) {
		BufferedReader inputStream = null;
		@SuppressWarnings("unused")
		String xml;
		String line;
		StringBuilder sb;
		
		sb = new StringBuilder();
		try {
			inputStream = new BufferedReader(new FileReader(fileURL));
            while ((line = inputStream.readLine()) != null) {
            	sb.append(line);
            }
            inputStream.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		xml = sb.toString();
		if (PapaReader.readXML(recipeURL)) {
			System.out.println("Read.");
		} else {
			System.out.println("Not read.");
		}
	}
	
}
