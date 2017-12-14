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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.hulles.a1icia.webx.client.content;

import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.media.client.Audio;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.hulles.a1icia.prong.shared.ProngException;
import com.hulles.a1icia.webx.client.A1iciaClientUtils;
import com.hulles.a1icia.webx.client.A1iciaImageResources;
import com.hulles.a1icia.webx.client.services.MindServiceEvent;
import com.hulles.a1icia.webx.client.services.ServiceHandler;
import com.hulles.a1icia.webx.client.services.ServiceHandler.MindServices;
import com.hulles.a1icia.webx.shared.SerialConsoleIn;
import com.hulles.a1icia.webx.shared.SerialConsoleOut;
import com.hulles.a1icia.webx.shared.SharedUtils;

/**
 *
 * @author hulles
 */
public final class ConsoleContent {
	final static Logger LOGGER = Logger.getLogger("A1iciaWeb.ConsoleContent");
	static final Level LOGLEVEL = Level.FINE;
    private static final String DEFAULT_STYLE_NAME = "ConsoleContent";
    private static final String ME = "Me";
    private static final String YOU = "A1icia";
    private static final int POLLING_INTERVAL = 1000; // 1 per second in millis
    private ScrollPanel messagePanel;
    private ScrollPanel explainPanel;
    private final HTML messageHTML;
    private final HTML explainHTML;
	Button sendButton;
    TextArea textArea;
    private final Timer pollingTimer;
//    private final Audio audio;
    private final A1iciaImageResources imageResources;
    private final Image a1iciaLogo;
    
    public ConsoleContent() {
        
        messageHTML = new HTML("...");
        explainHTML = new HTML();
        pollingTimer = new Timer () {
        	@Override
        	public void run() {
        		pollConsole();
        	}
        };
		imageResources = GWT.create(A1iciaImageResources.class);
    	a1iciaLogo = new Image(imageResources.logo());
   }

