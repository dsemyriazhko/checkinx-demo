package com.checkinx.utils.sql.plan.parse.models

data class PlanNode(
    val raw: String,
    var target: String?,
    var coverage: String?,
    val children: MutableList<PlanNode> = mutableListOf(),
    val properties: MutableList<Pair<String, String>> = mutableListOf(),
    val others: MutableList<String> = mutableListOf()
)
