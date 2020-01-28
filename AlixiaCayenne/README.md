# Alixia Cayenne

This is the Cayenne Module for the [Alixia Project](https://github.com/markhull/Alixia).

Cayenne is the module that provides RDBMS services to Alixia. Alixia currently uses MariaDB as the RDBMS engine. However, this module is implemented using [Apache Cayenne](https://cayenne.apache.org/) (hence the name), which means that Alixia can be built with any of the [following RDBMS products](http://cayenne.apache.org/database-support.html):

* DB2
* Derby
* FrontBase
* HSQLDB
* H2
* Ingres
* MySQL
* OpenBase
* Oracle
* PostgreSQL
* SQLite 3.*
* SQLServer
* Sybase

This is awesome, because it means that Alixia Central can run on very small to very large platforms with no discomfiture. Go Alixia.

Alixia Cayenne is now Java 10. See `module-info.java` for module path requirements.
