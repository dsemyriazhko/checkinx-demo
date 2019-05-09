package com.checkinx.demo2.utils.sql.plan.query

interface ExecutionPlanQuery {
    fun execute(sqlStatement: String): List<String>
}