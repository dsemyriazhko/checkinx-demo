package com.checkinx.demo2.utils.sql.interceptors.impl

import com.checkinx.demo2.utils.sql.interceptors.SqlInterceptor
import com.zaxxer.hikari.pool.HikariProxyResultSet
import net.ttddyy.dsproxy.ExecutionInfo
import net.ttddyy.dsproxy.QueryInfo
import net.ttddyy.dsproxy.listener.QueryExecutionListener
import net.ttddyy.dsproxy.support.ProxyDataSource
import org.postgresql.jdbc.PgResultSet
import javax.sql.DataSource

open class PostgresInterceptor(private val dataSource: ProxyDataSource) : SqlInterceptor {
    private var statementsList: MutableList<String> = mutableListOf()

    override val statements: List<String>
        get() = statementsList.toList()

    override fun startInterception() {
        dataSource.addListener(object : QueryExecutionListener {
            override fun beforeQuery(execInfo: ExecutionInfo, queryInfoList: List<QueryInfo>) {
                // nothing
            }

            override fun afterQuery(execInfo: ExecutionInfo, queryInfoList: List<QueryInfo>) {
                if (execInfo.result !is HikariProxyResultSet) {
                    return
                }

                val sql = (execInfo.result as HikariProxyResultSet).unwrap<PgResultSet>(PgResultSet::class.java)
                    .statement.toString()

                statementsList.add(sql)
            }
        })
    }

    override fun stopInterception() {
        dataSource.proxyConfig.queryListener.listeners.
    }

//    private fun isDataSourceHasAppropriateType() {
//        if (dataSource !is ProxyDataSource) {
//            throw IllegalDataSource(
//                """DataSource actual type is ${dataSource::class}
//                    |expected ${ProxyDataSource::class}""".trimMargin()
//            )
//        }
//    }
}
