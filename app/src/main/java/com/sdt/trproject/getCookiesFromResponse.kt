package com.sdt.trproject

import okhttp3.Headers
import okhttp3.OkHttpClient
import okhttp3.Request

fun getCookiesFromResponse(response: okhttp3.Response): List<String> {
    val cookies: MutableList<String> = mutableListOf()

    val headers: Headers = response.headers
    val cookieHeaders = headers.values("Set-Cookie")

    for (cookieHeader in cookieHeaders) {
        cookies.add(cookieHeader)
    }

    return cookies
}