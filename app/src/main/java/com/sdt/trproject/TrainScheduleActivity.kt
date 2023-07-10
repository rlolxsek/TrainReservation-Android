package com.sdt.trproject

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sdt.trproject.adapters.TrainScheduleAdapter
import com.sdt.trproject.services.RequestTrainScheduleResponse
import com.sdt.trproject.services.TrainApiService
import com.sdt.trproject.services.RequestTrainScheduleItem
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import javax.inject.Inject

@AndroidEntryPoint
class TrainScheduleActivity : AppCompatActivity() {

    //appbar
    private lateinit var appbarTitle : TextView
    private lateinit var backBtn : ImageView

    private lateinit var recyclerView: RecyclerView
    @Inject
    lateinit var adapter: TrainScheduleAdapter
    private lateinit var btnNextDay: Button
    private lateinit var btnPreviousDay: Button
    private lateinit var tvTrainScheduleDate: TextView

    private lateinit var data: List<RequestTrainScheduleItem>

    private var departureStation: String = ""
    private var arrivalStation: String = ""
    private var departureDate: String = ""
    private var departureTime: Int = 0
    private var returnDate: String = ""
    private var returnTime: Int = 0
    private var adultCount: Int = 0
    private var childCount: Int = 0
    private var oldCount: Int = 0

    private var nextDate: String? = null

    private val currentDate = LocalDate.now()

    @Inject
    lateinit var trainApiService: TrainApiService




    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_train_schedule)

        // appbar 추가
        appbarTitle = findViewById<TextView?>(R.id.appbarTitle).apply {
            setText("조회결과")
        }
        backBtn = findViewById<ImageView?>(R.id.back_btn).apply {
            setOnClickListener(){
                onBackPressed()
            }
        }

        btnNextDay = findViewById(R.id.btnTrainScheduleNextDay)
        btnPreviousDay = findViewById(R.id.btnTrainSchedulePreviousDay)
        tvTrainScheduleDate = findViewById(R.id.tvTrainScheduleDate)

        // MainActivity에서 넘어온 선택된 데이터
        departureStation = intent.getStringExtra("DEPARTURESTATION")?: ""
        arrivalStation = intent.getStringExtra("ARRIVALSTATION")?: ""
        departureDate = intent.getStringExtra("DEPARTUREDATE")?: ""
        departureTime = intent.getIntExtra("DEPARTURETIME", 0)
        returnDate = intent.getStringExtra("RETURNDATE")?: ""
        returnTime = intent.getIntExtra("RETURNTIME", 0)
        adultCount = intent.getIntExtra("ADULTCOUNT", 0)
        childCount = intent.getIntExtra("CHILDCOUNT", 0)
        oldCount = intent.getIntExtra("OLDCOUNT", 0)


        // api 요청으로 받아온 데이터
        val result = intent.getStringExtra("RESULT")
        val dataString = intent.getStringExtra("DATA")

        // 날짜 형식 변경
        val originalFormat = SimpleDateFormat("yyyyMMdd")
        val targetFormat = SimpleDateFormat("MM월 dd일")
        val date = originalFormat.parse(departureDate)
        val formattedDate = targetFormat.format(date)

        tvTrainScheduleDate.text = formattedDate

        // DATA 문자열을 다시 List<TrainItem>으로 변환
        val gson = Gson()
        val listType = object : TypeToken<List<RequestTrainScheduleItem>>() {}.type
        data = gson.fromJson(dataString, listType)

        val itemDecorator = VerticalSpacingItemDecorator(20)

        println("departureStation : $departureStation")
        println("arrivalStation : $arrivalStation")
        println("departureDate : $departureDate")
        println("departureTime : $departureTime")
        println("returnDate : $returnDate")
        println("returnTime : $returnTime")


        recyclerView = findViewById(R.id.trainTimeRecyclerView)

