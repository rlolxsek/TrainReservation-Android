package com.sdt.trproject

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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

class SignUpActivity : AppCompatActivity() {
    companion object {
        const val INPUT_EMAIL = "EMAIL"
    }

    private lateinit var appbarTitle : TextView
    private lateinit var clearBtn : ImageView

    // TextView
    private lateinit var input_name: TextView
    private lateinit var input_birth: TextView
    private lateinit var input_email_1: TextView
    private lateinit var input_email_2: TextView
    private lateinit var input_pw: TextView
    private lateinit var input_pw_check: TextView
    private lateinit var inputPhoneNum1: Spinner
    private lateinit var inputPhoneNum2: TextView
    private lateinit var inputPhoneNum3: TextView

    // Button
    private lateinit var Man: Button
    private lateinit var Woman: Button
    private lateinit var checkBox: Button
    private lateinit var phone_num_check: TextView
    private lateinit var register_btn: TextView

    // phone_num_check 됬을때 지정된 전화번호를 담는 인스턴스
    var savedPhoneNum: String = ""


    private val httpClient by lazy { OkHttpClient() }

    @Parcelize
    data class MyData(
        val email: String,
        val phone: String,
        val pw: String,
        val name: String,
        val gender: String,
        val birth: String
    ) : Parcelable

    @Parcelize
    data class MyPhoneData(
        val phone: String,
    ) : Parcelable


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        appbarTitle = findViewById<TextView?>(R.id.appbarTitle).apply {
            setText("회원가입")
        }
        clearBtn = findViewById<ImageView?>(R.id.clearBtn).apply {
            setOnClickListener(){
                finish()
            }
        }


        input_name = findViewById(R.id.input_name)
        input_birth = findViewById(R.id.input_birth)
        input_email_1 = findViewById(R.id.input_email_1)
        input_email_2 = findViewById(R.id.input_email_2)
        input_pw = findViewById(R.id.input_pw)
        input_pw_check = findViewById(R.id.input_pw_check)
        inputPhoneNum1 = findViewById(R.id.inputPhoneNum1)
        inputPhoneNum2 = findViewById(R.id.inputPhoneNum2)
        inputPhoneNum3 = findViewById(R.id.inputPhoneNum3)

        Man = findViewById(R.id.Man)
        Woman = findViewById(R.id.Woman)

        var gender: String = " "
        phone_num_check = findViewById(R.id.phone_num_btn)
        register_btn = findViewById(R.id.register_btn)

        // 시작시 회원 등록버튼을 비활성화 시킴
        register_btn.isEnabled = false

        Man.setOnClickListener() {
            gender = "남"
        }
        Woman.setOnClickListener() {
            gender = "여"
        }

        val email = intent.getStringExtra(INPUT_EMAIL)
        val splitEmail = (email ?: "").split("@")
        val fEmail = splitEmail[0]
        val sEmail = splitEmail[1]

        input_email_1.text = fEmail
        input_email_2.text = sEmail


