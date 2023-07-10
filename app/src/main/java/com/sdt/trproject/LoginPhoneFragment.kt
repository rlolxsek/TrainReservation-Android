package com.sdt.trproject

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.sdt.trproject.databinding.FragmentLoginPhoneBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import okhttp3.Call
import okhttp3.Callback
import okhttp3.JavaNetCookieJar
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.lang.StringBuilder
import java.net.CookieManager

class LoginPhoneFragment : Fragment() {

    lateinit var loginPhoneBinding: FragmentLoginPhoneBinding

    private lateinit var userPhone: TextView
    private lateinit var userPw: TextView

    private lateinit var savePhoneBtn: CheckBox
    private lateinit var autoLoginBtn: CheckBox
    private lateinit var loginBtn: Button

    private lateinit var editor: SharedPreferences.Editor

    // OkHttpClient 인스턴스 생성
    private val httpClient by lazy {
        OkHttpClient().newBuilder().apply {
            cookieJar(JavaNetCookieJar(CookieManager()))
        }.build()
    }


    // SharedPreferences 인스턴스 생성
    private val sharedPreferences by lazy {
        requireActivity().getSharedPreferences(SharedPrefKeys.PREF_NAME, Context.MODE_PRIVATE)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        editor = sharedPreferences.edit()

        loginPhoneBinding = FragmentLoginPhoneBinding.inflate(inflater, container, false)

        // View 요소 초기화
        userPhone = loginPhoneBinding.root.findViewById(R.id.userNumId)
        userPw = loginPhoneBinding.root.findViewById(R.id.userPw)


        savePhoneBtn = loginPhoneBinding.root.findViewById(R.id.saveUserNumBtn)
        autoLoginBtn = loginPhoneBinding.root.findViewById(R.id.autoLoginBtn)
        loginBtn = loginPhoneBinding.root.findViewById(R.id.loginBtn)

        userPhone.text = sharedPreferences.getString(SharedPrefKeys.SAVED_PHONE_NUM, "")

        savePhoneBtn.isChecked =
            sharedPreferences.getBoolean(SharedPrefKeys.IS_PHONE_NUM_CHECK_BOX_CHECKED, false)
        autoLoginBtn.isChecked =
            sharedPreferences.getBoolean(SharedPrefKeys.IS_PHONE_NUM_AUTO_LOGIN, false)

        // 자동 입력 로직
        if (sharedPreferences.getBoolean(SharedPrefKeys.IS_PHONE_NUM_CHECK_BOX_CHECKED, false)) {
            val savedUserId = sharedPreferences.getString(SharedPrefKeys.USER_PHONE, "")
            if (!savedUserId.isNullOrEmpty()) {
                userPhone.text = savedUserId
            }
        }

        // 자동 로그인 로직
        if (autoLoginBtn.isChecked) {
            val savedEmail = sharedPreferences.getString(SharedPrefKeys.SAVED_PHONE_NUM, "")
            val savedUserPw = sharedPreferences.getString(SharedPrefKeys.SAVED_USER_PW, "")

            if (!savedEmail.isNullOrEmpty() && !savedUserPw.isNullOrEmpty()) {
                sendCredentials(savedEmail, savedUserPw)
            }
        }

// 체크박스 클릭 시, 상태를 저장하고 member_get 텍스트를 저장 또는 삭제
        savePhoneBtn.setOnCheckedChangeListener { _, isChecked ->
            // 체크박스 상태 저장
            editor.putBoolean(SharedPrefKeys.IS_PHONE_NUM_CHECK_BOX_CHECKED, true)
            // 체크가 되어 있는 경우
            if (isChecked) {
                showToast("사용자 전화번호 저장을 활성화하였습니다!")
            } else {
                editor.remove(SharedPrefKeys.IS_PHONE_NUM_CHECK_BOX_CHECKED)
                showToast("사용자 전화번호 저장을 비활성화하였습니다!")
            }
        }

// 자동 로그인 체크박스 클릭 시 상태 저장
        autoLoginBtn.setOnCheckedChangeListener { _, isChecked ->

            editor.putBoolean(SharedPrefKeys.IS_PHONE_NUM_AUTO_LOGIN, true)


            if (!isChecked) {
                editor.remove(SharedPrefKeys.SAVED_PHONE_NUM)
                editor.remove(SharedPrefKeys.SAVED_USER_PW)
            }
        }


        // 로그인 버튼 클릭 시 이벤트 처리
        loginBtn.setOnClickListener {

            val phoneNumToString = splitPhoneNum(userPhone.text.toString())
            val userPwToString = userPw.text.toString()
            // 회원번호 저장 체크박스가 체크된 경우
            if (savePhoneBtn.isChecked) {
                // 사용자 번호 저장
                editor.putString(SharedPrefKeys.USER_PHONE, userPhone.text.toString())
                editor.putString(SharedPrefKeys.DEFAULT_LOGIN_TYPE, "login_phone_fragment")
            } else {

                editor.remove(SharedPrefKeys.USER_PHONE)

            }

            if (autoLoginBtn.isChecked) { // 자동 로그인 체크박스가 체크된 경우
                // 사용자 ID, PW 저장

                editor.putString(SharedPrefKeys.SAVED_PHONE_NUM, userPhone.text.toString())
                editor.putString(SharedPrefKeys.SAVED_USER_PW, userPwToString)

            } else {

                editor.remove(SharedPrefKeys.SAVED_PHONE_NUM)
                editor.remove(SharedPrefKeys.SAVED_USER_PW)

            }

            sendCredentials(phoneNumToString, userPwToString)
        }

        return loginPhoneBinding.root
    }

