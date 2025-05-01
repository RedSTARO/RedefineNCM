package com.redstar.redefinencm.server

import fi.iki.elonen.NanoHTTPD

class MyApiServer : NanoHTTPD(8080) {
    override fun serve(session: IHTTPSession): Response {
        return when (session.uri) {

            "/user/account" -> {
                val json = """{
                    "code": 200,
                    "profile": {
                        "userId": 123456,
                        "nickname": "测试用户"
                    }
                }"""
                newFixedLengthResponse(Response.Status.OK, "application/json", json)
            }

            "/user/detail" -> {
                val uid = session.parameters["uid"]?.firstOrNull() ?: "0"
                val json = """{
                    "code": 200,
                    "userId": $uid,
                    "level": 10
                }"""
                newFixedLengthResponse(Response.Status.OK, "application/json", json)
            }

            "/login/status" -> {
                val cookie = session.parameters["cookie"]?.firstOrNull() ?: ""
                val json = """{
                    "code": 200,
                    "data": {
                        "cookie": "$cookie",
                        "loggedIn": true
                    }
                }"""
                newFixedLengthResponse(Response.Status.OK, "application/json", json)
            }


            else -> newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "404 Not Found")
        }
    }
}
