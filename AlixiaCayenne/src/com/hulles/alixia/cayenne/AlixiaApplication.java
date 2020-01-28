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

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;

import org.apache.cayenne.ConfigurationException;
import org.apache.cayenne.DataRow;
import org.apache.cayenne.DeleteDenyException;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.configuration.server.ServerRuntime;
import org.apache.cayenne.di.ClassLoaderManager;
import org.apache.cayenne.log.JdbcEventLogger;
import org.apache.cayenne.log.NoopJdbcEventLogger;
import org.apache.cayenne.query.SQLExec;
import org.apache.cayenne.query.SQLSelect;
import org.apache.cayenne.resource.ResourceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hulles.alixia.api.shared.AlixiaException;
import com.hulles.alixia.api.shared.SharedUtils;

public final class AlixiaApplication {
	private final static Logger LOGGER = LoggerFactory.getLogger(AlixiaApplication.class);
    private static ObjectContext entityContext = null;
	private static ServerRuntime cayenneRuntime = null;
    private static boolean uncommittedObjectsError = true;
    private static boolean logging = false;
    private final static String COPYTABLE = "CREATE TABLE IF NOT EXISTS %2$s LIKE %1$s;\n" + 
            "INSERT INTO %2$s SELECT * FROM %1$s;";
    private final static String DROPTABLE = "DROP TABLE IF EXISTS %s;";
    private final static String GETALLTABLES = "SELECT `TABLE_NAME` FROM `information_schema`.`tables` WHERE `table_type` = 'BASE TABLE'" +
            " AND `table_schema` = 'wrg';";
    private final static String OPTIMIZETABLE = "OPTIMIZE TABLE %s;";
    
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
    	ResourceLocator alixiaLocator;
    
    	if (cayenneRuntime == null) {
    		alixiaLocator = new AlixiaResourceLocator(AlixiaApplication.class, "cayenne-alixia.xml");
            showURLs("com/hulles/alixia/cayenne/cayenne-alixia.xml");
            showURLs("com/hulles/alixia/cayenne/alixia_datamap.map.xml");
            
    		if (!logging) {
	    		cayenneRuntime = ServerRuntime.builder()
	    	            .addModule(binder -> binder.bind(ResourceLocator.class)
	    	            		.toInstance(alixiaLocator))
	    				.addConfig("com/hulles/alixia/cayenne/cayenne-alixia.xml")
	    				.addModule(binder -> binder.bind(JdbcEventLogger.class)
	    						.to(NoopJdbcEventLogger.class))
	    				.build();
    		} else {
	    		cayenneRuntime = ServerRuntime.builder()
	    	            .addModule(binder -> binder.bind(ResourceLocator.class)
	    	            		.toInstance(alixiaLocator))
	    				.addConfig("com/hulles/alixia/cayenne/cayenne-alixia.xml")
	    				.build();
    		}
    		
    	}
    	return cayenneRuntime;
    }

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
   
    /**
     * We use the check for uncommitted objects to "fail fast" if there are any dangling db objects.
     * @param value True to check for uncommitted objects, false otherwise
     */
    public static void setErrorOnUncommittedObjects(boolean value) {
    	
    	uncommittedObjectsError = value;
    }

    /**
     * We use the check for uncommitted objects to "fail fast" if there are any dangling db objects.
     * @return True if we're currently checking for uncommitted objects, false otherwise
     */
    public static boolean getErrorOnUncommittedObjects() {
        
        return uncommittedObjectsError;
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
	    			LOGGER.error("Uncommitted Object {}", object.toString());
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
        boolean originalValue;
        ObjectContext context;
        
        // since we're doing a commit we might have uncommitted objects outstanding...
        originalValue = getErrorOnUncommittedObjects();
        setErrorOnUncommittedObjects(false);        
        context = getEntityContext();
        context.rollbackChanges();
        setErrorOnUncommittedObjects(originalValue);
    }
    
    /**
     * Commit all objects in the ObjectContext.
     * 
     * @param object The object to commit
     */
    public static void commitAll() {
        boolean originalValue;
        ObjectContext context;
        
        // since we're doing a commit we might have uncommitted objects outstanding...
        originalValue = getErrorOnUncommittedObjects();
        setErrorOnUncommittedObjects(false);        
        context = getEntityContext();
        context.commitChanges();
        setErrorOnUncommittedObjects(originalValue);
    }

    /**
     * Delete a list of Cayenne data objects; the deletions are not committed.
     * 
     * @param objects The list of objects to delete
     */
    public static void delete(List<?> objects) {
        ObjectContext context;
        
        SharedUtils.checkNotNull(objects);
        context = getEntityContext();
        try {
            context.deleteObjects(objects);
//            commitAll();
        } catch (DeleteDenyException e) {
            LOGGER.error("Delete error", e);
        }
    }
    
    /**
     * Drop a table from the database if it exists.
     * 
     * @param table The name of the table to drop
     * 
     */
    static void dropTable(String table) {
        String sql;
        
        SharedUtils.checkNotNull(table);
        sql = String.format(DROPTABLE, table);
        runSQLUpdate(sql);
    }
    
    /**
     * Copy one table to another in the same database. If the destination table exists already,
     * the copy will not be performed.
     * 
     * @param fromTable The origin table to be copied
     * @param toTable The destination table, which should not exist
     * 
     */
    static void copyTable(String fromTable, String toTable) {
        String sql;
        
        SharedUtils.checkNotNull(fromTable);
        SharedUtils.checkNotNull(toTable);
        sql = String.format(COPYTABLE, fromTable, toTable);
        runSQLUpdate(sql);
    }
    /**
     * Copy one table to another in the same database. If the destination table exists,
     * it will be backed up prior to the copy.
     * 
     * @param fromTable The origin table to be copied
     * @param toTable The destination table, which may or may not exist
     * @param backupTable The backup table; if it exists it will be dropped
     * 
     */
    static void copyTable(String fromTable, String toTable, String backupTable) {
        String sql;
        
        SharedUtils.checkNotNull(fromTable);
        SharedUtils.checkNotNull(toTable);
        SharedUtils.checkNotNull(backupTable);
        sql = String.format(DROPTABLE, backupTable);
        runSQLUpdate(sql);  
        sql = String.format(COPYTABLE, toTable, backupTable);
        runSQLUpdate(sql);
        sql = String.format(DROPTABLE, toTable);
        runSQLUpdate(sql);  
        sql = String.format(COPYTABLE, fromTable, toTable);
        runSQLUpdate(sql);
    }
    
    public static void optimizeTables() {
        ObjectContext context;
        List<DataRow> rows;
        String table;
        String stmt;
        List<DataRow> results;
        String type;
        
        context = getEntityContext();
        rows = SQLSelect.dataRowQuery(GETALLTABLES).select(context);
        for (DataRow row : rows) {
            table = (String)row.get("TABLE_NAME");
            stmt = String.format(OPTIMIZETABLE, table);
            results = SQLSelect.dataRowQuery(stmt).select(context);
            for (DataRow result : results) {
                type = (String)result.get("Msg_type");
                if (!type.equals("status")) {
                    // discard InnoDb note "Table does not support optimize, doing recreate + analyze instead"
                    continue;
                }
                LOGGER.info("Optimizing table {}: {}", result.get("Table"), result.get("Msg_text"));
            }
        }
    }
        
    /**
     * Run an update from provided SQL string
     * 
     * @param sql The SQL update statement as a String
     * @return The number of records affected
     * 
     */
    public static int runSQLUpdate(String sql) {
        ObjectContext context;
        
        context = getEntityContext();
        return SQLExec.query(sql).update(context);
    }
	
}
