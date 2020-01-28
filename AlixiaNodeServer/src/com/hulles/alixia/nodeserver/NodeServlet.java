/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hulles.alixia.nodeserver;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eclipsesource.v8.JavaCallback;
import com.eclipsesource.v8.NodeJS;
import com.eclipsesource.v8.V8;
import com.hulles.alixia.api.shared.AlixiaException;
import com.hulles.alixia.api.shared.SharedUtils;
import com.hulles.alixia.nodeserver.pages.NodeWebServer;
import com.hulles.alixia.nodeserver.v8.HtmlResponder;
import com.hulles.alixia.nodeserver.v8.JsonResponder;
import com.hulles.alixia.nodeserver.v8.MediaResponder;
import com.hulles.alixia.nodeserver.v8.Registrar;
import com.hulles.alixia.nodeserver.v8.ServerParams;
import com.hulles.alixia.nodeserver.v8.TextResponder;
import com.hulles.alixia.nodeserver.v8.URLResponder;

/**
 *
 * @author hulles
 */
public final class NodeServlet {
	private final static Logger LOGGER = LoggerFactory.getLogger(NodeServlet.class);
	private NodeJS nodeJS;
    private final Registrar registrar;
    
    public NodeServlet(NodeWebServer nodeServer) {
        
        SharedUtils.checkNotNull(nodeServer);
		registrar = new Registrar(nodeServer);
    }
	
	// it sucks that we need to do this just to get a file for the j2v8 exec method...
	//  TODO: Let's change that.
	private File getResourceAsFile(String resourcePath) {
		File tempFile;
        int bytesRead;
        byte[] buffer;
        Module module;
		
        module = this.getClass().getModule();
	    try (InputStream in = module.getResourceAsStream(resourcePath)) {
	        if (in == null) {
		    	throw new AlixiaException("NodeServlet: can't create stream for JS file");
	        }
	        tempFile = File.createTempFile(String.valueOf(in.hashCode()), ".tmp");
	        tempFile.deleteOnExit();
	        try (FileOutputStream out = new FileOutputStream(tempFile)) {
	            //copy stream
	            buffer = new byte[1024];
	            while ((bytesRead = in.read(buffer)) != -1) {
	                out.write(buffer, 0, bytesRead);
	            }
	        }
	    } catch (IOException e) {
	    	throw new AlixiaException("NodeServlet: IO exception creating JS file", e);
	    }
        return tempFile;
	}
    
    public Registrar getRegistrar() {
        
        return registrar;
    }
    
	/**
	 * 
	 * "All access to a single runtime must be from the same thread." Sigh. It helps to RTFM.
	 * 
	 */
	public void serve() {
		File nodeScript;
		JavaCallback textCallback;
		JavaCallback htmlCallback;
        JavaCallback jsonCallback;
        JavaCallback mediaCallback;
        JavaCallback urlCallback;
        JavaCallback serverParams;
		V8 runtime;
		String version;
        
		nodeJS = NodeJS.createNodeJS();
		version = nodeJS.getNodeVersion();
        LOGGER.info("Node.js Version: {}", version);
		textCallback = new TextResponder(registrar);
		jsonCallback = new JsonResponder(registrar);
		htmlCallback = new HtmlResponder();
        mediaCallback = new MediaResponder();
        urlCallback = new URLResponder();
        serverParams = new ServerParams();
		runtime = nodeJS.getRuntime();
		runtime.registerJavaMethod(registrar, "register");
		runtime.registerJavaMethod(textCallback, "textAnswer");
		runtime.registerJavaMethod(jsonCallback, "jsonAnswer");
		runtime.registerJavaMethod(htmlCallback, "htmlAnswer");
		runtime.registerJavaMethod(mediaCallback, "mediaAnswer");
		runtime.registerJavaMethod(urlCallback, "urlFinder");
		runtime.registerJavaMethod(serverParams, "serverParams");
		nodeScript = getResourceAsFile("com/hulles/alixia/nodeserver/AlixiaExpressServer.js");
		nodeJS.exec(nodeScript);
		
		while(nodeJS.isRunning()) {
			nodeJS.handleMessage();
		}
        LOGGER.info("NodeServlet is out of running loop");
	}
    
    public void stopServing() {
        
        LOGGER.info("NodeServlet is stopping");
        nodeJS.getRuntime().terminateExecution();
    }
}
