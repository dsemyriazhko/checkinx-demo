package com.checkinx.utils.sql.interceptors.impl

import com.checkinx.utils.sql.interceptors.SqlInterceptor
import com.zaxxer.hikari.pool.HikariProxyResultSet
import net.ttddyy.dsproxy.ExecutionInfo
import net.ttddyy.dsproxy.QueryInfo
import net.ttddyy.dsproxy.listener.QueryExecutionListener
import net.ttddyy.dsproxy.support.ProxyDataSource
import org.postgresql.jdbc.PgResultSet
import org.springframework.stereotype.Component
import java.sql.SQLException
import javax.sql.DataSource

@Component
open class PostgresInterceptor(private val dataSource: DataSource) : SqlInterceptor {

    private var statementsList: MutableList<String> = mutableListOf()

    override val statements: List<String>
        get() = statementsList.toList()

    override fun startInterception() {
        (dataSource as ProxyDataSource).addListener(object : QueryExecutionListener {
            override fun beforeQuery(execInfo: ExecutionInfo, queryInfoList: List<QueryInfo>) {
                // nothing
            }

            override fun afterQuery(execInfo: ExecutionInfo, queryInfoList: List<QueryInfo>) {
                try {
                    val sql = (execInfo.result as HikariProxyResultSet).unwrap<PgResultSet>(PgResultSet::class.java!!)
                        .statement.toString()
                    statementsList.add(sql)
                } catch (e: SQLException) {
                    e.printStackTrace()
                }
            }
        })
    }

}
