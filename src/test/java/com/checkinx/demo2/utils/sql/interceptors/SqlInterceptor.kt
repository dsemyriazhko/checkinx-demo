package com.checkinx.demo2.utils.sql.interceptors

interface SqlInterceptor {

    val statements: List<String>

    fun startInterception()

}
