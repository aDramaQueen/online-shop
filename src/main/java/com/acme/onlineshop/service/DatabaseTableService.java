package com.acme.onlineshop.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.Query;

/**
 * <p>This service serves only one purpose:</p>
 *
 * <p>If <i>"Liquibase"</i> migrations were done by hand and you start the application in <i>"developer mode"</i> afterwards, special tables of <i>"Liquibase"</i> (@see <a href="https://docs.liquibase.com/concepts/tracking-tables/tracking-tables.html">Liquibase documentation: Tracking Table</a>) have to be deleted to run properly.</p>
 *
 * <p>ATTENTION: The <i>"developer mode"</i> will delete <b>ALL</b> JPA entity tables if the application shuts down.</p>
 *
 * @see  <a href="https://www.liquibase.org/get-started/how-liquibase-works">Liquibase documentation: How liquibase works</a>
 */
@Service
public class DatabaseTableService {

    private final EntityManager entityManager;
    private final static String QUERY_STR = "DROP TABLE IF EXISTS %s CASCADE";

    @Autowired
    public DatabaseTableService(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Transactional
    public int dropTableIfExists(String tableName) {
        Query query = entityManager.createNativeQuery(QUERY_STR.formatted(tableName));
        return query.executeUpdate();
    }

    @Transactional
    public int dropTableIfExists(String[] tableNames) {
        Query query;
        int deletedTables = 0;
        for(int i=0; i<tableNames.length; i++) {
            query = entityManager.createNativeQuery(QUERY_STR.formatted(tableNames[i]));
            deletedTables += query.executeUpdate();
        }
        return deletedTables;
    }

    @Transactional
    public int dropTableIfExists(Iterable<String> tableNames) {
        Query query;
        int deletedTables = 0;
        for(String tableName: tableNames) {
            query = entityManager.createNativeQuery(QUERY_STR.formatted(tableName));
            deletedTables += query.executeUpdate();
        }
        return deletedTables;
    }
}
