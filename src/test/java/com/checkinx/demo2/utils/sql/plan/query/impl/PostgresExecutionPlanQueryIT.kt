package com.checkinx.demo2.utils.sql.plan.query.impl

import com.checkinx.AbstractIntegrationTest
import org.junit.Assert.*

import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired

class PostgresExecutionPlanQueryIT : AbstractIntegrationTest() {

    @Autowired
    private lateinit var postgresExecutionPlanQuery: PostgresExecutionPlanQuery

    @Test
    fun testExecute() {
        // ARRANGE
        val sqlStatement = "select * from pets where name = 'Nick'"

        // ACT
        val plan = postgresExecutionPlanQuery.execute(sqlStatement)

        // ASSERT
        assertFalse(plan.isEmpty())
    }
}