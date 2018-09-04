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
package com.hulles.alixia.cayenne;

import com.hulles.alixia.api.AlixiaConstants;
import com.hulles.alixia.api.shared.AlixiaException;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.configuration.server.ServerRuntime;
import org.apache.cayenne.log.JdbcEventLogger;
import org.apache.cayenne.log.NoopJdbcEventLogger;
import org.apache.cayenne.resource.ResourceLocator;

public final class AlixiaApplication {
	private final static Logger LOGGER = Logger.getLogger("AlixiaCayenne.AlixiaApplication");
	private final static Level LOGLEVEL = AlixiaConstants.getAlixiaLogLevel();
    private static ObjectContext entityContext = null;
	private static ServerRuntime cayenneRuntime = null;
    private static boolean uncommittedObjectsError = true;
    private static boolean logging = false;
    
	private AlixiaApplication() {
		// only static methods now...
	}
	
    /**
     * Return the project's Cayenne ServerRuntime object, for use in creating local 
     * ObjectContexts e.g.
     * 
     * @return The Cayenne ServerRuntime
     */
    public synchronized static ServerRuntime getServerRuntime() {
    	
    	if (cayenneRuntime == null) {
    		
    		if (!logging) {
//    			showURLs("com/hulles/alixia/cayenne/cayenne-alixia.xml");
	    		cayenneRuntime = ServerRuntime.builder()
	    	            .addModule(binder -> binder.bind(ResourceLocator.class)
	    	            		.toInstance(new AlixiaResourceLocator(AlixiaApplication.class, "cayenne-alixia.xml")))
	    				.addConfig("com/hulles/alixia/cayenne/cayenne-alixia.xml")
	    				.addModule(binder -> binder.bind(JdbcEventLogger.class)
	    						.to(NoopJdbcEventLogger.class))
	    				.build();
    		} else {
	    		cayenneRuntime = ServerRuntime.builder()
	    	            .addModule(binder -> binder.bind(ResourceLocator.class)
	    	            		.toInstance(new AlixiaResourceLocator(AlixiaApplication.class, "cayenne-alixia.xml")))
	    				.addConfig("com/hulles/alixia/cayenne/cayenne-alixia.xml")
	    				.build();
    		}
    		
    	}
    	return cayenneRuntime;
    }
/*
    private static void showURLs(String name) {
    	ClassLoaderManager classLoaderManager;
        Enumeration<URL> urls;
        
        classLoaderManager = new DefaultClassLoaderManager();
        try {
            urls = classLoaderManager.getClassLoader(name).getResources(name);
        } catch (IOException e) {
            throw new ConfigurationException("Error getting resources for ");
        }
        while (urls.hasMoreElements()) {
        	System.out.println("URL: " + urls.nextElement());
        }
    }
    
    static class DefaultClassLoaderManager implements ClassLoaderManager {

        @Override
        public ClassLoader getClassLoader(String resourceName) {
            // here we are ignoring 'className' when looking for ClassLoader...
            // other implementations (such as OSGi) may actually use it

            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

            if (classLoader == null) {
            	System.out.println("Using THIS classLoader");
                classLoader = DefaultClassLoaderManager.class.getClassLoader();
            } else {
            	System.out.println("Using THREAD classLoader");
            }

            // this is too paranoid I guess... "this" class will always have a
            // ClassLoader
            if (classLoader == null) {
                throw new IllegalStateException("Can't find a ClassLoader");
            }

            return classLoader;
        }

    } 
*/    
    public static void setErrorOnUncommittedObjects(boolean value) {
    	
    	uncommittedObjectsError = value;
    }
    
    /**
     * This needs to be called before the ServerRuntime is generated, at least until I figure
     * out how to @*#$!(! adjust the logging for Cayenne in slf4j.
     * 
     * @param value Logging on or off
     */
    public static void setJdbcLogging(boolean value) {
    
    	logging = value;
    }
    
    /**
     * Return an instance of ObjectContext for use by the various Cayenne entities. It needs to be 
     * the same for all entities to avoid keeping track of contexts on an individual entity basis. 
     * For example, you can't update an object dependency if the dependent object is in a 
     * different context.
     *  
     * @return A Cayenne ObjectContext
     */
    public synchronized static ObjectContext getEntityContext() {
    	Collection<?> objects;

    	if (entityContext == null) {
    		entityContext = getServerRuntime().newContext();
    	}
    	if (uncommittedObjectsError) {
	    	if (entityContext.hasChanges()) {
	    		objects = entityContext.uncommittedObjects();
	    		for (Object object : objects) {
	    			LOGGER.log(Level.SEVERE, "Uncommitted Object {0}", object.toString());
	    		}
	    		throw new AlixiaException("Cayenne entity context has changes pending");
	    	}
    	}
    	return entityContext;
    }

    public static void shutdown() {
    	
    	if (cayenneRuntime != null) {
    		cayenneRuntime.shutdown();
    	}
    }
    
	public static boolean isProductionServer() {
		
		// TODO fix this when we go to production
		return false;
	}
	
	public static void rollBack() {
		
		getEntityContext().rollbackChanges();
	}
	
	public static void commit() {
		
		getEntityContext().commitChanges();
	}
	
}
