package com.sdt.trproject

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
import java.util.Timer

class FindUserIdSendMailActivity : AppCompatActivity() {

    private lateinit var appbarTitle : TextView
    private lateinit var clearBtn : ImageView

    // 비밀번호 변수
    var token: String? = null

    private lateinit var inputEmail3: TextView
    private lateinit var emailAutoComplete: AutoCompleteTextView

    private lateinit var userFindTokenSendBtn: TextView
    private lateinit var timerTextView: TextView

    //토큰 인증 관련 변수
    private lateinit var userFindTokenText: TextView
    private lateinit var userFindTokenMatchBtn: TextView

    // 타이머 관련 변수
    private var timer: Timer? = null
    private var remainingSeconds = 90

    // OkHttpClient 인스턴스 생성
    private val httpClient by lazy { OkHttpClient() }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_find_user_id_send_mail)

        appbarTitle = findViewById<TextView?>(R.id.appbarTitle).apply {
            setText("회원번호 찾기")
        }
        clearBtn = findViewById<ImageView?>(R.id.clearBtn).apply {
            setOnClickListener(){
                finish()
            }
        }


        inputEmail3 = findViewById(R.id.inputEmail3)

        emailAutoComplete = findViewById(R.id.emailAutoComplete)
        val emailItems = arrayOf("gmail.com", "naver.com", "daum.net")

