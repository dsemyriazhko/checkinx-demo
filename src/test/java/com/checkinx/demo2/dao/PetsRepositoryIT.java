package com.checkinx.demo2.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.postgresql.jdbc.PgResultSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;

import com.checkinx.AbstractIntegrationTest;
import com.checkinx.demo2.models.Pet;
import com.checkinx.utils.CheckInxAssert;
import com.checkinx.utils.CoverLevel;
import com.checkinx.utils.sql.interceptors.SqlInterceptor;
import com.checkinx.utils.sql.interceptors.postgres.PostgresInterceptor;
import com.checkinx.utils.sql.plan.parse.ExecutionPlanParser;
import com.checkinx.utils.sql.plan.parse.models.ExecutionPlan;
import com.checkinx.utils.sql.plan.parse.models.PlanNode;
import com.checkinx.utils.sql.plan.query.ExecutionPlanQuery;
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

    @Autowired
    private ExecutionPlanQuery executionPlanQuery;

    private SqlInterceptor sqlInterceptor;

    @Autowired
    private ExecutionPlanParser executionPlanParser;

    @Before
    public void setUp() {
        sqlInterceptor = new PostgresInterceptor((ProxyDataSource) dataSource);
        sqlInterceptor.startInterception();
    }

    @Sql("pets.sql")
    @Test
    public void testFindByNameWithProxyTestDataSourceWithJpa() {
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
    public void testFindByNameWhenProxyTestDataSourceWithJdbc() {
        // ARRANGE
        final ProxyTestDataSource ds = new ProxyTestDataSource(dataSource);

        // ACT
        final List<Map<String, Object>> list = jdbcTemplate.queryForList("select * from pets");

        // ASSERT
        final String query = ds.getFirstPrepared().getQuery();
    }

    @Ignore
    @Sql("pets.sql")
    @Test
    public void testFindByNameWhenProxyDataSourceHandles() {
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

    @Sql("pets.sql")
    @Test
    public void testFindByNameWhenNoIndex() {
        // ARRANGE

        // ACT
        final List<Pet> pets = repository.findByName("Jack");

        // ASSERT
        sqlInterceptor.stopInterception();
        assertEquals(1, sqlInterceptor.getStatements().size());

        final List<String> executionPlan = executionPlanQuery.execute(sqlInterceptor.getStatements().get(0));
        assertTrue(executionPlan.size() > 0);

        final ExecutionPlan plan = executionPlanParser.parse(executionPlan);
        assertEquals("pets pet0_ ", plan.getTable());
        assertEquals("Seq Scan", plan.getRootPlanNode().getCoverage());

//        CheckInxAssert.assertIndex(CoverLevel.ZERO, plan);

//        final List<Map<String, Object>> query = jdbcTemplate.queryForList("explain " + sqlInterceptor.getStatements().get(0));
//        assertNotNull(query);
    }

    @Sql("pets.sql")
    @Test
    public void testFindByNameWhenTextIndexedField() {
        // ARRANGE

        // ACT
        final List<Pet> pets = repository.findByLocation("Moscow");

        // ASSERT
        sqlInterceptor.stopInterception();
        assertEquals(1, sqlInterceptor.getStatements().size());

        final List<String> executionPlan = executionPlanQuery.execute(sqlInterceptor.getStatements().get(0));
        assertTrue(executionPlan.size() > 0);

        final ExecutionPlan plan = executionPlanParser.parse(executionPlan);
        assertNotNull(plan);

        final PlanNode childNode = plan.getRootPlanNode().getChildren().get(0);
        assertEquals("ix_pets_location", childNode.getTarget());
        assertEquals("Bitmap Index Scan", childNode.getCoverage());

        CheckInxAssert.assertIndex(CoverLevel.HALF, "index name", plan);
//        CheckInxAssert.assertIndex(CoverLevel.NOT_INDEX, plan);

//        final List<Map<String, Object>> query = jdbcTemplate.queryForList("explain " + sqlInterceptor.getStatements().get(0));
//        assertNotNull(query);
    }

    @Sql("pets.sql")
    @Test
    public void testFindByNameWithCheckInxAssertWhenIntIndexedField() {
        // ARRANGE

        // ACT
        final List<Pet> pets = repository.findByAge(1);

        // ASSERT
        sqlInterceptor.stopInterception();
        assertEquals(1, sqlInterceptor.getStatements().size());

        final List<String> executionPlan = executionPlanQuery.execute(sqlInterceptor.getStatements().get(0));
        assertTrue(executionPlan.size() > 0);

//        final ExecutionPlan plan = executionPlanParser.parse(executionPlan);

//        CheckInxAssert.assertIndex(CoverLevel.FULL, "index name", plan);

//        CheckInxAssert.assertIndex(CoverLevel.HALF, "index name", plan);
//        CheckInxAssert.assertIndex(CoverLevel.NOT_INDEX, plan);

//        final List<Map<String, Object>> query = jdbcTemplate.queryForList("explain " + sqlInterceptor.getStatements().get(0));
//        assertNotNull(query);
    }
}