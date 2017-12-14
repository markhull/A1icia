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
package com.hulles.a1icia.graphviz;

import com.hulles.a1icia.tools.A1iciaUtils;

/**
 * In (mathematical) graph terms, an edge connects two nodes. In a directed graph, which
 * is what we use in A1icia, an edge connects a from node and a to node.
 * 
 * @author hulles
 *
 */
public class Edge extends GraphObject {
    private final Node fromNode;
    private final Node toNode;

    public Edge(String id, Node fromNode, Node toNode) {
        super(id);
        
        A1iciaUtils.checkNotNull(id);
        A1iciaUtils.checkNotNull(fromNode);
        A1iciaUtils.checkNotNull(toNode);
        this.fromNode = fromNode;
        this.toNode = toNode;
    }
    public Edge(Long documentID, Node fromNode, Node toNode) {
    	
        this(documentID.toString(), fromNode, toNode);
    }
    
    public Node getFromNode() {
    	
        return this.fromNode;
    }

    public Node getToNode(){
    	
        return this.toNode;
    }

    @Override
    public String genDotString() {
    	StringBuffer sb;
    	String attrStr;
    	
    	sb = new StringBuffer();
        sb.append("[");
        attrStr = this.genAttributeDotString();
        sb.append(attrStr);
        sb.append("]");
        return sb.toString();
    }
    
	@Override
	public GraphObjectType getGraphObjectType() {

		return GraphObjectType.EDGE;
	}
}