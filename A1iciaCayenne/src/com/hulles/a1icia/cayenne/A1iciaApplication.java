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
package com.hulles.a1icia.cayenne;

import java.util.Collection;

import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.configuration.server.ServerRuntime;
import org.apache.cayenne.log.JdbcEventLogger;
import org.apache.cayenne.log.NoopJdbcEventLogger;
import org.apache.cayenne.resource.ResourceLocator;

public final class A1iciaApplication {
    private static ObjectContext entityContext = null;
	private static ServerRuntime cayenneRuntime = null;
    private static boolean uncommittedObjectsError = true;
    private static boolean logging = false;
    
	private A1iciaApplication() {
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
//    		@SuppressWarnings("rawtypes")
//			Class clazz = A1iciaApplication.class;
//    		System.out.println("Class " + clazz.getName());
//    		System.out.println("Canonical name " + clazz.getCanonicalName());
//    		System.out.println("Package name " + clazz.getPackageName());
//    		System.out.println("Simple name " + clazz.getSimpleName());
//    		System.out.println("Type name " + clazz.getTypeName());
//    		System.out.println("Generic string " + clazz.toGenericString());
//    		System.out.println("Class resource URL " + clazz.getResource("cayenne-a1icia.xml"));
//    		ClassLoader cl = clazz.getClassLoader();
//			System.out.println("CAYENNE");
//    		System.out.println("Module name " + clazz.getModule().getName());
//    		System.out.println("Module descriptor " + clazz.getModule().getDescriptor());
//    		System.out.println("Module layer " + clazz.getModule().getLayer());
//    		System.out.println();
//    		System.out.println("ClassLoader resource URL " + cl.getResource("cayenne-a1icia.xml"));
//    		System.out.println("ClassLoader name " + cl.getName());
    		
    		if (!logging) {
//    			showURLs("com/hulles/a1icia/cayenne/cayenne-a1icia.xml");
	    		cayenneRuntime = ServerRuntime.builder()
	    	            .addModule(binder -> binder.bind(ResourceLocator.class)
	    	            		.toInstance(new A1iciaResourceLocator(A1iciaApplication.class, "cayenne-a1icia.xml")))
	    				.addConfig("com/hulles/a1icia/cayenne/cayenne-a1icia.xml")
	    				.addModule(binder -> binder.bind(JdbcEventLogger.class)
	    						.to(NoopJdbcEventLogger.class))
	    				.build();
    		} else {
	    		cayenneRuntime = ServerRuntime.builder()
	    	            .addModule(binder -> binder.bind(ResourceLocator.class)
	    	            		.toInstance(new A1iciaResourceLocator(A1iciaApplication.class, "cayenne-a1icia.xml")))
	    				.addConfig("com/hulles/a1icia/cayenne/cayenne-a1icia.xml")
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
	    			java.lang.System.err.println("***** Uncommitted Object " + object.toString());
	    		}
	    		throw new RuntimeException("Cayenne entity context has changes pending");
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
