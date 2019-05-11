package com.checkinx.utils

enum class CoverLevel(val level: Int?) {
    /* Index Only Scan */
    FULL(2),
    /* Index Scan */
    HALF(1),
    /* Seq Scan */
    ZERO(0),
    /* Index isn't using at all. */
    NOT_USING(null)
}
