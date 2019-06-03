package com.checkinx.demo2.dao

import com.checkinx.AbstractDbTest
import com.checkinx.utils.asserts.CheckInxAssertService
import com.checkinx.utils.asserts.CoverageLevel
import com.checkinx.utils.sql.interceptors.SqlInterceptor
import com.checkinx.utils.sql.plan.parse.ExecutionPlanParser
import com.checkinx.utils.sql.plan.query.ExecutionPlanQuery
import net.ttddyy.dsproxy.asserts.ProxyTestDataSource
import net.ttddyy.dsproxy.support.ProxyDataSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.jdbc.Sql
import org.testng.AssertJUnit.assertEquals
import org.testng.AssertJUnit.assertNotNull
import org.testng.AssertJUnit.assertTrue
import org.testng.annotations.Ignore
import org.testng.annotations.Test
import javax.sql.DataSource

class PetsRepositoryDbIT : AbstractDbTest() {

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
    @Autowired
    private lateinit var sqlInterceptor: SqlInterceptor

    @Sql("pets.sql")
    @Test
    fun testFindByNameWithProxyTestDataSourceWithJpa() {
        // ARRANGE
        val ds = ProxyTestDataSource(dataSource)
        (dataSource as ProxyDataSource).addListener(ds.queryExecutionFactoryListener)

        val name = "Jack-1"

        // ACT
        val pets = repository.findByName(name)

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
        assertEquals("pets pet0_", plan.rootPlanNode.target)
        assertEquals("Seq Scan", plan.rootPlanNode.coverage)

        checkInxAssertService.assertCoverage(CoverageLevel.ZERO, "pets pet0_", plan)
    }

    // If you want to get truthful execution plan, generate enough test data
    @Sql("pets.sql") // do it by db dump ...
    @Test
    fun findByLocation() {
        // ARRANGE
        val location = "NY"

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

        val rootNode = plan.rootPlanNode
        assertEquals("Index Scan", rootNode.coverage)
        assertEquals("ix_pets_location", rootNode.target)
        assertEquals("pets pet0_", rootNode.table)

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
        val age = 1

        // ACT
        sqlInterceptor.startInterception()

        val pets = repository.findByAge(age)

        sqlInterceptor.stopInterception()

        // ASSERT
        checkInxAssertService.assertCoverage(CoverageLevel.HALF, sqlInterceptor.statements[0])
    }
}