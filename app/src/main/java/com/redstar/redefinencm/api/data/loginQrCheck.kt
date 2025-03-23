package com.redstar.redefinencm.api.data

data class loginQrCheck(
//    {"code":800,"message":"二维码不存在或已过期",
//    "cookie":"NMTID=00OO_t4LVTc-DwICkWVl4U3U7fmICIAAAGVa1RzsQ; Max-Age=315360000; Expires=Sun, 04 Mar 2035 12:00:51 GMT; Path=/;"}
    val code: Int,
    val message: String,
    val cookie: String
)