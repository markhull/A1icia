package com.hulles.a1icia.cayenne;

import javax.sql.DataSource;

import org.apache.cayenne.configuration.DataNodeDescriptor;
import org.apache.cayenne.configuration.server.DataSourceFactory;

import com.hulles.a1icia.api.shared.PurdahKeys;
import com.mysql.jdbc.jdbc2.optional.MysqlConnectionPoolDataSource;

public final class A1iciaDataSourceFactory implements DataSourceFactory {

	@Override
	public DataSource getDataSource(DataNodeDescriptor nodeDescriptor) throws Exception {
        MysqlConnectionPoolDataSource dataSource;
		PurdahKeys purdah;

		purdah = PurdahKeys.getInstance();
        dataSource = new MysqlConnectionPoolDataSource();
        dataSource.setUser(purdah.getDatabaseUser());
        dataSource.setPassword(purdah.getDatabasePassword());
        dataSource.setServerName(purdah.getDatabaseServer());
        dataSource.setPort(purdah.getDatabasePort());
        dataSource.setDatabaseName(purdah.getDatabaseName());
        dataSource.setUseSSL(purdah.getDatabaseUseSSL());
        return dataSource;
	}

}
