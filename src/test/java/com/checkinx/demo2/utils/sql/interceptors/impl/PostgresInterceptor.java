package com.checkinx.demo2.utils.sql.interceptors.impl;

import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.postgresql.jdbc.PgResultSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.checkinx.demo2.utils.sql.interceptors.SqlInterceptor;
import com.zaxxer.hikari.pool.HikariProxyResultSet;
import net.ttddyy.dsproxy.ExecutionInfo;
import net.ttddyy.dsproxy.QueryInfo;
import net.ttddyy.dsproxy.listener.QueryExecutionListener;
import net.ttddyy.dsproxy.support.ProxyDataSource;

@RequiredArgsConstructor
@Component
public class PostgresInterceptor implements SqlInterceptor {
    private final DataSource dataSource;

    @Override
    public void startInterception() {
        ((ProxyDataSource) dataSource).addListener(new QueryExecutionListener() {
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
    }

    @Override
    public void stopInterception() {

    }

    @Override
    public List<String> getStatements() {
        return null;
    }
}