    public Widget createWidget() {
    	HorizontalPanel panel;
        VerticalPanel leftPanel;
        HorizontalPanel buttonPanel;
        HorizontalPanel logoPanel;
        
        panel = new HorizontalPanel();
        panel.setSpacing(6);
        panel.addStyleName(DEFAULT_STYLE_NAME + "-mainPanel");
        panel.setSize("100%", "100%");
        
        leftPanel = new VerticalPanel();
        leftPanel.setSpacing(8);
        
        explainPanel = new ScrollPanel();
        explainPanel.setSize("600px", "100%");
//        explainPanel.setSize("600px", "600px");
        explainPanel.addStyleName(DEFAULT_STYLE_NAME + "-explainPanel");
        explainPanel.add(explainHTML);
        
        messagePanel = new ScrollPanel();
        messagePanel.setSize("600px", "600px");
//        messagePanel.setSize("600px", "100%");
        messagePanel.addStyleName(DEFAULT_STYLE_NAME + "-messagePanel");
        
        logoPanel = new HorizontalPanel();
        logoPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        logoPanel.add(a1iciaLogo);
        logoPanel.setSize("100%","100%");
        
        messagePanel.add(messageHTML);
        
        textArea = new TextArea();
        textArea.setWidth("600px");
        textArea.setVisibleLines(4);
        textArea.setTitle("Type your stuff here");
        textArea.setReadOnly(false);
        textArea.setEnabled(true);
        textArea.setFocus(true);
        textArea.addKeyPressHandler(new KeyPressHandler() {
			@Override
			public void onKeyPress(KeyPressEvent event) {
				
				SharedUtils.checkNotNull(event);
				sendButton.setEnabled(true);
			}
			
        });

        sendButton = new Button("send");
        sendButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
            	String text;
             	
            	text = textArea.getText();
            	sendConsole(text);
                messageAppendHTML(ME, text);
                textArea.setText("");
                textArea.setFocus(true);
				sendButton.setEnabled(false);
            }
        });
        
        buttonPanel = new HorizontalPanel();
        buttonPanel.setSpacing(8);
        buttonPanel.add(textArea);
        buttonPanel.add(sendButton);
        
        leftPanel.add(logoPanel);
        leftPanel.add(messagePanel);
        leftPanel.add(buttonPanel);
        

        panel.add(leftPanel);
        panel.add(explainPanel);
        
        textArea.setFocus(true);
        
        return panel;
    }
    
    void explainAppendHTML(String explain) {
    	SafeHtml safeText;
    	String newExpl;
    	String formattedExpl;
    	
		SharedUtils.nullsOkay(explain);
    	if (explain == null) {
    		safeText = SafeHtmlUtils.fromSafeConstant("(no response)");
    	} else {
    		safeText = SafeHtmlUtils.fromString(explain);
    	}
    	newExpl = safeText.asString();
    	if (newExpl == null || newExpl.isEmpty()) {
    		return;
    	}
		formattedExpl = newExpl.replace("\n", "<br />");
        explainHTML.setHTML(explainHTML.getHTML() + formattedExpl +  "<br /><br />");
        explainPanel.scrollToBottom();
    }
    
    void messageAppendHTML(String meYou, String html) {
    	String newMsg;
    	SafeHtml safeText;
    	String speak;
    	
		SharedUtils.checkNotNull(meYou);
		// TODO get rid of meYou and just use the prefixes that we were sent
		if (html == null) {
    		safeText = SafeHtmlUtils.fromSafeConstant("(no response)");
    	} else {
    		safeText = SafeHtmlUtils.fromString(html);
    	}
		newMsg = safeText.asString();
		if (newMsg.startsWith("ME:")) {
			speak = newMsg.substring(4);
		} else if (newMsg.startsWith("ALICIA:")) {
			speak = newMsg.substring(8);
		} else {
			speak = newMsg;
		}
		if ((speak == null) || (speak.isEmpty())) {
			speak = "...";
		}
		newMsg = "<br /><br /><strong>" + meYou + "</strong>: " + speak;
        messageHTML.setHTML(messageHTML.getHTML() + newMsg);
        messagePanel.scrollToBottom();
    }
    
    static void playAudio(String elementType, String url) {
        Audio audio;
   	
        audio = Audio.createIfSupported();
    	if (audio == null) {
    		return;
    	}
    	SharedUtils.checkNotNull(url);
    	LOGGER.log(LOGLEVEL, "ConsoleContent: in playAudio, url = " + url);
    	audio.addSource(url, elementType);
//    	audio.load();
    	audio.play();
    	LOGGER.log(LOGLEVEL, "ConsoleContent: through playAudio");
    }
    
    private static native void startMic() /*-{
		'use strict';
		
		navigator.getUserMedia = navigator.getUserMedia ||
		navigator.webkitGetUserMedia || navigator.mozGetUserMedia;
		
		var n = navigator.getUserMedia({
		  audio: true
		}, function(mediaStream) {
		  var stream = mediaStream;
		  var audioElement = document.querySelector('audio');
		  try {
		    audioElement.src = window.URL.createObjectURL(stream);
		  } catch (event0) {
		    try {
		      audioElement.mozSrcObject = stream;
		      audioElement.play();
		    } catch (event1) {
		      console.log('Error setting video src: ', event1);
		    }
		  }
		}, function(error) {
		  console.log('navigator.getUserMedia error: ', error);
		});
		
		console.log(n);
	}-*/;
    
    void sendConsole(String text) {
    	MindServiceEvent<Void> event;
    	SerialConsoleIn consoleIn;
    	
		SharedUtils.checkNotNull(text);
		consoleIn = new SerialConsoleIn();
		consoleIn.setDatestamp(new Date());
		consoleIn.setText(text);
        final AsyncCallback<Void> callback = new AsyncCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
            }
            @Override
            public void onFailure(Throwable caught) {
            	messageAppendHTML(YOU, "Message transmission failure, sorry.");
		        if (caught instanceof ProngException) {
		        	A1iciaClientUtils.prongError(caught);
		        } else {
		        	A1iciaClientUtils.commError(caught);
		        }
            }
        };
 		event = new MindServiceEvent<>(MindServices.SENDCONSOLE);
 		event.addParam(consoleIn);
		event.setCallback(callback);
		ServiceHandler.handleMindServiceEvent(event);
	    // Schedule the timer for POLLING_INTERVAL millis
		// This resets the timer if it's already running
	    pollingTimer.scheduleRepeating(POLLING_INTERVAL);
    }
    
    void pollConsole() {
    	MindServiceEvent<SerialConsoleOut> event;
    	
        final AsyncCallback<SerialConsoleOut> callback = new AsyncCallback<SerialConsoleOut>() {
            @Override
            public void onSuccess(SerialConsoleOut result) {
            	List<String> urls;
            	List<String> formats;
            	
            	if (result == null) {
            		return;
            	}
            	messageAppendHTML(YOU, result.getText());
            	explainAppendHTML(result.getExplain());
            	urls = result.getUrls();
            	formats = result.getFormats();
            	for (int ix=0; ix<urls.size(); ix++) {
                	LOGGER.log(LOGLEVEL, "pollConsole: adding url = " + urls.get(ix));
                	playAudio(formats.get(ix), urls.get(ix));
            	}
            	LOGGER.log(LOGLEVEL, "pollConsole: got " + urls.size() + " urls");
            }
            @Override
            public void onFailure(Throwable caught) {
            	messageAppendHTML(YOU, "Message transmission failure, sorry.");
		        if (caught instanceof ProngException) {
		        	A1iciaClientUtils.prongError(caught);
		        } else {
		        	A1iciaClientUtils.commError(caught);
		        }
            }
        };
 		event = new MindServiceEvent<>(MindServices.RECEIVECONSOLE);
		event.setCallback(callback);
		ServiceHandler.handleMindServiceEvent(event);
    }
}
