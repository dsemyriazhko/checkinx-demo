package com.checkinx.utils.sql.plan.query.impl

import com.checkinx.AbstractIntegrationTest
import com.checkinx.demo2.dao.PetsRepository
import com.checkinx.demo2.models.Pet
import org.junit.Assert.*

import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.jdbc.Sql
import java.util.*

class PostgresExecutionPlanQueryIT : AbstractIntegrationTest() {

    @Autowired
    private lateinit var postgresExecutionPlanQuery: PostgresExecutionPlanQuery
    @Autowired
    private lateinit var petsRepository: PetsRepository

    @Test
    fun testExecuteWhenNoIndex() {
        // ARRANGE
        val sqlStatement = "select * from pets where name = 'Nick'"

        // ACT
        val plan = postgresExecutionPlanQuery.execute(sqlStatement)

        // ASSERT
        assertFalse(plan.isEmpty())
    }

    @Test
    fun testExecuteWhenTextIndex() {
        // ARRANGE
        val sqlStatement = "select location from pets where location = 'Moscow'"

        // ACT
        val plan = postgresExecutionPlanQuery.execute(sqlStatement)

        // ASSERT
        assertFalse(plan.isEmpty())
    }

    @Sql("/com/checkinx/demo2/dao/pets.sql")
    @Test
    fun testExecuteGivenManyPetsWhenIntIndexThanIndexOnlyScan() {
        // ARRANGE
        IntRange(1, 10000).forEach {
            val pet = Pet()
            pet.id = UUID.randomUUID()
            pet.age = it
            pet.location = "Moscow"
            pet.name = "Jack-$it"

            petsRepository.save(pet)
        }

        assertEquals(10002, petsRepository.findAll().size)

        val sqlStatement = "select * from pets where age < 10 and name = 'Jack'"

        // ACT
        val plan = postgresExecutionPlanQuery.execute(sqlStatement)

        // ASSERT
        assertFalse(plan.isEmpty())
        assertTrue(plan.get(1).contains("Index Only Scan using ix_pets_age on pets", true))
    }
}