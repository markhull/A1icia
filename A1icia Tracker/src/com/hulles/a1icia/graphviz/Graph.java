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
package com.hulles.a1icia.graphviz;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.hulles.a1icia.api.shared.SharedUtils;
import com.hulles.a1icia.ticket.Ticket;

public class Graph extends GraphObject {
    private final GraphType graphType;
    private final Set<Node> nodeSet;
    private final List<Edge> edgeList;
    private final List<Graph> subgraphList;

    public Graph(Ticket ticket) {
    	this(ticket.toString(), GraphType.DIRECTEDGRAPH);
    }
    public Graph(String id, GraphType graphType) {
        super(id);
 
        SharedUtils.checkNotNull(id);
        SharedUtils.checkNotNull(graphType);
        this.graphType = graphType;
        this.nodeSet = Collections.synchronizedSet(new HashSet<>());
        this.edgeList = Collections.synchronizedList(new ArrayList<>());
        this.subgraphList = Collections.synchronizedList(new ArrayList<>());
    }

    public GraphType getGraphType(){
    	
        return this.graphType;
    }

    public void addNode(Node node) {

    	SharedUtils.checkNotNull(node);
        this.nodeSet.add(node);
    }

    public void addEdge(Edge edge) {
 
    	SharedUtils.checkNotNull(edge);
        this.edgeList.add(edge);
    }

    public void addSubgraph(Graph graph) {
    	
    	SharedUtils.checkNotNull(graph);
    	this.subgraphList.add(graph);
    }
    
    public String genGraphDotString() {
        StringBuffer sb;
        String dotStr;
        
        sb = new StringBuffer();
        switch (this.getGraphType()) {
	        case DIRECTEDGRAPH:
	            sb.append("digraph ");
	            break;
	        case GRAPH:
	            sb.append("graph ");
	            break;
        	default:
                throw new GraphException("Unknown graph type = " + this.getGraphType());
        }
        sb.append("\"");
        sb.append(this.getId());
        sb.append("\"");
        dotStr = this.genDotString();
        sb.append(dotStr);
        return sb.toString();
    }

    @Override
    public String genDotString() {
        StringBuffer sb;
        String attrStr;
        String subGraphStr;
        String nodesStr;
        String edgesStr;
        
        sb = new StringBuffer();
        sb.append("{\n");
        attrStr = this.genAttributeDotString();
        sb.append(attrStr);
        subGraphStr = genSubgraphString(); 
        sb.append(subGraphStr);
        nodesStr = genNodesString();
        sb.append(nodesStr);
        edgesStr = genEdgesDotString();
        sb.append(edgesStr);
        sb.append("}\n");
        return sb.toString();
    }

    private String genSubgraphString() {
        StringBuffer sb;
        String dotStr;
        
        sb = new StringBuffer();
        synchronized (subgraphList) {
	        for(Graph graph : subgraphList) {
	            sb.append("subgraph ");
	            sb.append(graph.getId());
	            dotStr = graph.genDotString();
	            sb.append(dotStr);
	            sb.append("\n");
	        }
        }
        return sb.toString();
    }

    private String genNodesString() {
        StringBuffer sb;
        String dotStr;
        
        sb = new StringBuffer();
        synchronized (nodeSet) {
	        for(Node node : nodeSet){
	            sb.append(node.getId());
	            dotStr = node.genDotString();
	            sb.append(dotStr);
	            sb.append("\n");
	        }
        }
        return sb.toString();
    }

    private String genEdgesDotString(){
        StringBuffer sb;
        Node fromNode;
        Node toNode;
        String dotStr;
        
        sb = new StringBuffer();
        synchronized (edgeList) {
	        for (Edge edge : edgeList){
	        	fromNode = edge.getFromNode();
	            sb.append(fromNode.getId());
	            sb.append(getLinkStr());
	            toNode = edge.getToNode();
	            sb.append(toNode.getId());
	            dotStr = edge.genDotString();
	            sb.append(dotStr);
	            sb.append("\n");
	        }
        }
        return sb.toString();
    }

    private String getLinkStr(){
    	
    	switch (this.graphType) {
	    	case DIRECTEDGRAPH:
	            return "->";
	    	case GRAPH:
	            return "--";
    		default:
    	        throw new GraphException("Graph type not supported.");
    	}
    }
    
	@Override
	public GraphObjectType getGraphObjectType() {

		return GraphObjectType.GRAPH;
	}
}

