package com.checkinx.utils.sql.interceptors;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

//@Component
public class TestSqlInterceptor implements SqlInterceptor {
    @NotNull
    @Override
    public List<String> getStatements() {
        return null;
    }

    @Override
    public void startInterception() {

    }
}
