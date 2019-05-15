package com.checkinx.demo2.dao

import com.checkinx.AbstractIntegrationTest
import com.checkinx.demo2.models.Pet
import com.checkinx.utils.asserts.CheckInxAssertService
import com.checkinx.utils.asserts.CoverageLevel
import com.checkinx.utils.sql.interceptors.SqlInterceptor
import com.checkinx.utils.sql.interceptors.postgres.PostgresInterceptor
import com.checkinx.utils.sql.plan.parse.ExecutionPlanParser
import com.checkinx.utils.sql.plan.query.ExecutionPlanQuery
import com.zaxxer.hikari.pool.HikariProxyResultSet
import net.ttddyy.dsproxy.ExecutionInfo
import net.ttddyy.dsproxy.QueryInfo
import net.ttddyy.dsproxy.asserts.ProxyTestDataSource
import net.ttddyy.dsproxy.listener.QueryExecutionListener
import net.ttddyy.dsproxy.support.ProxyDataSource
import org.junit.Assert.*
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.postgresql.jdbc.PgResultSet
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.jdbc.Sql
import java.sql.SQLException
import java.util.*
import javax.sql.DataSource

class PetsRepositoryIT : AbstractIntegrationTest() {

    @Autowired
    private lateinit var repository: PetsRepository
    @Autowired
    private lateinit var dataSource: DataSource
    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate
    @Autowired
    private lateinit var executionPlanQuery: ExecutionPlanQuery
    @Autowired
    private lateinit var executionPlanParser: ExecutionPlanParser
    @Autowired
    private lateinit var checkInxAssertService: CheckInxAssertService

    private lateinit var sqlInterceptor: SqlInterceptor

    @Before
    fun setUp() {
        sqlInterceptor = PostgresInterceptor(dataSource as ProxyDataSource)
    }

    @Sql("pets.sql")
    @Test
    fun testFindByNameWithProxyTestDataSourceWithJpa() {
        // ARRANGE
        val ds = ProxyTestDataSource(dataSource)
        (dataSource as ProxyDataSource).addListener(ds.queryExecutionFactoryListener)

        // ACT
        val pets = repository.findByName("Jack")

        // ASSERT
        assertEquals(1, pets.size.toLong())
        val queryExecutions = ds.queryExecutions
        assertNotNull(queryExecutions)
    }

    @Ignore
    @Sql("pets.sql")
    @Test
    fun testFindByNameWhenProxyTestDataSourceWithJdbc() {
        // ARRANGE
        val ds = ProxyTestDataSource(dataSource)

        // ACT
        val list = jdbcTemplate.queryForList("select * from pets")

        // ASSERT
        val query = ds.firstPrepared.query
    }

    @Sql("pets.sql")
    @Test
    fun testFindByNameWhenNoIndex() {
        // ARRANGE
        val name = "Jack"

        // ACT
        sqlInterceptor.startInterception()

        val pets = repository.findByName(name)

        sqlInterceptor.stopInterception()

        // ASSERT
        assertEquals(1, sqlInterceptor.statements.size.toLong())

        val executionPlan = executionPlanQuery.execute(sqlInterceptor.statements[0])
        assertTrue(executionPlan.isNotEmpty())

        val plan = executionPlanParser.parse(executionPlan)
        assertEquals("pets pet0_", plan.table)
        assertEquals("Seq Scan", plan.rootPlanNode.coverage)

        checkInxAssertService.assertCoverage(CoverageLevel.ZERO, "pets pet0_", plan)
    }

