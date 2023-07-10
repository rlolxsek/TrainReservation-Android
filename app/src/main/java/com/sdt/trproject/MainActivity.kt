package com.sdt.trproject

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.View.OnClickListener
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.sdt.trproject.appbar_title.AppbarTitle
import com.sdt.trproject.ksh.BoardActivity
import com.sdt.trproject.ksh.enquiry.EnquiryActivity
import com.sdt.trproject.services.RequestTrainScheduleResponse
import com.sdt.trproject.services.TrainApiService
import com.sdt.trproject.utils.RetrofitModule
import com.sdt.trproject.utils.requestBody
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Headers
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.properties.Delegates


@AndroidEntryPoint
class MainActivity() : BaseActivity(), OnClickListener {
    private var selectedMenuItem: MenuItem? = null

    private lateinit var tripSelectRadioGroup: RadioGroup
    private lateinit var radioButtonOnewayTrip: RadioButton
    private lateinit var radioButtonRoundTrip: RadioButton
    private lateinit var defaultCalendarDateText: String
    private lateinit var btnDepartureOpenCalendar: Button
    private var btnDepartureOpenCalendarDate: String? = null
    private var btnDepartureOpenCalendarText: String? = null
    private lateinit var btnReturnOpenCalendar: Button
    private var btnReturnOpenCalendarDate: String? = null
    private var btnReturnOpenCalendarText: String? = null
    private lateinit var btnDepartureStationSelect: Button
    private lateinit var btnDepartureStationSelectText: String
    private lateinit var btnArrivalStationSelect: Button
    private lateinit var btnArrivalStationSelectText: String
    private lateinit var selectedDate: String
    private var selectedDepartureTimeStamp by Delegates.notNull<Long>()
    private var selectedReturnTimeStamp by Delegates.notNull<Long>()
    private var selectedDepartureTime: Int = 0
    private var selectedReturnTime: Int = 0

    private val dateFormat: SimpleDateFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
    private lateinit var departureDate: String
    private lateinit var returnDate: String

    private lateinit var btnAdultMinus: Button
    private lateinit var btnAdultPlus: Button
    private lateinit var tvAdultCount: TextView
    private lateinit var btnChildMinus: Button
    private lateinit var btnChildPlus: Button
    private lateinit var tvChildCount: TextView
    private lateinit var btnOldMinus: Button
    private lateinit var btnOldPlus: Button
    private lateinit var tvOldCount: TextView

    private lateinit var btnSearchTrain: Button

    private val MIN_CLICK_INTERVAL = 3000 // 3000ms
    private var lastClickTime: Long = 0

    @Inject
    lateinit var trainApiService: TrainApiService


    // ******* 차후 작업 공간 시작 *******
    private lateinit var profilePhotoBtn: ImageView
    var getCookie: Boolean = false

    // 앱 바 맴버 START

    private lateinit var loginText: TextView
    private lateinit var loginBtnInAppbarFooter: TextView


    // 앱 바 맴버 END
    // appbar 작업


    // httpClient
    private val httpClient by lazy { OkHttpClient() }

