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
package com.hulles.a1icia.cayenne;

import java.util.Collection;

import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.configuration.server.ServerRuntime;
import org.apache.cayenne.log.JdbcEventLogger;
import org.apache.cayenne.log.NoopJdbcEventLogger;

import com.hulles.a1icia.api.shared.PurdahKeys;

public final class A1iciaApplication {
    private static ObjectContext entityContext = null;
	private static ServerRuntime cayenneRuntime = null;
    private static boolean uncommittedObjectsError = true;
    private static boolean logging = false;
    
	private A1iciaApplication() {
		// only static methods now...
	}
	
    /**
     * Return the project's Cayenne ServerRuntime object, for use in creating local ObjectContexts e.g.
     * 
     * @return The Cayenne ServerRuntime
     */
    public synchronized static ServerRuntime getServerRuntime() {
    	@SuppressWarnings("unused")
		PurdahKeys purdah;
    	
    	if (cayenneRuntime == null) {
    		purdah = PurdahKeys.getInstance();
    		if (!logging) {
	    		cayenneRuntime = ServerRuntime.builder()
	    				.addConfig("com/hulles/a1icia/cayenne/cayenne-a1icia.xml")
	    				.addModule(binder -> binder.bind(JdbcEventLogger.class)
	    						.to(NoopJdbcEventLogger.class))
	    				.build();
    		} else {
	    		cayenneRuntime = ServerRuntime.builder()
	    				.addConfig("com/hulles/a1icia/cayenne/cayenne-a1icia.xml")
	    				.build();
    		}
    	}
    	return cayenneRuntime;
    }

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
	
	/**
	 * Get a count of the records in the named table
	 * 
	 * @param sqlTableName The SQL name (vs. the Cayenne name) of the table
	 * @return The number of rows
	 */
/*    public static Long getRecordCount(String sqlTableName) {
     	ObjectContext context;
    	DataRow row;
    	Long result;
    	String selectStr;
    	String stmt;
    	
    	SharedUtils.checkNotNull(sqlTableName);
    	context = CambioApplication.getEntityContext();
    	selectStr = "SELECT COUNT(*) AS `count` FROM `%s`";
    	stmt = String.format(selectStr, sqlTableName);
    	row = SQLSelect.dataRowQuery(stmt).selectFirst(context);
     	result = (Long) row.get("count");
    	return result;
    }
*/
/*        long count = ObjectSelect.query(Artist.class)
                .where(Artist.ARTIST_NAME.like("a%"))
                .selectCount(context);
*/
}