        val items = arrayOf("선택", "010", "011")
        val adapter =
            ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, items)
        inputPhoneNum1.adapter = adapter




        phone_num_check.setOnClickListener() {

            val phoneNum1 = inputPhoneNum1.selectedItem.toString()
            val phoneNum2 = inputPhoneNum2.text.toString()
            val phoneNum3 = inputPhoneNum3.text.toString()

            // phoneNum 010 일때 011 일때
            val phoneNumberPattern = "^01[016789]-\\d{3,4}-\\d{4}$".toRegex()
            var phoneNum = ""

            when (phoneNum1) {
                "선택" -> showToast("휴대폰 번호의 앞지리를 선택해주세요.")
                "010" -> {
                    if (phoneNum2.length != 4 || phoneNum3.length != 4) {
                        showToast("전화번호를 확인해주세요.")
                    } else {
                        phoneNum = phoneNum1 + "-" + phoneNum2 + "-" + phoneNum3
                    }
                }

                "011" -> {
                    if (phoneNum2.length != 3 || phoneNum3.length != 4) {
                        showToast("전화번호를 확인해주세요.")
                    } else {
                        phoneNum = phoneNum1 + "-" + phoneNum2 + "-" + phoneNum3
                    }
                }
            }
            when (phoneNum) {
                "" -> {
                    println("phone 값이 비어 있습니다.")
                }

                else -> {
                    // 중복조회 버튼 눌렀을때 savedPhoneNum 에 전화번호 저장
                    savedPhoneNum = phoneNum
                    isDuplicationPhone(phoneNum)
                }
            }

        }

        register_btn.setOnClickListener() {

            var emailStr = input_email_1.text.toString() + "@" + input_email_2.text.toString()
            var phoneStr = savedPhoneNum
            var pwStr = input_pw.text.toString()
            var nameStr = input_name.text.toString()
            var genderStr = gender
            var birthStr = input_birth.text.toString()


            if (input_birth.text.toString().length != 8) {

                showToast("생년월일이 8자리 인지 확인해 주세요")

            } else if (gender == " ") {

                showToast("성별을 선택해 주세요")

            } else if (validatePassword(input_pw.text.toString()) != true) {

                showToast("양식에 맞는 비밀번호인지 확인해주세요.")

            } else if (input_pw.text.toString() != input_pw_check.text.toString()) {

                showToast("비밀번호 확인이 올바른지 확인해주세요")

            } else {
                // 차후 확인 눌렀을시 한번더 전화번호 중복체크 할 수 있도록 수정할 예정 ~~~~~~~~~~~~~
                // DB 로 넣는 로직 추가~
                // 조건 만족 못했을시 로직추가해야함.~~!!!!!!!!!!!!!!!!!!!!!!!
                sendUser(emailStr, phoneStr, pwStr, nameStr, gender, birthStr)



            }
            println("email : $emailStr")
            println("phone : $phoneStr")
            println("password : $pwStr")
            println("check : ${input_pw_check.text.toString()}")
            println("name : $nameStr")
            println("gender : $genderStr")
            println("birth : $birthStr")
        }
    }

    // 휴대폰 번호 중복조회
    private fun isDuplicationPhone(phone: String) {

        val url = "${BuildConfig.SERVER_ADDR}/member/isDuplicated/phone"
        val gson = Gson()
        val data = MyPhoneData(phone)
//        val json2 = gson.toJson("{\"id\":\"${ data.toString() }\"}")
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

                    showToast(" 전화번호 전송에 실패하였습니다. , IOException ")
                    Log.d("isDuplication/phone", "IOException")

                }

            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    // 요청 실패 처리
                    println("Request failed")
                    lifecycleScope.launch(Dispatchers.Main) {
                        showToast(" 전화번호 전송에 실패하였습니다. , ResponseError ")
                        Log.d("isDuplication/phone", "ResponseError")
                    }
                    return
                }

                val responseData = response.body?.string()

                // 응답 데이터 처리
                println(responseData)

                lifecycleScope.launch(Dispatchers.Main) {

                    showToast("isDuplicationPhone 통신 성공.")

                    val jsonString = JSONObject(responseData)
                    val responseResult = jsonString.getString("result")


                    when (responseResult) {

                        "failure" -> {
                            val responseMessage = jsonString.getString("message")


                            when (responseMessage) {

                                "input_error" -> { Log.d("isDuplicationPhone", " responseMessage : ${responseMessage} ")
                                    showToast("전화 번호를 확인해 주세요.")}

                                "phone_duplicated" -> { Log.d("isDuplicationPhone", " responseMessage : ${responseMessage} ")
                                    showToast("동일한 사용자 가 존재합니다.")}

                                else -> { Log.d("isDuplicationPhone", " responseMessage : ${responseMessage} ")
                                    showToast("사용 불가능한 전화번호입니다.")}
                            }
                        }
                        "success" -> {
                            showToast("사용 가능한 전화번호 입니다.")
                            Log.d("/isDuplication/phone", "responseResult : ${responseResult}")
                            register_btn.isEnabled = true
                        }

                        else -> {
                            showToast("알 수 없는 오류")
                            Log.d("/isDuplication/phone", "${responseResult}")

                        }
                    }
                }
            }
        })
    }

    private fun sendUser(
        email: String,
        phone: String,
        pw: String,
        name: String,
        gender: String,
        birth: String
    ) {


        // 요청을 보낼 URL 설정

        val url = "${BuildConfig.SERVER_ADDR}/member/register"
        val gson = Gson()
        val data = MyData(email, phone, pw, name, gender, birth)
        val json = gson.toJson(data)
        val mediaType = "application/json".toMediaType()
        val requestBody = json.toRequestBody(mediaType)

        // 요청을 담을 파라미터 설정
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        httpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // 요청 실패 시 처리
                e.printStackTrace()
                lifecycleScope.launch(Dispatchers.Main) {
                    showToast("개인정보 값 전송에 실패하였습니다.")
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    // 요청 실패 처리
                    lifecycleScope.launch(Dispatchers.Main) {
                        showToast("개인정보 값 전송에 실패하였습니다.")
                        println("Request failed")
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
                            val responsMessage = jsonString.getString("message")
                            Log.d("responsMessage", responsMessage)
                        }
                        "success" -> {
                            showToast("개인정보 값을 성공적으로 전송 했습니다.")
                            showToast("회원가입이 완료 되었습니다")
                            val intent = Intent(this@SignUpActivity, MainActivity::class.java)
                            startActivity(intent)

                            println(request.toString())
                            finish()
                        }
                        else -> showToast("코드오류")

                    }

                }
            }
        })
    }

    // 토스트 메시지 출력 함수수
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



