package com.checkinx.utils.sql.plan.parse.models

data class ExecutionPlan(
    val executionPlan: List<String>,
    val table: String?,
    val rootPlanNode: PlanNode
) {
//    fun findTargetInPlanTree(target: String, node: PlanNode): PlanNode {
//        if (node.target == target) {
//            return node
//        }

//        node.children.forEach {
//            findTargetInPlanTree()
//        }
//    }
}
