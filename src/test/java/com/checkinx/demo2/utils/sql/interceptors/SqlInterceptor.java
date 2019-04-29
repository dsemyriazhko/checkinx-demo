package com.checkinx.demo2.utils.sql.interceptors;

import java.util.List;

public interface SqlInterceptor {

    void startInterception();

    void stopInterception();

    List<String> getStatements();
}
