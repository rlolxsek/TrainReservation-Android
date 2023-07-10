package com.sdt.trproject.network

import android.content.Context
import com.sdt.trproject.SharedPrefKeys
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.internal.cookieToString

class AppCookieJar(private val context: Context): CookieJar {
    // SharedPreferences를 사용하기 위해 context 저장
    private val sharedPreferences = context.getSharedPreferences(SharedPrefKeys.PREF_NAME, Context.MODE_PRIVATE)
    // 서버로부터 받은 쿠키를 저장하기 위한 변수
    private var cookies: List<Cookie>? = null

    // 서버 응답으로부터 쿠키를 저장
    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        // 쿠키 값을 세미콜론으로 구분하여 문자열로 변환.
        val cookieValue = cookies.joinToString(";") { cookie -> "${cookie.value}"}

        // SharedPreferences에 쿠키 값을 저장
        val editor = sharedPreferences.edit()
        editor.putString("cookie", cookieValue)
        editor.apply()

        println("cookieValue : ${cookieValue.toString()}")
        println("cookies : ${cookies.joinToString("  /  ")}")
    }

    // 요청을 위해 쿠키 불러오기
    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        // SharedPreferences로부터 쿠키 값을 불러온다.
        val cookieValue = sharedPreferences.getString("cookie", "")

        // 쿠키 값이 없는 경우 빈 리스트를 반환.
        if (cookieValue.isNullOrEmpty()) {
            return emptyList()
        }

        // 쿠키 값을 세미콜론으로 분리하여 각각의 쿠키로 변환.
        val cookieString = cookieValue.split(";")
        return cookieString.mapNotNull { cookieString ->
            // 쿠키 값이 "key=value" 형식인 경우 쿠키를 생성
            val cookieParts = cookieString.split("=")
            if (cookieParts.size == 2) {
                Cookie.Builder()
                    .domain(url.host)
                    .path("/")
                    .name(cookieParts[0].trim())
                    .value(cookieParts[1].trim())
                    .build()
            } else {
                null
            }

        }
    }
}