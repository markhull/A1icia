package com.hulles.a1icia.cayenne;

import javax.sql.DataSource;

import org.apache.cayenne.configuration.DataNodeDescriptor;
import org.apache.cayenne.configuration.server.DataSourceFactory;

import com.hulles.a1icia.api.shared.PurdahKeys;
import com.mysql.jdbc.jdbc2.optional.MysqlConnectionPoolDataSource;

public final class A1iciaDataSourceFactory implements DataSourceFactory {
//	private final static Logger LOGGER = Logger.getLogger("A1iciaCayenne.A1iciaDataSourceFactory");
//	private final static Level LOGLEVEL = Level.INFO;

	@Override
	public DataSource getDataSource(DataNodeDescriptor nodeDescriptor) throws Exception {
//		DataSourceInfo dataSourceDescriptor;
        MysqlConnectionPoolDataSource dataSource;
		PurdahKeys purdah;
//		String message;
		
//		dataSourceDescriptor = nodeDescriptor.getDataSourceDescriptor();
//		if (dataSourceDescriptor == null) {
//			message = "Null dataSourceDescriptor for nodeDescriptor '"
//					+ nodeDescriptor.getName()
//					+ "'";
//			throw new ConfigurationException(message);
//		}

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
