package com.sdt.trproject

object SharedPrefKeys {

    // 프레퍼런스 이름
    const val PREF_NAME = "user_prefs.pref"

    // 쿠키 관련
    const val COOKIE = "Cookie"
    const val SET_COOKIE = "Set-Cookie"

    // 로그인 Fragment 관련
    // 공통
    const val SAVED_USER_PW = "savedUserPw"
    // user id 로그인시
    const val USER_ID = "user_id"
    const val SAVED_USER_ID = "savedUserId"
    const val IS_USER_ID_CHECK_BOX_CHECKED = "isUserIdCheckboxChecked"
    const val IS_USER_ID_AUTO_LOGIN = "isUserIdAutoLogin"
    // phone num 로그인시
    const val USER_PHONE = "user_phone"
    const val SAVED_PHONE_NUM = "savedPhoneNum"
    const val IS_PHONE_NUM_CHECK_BOX_CHECKED = "isPhoneNumCheckboxChecked"
    const val IS_PHONE_NUM_AUTO_LOGIN = "isPhoneAutoLogin"
    // email 로그인시
    const val USER_EMAIL = "user_email"
    const val SAVED_Email = "savedEmail"
    const val IS_Email_CHECK_BOX_CHECKED = "isEmailCheckboxChecked"
    const val IS_Email_AUTO_LOGIN = "isPhoneAutoLogin"

    const val DEFAULT_LOGIN_TYPE = "login_fragment"

    // appbar 상단 화면 이름, 발권 카운트
    const val USER_NAME = "user_name"
    const val RESULVATION_CNT = "0"
}