//        val adapter = ArrayAdapter(this,android.R.layout.simple_spinner_item,emailItems)
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//        email_auto_complete.adapter = adapter

        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, emailItems)
        emailAutoComplete.setAdapter(adapter)

        emailAutoComplete.setOnItemClickListener() { parent, view, position, id ->
            val selectedEmailItem = parent.getItemAtPosition(position).toString()
            showToast("Selected : $selectedEmailItem")
        }

        userFindTokenSendBtn = findViewById(R.id.userFindTokenSendBtn)

        timerTextView = findViewById(R.id.timerTextView)


        var emailSave: String = ""

        userFindTokenSendBtn.setOnClickListener() {

            val emailJoin: String =
                inputEmail3.text.toString() + "@" + emailAutoComplete.text.toString()
            emailSave = emailJoin
            // 'Send' 버튼을 비활성화하여 중복 클릭 방지
            userFindTokenSendBtn.isEnabled = false

            // 수신자 이메일 주소 가져오기
            println(emailJoin)
            isDuplicationEmail(emailJoin)

        }

        userFindTokenText = findViewById(R.id.userFindTokenText)
        userFindTokenMatchBtn = findViewById(R.id.userFindTokenMatchBtn)

        // 이메일 코드 매칭
        userFindTokenMatchBtn.setOnClickListener() {
            Log.d("matchEmailCode 저장 함수", " ${emailSave},${userFindTokenText.text.toString()}")
            matchEmailCode(emailSave, userFindTokenText.text.toString())


        }

    }

    @Parcelize
    data class MyEmail(
        val email: String = ""
    ) : Parcelable

    @Parcelize
    data class AuthCode(
        val email: String = "",
        val authCode: String = ""
    ) : Parcelable


    private fun isDuplicationEmail(email: String) {
        // 주소 설정
        val url = "${BuildConfig.SERVER_ADDR}/member/isDuplicated/email"
        // json 타입 변환
        val gson = Gson()
        val data = MyEmail(email)
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
                    showToast("이메일 값 전송에 실패하였습니다,io엑셉션 ")
                    userFindTokenSendBtn.isEnabled = true
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    // 요청 실패 처리
                    println("Request failed")
                    lifecycleScope.launch(Dispatchers.Main) {
                        showToast("이메일 값 전송에 실패하였습니다, 리퀘스트 요청 실패")
                        println(email)
                        userFindTokenSendBtn.isEnabled = true
                    }
                    return
                }

                val responseData = response.body?.string()
                // 응답 데이터 처리
                println(responseData)

                lifecycleScope.launch(Dispatchers.Main) {

                    val jsonString = JSONObject(responseData)
                    val responseResult = jsonString.getString("result")

                    //SingUpActivity 로 넘기기

                    when (responseResult) {

                        "success" -> showToast("이메일과 일치하는 회원이 없습니다.")

                        "failure" -> {
                            userFindSendEmail(email)
                            showToast("이메일 값을 성공적으로 전송했습니다.")
                        }

                        else -> showToast("알 수 없는 오류")

                    }

                    userFindTokenSendBtn.isEnabled = true

                }
            }
        })
    }




    private fun userFindSendEmail(email: String) {
        // 이메일 요청 구현
        // 요청을 보낼 URL 설정
        val url = "${BuildConfig.SERVER_ADDR}/member/emailSend"
        //요청을 담을 파라미터 설정
//        val requestBody = FormBody.Builder()
//            .add("email",email).build()
        val gson = Gson()
        val data = MyEmail(email)
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
                    // 화면에 표시할 게 없으면 IO
                    // 화면에 표시할게 있으면 Main
                    showToast("이메일 전송에 실패하였습니다.")
                    // 현 상황은 IO로 돌리다가 표시할 게 있어서 Main 으로 전환 했다가 다시 IO로 돌아오는 상황
                    userFindTokenSendBtn.isEnabled = true
                }

            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    //요청 실패 처리
                    println("Request failed")
                    lifecycleScope.launch(Dispatchers.Main) {
                        showToast("이메일 전송에 실패하였습니다.")
                    }
                    return
                }

                val responseData = response.body?.string()
                // 응답 데이터 처리
                println(responseData)

                val jsonString = JSONObject(responseData)

                val responseResult = jsonString.getString("result")

                lifecycleScope.launch(Dispatchers.Main) {
                    showToast("이메일 전송에 성공.")

                    if (responseResult == "success") {
                        if (remainingSeconds != 0 or 90) {
                            remainingSeconds = 90
                            timerTextView.text = remainingSeconds.toString()
                            timer = null

                        }
                        startTimer()

                    } else {
                        showToast("코드오류")
                    }

                    userFindTokenSendBtn.isEnabled = response.isSuccessful
                }

            }
        })

    }

    private fun matchEmailCode(email: String, authCode: String) {
        // 요청을 보낼 URL 설정
        val url = "${BuildConfig.SERVER_ADDR}/member/matchEmailCode/showMemberId"

        val gson = Gson()
        val data = AuthCode(email, authCode)
        val json = gson.toJson(data)
        val mediaType = "application/json".toMediaType()
        val requestBody = json.toRequestBody(mediaType)

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        Log.d("matchEmailCode", "matchEmailCode 함수 시작")

        httpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // 요청 실패 시 처리
                e.printStackTrace()

                lifecycleScope.launch(Dispatchers.Main) {
                    showToast("이메일 및 토큰 값 전송에 실패하였습니다.")

                }
            }

            override fun onResponse(call: Call, response: Response) {

                if (!response.isSuccessful) {
                    // 요청 실패 처리
                    println("Request failed")

                    lifecycleScope.launch(Dispatchers.Main) {

                        showToast("이메일 및 토큰 값 전송에 실패하였습니다.")

                    }

                    return

                }

                val responseData = response.body?.string()
                // 응답 데이터 처리
                println(responseData)

                val JsonString = JSONObject(responseData)
                val responseResult = JsonString.getString("result")

                lifecycleScope.launch(Dispatchers.Main) {

                    when (timerTextView.text) {
                        "" -> showToast("이메일 인증을 해주세요")
                        // SingUpActivity 로 넘기기

                        else ->

                            when (responseResult) {


                                "failure" -> {

                                    val responseMessage = JsonString.getString("message")

                                    when (responseMessage) {

                                        "auth_code_expired" -> { showToast("인증번호가 만료되었습니다.") }

                                        else -> { showToast("이메일, 인증번호를 확인해주세요") }

                                    }
                                }

                                "success" -> {

                                    showToast("인증에 성공했습니다.")

                                    val userId = JsonString.getString("data")

                                    Log.d("UserID", userId)
//                                    matchEmailCodeAndGiveId(email, authCode)

                                    val intent = Intent(
                                        this@FindUserIdSendMailActivity,
                                        FindUserIdResultPage::class.java
                                    )

                                    intent.putExtra(FindUserIdResultPage.INPUT_USER_ID, userId)
                                    startActivity(intent)
                                    stopTimer()
                                    timerTextView.text = ""
                                    finish()
                                }

                                else -> {
                                    showToast("코드오류")
                                    println(responseResult)

                                }
                            }
                    }
                }
            }
        })
    }


    // 타이머 시작 함수
    fun startTimer() {
        // 이미 타이머가 실행 중인 경우 중복 실행 방지
        if (timer != null) {
            return
        }
        stopTimer()

        remainingSeconds = 90 // 초기화
        timer = Timer()
        lifecycleScope.launch(Dispatchers.IO) {
            remainingSeconds = 90
            while (true) {
                delay(1_000)

                withContext(Dispatchers.Main) {
                    // 남은 시간을 표시하고 1초씩 감소
                    timerTextView.text = remainingSeconds.toString()
                    remainingSeconds--


                    // 시간이 만료되면 타이머 중지, 비밀번호 초기화
                    if (remainingSeconds < 0) {
                        stopTimer()
                        token = null
                        showToast("인증번호가 만료되었습니다.")
                        userFindTokenText.text = ""
                        timerTextView.text = ""
                    }
                }
            }
        }
    }

    // 타이머 정지 함수
    private fun stopTimer() {
        lifecycleScope.coroutineContext.cancelChildren()
    }


    private fun showToast(message: String) {
        Toast.makeText(this@FindUserIdSendMailActivity, message, Toast.LENGTH_SHORT).show()
    }

}