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

import javax.sql.DataSource;

import org.apache.cayenne.configuration.DataNodeDescriptor;
import org.apache.cayenne.configuration.server.DataSourceFactory;
import org.mariadb.jdbc.MariaDbDataSource;

import com.hulles.a1icia.api.shared.PurdahKeys;
import com.hulles.a1icia.api.shared.PurdahKeys.PurdahKey;

public final class A1iciaDataSourceFactory implements DataSourceFactory {

	@Override
	public DataSource getDataSource(DataNodeDescriptor nodeDescriptor) throws Exception {
        MariaDbDataSource dataSource;
		PurdahKeys purdah;
		String portStr;
		Integer port;
//		String sslStr;
//		Boolean useSSL;
		
		purdah = PurdahKeys.getInstance();
        dataSource = new MariaDbDataSource();
        dataSource.setUser(purdah.getPurdahKey(PurdahKey.DATABASEUSER));
        dataSource.setPassword(purdah.getPurdahKey(PurdahKey.DATABASEPW));
        dataSource.setServerName(purdah.getPurdahKey(PurdahKey.DATABASESERVER));
		portStr = purdah.getPurdahKey(PurdahKey.DATABASEPORT);
		port = Integer.parseInt(portStr);
		dataSource.setPort(port);
        dataSource.setDatabaseName(purdah.getPurdahKey(PurdahKey.DATABASENAME));
//        sslStr = purdah.getPurdahKey(PurdahKey.DATABASEUSESSL);
//        useSSL = Boolean.parseBoolean(sslStr);
//        dataSource.setUseSSL(useSSL);
        return dataSource;
	}

}
