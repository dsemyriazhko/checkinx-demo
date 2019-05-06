package com.checkinx.demo2.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.postgresql.jdbc.PgResultSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;

import com.checkinx.AbstractIntegrationTest;
import com.checkinx.demo2.models.Pet;
import com.checkinx.demo2.utils.sql.interceptors.SqlInterceptor;
import com.checkinx.demo2.utils.sql.interceptors.impl.PostgresInterceptor;
import com.zaxxer.hikari.pool.HikariProxyResultSet;
import net.ttddyy.dsproxy.ExecutionInfo;
import net.ttddyy.dsproxy.QueryInfo;
import net.ttddyy.dsproxy.asserts.ProxyTestDataSource;
import net.ttddyy.dsproxy.asserts.QueryExecution;
import net.ttddyy.dsproxy.listener.QueryExecutionListener;
import net.ttddyy.dsproxy.support.ProxyDataSource;

public class PetsRepositoryIT extends AbstractIntegrationTest {

    @Autowired
    private PetsRepository repository;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private SqlInterceptor sqlInterceptor;

    @Before
    public void setUp() {
        sqlInterceptor = new PostgresInterceptor((ProxyDataSource) dataSource);
        sqlInterceptor.startInterception();
    }

    @Sql("pets.sql")
    @Test
    public void testFindByNameWithProxyTestDataSource() {
        // ARRANGE
        final ProxyTestDataSource ds = new ProxyTestDataSource(dataSource);
        ((ProxyDataSource)dataSource).addListener(ds.getQueryExecutionFactoryListener());

        // ACT
        final List<Pet> pets = repository.findByName("Jack");

        // ASSERT
        assertEquals(1, pets.size());
        final List<QueryExecution> queryExecutions = ds.getQueryExecutions();
        assertNotNull(queryExecutions);
    }

    @Ignore
    @Sql("pets.sql")
    @Test
    public void testFindByNameWithProxyDataSource() {
        // ARRANGE
        ((ProxyDataSource)dataSource).addListener(new QueryExecutionListener() {
            @Override
            public void beforeQuery(ExecutionInfo execInfo, List<QueryInfo> queryInfoList) {
                // nothing
            }

            @Override
            public void afterQuery(ExecutionInfo execInfo, List<QueryInfo> queryInfoList) {
                try {
//                    System.out.println(((HikariProxyResultSet)execInfo.getResult()).getStatement());

                    final String sql = ((HikariProxyResultSet) execInfo.getResult()).unwrap(PgResultSet.class).getStatement().toString();
//                    final List<Map<String, Object>> result = jdbcTemplate.queryForList("explain " + sql);
                    System.out.println(sql);
                }
                catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });

        // ACT
        final List<Pet> pets = repository.findByName("Jack");

        // ASSERT
        assertEquals(1, pets.size());
    }

    @Ignore
    @Sql("pets.sql")
    @Test
    public void testFindByNameWithProxyTestDataSourceWhenJdbc() {
        // ARRANGE
        final ProxyTestDataSource ds = new ProxyTestDataSource(dataSource);

        // ACT
        final List<Map<String, Object>> list = jdbcTemplate.queryForList("select * from pets");

        // ASSERT
        final String query = ds.getFirstPrepared().getQuery();
    }

    @Sql("pets.sql")
    @Test
    public void testFindByNameWithCheckInxAssert() {
        // ARRANGE

        // ACT
        final List<Pet> pets = repository.findByName("Jack");

        // ASSERT
        sqlInterceptor.stopInterception();
        assertEquals(1, sqlInterceptor.getStatements().size());

        final List<Map<String, Object>> query = jdbcTemplate.queryForList("explain " + sqlInterceptor.getStatements().get(0));
        assertNotNull(query);
    }
}