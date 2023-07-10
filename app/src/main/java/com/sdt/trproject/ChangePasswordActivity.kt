package com.sdt.trproject

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class ChangePasswordActivity : AppCompatActivity() {

    companion object {
        const val INPUT_USER_NUM = "USER_NUM"
    }

    private lateinit var userNumText: TextView

    private lateinit var newPwText: TextView

    private lateinit var newPwMatchText: TextView

    private lateinit var changedPwBtn: Button

    private lateinit var appbarTitle : TextView
    private lateinit var clearBtn : ImageView

    private val httpClient by lazy { OkHttpClient() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_password)
        userNumText = findViewById(R.id.changedPwUserNumText)

        appbarTitle = findViewById<TextView?>(R.id.appbarTitle).apply {
            setText("비밀번호 찾기")
        }
        clearBtn = findViewById<ImageView?>(R.id.clearBtn).apply {
            setOnClickListener(){
                finish()
            }
        }
        // 값이없으면 이전화면을
        userNumText.text = intent.getStringExtra(INPUT_USER_NUM)

        val UserNum = userNumText.text.toString()

        userNumText.isEnabled = false

        newPwText = findViewById(R.id.changedPwNewPasswordText)


        newPwMatchText = findViewById(R.id.changedPwNewPasswordMatchText)


        changedPwBtn = findViewById(R.id.changedPwBtn)

        changedPwBtn.setOnClickListener() {
            if (newPwText.text.toString() == newPwMatchText.text.toString()) {
                updatePw(UserNum, newPwMatchText.text.toString())

            } else {
                showToast("비밀번호와 확인 비밀번호를 확인해주세요. ")
            }
        }

    }

    @Parcelize
    data class MyData(
        val id: String = "",
        val pw: String = ""

    ) : Parcelable


    private fun updatePw(id: String, pw: String) {
        // 주소 설정
        val url = "${BuildConfig.SERVER_ADDR}/member/updatePassword"
        // json 타입 변환
        val gson = Gson()
        val data = MyData(id, pw)
        val json = gson.toJson(data)
        val mediaType = "application/json".toMediaType()
        val requestBody = json.toRequestBody(mediaType)
        // 요청 보내기
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        httpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // 요청 실패 시 처리
                e.printStackTrace()
                lifecycleScope.launch(Dispatchers.Main) {
                    showToast("비밀번호 변경에 실패하였습니다, IOException ")
                    changedPwBtn.isEnabled = true
                }
            }

            override fun onResponse(call: Call, response: Response) {

                if (!response.isSuccessful) {
                    // 요청 실패 처리
                    Log.d("updatePw" , "Request failed")
                    lifecycleScope.launch(Dispatchers.Main) {
                        showToast("비밀번호 변경에 실패하였습니다, Request Error")
                        changedPwBtn.isEnabled = true
                    }
                    return
                }
                val responseData = response.body?.string()
                // 응답 데이터 처리
                println(responseData)
                lifecycleScope.launch(Dispatchers.Main) {

                    val jsonString = JSONObject(responseData)

                    val responseResult = jsonString.getString("result")

                    when (responseResult) {

                        "failure" -> {
                            showToast("")
                        }

                        "success" -> {

                            showToast("비밀번호 값을 성공적으로 변경했습니다.")
                            val intent =
                                Intent(this@ChangePasswordActivity, LoginActivity::class.java)
                            this@ChangePasswordActivity.startActivity(intent)
                            finish()

                        }

                        else -> {

                            showToast("조건추가")
                            println(id)
                            println(pw)
                            changedPwBtn.isEnabled = true

                        }
                    }
                }
            }
        })
    }

    // 토스트 메시지 출력 함수
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    fun validatePassword(password: String): Boolean {
        // 비밀번호 길이가 8자리 이상인지 확인
        if (password.length < 8) {
            return false
        }

        // 영문자, 숫자, 특수문자가 각각 한 자 이상 포함되어 있는지 확인
        var hasLetter = false
        var hasDigit = false
        var hasSpecialChar = false

        for (char in password) {
            when {
                char.isLetter() -> hasLetter = true
                char.isDigit() -> hasDigit = true
                else -> hasSpecialChar = true
            }
        }

        return hasLetter && hasDigit && hasSpecialChar
    }

}