//        adapter = TrainScheduleAdapter(
//            context = this,
//            null,
        adapter.initData(
            recyclerView,
            departureStation = departureStation,
            arrivalStation = arrivalStation,
            departureDate = departureDate,
            departureTime = departureTime,
            returnDate = returnDate,
            returnTime = returnTime,
            adultCount = adultCount,
            childCount = childCount,
            oldCount = oldCount,
            personCount = adultCount + childCount + oldCount
        )


        recyclerView.adapter = adapter
        recyclerView.addItemDecoration(itemDecorator)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val seatGradeSelectRadioGroup = findViewById<RadioGroup>(R.id.radioGroupSeatGradeSelect)
        val radioBtnPremiumSeatSelect = findViewById<RadioButton>(R.id.radioBtnPremiumSeatSelect)
        val radioBtnStandardSeatSelect = findViewById<RadioButton>(R.id.radioBtnStandardSeatSelect)
        val trainScheduleDropdownMenu = findViewById<LinearLayout>(R.id.trainScheduleDropdownMenuLl)

        btnPreviousDay = findViewById(R.id.btnTrainSchedulePreviousDay)
        btnNextDay = findViewById(R.id.btnTrainScheduleNextDay)

        // 입력 받은 날짜 별 로직
        val trainDateFormat = SimpleDateFormat("yyyyMMdd")
        val localDepartureDate =
            LocalDate.parse(departureDate, DateTimeFormatter.ofPattern("yyyyMMdd"))

        val lastDate = currentDate.plusDays(6)

        if (localDepartureDate.isEqual(currentDate)) {
            // 넘어온 출발 날짜와 현재 날짜가 같은 경우
            println("날짜가 같음")
            filterData(departureTime!!)

            // 조회날짜가 오늘과 같을경우 이전날 선택 버튼 안보이게
            btnPreviousDay.visibility = View.GONE

            if (localDepartureDate.isEqual(lastDate)) {
                btnNextDay.visibility = View.GONE
            }

        } else if (localDepartureDate.isAfter(currentDate)) {
            // 넘어온 출발 날짜가 현재 날짜보다 이후인 경우
            println("다음 날짜임")

            filterData(departureTime!!)

            if (localDepartureDate.isEqual(lastDate)) {
                btnNextDay.visibility = View.GONE
            }
        }




        btnPreviousDay.setOnClickListener {
            println("이전날 클릭")
            searchTrainScheduleForPreviousDay()
        }

        btnNextDay.setOnClickListener {
            searchTrainScheduleForNextDay()
        }



    }

    private fun filterData(departureTime: Int) {
        val currentTime = LocalDateTime.now().hour

        val filteredData = data.filter { item ->
            val trainDateTimeFormat = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
            val trainDateTime =
                LocalDateTime.parse(item.date + item.depPlandTime, trainDateTimeFormat)
            val localDepartureDate =
                LocalDate.parse(departureDate, DateTimeFormatter.ofPattern("yyyyMMdd"))

//            입력받은 departureTime 이후의 시간만 필터링
            if (localDepartureDate.isEqual(currentDate)) {
                trainDateTime.hour >= departureTime && trainDateTime.hour >= currentTime
            } else {
                trainDateTime.hour >= departureTime
            }



        }

        adapter.setData(filteredData) // 어댑터에 데이터 설정

    }

    // *********** 다른 날짜 기차 스케줄 조회 ***********
    private fun searchTrainScheduleForDate(value: Int) {
        val currentDate = SimpleDateFormat("yyyyMMdd").parse(departureDate)
        val calendar = Calendar.getInstance()
        calendar.time = currentDate
        calendar.add(Calendar.DATE, value)
        nextDate = SimpleDateFormat("yyyyMMdd").format(calendar.time)

        // 검색에 필요한 정보를 Intent에 추가
        val intent = Intent(this, TrainScheduleActivity::class.java)

        // 새로운 날짜 표시

        val jsonObject = JSONObject()
        jsonObject.put("departStation", departureStation)
        jsonObject.put("arriveStation", arrivalStation)
        jsonObject.put("date", nextDate)
        jsonObject.put("adult", adultCount)
        jsonObject.put("child", childCount)
        jsonObject.put("old", oldCount)

        val requestBody = jsonObject.toString()
            .toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

        val call = trainApiService.requestTrainSchedule(requestBody)
        call.enqueue(object : Callback<RequestTrainScheduleResponse> {
            override fun onResponse(
                call: Call<RequestTrainScheduleResponse>,
                response: Response<RequestTrainScheduleResponse>
            ) {
                if (response.isSuccessful) {
                    // 서버 응답 처리
                    val apiResponse = response.body()
//                    println("response.message() : " + response.message())
                    // TODO: 서버 응답에 대한 로직 추가
                    println("요청 성공")
                    handleTrainResponse(apiResponse)
                } else {
                    // 서버 응답 실패
                    // TODO: 실패에 대한 처리 로직 추가
                    println("요청 실패")
                    handleTrainError()
                }
            }

            override fun onFailure(call: Call<RequestTrainScheduleResponse>, t: Throwable) {
                // 요청 실패
                // TODO: 실패에 대한 처리 로직을 추가하세요.
                handleTrainError()
            }
        })
    }

    fun handleTrainResponse(requestTrainScheduleResponse: RequestTrainScheduleResponse?) {
        // TODO : 서버 응답에 대한 로직 구현
        requestTrainScheduleResponse?.let {
            val gson = Gson()
            val dataString = gson.toJson(it.data)
            val result = it.result
            if (it.data == null) {
                println("데이터 조회가 불가능합니다.")
                Toast.makeText(applicationContext, "데이터 조회가 불가능합니다.", Toast.LENGTH_LONG).show()
                return
            }
            val data = it.data
            println("result : $result")
            println("data : $data")

            // 열차 시간 조회 액티비티 시작 & 데이터 전달
            val intent = Intent(this, TrainScheduleActivity::class.java).apply {
                putExtra("RESULT", result)
                putExtra("DATA", dataString)
                putExtra("DEPARTURESTATION", departureStation)
                putExtra("ARRIVALSTATION", arrivalStation)
                putExtra("DEPARTUREDATE", nextDate)
                putExtra("DEPARTURETIME", -1)
                putExtra("ADULTCOUNT", adultCount)
                putExtra("CHILDCOUNT", childCount)
                putExtra("OLDCOUNT", oldCount)
            }

            startActivity(intent)
            finish()
        }
    }

    fun handleTrainError() {
        // TODO : 서버 응답 실패 또는 요청 실패에 대한 로직 처리 구현
        // ex: Error message, 재시도 ...
    }

    // 이전날 조회
    private fun searchTrainScheduleForPreviousDay() {
        println("이전날 조회")
        searchTrainScheduleForDate(-1)
    }

    // 다음날 조회
    private fun searchTrainScheduleForNextDay() {
        println("다음날 조회")
        searchTrainScheduleForDate(1)
    }

    // *********** 기차 스케줄 검색 조회 끝 ***********

}