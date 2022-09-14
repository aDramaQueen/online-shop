package com.acme.onlineshop.service;

import com.acme.onlineshop.utils.Profile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.sql.DataSource;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * <p>This service serves only one purpose:</p>
 *
 * <p>If <i>"Liquibase"</i> migrations were done by hand and you start the application in <i>"developer mode"</i> afterwards, special tables of <i>"Liquibase"</i> (@see <a href="https://docs.liquibase.com/concepts/tracking-tables/tracking-tables.html">Liquibase documentation: Tracking Table</a>) have to be deleted to run properly.</p>
 *
 * <p>ATTENTION: The <i>"developer mode"</i> will delete <b>ALL</b> JPA entity tables if the application shuts down.</p>
 *
 * @see  <a href="https://www.liquibase.org/get-started/how-liquibase-works">Liquibase documentation: How liquibase works</a>
 * @author Richard Saeuberlich
 * @version 1.0
 */
@Service
public class DatabaseTableService {

    private final EntityManager entityManager;
    private final DataSource dataSource;
    private final String QUERY = "DROP TABLE IF EXISTS %s CASCADE";
    private final String QUERY_DERBY = "DROP TABLE %s";

    @Autowired
    public DatabaseTableService(EntityManager entityManager, DataSource dataSource) {
        this.entityManager = entityManager;
        this.dataSource = dataSource;
    }

    @Transactional
    public int dropTableIfExists(Profile databaseProfile, String tableName) throws SQLException {
        Query query;
        if (databaseProfile == Profile.DERBY) {
            DatabaseMetaData metaData = dataSource.getConnection().getMetaData();
            if(tableExists(tableName, metaData)) {
                return entityManager.createNativeQuery(QUERY_DERBY.formatted(tableName)).executeUpdate();
            } else {
                return 0;
            }
        } else {
            return entityManager.createNativeQuery(QUERY.formatted(tableName)).executeUpdate();
        }
    }

    @Transactional
    public int dropTableIfExists(Profile databaseProfile, String[] tableNames) throws SQLException {
        Query query;
        int deletedTables = 0;
        if (databaseProfile == Profile.DERBY) {
            DatabaseMetaData metaData = dataSource.getConnection().getMetaData();
            for(int i=0; i<tableNames.length; i++) {
                if (tableExists(tableNames[i], metaData)) {
                    query = entityManager.createNativeQuery(QUERY_DERBY.formatted(tableNames[i]));
                    deletedTables += query.executeUpdate();
                }
            }
        } else {
            for(int i=0; i<tableNames.length; i++) {
                query = entityManager.createNativeQuery(QUERY.formatted(tableNames[i]));
                deletedTables += query.executeUpdate();
            }
        }
        return deletedTables;
    }

    @Transactional
    public int dropTableIfExists(Profile databaseProfile, Iterable<String> tableNames) throws SQLException {
        Query query;
        int deletedTables = 0;
        if (databaseProfile == Profile.DERBY) {
            for(String tableName: tableNames) {
                DatabaseMetaData metaData = dataSource.getConnection().getMetaData();
                if (tableExists(tableName, metaData)) {
                    query = entityManager.createNativeQuery(QUERY_DERBY.formatted(tableName));
                    deletedTables += query.executeUpdate();
                }
            }
        } else {
            for(String tableName: tableNames) {
                query = entityManager.createNativeQuery(QUERY.formatted(tableName));
                deletedTables += query.executeUpdate();
            }
        }
        return deletedTables;
    }

    private boolean tableExists(String tableName, DatabaseMetaData metaData) throws SQLException {
        ResultSet resultSet = metaData.getTables(null, null, tableName, new String[] {"TABLE"});
        return resultSet.next();
    }
}
