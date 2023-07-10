package com.sdt.trproject

import android.content.Intent
import android.nfc.Tag
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
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



class SignUpVerificationActivity : AppCompatActivity() {


    private lateinit var appbarTitle : TextView
    private lateinit var clearBtn : ImageView

    // 비밀번호 변수
    var token: String? = null

    // View 요소 선언
    private lateinit var sendInputEmailName: TextView
    private lateinit var sendInputEmailAddr: TextView
    private lateinit var sendButton: TextView
    private lateinit var matchButton: TextView
    private lateinit var tokenText: TextView
    private lateinit var timerTextView: TextView


    // 타이머 관련 변수
    private var timer: Timer? = null
    private var remainingSeconds = 90

    // OkHttpClient 인스턴스 생성
    private val httpClient by lazy { OkHttpClient() }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up_verification)

        appbarTitle = findViewById<TextView?>(R.id.appbarTitle).apply {
            setText("회원가입")
        }
        clearBtn = findViewById<ImageView?>(R.id.clearBtn).apply {
            setOnClickListener(){
                finish()
            }
        }

        // View 요소 초기화
        sendInputEmailName = findViewById(R.id.sendInputEmailName)
        sendInputEmailAddr = findViewById(R.id.sendInputEmailAddr)

        var emailEditText = ""

        // 미리 정의된 이메일 주소 설정
        sendInputEmailName.setText("")
        sendInputEmailAddr.setText("")

        sendButton = findViewById(R.id.sendButton)
        // 'Send' 버튼을 비활성화하여 중복 클릭 방지

        // 'Send' 버튼이 클릭될 때 실행되는 리스너
        sendButton.setOnClickListener {

            emailEditText =
                (sendInputEmailName.text.toString() + "@" + sendInputEmailAddr.text.toString())
            println(emailEditText)

            sendButton.isEnabled = false
            // 수신자 이메일 주소 가져오기
            println(emailEditText)
            isDuplicationEmail(emailEditText)


        }

        // View 요소 초기화
        tokenText = findViewById(R.id.tokenText)
        matchButton = findViewById(R.id.matchButton)
        timerTextView = findViewById(R.id.timerTextView)

        // 'Match' 버튼이 클릭될 때 실행되는 리스너
        matchButton.setOnClickListener() {
            matchButton.isEnabled = false

            matchEmailCode(emailEditText, tokenText.text.toString())
            println(emailEditText)
            println(tokenText.text.toString())

            matchButton.isEnabled = true
        }

    }


    // 토스트 메시지 출력 함수
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    @Parcelize
    data class MyData(
        val email: String = "",
        val authCode: String = ""
    ) : Parcelable



    private fun sendEmail(email: String) {
        // 이메일 요청 구현
        // 요청을 보낼 URL 설정
        val url = "${BuildConfig.SERVER_ADDR}/member/emailSend"
        //요청을 담을 파라미터 설정
        //val requestBody = FormBody.Builder()
        //.add("email",email).build()

        val gson = Gson()
        val data = MyData(email)
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
                    sendButton.isEnabled = true
                }

            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    //요청 실패 처리
                    println("Request failed")
                    lifecycleScope.launch(Dispatchers.Main) {
                        showToast("이메일 전송에 실패하였습니다.")
                        sendButton.isEnabled = true
                    }
                    return
                }

                val responseData = response.body?.string()
                // 응답 데이터 처리


                println(responseData)

                lifecycleScope.launch(Dispatchers.Main) {

                    showToast("이메일 전송에 성공.")

                    lifecycleScope.launch(Dispatchers.IO) {
                        if (remainingSeconds != 0 or 90) {
                            remainingSeconds = 90
                            lifecycleScope.launch(Dispatchers.Main) {
                                timerTextView.text = remainingSeconds.toString()
                                timer = null
                                startTimer()
                            }
                        }

                        startTimer()
                    }

                    sendButton.isEnabled = response.isSuccessful

                }
            }
        })
    }


    private fun matchEmailCode(email: String, authCode: String) {
        // 요청을 보낼 URL 설정
        val url = "${BuildConfig.SERVER_ADDR}/member/matchEmailCode"

//        val requestEmail = requestBody(email, token)
//        val json = Json.encodeToString(loginInfo)

        val gson = Gson()
        val data = MyData(email, authCode)
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

                lifecycleScope.launch(Dispatchers.Main) {

                    val jsonString = JSONObject(responseData)

                    val responseResult = jsonString.getString("result")

                    when (timerTextView.text) {
                        "" -> showToast("이메일 인증을 해주세요")
                        // SingUpActivity 로 넘기기

                        else ->


                            when (responseResult) {

                                "failure" -> {
                                    val responseMessage = jsonString.getString("message")

                                    when (responseMessage) {

                                        "auth_code_expired" -> { showToast(" 인증번호가 만료되었습니다. ") }

                                        "input_error" -> { showToast(" 이메일 및 인증번호를 확인해주세요 ") }

                                        else -> {showToast(" 알 수 없는 코드 오류 ")
                                            Log.d("matchEmailCode / Message Error" , " message : ${responseMessage} ")}

                                    }
                                }

                                "success" -> {
                                    showToast("인증에 성공했습니다.")

                                    val intent =
                                        Intent(this@SignUpVerificationActivity, SignUpActivity::class.java)
                                    intent.putExtra(SignUpActivity.INPUT_EMAIL, email)
                                    this@SignUpVerificationActivity.startActivity(intent)
                                    stopTimer()
                                    timerTextView.text = ""
                                    finish()
                                }

                                else -> {
                                    showToast(" 알 수 없는 코드 오류")
                                    println(responseData)
                                    Log.d("matchEmailCode / ResponseData Error"," message : ${responseData} ")

                                }
                            }
                    }
                }
            }
        })
    }

    // id = email
    private fun isDuplicationEmail(email: String) {
        // 주소 설정
        val url = "${BuildConfig.SERVER_ADDR}/member/isDuplicated/email"
        // json 타입 변환
        val gson = Gson()
        val data = MyData(email)
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
                    showToast("이메일 값 전송에 실패하였습니다 , IOException")
                    sendButton.isEnabled = true
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    // 요청 실패 처리
                    println("Request failed")
                    lifecycleScope.launch(Dispatchers.Main) {
                        showToast("이메일 값 전송에 실패하였습니다, Response Error ")
                        println(email)
                        sendButton.isEnabled = true
                    }
                    return
                }

                val responseData = response.body?.string()
                // 응답 데이터 처리
                println("isDuplicationEmail : ${responseData}")

                lifecycleScope.launch(Dispatchers.Main) {

                    val jsonString = JSONObject(responseData)

                    val responseResult = jsonString.getString("result")

                    when (responseResult) {
                        "failure" -> {
                            val responseMessage = jsonString.getString("message")

                            when (responseMessage) {
                                "email_duplicated" -> {showToast( "동일한 이메일을 가진 사용자가 있습니다." )}
                                else -> {showToast( "알 수 없는 코드에러 " )}
                            }
                            sendButton.isEnabled = true
                        }
                        "success" -> {
                            Log.d("isDuplicationEmail" , " 이메일 인증 발송 ")
                            sendEmail(email)
                        }
                        else -> { showToast("알 수 없는 코드 오류")
                            sendButton.isEnabled = true
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
                        tokenText.text = ""
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

}

