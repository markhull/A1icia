package com.hulles.a1icia.cayenne;

import javax.sql.DataSource;

import org.apache.cayenne.configuration.DataNodeDescriptor;
import org.apache.cayenne.configuration.server.DataSourceFactory;

import com.hulles.a1icia.api.shared.PurdahKeys;
import com.hulles.a1icia.api.shared.PurdahKeys.PurdahKey;
import com.mysql.jdbc.jdbc2.optional.MysqlConnectionPoolDataSource;

public final class A1iciaDataSourceFactory implements DataSourceFactory {

	@Override
	public DataSource getDataSource(DataNodeDescriptor nodeDescriptor) throws Exception {
        MysqlConnectionPoolDataSource dataSource;
		PurdahKeys purdah;
		String portStr;
		Integer port;
		String sslStr;
		Boolean useSSL;
		
		purdah = PurdahKeys.getInstance();
        dataSource = new MysqlConnectionPoolDataSource();
        dataSource.setUser(purdah.getPurdahKey(PurdahKey.DATABASEUSER));
        dataSource.setPassword(purdah.getPurdahKey(PurdahKey.DATABASEPW));
        dataSource.setServerName(purdah.getPurdahKey(PurdahKey.DATABASESERVER));
		portStr = purdah.getPurdahKey(PurdahKey.DATABASEPORT);
		port = Integer.parseInt(portStr);
		dataSource.setPort(port);
        dataSource.setDatabaseName(purdah.getPurdahKey(PurdahKey.DATABASENAME));
        sslStr = purdah.getPurdahKey(PurdahKey.DATABASEUSESSL);
        useSSL = Boolean.parseBoolean(sslStr);
        dataSource.setUseSSL(useSSL);
        return dataSource;
	}

}