    // If you want to get truthful execution plan, generate enough test data
    @Sql("pets.sql") // do it by db dump ...
    @Test
    fun testFindByNameGivenLocationWhenIndexUsingThenCoverageIsHalf() {
        // ARRANGE
        val location = "Moscow"

        // ... or generate test data by code
        IntRange(1, 10000).forEach {
            val pet = Pet()
            pet.id = UUID.randomUUID()
            pet.age = it
            pet.location = "Saint Petersburg"
            pet.name = "Jack-$it"

            repository.save(pet)
        }

        // ACT

        // After all arrangements start interception of sql statements
        sqlInterceptor.startInterception()

        // Your investigation might be here
        val pets = repository.findByLocation(location)

        // After all investigating queries finished stop interception
        sqlInterceptor.stopInterception()

        // ASSERT

        // Here you can check how many queries were executed
        assertEquals(1, sqlInterceptor.statements.size.toLong())

        // If you want something spicy, you can parse raw plan on your own ...
        val executionPlan = executionPlanQuery.execute(sqlInterceptor.statements[0])
        assertTrue(executionPlan.isNotEmpty())

        // ... or travers plan tree ...
        val plan = executionPlanParser.parse(executionPlan)
        assertNotNull(plan)

        val (_, target, coverage) = plan.rootPlanNode.children[0]
        assertEquals("ix_pets_location", target)
        assertEquals("Bitmap Index Scan", coverage)

        // Now assert coverage is simple like never before ...
        checkInxAssertService.assertCoverage(CoverageLevel.HALF, "ix_pets_location", plan)

        // One more thing, it even could be more simple ...
        checkInxAssertService.assertCoverage(CoverageLevel.HALF, "ix_pets_location", sqlInterceptor.statements[0])

        // or if you just want to prevent "seq scan" for example, without searching concrete index
        checkInxAssertService.assertCoverage(CoverageLevel.HALF, sqlInterceptor.statements[0])
    }

    @Sql("pets.sql")
    @Test
    fun testFindByNameWithCheckInxAssertWhenIntIndexedField() {
        // ARRANGE
        val age = 1

        // ACT
        sqlInterceptor.startInterception()

        val pets = repository.findByAge(age)

        sqlInterceptor.stopInterception()

        // ASSERT
        assertEquals(1, sqlInterceptor.statements.size.toLong())

        val executionPlan = executionPlanQuery.execute(sqlInterceptor.statements[0])
        assertTrue(executionPlan.isNotEmpty())

        val plan = executionPlanParser.parse(executionPlan)
        checkInxAssertService.assertCoverage(CoverageLevel.HALF, "ix_pets_age", plan)
    }

    @Sql("pets.sql")
    @Test
    fun testFindByNameGivenAgeWhenIndexedFieldThenAllCoverageHalf() {
        // ARRANGE
        IntRange(1, 10000).forEach {
            val pet = Pet()
            pet.id = UUID.randomUUID()
            pet.age = it
            pet.location = "Moscow"
            pet.name = "Jack-$it"

            repository.save(pet)
        }

        val age = 1

        // ACT
        sqlInterceptor.startInterception()

        val pets = repository.findByAge(age)

        sqlInterceptor.stopInterception()

        // ASSERT
        checkInxAssertService.assertCoverage(CoverageLevel.HALF, sqlInterceptor.statements[0])
    }

    @Ignore
    @Sql("pets.sql")
    @Test
    fun testFindByNameWhenProxyDataSourceHandles() {
        // ARRANGE
        (dataSource as ProxyDataSource).addListener(object : QueryExecutionListener {
            override fun beforeQuery(execInfo: ExecutionInfo, queryInfoList: List<QueryInfo>) {
                // nothing
            }

            override fun afterQuery(execInfo: ExecutionInfo, queryInfoList: List<QueryInfo>) {
                try {
                    //                    System.out.println(((HikariProxyResultSet)execInfo.getResult()).getStatement());

                    val sql =
                        (execInfo.result as HikariProxyResultSet).unwrap(PgResultSet::class.java).statement.toString()
                    //                    final List<Map<String, Object>> result = jdbcTemplate.queryForList("explain " + sql);
                    println(sql)
                } catch (e: SQLException) {
                    e.printStackTrace()
                }

            }
        })

        // ACT
        val pets = repository.findByName("Jack")

        // ASSERT
        assertEquals(1, pets.size.toLong())
    }
}