    // 토스트 메시지 출력 함수
    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    @Parcelize
    data class MyData(
        val phone: String = "",
        val pw: String = ""

    ) : Parcelable

    private fun sendCredentials(phone: String, pw: String) {
        // 요청을 보낼 URL 설정
        val url = "${BuildConfig.SERVER_ADDR}/member/login"

        val gson = Gson()
        val data = MyData(phone, pw)
        val json = gson.toJson(data)
        val mediaType = "application/json".toMediaType()
        val requestBody = json.toRequestBody(mediaType)

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        httpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // 요청 실패 시 처리
                e.printStackTrace()
                lifecycleScope.launch(Dispatchers.Main) {
                    showToast("전화번호 및 비밀번호 값 전송에 실패하였습니다.")
                }
            }

            override fun onResponse(call: Call, response: Response) {

                if (!response.isSuccessful) {

                    // 요청 실패 처리
                    lifecycleScope.launch(Dispatchers.Main) {
                        showToast("전화번호 및 비밀번호 값 전송에 실패하였습니다.")

                    }
                    return
                }

                val responseData = response.body?.string()
                // 응답 데이터 처리
                lifecycleScope.launch(Dispatchers.Main) {

                    val jsonString = JSONObject(responseData)

                    val responseResult = jsonString.getString("result")


                    // SingUpActivity 로 넘기기
                    // key : JSESSIONID
                    when (responseResult) {

                        "success" -> {
                            val responseUserData = jsonString.getString("data")
                            val dataJsonObject = JSONObject(responseUserData)
                            val responseUserName = dataJsonObject.getString("memberName")
                            val responseReservationCnt = dataJsonObject.getString("reservationCnt")


                            Log.d(">>>>DataName<<<<<", "$responseUserName")
                            Log.d(">>>>DataName<<<<<", "$responseReservationCnt")


                            val cookieHeaderValue =
                                response.header(SharedPrefKeys.SET_COOKIE)?.substringBefore(";")

                            editor.putString(SharedPrefKeys.SET_COOKIE, cookieHeaderValue)
                            editor.putString(SharedPrefKeys.USER_NAME, responseUserName)
                            editor.putString(SharedPrefKeys.RESULVATION_CNT, responseReservationCnt)

                            val activity = requireActivity()
                            val intent = Intent(activity, MainActivity::class.java)
                            //intent.putExtra(MainActivity.INPUT_COOKIE,cookies.toString())
                            editor.apply()
                            startActivity(intent)
                            activity.finish()
                        }

                        "failure" -> {
                            val messageResult: String? = jsonString.getString("message")

                            if (messageResult.equals("input_error")) showToast("전화번호가 존재하지 않습니다.")
                            if (messageResult.equals("password_no_match")) showToast("비밀번호를 확인해주세요.")
                            if (messageResult.equals("id_no_match")) showToast("전화번호가 존재하지 않습니다.")
                            Log.d(">>>>DataName<<<<<", "$phone")
                            Log.d(">>>>DataName<<<<<", "$pw")
                            Log.d("message", "message: $messageResult")
                        }
                    }
                }
            }
        })
    }
    private fun splitPhoneNum ( input : String) : String {
        val phoneNum = StringBuilder(input)
        when (phoneNum.length) {

            10 -> {
                phoneNum.insert(3,"-")
                phoneNum.insert(7,"-")
                return phoneNum.toString()
            }

            11 -> {
                phoneNum.insert(3,"-")
                phoneNum.insert(8,"-")
                return phoneNum.toString()
            }
            else -> {

                showToast("전화번호를 확인해주세요.")
                return ""
            }
        }
    }
    private fun removeHyphens(input : String) : String {
        return input.replace("-","")
    }


}
