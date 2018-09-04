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
package com.hulles.alixia.papa;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.hulles.alixia.api.shared.SharedUtils;

public class RecipeMLParser {

//	public void parseRecipeML(Element document) throws SAXParseException {
//		
//		GrrrReader.checkNotNull(document);
//		getTopLevelElements(document);
//	}
//	
//	private static void getTopLevelElements(Element document) throws SAXParseException {
//		NodeList nodeList;
//		Node node;
//		String nodeName;
//		
//		nodeList = document.getChildNodes();
//		dumpNodeList(nodeList);
//		for (int i=0; i<nodeList.getLength(); i++) {
//			node = nodeList.item(i);
//			if (node.getNodeType() != Node.ELEMENT_NODE) {
//				continue;
//			}
//			nodeName = node.getNodeName();
//			if (nodeName.equals("menu")) {
//				createMenu((Element) node);
//			} else if (nodeName.equals("recipe")) {
//				createRecipe((Element) node);
//			} else {
//				throw new SAXParseException(nodeName, null);
//			}
//		}
//	}
//	
//	private static Menu createMenu(Element menuNode) throws SAXParseException {
//		ObjectContext context;
//		Menu dbMenu;
//		NodeList nodeList;
//		Node node;
//		String nodeName;
//		StringBuilder sb;
//		Recipe dbRecipe;
//		MenuRecipe dbMenuRecipe;
//		
//    	context = RecipeMLApplication.getEntityContext();
//        dbMenu = context.newObject(Menu.class);
//        sb = new StringBuilder();
//        nodeList = menuNode.getChildNodes();
//		for (int i=0; i<nodeList.getLength(); i++) {
//			node = nodeList.item(i);
//			if (node.getNodeType() != Node.ELEMENT_NODE) {
//				continue;
//			}
//			nodeName = node.getNodeName();
//			if (nodeName.equals("head")) {
//				setHead((Element) node, dbMenu);
//			} else if (nodeName.equals("description")) {
//				if (sb.length() > 0) {
//					sb.append("\n\n");
//				}
//				sb.append(getTextValue((Element) node));
//			} else if (nodeName.equals("recipe")) {
//				dbRecipe = createRecipe((Element) node);
//				dbMenuRecipe = context.newObject(MenuRecipe.class);
//				dbMenuRecipe.setMenu(dbMenu);
//				dbMenuRecipe.setRecipe(dbRecipe);
//			} else {
//				throw new SAXParseException(nodeName, null);
//			}
//			if (sb.length() > 0) {
//				dbMenu.setDescription(sb.toString());
//			}
//		}
//        context.commitChanges();
//        return dbMenu;
// 	}
//	
//	private static Recipe createRecipe(Element recipeNode) throws SAXParseException {
//		ObjectContext context;
//		Recipe dbRecipe;
//		NodeList nodeList;
//		Node node;
//		String nodeName;
//		StringBuilder sb;
//		NodeList ingDivs;
//		IngredientDiv dbIngDiv;
//		Element ingDiv;
//		NodeList titleList;
//		Element title;
//		NodeList descriptionList;
//		Element description;
//		
//    	context = RecipeMLApplication.getEntityContext();
//        dbRecipe = context.newObject(Recipe.class);
//        sb = new StringBuilder();
//        nodeList = recipeNode.getChildNodes();
//		for (int i=0; i<nodeList.getLength(); i++) {
//			node = nodeList.item(i);
//			if (node.getNodeType() != Node.ELEMENT_NODE) {
//				continue;
//			}
//			nodeName = node.getNodeName();
//			if (nodeName.equals("head")) {
//				setHead((Element) node, dbRecipe);
//			} else if (nodeName.equals("description")) {
//				if (sb.length() > 0) {
//					sb.append("\n\n");
//				}
//				sb.append(getTextValue((Element) node));
//				// ignore amt child elements
//				// ignore time child elements
//				// ignore temp child elements
//				// ignore span child elements
//				// ignore frac child elements
//				// ignore sep child elements
//			} else if (nodeName.equals("equipment")) {
//				// ignore equip-div child elements
//				// ignore tool child elements
//			} else if (nodeName.equals("ingedients")) {
//		    	ingDivs = ((Element)node).getElementsByTagName("ing-div");
//				if (ingDivs.getLength() == 0) {
//					// we create our own default ing-div
//					dbIngDiv = context.newObject(IngredientDiv.class);
//					dbIngDiv.setRecipe(dbRecipe);
//					context.commitChanges();
//					setIngredients((Element)node, dbIngDiv);
//				} else {
//					for (int j=0; j<ingDivs.getLength(); j++) {
//						dbIngDiv = context.newObject(IngredientDiv.class);
//						dbIngDiv.setRecipe(dbRecipe);
//						ingDiv = (Element) ingDivs.item(j);
//						titleList = ingDiv.getElementsByTagName("title");
//						if (titleList.getLength() > 0) {
//							// s/b 0 or 1 title
//							dbIngDiv.setTitle(getTextValue((Element) titleList.item(0)));
//						}
//						descriptionList = ingDiv.getElementsByTagName("description");
//						if (descriptionList.getLength() > 0) {
//							// s/b 0 or 1 description
//							dbIngDiv.setDescription(getTextValue((Element) descriptionList.item(0)));
//						}
//						context.commitChanges();
//						setIngredients(ingDiv, dbIngDiv);
//					}
//				}
//			} else if (nodeName.equals("directions")) {
//			} else if (nodeName.equals("nutrition")) {
//			} else if (nodeName.equals("diet-exchanges")) {
//			} else {
//				throw new SAXParseException(nodeName, null);
//			}
//			if (sb.length() > 0) {
//				dbRecipe.setDescription(sb.toString());
//			}
//		}
//        context.commitChanges();
//		return dbRecipe;
//	}
//	
//	private static void setHead(Element headNode, HasHead dbHasHead) throws SAXParseException {
//		NodeList nodeList;
//		Node node;
//		String nodeName;
//		NodeList catNodeList;
//		Node catNode;
//		String catName;
//		Category dbCategory;
//		ObjectContext context;
//		MenuCategory dbMenuCategory;
//		RecipeCategory dbRecipeCategory;
//		NodeList timeNodeList;
//		
//    	context = RecipeMLApplication.getEntityContext();
//		nodeList = headNode.getChildNodes();
//		for (int i=0; i<nodeList.getLength(); i++) {
//			node = nodeList.item(i);
//			if (node.getNodeType() != Node.ELEMENT_NODE) {
//				continue;
//			}
//			nodeName = node.getNodeName();
//			if (nodeName.equals("title")) {
//				dbHasHead.setTitle(getTextValue((Element) node));
//				// ignore brandname child elements
//				// ignore span child elements
//				// ignore frac child elements
//				// ignore sep chilc elements
//			} else if (nodeName.equals("subtitle")) {
//				dbHasHead.setSubtitle(getTextValue((Element) node));
//				// ignore brandname child elements
//				// ignore span child elements
//				// ignore frac child elements
//				// ignore sep chilc elements
//			} else if (nodeName.equals("version")) {
//				dbHasHead.setVersion(getTextValue((Element) node));
//			} else if (nodeName.equals("source")) {
//				dbHasHead.setSource(getTextValue((Element) node));
//				// ignore srcitem child elements
//			} else if (nodeName.equals("categories")) {
//				// TODO check the values here later, possibly put into Category table...
//				dbHasHead.setCategories(getTextValue((Element) node));
//				catNodeList = node.getChildNodes();
//				for (int j=0; j<catNodeList.getLength(); j++) {
//					catNode = catNodeList.item(i);
//					if (catNode.getNodeType() != Node.ELEMENT_NODE) {
//						continue;
//					}
//					if (!catNode.getNodeName().equals("cat")) {
//						throw new SAXParseException(catNode.getNodeName(), null);
//					}
//					catName = getTextValue((Element) catNode);
//					dbCategory = Category.findCategory(catName);
//					if (dbCategory == null) {
//						dbCategory = Category.addCategory(catName);
//					}
//					if (dbHasHead instanceof Menu) {
//				        dbMenuCategory = context.newObject(MenuCategory.class);
//				        dbMenuCategory.setCategory(dbCategory);
//				        dbMenuCategory.setMenu((Menu) dbHasHead);
//				        context.commitChanges();
//					} else {
//				        dbRecipeCategory = context.newObject(RecipeCategory.class);
//				        dbRecipeCategory.setCategory(dbCategory);
//				        dbRecipeCategory.setRecipe((Recipe) dbHasHead);
//				        context.commitChanges();
//					}
//				}
//			} else if (nodeName.equals("preptime")) {
//				dbHasHead.setPreptime(getTextValue((Element) node));
//				timeNodeList = ((Element) node).getElementsByTagName("time");
//				for (int k=0; k<timeNodeList.getLength(); k++) {
//					if (dbHasHead.getPreptime() == null) {
//						dbHasHead.setPreptime(getTextValue((Element) timeNodeList.item(k)));
//					} else {
//						dbHasHead.setPreptime(dbHasHead.getPreptime() + "\n" +getTextValue((Element) timeNodeList.item(k)));
//					}
//				}
//			} else if (nodeName.equals("yield")) {
//				dbHasHead.setYield(getTextValue((Element) node));
//				// ignore qty child element
//				// ignore range child element
//				// ignore unit child element
//			} else {
//				throw new SAXParseException(nodeName, null);
//			}
//		}
//	}
//	
//	private static void setIngredients(Element ingredientNode, IngredientDiv dbIngDiv) {
//		ObjectContext context;
//		NodeList nodeList;
//		Node node;
//		String nodeName;
//		
//    	context = RecipeMLApplication.getEntityContext();
//		nodeList = ingredientNode.getChildNodes();
//		for (int i=0; i<nodeList.getLength(); i++) {
//			node = nodeList.item(i);
//			if (node.getNodeType() != Node.ELEMENT_NODE) {
//				continue;
//			}
//			nodeName = node.getNodeName();
//			if (nodeName.equals("ing")) {
//				setIngredient((Element)node, dbIngDiv);
//			} else if (nodeName.equals("note")) {
//				
//			}
//		}
//	}
//	
//	private static void setIngredient(Element ingredientNode, IngredientDiv dbIngDiv) {
//		ObjectContext context;
//		NodeList nodeList;
//		Node node;
//		String nodeName;
//		
//    	context = RecipeMLApplication.getEntityContext();
//		nodeList = ingredientNode.getChildNodes();
//		for (int i=0; i<nodeList.getLength(); i++) {
//			node = nodeList.item(i);
//			if (node.getNodeType() != Node.ELEMENT_NODE) {
//				continue;
//			}
//			nodeName = node.getNodeName();
//			if (nodeName.equals("amt")) {
//			} else if (nodeName.equals("sep")) {
//			} else if (nodeName.equals("modifier")) {
//			} else if (nodeName.equals("item")) {
//			} else if (nodeName.equals("prep")) {
//			} else if (nodeName.equals("ing-note")) {
//			} else if (nodeName.equals("prod-code")) {
//			} else if (nodeName.equals("alt-ing")) {
//			}
//		}
//	}
	
	@SuppressWarnings("unused")
	private static String getTextValue(Element node) {
	    String value = null;
	    
	    SharedUtils.checkNotNull(node);
	    if (node.hasChildNodes()) {
	        value = node.getFirstChild().getNodeValue();
	    }
	    return value;
	}
	
	@SuppressWarnings("unused")
	private static void dumpNodeList(NodeList nl) {
		Node node;
		
		SharedUtils.checkNotNull(nl);
		for (int i=0; i<nl.getLength(); i++) {
			node = nl.item(i);
			System.out.print(node.getNodeName() + " : " + node.getNodeType() + " : " + node.getNodeValue());
			System.out.println(", is element = " + (node.getNodeType() == Node.ELEMENT_NODE));
		}
	}
}
