/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hulles.alixia.house;

import com.google.common.eventbus.EventBus;
import com.hulles.alixia.api.remote.AlixianID;
import com.hulles.alixia.api.shared.SessionType;
import com.hulles.alixia.api.shared.SharedUtils;
import com.hulles.alixia.media.Language;
import java.io.Closeable;
import java.util.Map;
import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manage our collection of prompters.
 * 
 * @author hulles
 */
public class PrompterManager implements Closeable {
	private Map<AlixianID, Prompter> prompters;
	private final static int PROMPTDELAY = 45 * 1000;
	private final static int NAGDELAY = 60 * 1000;
	private Timer promptTimer;
	private Boolean noPrompts;
    private final EventBus street;
    
    public PrompterManager(EventBus street, Boolean noPrompts) {
        
        SharedUtils.checkNotNull(street);
        SharedUtils.checkNotNull(noPrompts);
        this.street = street;
        this.noPrompts = noPrompts;
		if (!noPrompts) {
			promptTimer = new Timer();
			prompters = new ConcurrentHashMap<>();
		}
    }

	public void resetPrompter(AlixianID alixianID, SessionType sessionType, Language language, Boolean isQuiet) {
        Prompter prompter;
        
        SharedUtils.checkNotNull(alixianID);
        SharedUtils.checkNotNull(sessionType);
        SharedUtils.checkNotNull(language);
        SharedUtils.checkNotNull(isQuiet);
		if (!noPrompts) {
			// cancel existing prompter for this Alician, if any...
            prompters.remove(alixianID);
			// ...and start a new one
			prompter = new Prompter(alixianID, sessionType, language, isQuiet, street);
	        promptTimer.schedule(prompter, PROMPTDELAY, NAGDELAY);
	        prompters.put(alixianID, prompter);
		}
    }
    
    public void setNoPrompts(Boolean noPrompts) {
    
		SharedUtils.checkNotNull(noPrompts);
		this.noPrompts = noPrompts;
    }
    
    @Override
    public void close() {
        
		if (promptTimer != null) {
            
			for (Prompter prompter : prompters.values()) {
				prompter.cancel();
			}
			promptTimer.cancel();
		}
        
    }
}