    // SharedPreferences 인스턴스 생성
    private val sharedPreferences by lazy {
        getSharedPreferences("userPrefs", Context.MODE_PRIVATE)
        getSharedPreferences(SharedPrefKeys.PREF_NAME, Context.MODE_PRIVATE)
    }
    // ******* 차후 작업 공간 끝 *******


    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)





        profilePhotoBtn = navigationHeader.findViewById<ImageView>(R.id.profileImg).apply {
            this?.setOnClickListener(this@MainActivity)
        }

        loginText = navigationHeader.findViewById<TextView>(R.id.loginText).apply {
            this?.setOnClickListener(this@MainActivity)
        }

        loginBtnInAppbarFooter =
            navigationFooter.findViewById<TextView>(R.id.loginBtnInAppbarFooter).apply {
                this?.setOnClickListener(this@MainActivity)
            }

        val sidebar = findViewById<DrawerLayout>(R.id.drawer_layout, true)
        val toolbar = findViewById<Toolbar>(R.id.toolbar, true)

        // sideBar menu 클릭 이벤트
        for (idx in 0..4) {
            val menuItem = getNavigationHeaderMenuItem(idx)  // 인덱스 0의 메뉴 아이템을 가져옵니다.
            menuItem.setOnMenuItemClickListener {

                authorized(loginText, loginBtnInAppbarFooter)

                val intent = when (it.itemId) {
                    R.id.nav_home -> Intent(this@MainActivity, this@MainActivity::class.java)
                    R.id.nav_reservation -> {
                        when (getCookie) {
                            true -> {
                                Intent(this@MainActivity, ReservationTicketListActivity::class.java)
                            }
                            else -> {
                                Intent(this@MainActivity, LoginActivity::class.java)
                            }
                        }
                    }
                    R.id.nav_notice -> Intent(this@MainActivity, BoardActivity::class.java)
                    R.id.nav_faq -> Intent(this@MainActivity, EnquiryActivity::class.java)
                    R.id.nav_coupon -> Intent(this@MainActivity, this::class.java)
                    else -> null
                }

                intent?.let {
                    @Suppress("NestedLambdaShadowedImplicitParameter")
                    selectedMenuItem = menuItem
                    startActivity(it)
                    sidebar.closeDrawer(GravityCompat.START)
                    setSupportActionBar(toolbar)
//                    finish();
                }
                true // 이벤트가 처리되었음을 의미합니다.
            }
        }



        // 표준 함수 4가지
        // apply 자기자신 돌려줌 this
        btnDepartureOpenCalendar = findViewById<Button>(R.id.btnOpenDepartureCalendar).apply {
            setOnClickListener(this@MainActivity)
        }
        // also 자기자신 돌려줌 this -> it
        btnReturnOpenCalendar = findViewById<Button>(R.id.btnOpenReturnCalendar).also {
            it.setOnClickListener(this)
        }

        // 함수안에서 돌려주는 값을 결정해줄것. 람다의 마지막은 리턴 값 , if 문 같은 경우 return@let it 사용
        btnDepartureStationSelect = findViewById<Button>(R.id.btnDepartureStationSelect).let {
            it.setOnClickListener(this)
            it
//            return@let it
        }
        // 함수안에서 돌려주는 값을 결정. this를 반환
        btnArrivalStationSelect = findViewById<Button>(R.id.btnArrivalStationSelect).run {
            setOnClickListener(this@MainActivity)
            this
        }


        val currentDate = getCurrentDate()
        selectedDepartureTimeStamp = getCurrentTimeStamp()
        selectedReturnTimeStamp = getCurrentTimeStamp()
        defaultCalendarDateText = currentDate

        btnDepartureOpenCalendar.text = "출발일 : $defaultCalendarDateText"
        btnReturnOpenCalendar.text = "오는날 : $defaultCalendarDateText"


        btnDepartureStationSelect.setOnClickListener {
            val stationSelectIntent = Intent(this, StationSelectActivity::class.java).apply {
                putExtra(
                    StationSelectActivity.DEPARTURE_STATION,
                    if (this@MainActivity::btnDepartureStationSelectText.isInitialized) {
                        btnDepartureStationSelectText
                    } else {
                        "출발역"
                    }
                )
                putExtra(
                    StationSelectActivity.ARRIVAL_STATION,
                    if (this@MainActivity::btnArrivalStationSelectText.isInitialized) {
                        btnArrivalStationSelectText
                    } else {
                        "도착역"
                    }
                )
            }
            // 출발 역 선택 플래그 설정 ( 출발역인가 true )
            stationSelectIntent.putExtra("isDeparture", true)
            stationSelectActivityResult.launch(stationSelectIntent)
        }

        btnArrivalStationSelect.setOnClickListener {
            val stationSelectIntent = Intent(this, StationSelectActivity::class.java).apply {
                putExtra(
                    StationSelectActivity.DEPARTURE_STATION,
                    if (this@MainActivity::btnDepartureStationSelectText.isInitialized) {
                        btnDepartureStationSelectText
                    } else {
                        "출발역"
                    }
                )
                putExtra(
                    StationSelectActivity.ARRIVAL_STATION,
                    if (this@MainActivity::btnArrivalStationSelectText.isInitialized) {
                        btnArrivalStationSelectText
                    } else {
                        "도착역"
                    }
                )
            }
            // 도착 역 선택 플래그 설정 ( 출발역인가 false )
            stationSelectIntent.putExtra("isDeparture", false)
            stationSelectActivityResult.launch(stationSelectIntent)
        }

        btnAdultMinus = findViewById<Button>(R.id.btnAdultMinus)
        btnAdultPlus = findViewById<Button>(R.id.btnAdultPlus)
        tvAdultCount = findViewById<TextView>(R.id.tvAdultCount)
        btnChildMinus = findViewById<Button>(R.id.btnChildMinus)
        btnChildPlus = findViewById<Button>(R.id.btnChildPlus)
        tvChildCount = findViewById<TextView>(R.id.tvChildCount)
        btnOldMinus = findViewById<Button>(R.id.btnOldMinus)
        btnOldPlus = findViewById<Button>(R.id.btnOldPlus)
        tvOldCount = findViewById<TextView>(R.id.tvOldCount)

        val adultCount = 1
        val childCount = 0
        val oldCount = 0

        val formattedAdultCount = getString(R.string.adult_count)
        val formattedChildCount = getString(R.string.child_count)
        val formattedOldCount = getString(R.string.old_count)

        tvAdultCount.text = formattedAdultCount
        tvChildCount.text = formattedChildCount
        tvOldCount.text = formattedOldCount

        setupCounterButton(btnAdultPlus, btnAdultMinus, tvAdultCount)
        setupCounterButton(btnChildPlus, btnChildMinus, tvChildCount)
        setupCounterButton(btnOldPlus, btnOldMinus, tvOldCount)

        btnSearchTrain = findViewById(R.id.btnSearchTrain)


        // 초기값
        btnDepartureStationSelectText = "동대구"
        btnArrivalStationSelectText = "수서"
        btnDepartureStationSelect.setText(btnDepartureStationSelectText)
        btnArrivalStationSelect.setText(btnArrivalStationSelectText)
        departureDate = dateFormat.format(getCurrentTimeStamp()).toString()
        returnDate = dateFormat.format(getCurrentTimeStamp()).toString()

        // 편도, 왕복 선택
        tripSelectRadioGroup = findViewById(R.id.radioGroupTripSelect)
        radioButtonOnewayTrip = findViewById<RadioButton>(R.id.radioButtonOnewayTrip)
        radioButtonRoundTrip = findViewById<RadioButton>(R.id.radioButtonRoundTrip)

        tripSelectRadioGroup.check(R.id.radioButtonOnewayTrip)
        btnReturnOpenCalendar.visibility = View.GONE
        radioButtonOnewayTrip.setTextColor(Color.RED)

        tripSelectRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.radioButtonOnewayTrip -> {
                    btnReturnOpenCalendar.visibility = View.GONE
                    radioButtonOnewayTrip.setTextColor(Color.RED)
                    radioButtonRoundTrip.setTextColor(Color.WHITE)
                    radioButtonOnewayTrip.setTypeface(null, Typeface.BOLD)
                    println(btnDepartureOpenCalendarText)

                    btnDepartureOpenCalendar.text = "출발일 : $defaultCalendarDateText"

                    if (btnDepartureOpenCalendarText != null)
                        btnDepartureOpenCalendar.text = "출발일 : $btnDepartureOpenCalendarText"
                }

                R.id.radioButtonRoundTrip -> {
                    btnReturnOpenCalendar.visibility = View.VISIBLE
                    radioButtonRoundTrip.setTextColor(Color.RED)
                    radioButtonOnewayTrip.setTextColor(Color.WHITE)
                    radioButtonRoundTrip.setTypeface(null, Typeface.BOLD)

                    println(btnDepartureOpenCalendarText)

                    btnDepartureOpenCalendar.text = "가는날 : $defaultCalendarDateText"
                    btnReturnOpenCalendar.text = "오는날 : $defaultCalendarDateText"

                    if (btnDepartureOpenCalendarText != null)
                        btnDepartureOpenCalendar.text = "가는날 : $btnDepartureOpenCalendarText"

                    if (btnReturnOpenCalendarText != null)
                        btnReturnOpenCalendar.text = "오는날 : $btnReturnOpenCalendarText"
                }
            }

        }


        // 열차 조회하기
        btnSearchTrain.setOnClickListener {
            println("열차 조회 클릭 이벤트 발생")
            val currentTime = SystemClock.elapsedRealtime()
            if (currentTime - lastClickTime > MIN_CLICK_INTERVAL) {
                println("열차 조회 요청 보냄")
                // 클릭 이벤트 처리
                // 편도, 왕복에 따른 데이터 구분하여 Intent에 데이터 담기
                if (radioButtonOnewayTrip.isChecked) {
                    intent.putExtra("isRoundTrip", false)
                    sendRequestToOnewayTripSearchForTrains()
                } else if (radioButtonRoundTrip.isChecked) {
                    intent.putExtra("isRoundTrip", true)
                    sendRequestToRoundTripSearchForTrains()
                }
                lastClickTime = currentTime
            }


        }

        // 차후 작업 공간 시작


        profilePhotoBtn = navigationHeader.findViewById(R.id.profileImg)

        profilePhotoBtn.setOnClickListener() {

            intent = Intent(this@MainActivity, MyProfileActivity::class.java)
            startActivity(intent)

        }


        // 차후 작업 공간 끝
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            onBackPressedDispatcher.addCallback(
                this,
                object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        showExitDialog()
                    }
                }
            )
        }
    }

    override fun onResume() {
        super.onResume()

        // 이전에 선택된 메뉴 아이템을 초기화하여 선택 해제
        selectedMenuItem?.isChecked = false
        selectedMenuItem = null
    }

    override fun onBackPressed() {
        showExitDialog()
    }

    override fun onStart() {
        super.onStart()
        setTitleText("${AppbarTitle.MAIN_TITLE}")
        loginText = navigationHeader.findViewById(R.id.loginText)
        loginBtnInAppbarFooter = navigationFooter.findViewById(R.id.loginBtnInAppbarFooter)
        authorized(loginText, loginBtnInAppbarFooter)
    }

    // 출발역, 도착역 선택
    private val stationSelectActivityResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode != Activity.RESULT_OK) {
                return@registerForActivityResult
            }
            val data = result.data ?: return@registerForActivityResult
            btnDepartureStationSelectText =
                data.getStringExtra(StationSelectActivity.DEPARTURE_STATION) ?: "동대구"
            btnArrivalStationSelectText =
                data.getStringExtra(StationSelectActivity.ARRIVAL_STATION) ?: "수서"
            btnDepartureStationSelect.text = btnDepartureStationSelectText
            btnArrivalStationSelect.text = btnArrivalStationSelectText
        }

    // 출발 일정 선택
    @SuppressLint("SetTextI18n")
    private val departureCalendarActivityResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode != Activity.RESULT_OK) {
                return@registerForActivityResult
            }
            val data = result.data ?: return@registerForActivityResult

            selectedDepartureTimeStamp =
                data.getLongExtra(DepartureCalendarActivity.SELECTED_TIMESTAMP, 0)
            val selectedYear = data.getStringExtra(DepartureCalendarActivity.SELECTED_YEAR)
            val selectedMonth = data.getStringExtra(DepartureCalendarActivity.SELECTED_MONTH)
            val selectedDay = data.getStringExtra(DepartureCalendarActivity.SELECTED_DAY)
            val selectedDayOfWeek =
                data.getStringExtra(DepartureCalendarActivity.SELECTED_DAYOFWEEK)

            departureDate = dateFormat.format(selectedDepartureTimeStamp).toString()

            selectedDepartureTime = data.getIntExtra(DepartureCalendarActivity.SELECTED_TIME, 0)

            println("selectedDepartureTimeStamp : " + selectedDepartureTimeStamp)
            println("departureDate : " + departureDate)
            println("selectedDepartureTime : " + selectedDepartureTime)

            if (selectedDepartureTime < 10) {
                btnDepartureOpenCalendarText =
                    "${selectedYear}년 ${selectedMonth}월 ${selectedDay}일(${selectedDayOfWeek}) 0${selectedDepartureTime}시 이후"
                btnDepartureOpenCalendar.text = "출발일 : $btnDepartureOpenCalendarText"
            } else {
                btnDepartureOpenCalendarText =
                    "${selectedYear}년 ${selectedMonth}월 ${selectedDay}일(${selectedDayOfWeek}) ${selectedDepartureTime}시 이후"
                btnDepartureOpenCalendar.text = "출발일 : $btnDepartureOpenCalendarText"
            }
        }

    // 도착 일정 선택
    @SuppressLint("SetTextI18n")
    private val arrivalCalendarActivityResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode != Activity.RESULT_OK) {
                return@registerForActivityResult
            }
            val data = result.data ?: return@registerForActivityResult

            selectedReturnTimeStamp =
                data.getLongExtra(ArrivalCalendarActivity.SELECTED_TIMESTAMP, 0)
            val selectedYear = data.getStringExtra(ArrivalCalendarActivity.SELECTED_YEAR)
            val selectedMonth = data.getStringExtra(ArrivalCalendarActivity.SELECTED_MONTH)
            val selectedDay = data.getStringExtra(ArrivalCalendarActivity.SELECTED_DAY)
            val selectedDayOfWeek =
                data.getStringExtra(ArrivalCalendarActivity.SELECTED_DAYOFWEEK)

            returnDate = dateFormat.format(selectedReturnTimeStamp).toString()

            selectedReturnTime = data.getIntExtra(ArrivalCalendarActivity.SELECTED_TIME, 0)

            println("selectedArrivalTimeStamp : " + selectedReturnTimeStamp)
            println("arrivalDate : " + returnDate)
            println("selectedArrivalTime : " + selectedReturnTime)

            if (selectedReturnTime < 10) {
                btnReturnOpenCalendarText =
                    "${selectedYear}년 ${selectedMonth}월 ${selectedDay}일(${selectedDayOfWeek}) 0${selectedReturnTime}시 이후"
                btnReturnOpenCalendar.text = "오는날 : $btnReturnOpenCalendarText"
            } else {
                btnReturnOpenCalendarText =
                    "${selectedYear}년 ${selectedMonth}월 ${selectedDay}일(${selectedDayOfWeek}) ${selectedReturnTime}시 이후"
                btnReturnOpenCalendar.text = "오는날 : $btnReturnOpenCalendarText"
            }
        }

    // 날짜, 요일, 시간 초기값 반환
    private fun getCurrentDate(): String {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy년 MM월 dd일", Locale.getDefault())
        val date = dateFormat.format(calendar.time)
        val timeFormat = SimpleDateFormat("HH시", Locale.getDefault())
        val currentTime = calendar.get(Calendar.HOUR_OF_DAY)
        val time = timeFormat.format(calendar.time)
        val dayOfWeek = when (calendar.get(Calendar.DAY_OF_WEEK)) {
            Calendar.SUNDAY -> "일"
            Calendar.MONDAY -> "월"
            Calendar.TUESDAY -> "화"
            Calendar.WEDNESDAY -> "수"
            Calendar.THURSDAY -> "목"
            Calendar.FRIDAY -> "금"
            Calendar.SATURDAY -> "토"
            else -> ""
        }

        selectedDepartureTime = currentTime
        selectedReturnTime = currentTime

        return "${date}(${dayOfWeek}) ${time} 이후"
    }

    private fun getCurrentTimeStamp(): Long {
        val calendar = Calendar.getInstance()
        return calendar.timeInMillis
    }

    override fun onClick(v: View?) {
        when (v?.id ?: 0) {

            R.id.btnOpenDepartureCalendar -> {
                goDepartureDateSelectActivity()
            }

            R.id.btnOpenReturnCalendar -> {
                goArrivalDateSelectActivity()
            }

            R.id.loginText -> {
                //loginFun(v as TextView)
            }
            R.id.loginBtnInAppbarFooter -> {
                loginBtnInAppbarFooter = findViewById(R.id.loginBtnInAppbarFooter)
                loginFun(v as TextView)
            }
        }
    }

    private fun loginFun(loginBtnInAppbarFooter: TextView) {

        when (loginBtnInAppbarFooter.text.toString()) {

            AppbarKeys.LOG_IN -> {
                val intent = Intent(this@MainActivity, LoginActivity::class.java)
                startActivity(intent)
            }

            AppbarKeys.LOG_OUT -> {
                val sharedPreferences =
                    getSharedPreferences(SharedPrefKeys.PREF_NAME, Context.MODE_PRIVATE)
                val editor = sharedPreferences.edit()
                editor.remove(SharedPrefKeys.SET_COOKIE)
                editor.putBoolean("isAutoLogin", false)
                editor.remove("savedUserPw")
                editor.apply()

                val intent = Intent(this@MainActivity, MainActivity::class.java)
                startActivity(intent)
            }

            else -> {
                Log.d("goLoginButton", "코드오류")
                Log.d("텍스트 확인", loginText.text.toString())
                showToast("kalsjdlkasjdklas")
                val intent = Intent(this@MainActivity, this@MainActivity::class.java)
                startActivity(intent)
            }
        }
    }

    // 출발/도착 일정
    fun goDepartureDateSelectActivity() {
        val departureCalendarIntent =
            Intent(this, DepartureCalendarActivity::class.java).apply {
                putExtra(
                    DepartureCalendarActivity.DEPARTURE_DATE,
                    selectedDepartureTimeStamp,
                )
                putExtra(
                    DepartureCalendarActivity.SELECTED_TIME, selectedDepartureTime
                )
            }
        departureCalendarActivityResult.launch(departureCalendarIntent)
    }

    fun goArrivalDateSelectActivity() {
        val arrivalCalendarIntent = Intent(this, ArrivalCalendarActivity::class.java).apply {
            putExtra(
                ArrivalCalendarActivity.ARRIVAL_DATE, selectedReturnTimeStamp
            )
            putExtra(
                ArrivalCalendarActivity.SELECTED_TIME, selectedReturnTime
            )
        }
        arrivalCalendarActivityResult.launch(arrivalCalendarIntent)
    }

    // 인원수 설정
    fun setupCounterButton(btnPlus: Button, btnMinus: Button, tvCount: TextView) {
        btnPlus.setOnClickListener {
            var currentCount = tvCount.text.toString().toInt()
            if (currentCount < 10) {
                tvCount.text = (currentCount + 1).toString()
            }
        }

        btnMinus.setOnClickListener {
            var currentCount = tvCount.text.toString().toInt()
            if (currentCount > 0) {
                tvCount.text = (currentCount - 1).toString()
            }
        }
    }

    // 열차 조회 (서버에 요청)

    // 편도 여행 열차 시간표 조회
    private fun sendRequestToOnewayTripSearchForTrains() {
        val departureStation = btnDepartureStationSelectText
        val arrivalStation = btnArrivalStationSelectText
        val departureDate = departureDate
        val departureTime = selectedDepartureTime
        val adultCount = tvAdultCount.text
        val childCount = tvChildCount.text
        val oldCount = tvOldCount.text


        val jsonObject = JSONObject()
        jsonObject.put("departStation", departureStation)
        jsonObject.put("arriveStation", arrivalStation)
        jsonObject.put("date", departureDate)
        jsonObject.put("adult", adultCount)
        jsonObject.put("child", childCount)
        jsonObject.put("old", oldCount)

        val requestBody = jsonObject.requestBody()
        val call = trainApiService.requestTrainSchedule(requestBody)

        RetrofitModule.executeCall(
            call,
            onFailure = { message, httpCode ->
                println("TrainSchedule 요청 실패 : Message = $message, HttpCode = $httpCode")
            },
            onSuccess = { response ->
                println("TrainSchedule 요청 성공 : Response = $response")
                handleOnewayTrainResponse(response)
            }
        )
    }

    fun handleOnewayTrainResponse(requestTrainScheduleResponse: RequestTrainScheduleResponse?) {
        // TODO : 서버 응답에 대한 로직 구현
        requestTrainScheduleResponse?.let {
            val gson = Gson()
            val dataString = gson.toJson(it.data)
            val result = it.result
            if (it.data == null) {
                print("데이터 조회가 불가능합니다.")
                Toast.makeText(applicationContext, "데이터 조회가 불가능합니다.", Toast.LENGTH_LONG).show()
                return
            }
            val data = it.data
            println("result : $result")
            println("data : $data")

            // 열차 시간 조회 액티비티 시작 & 데이터 전달
            val intent = Intent(this@MainActivity, TrainScheduleActivity::class.java).apply {
                putExtra("RESULT", result)
                putExtra("DATA", dataString)
                putExtra("DEPARTURESTATION", btnDepartureStationSelectText)
                putExtra("ARRIVALSTATION", btnArrivalStationSelectText)
                putExtra("DEPARTUREDATE", departureDate)
                putExtra("DEPARTURETIME", selectedDepartureTime)
                putExtra("ADULTCOUNT", tvAdultCount.text.toString().toInt())
                putExtra("CHILDCOUNT", tvChildCount.text.toString().toInt())
                putExtra("OLDCOUNT", tvOldCount.text.toString().toInt())
            }

            startActivity(intent)
        }
    }

    fun handleOnewayTrainError() {
        // TODO : 서버 응답 실패 또는 요청 실패에 대한 로직 처리 구현
        // ex: Error message, 재시도 ...
    }


    // 왕복 여행 기차 시간표 조회
    private fun sendRequestToRoundTripSearchForTrains() {
        val departureStation = btnDepartureStationSelectText
        val arrivalStation = btnArrivalStationSelectText
        val departureDate = departureDate
        val departureTime = selectedDepartureTime
        val returnDate = returnDate
        val returnDateTime = selectedReturnTime
        val adultCount = tvAdultCount.text
        val childCount = tvChildCount.text
        val oldCount = tvOldCount.text


        val jsonObject = JSONObject()
        jsonObject.put("departStation", departureStation)
        jsonObject.put("arriveStation", arrivalStation)
        jsonObject.put("date", departureDate)
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
                if (!response.isSuccessful) {
                    // 서버 응답 실패
                    // TODO: 실패에 대한 처리 로직 추가
                    println("요청 실패")
                    handleOnewayTrainError()
                    return
                }

                // 서버 응답 처리
                val apiResponse = response.body()
                // TODO: 서버 응답에 대한 로직 추가
                println("요청 성공")
                handleRoundTrainResponse(apiResponse)
            }

            override fun onFailure(call: Call<RequestTrainScheduleResponse>, t: Throwable) {
                // 요청 실패
                // TODO: 실패에 대한 처리 로직을 추가하세요.
                handleRoundTrainError()
            }
        })
    }


    fun handleRoundTrainResponse(requestTrainScheduleResponse: RequestTrainScheduleResponse?) {
        // TODO : 서버 응답에 대한 로직 구현
        requestTrainScheduleResponse?.let {
            val gson = Gson()
            val dataString = gson.toJson(it.data)
            val result = it.result
            val data = it.data

            if (it.data == null) {
                print("데이터 조회가 불가능합니다.")
                Toast.makeText(applicationContext, "데이터 조회가 불가능합니다.", Toast.LENGTH_LONG).show()
                return
            }
            println("result : $result")
            println("data : $data")

            // 열차 시간 조회 액티비티 시작 & 데이터 전달
            val intent = Intent(this@MainActivity, TrainScheduleActivity::class.java).apply {
                putExtra("RESULT", result)
                putExtra("DATA", dataString)
                putExtra("DEPARTURESTATION", btnDepartureStationSelectText)
                putExtra("ARRIVALSTATION", btnArrivalStationSelectText)
                putExtra("DEPARTUREDATE", departureDate)
                putExtra("DEPARTURETIME", selectedDepartureTime)
                putExtra("RETURNDATE", returnDate)
                putExtra("RETURNTIME", selectedReturnTime)
                putExtra("ADULTCOUNT", tvAdultCount.text)
                putExtra("CHILDCOUNT", tvChildCount.text)
                putExtra("OLDCOUNT", tvOldCount.text)
            }

            startActivity(intent)
        }
    }

    fun handleRoundTrainError() {
        // TODO : 서버 응답 실패 또는 요청 실패에 대한 로직 처리 구현
        // ex: Error message, 재시도 ...
    }


    fun authorized(loginText: TextView, loginBtnInAppbarFooter: TextView) {
        val url = "${BuildConfig.SERVER_ADDR}/member/authorized/member"
        val gson = Gson()
        val data = ""
        val json = gson.toJson(data)
        val mediaType = "application/json".toMediaType()
        val requestBody = json.toRequestBody(mediaType)

        val request = Request.Builder()
            .url(url)
            .headers(
                Headers.headersOf(
                    //SharedPrefKeys.SET_COOKIE
                    SharedPrefKeys.COOKIE,
                    sharedPreferences.getString(SharedPrefKeys.SET_COOKIE, "") ?: ""
                    //, sharedPreferences.getString(SharedPrefKeys.SET_COOKIE, "") !!
                )
            )
            .post(requestBody)
            .build()


        httpClient.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                // 요청 실패 시 처리
                e.printStackTrace()
                lifecycleScope.launch(Dispatchers.Main) {
                    Log.d("/member/authorized1", "IOException")
                    showToast("쿠키값 확인 실패.")
                }
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {

                if (!response.isSuccessful) {

                    // 요청 실패 처리
                    lifecycleScope.launch(Dispatchers.Main) {
                        Log.d("/member/authorized2", "Response")
                        showToast("쿠키값 확인 실패.")
                    }
                    return
                }

                val responseData = response.body?.string()
                // 응답 데이터 처리
                lifecycleScope.launch(Dispatchers.Main) {

                    val jsonString = JSONObject(responseData)
                    val responseResult = jsonString.getString("result")

                    Log.d("/member/authorized3", "${responseData}")
                    Log.d("이거 확인좀", (SharedPrefKeys.SET_COOKIE))
                    Log.d(
                        "이거 확인좀",
                        (sharedPreferences.getString(SharedPrefKeys.SET_COOKIE, "") ?: "")
                    )


                    when (responseResult) {

                        "failure" -> {
                            loginText.text = AppbarKeys.LOG_OUT_STATUS
                            loginBtnInAppbarFooter.text = AppbarKeys.LOG_IN
                            Log.d("/member/authorized4", loginBtnInAppbarFooter.text.toString())
                            profilePhotoBtn.isEnabled = false
                            getCookie = false
                        }

                        "success" -> {

                            val userName =
                                sharedPreferences.getString(SharedPrefKeys.USER_NAME, "")
                            val reservationCnt =
                                sharedPreferences.getString(SharedPrefKeys.RESULVATION_CNT, "")

                            loginText.text =
                                " ${userName}" + AppbarKeys.LOG_IN_STATUS + "${reservationCnt}"
                            loginBtnInAppbarFooter.text = AppbarKeys.LOG_OUT
                            Log.d("/member/authorized5", loginBtnInAppbarFooter.text.toString())
                            profilePhotoBtn.isEnabled = true
                            getCookie = true
                            // TODO: 이부분 수정할 것. null 처리 임시로 막아.
//                            resizeBitmap(profilePhotoBtn)
                        }

                    }
                }
            }
        })

    }

    private fun showExitDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.exit_dialog_layout, null)

        val builder = AlertDialog.Builder(this)
        builder.setView(dialogView)
            .setCancelable(false)

        val dialog: AlertDialog = builder.create()

        val btnCancel = dialogView.findViewById<TextView>(R.id.btn_cancel)
        val btnExit = dialogView.findViewById<TextView>(R.id.btn_exit)

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnExit.setOnClickListener {
            dialog.dismiss()
            finish()
        }

        dialog.show()
    }


    ////////////////////////////게시판 단 쿠키확인용///////////////////////////////////


    fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }


}




