package com.sdt.trproject

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CalendarView
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sdt.trproject.adapters.TimeSelectAdapter
import java.util.*

class ArrivalCalendarActivity : AppCompatActivity(), TimeSelectAdapter.OnTimeClickListener {
    companion object {
        const val ARRIVAL_DATE = "ARRIVAL_DATE"
        const val SELECTED_TIMESTAMP = "SELECTED_TIMESTAMP"
        const val SELECTED_YEAR = "SELECTED_YEAR"
        const val SELECTED_MONTH = "SELECTED_MONTH"
        const val SELECTED_DAY = "SELECTED_DAY"
        const val SELECTED_DAYOFWEEK = "SELECTED_DAYOFWEEK"
        const val SELECTED_TIME = "SELECTED_TIME"
    }
    // appbar 추가
    private lateinit var appbarTitle : TextView
    private lateinit var clearBtn : ImageView

    private lateinit var timeSelectAdapter: RecyclerView.Adapter<*>
    private lateinit var selectedDate: String
    private var selectedDateMillis: Long = 0L
    private lateinit var selectedYear: String
    private lateinit var selectedMonth: String
    private lateinit var selectedDay: String
    private lateinit var selectedDayOfWeek: String
    private lateinit var dayOfWeek: String
    private var selectedTime: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_arrival_calendar)

        // appbar 추가
        appbarTitle = findViewById<TextView?>(R.id.appbarTitle).apply {
            setText("도착일시")
        }
        clearBtn = findViewById<ImageView?>(R.id.clearBtn).apply {
            setOnClickListener(){
                finish()
            }
        }

        val calendarView: CalendarView = findViewById(R.id.arrivalCalendarView)
        val btnGet: Button = findViewById(R.id.btnArrivalDateSelect)
        val arrivalCalendar = Calendar.getInstance()
        val maxCalendar = Calendar.getInstance()
        maxCalendar.add(Calendar.DAY_OF_YEAR, 6)

        // Calendar 최소 날짜를 오늘 날짜로 설정
        calendarView.minDate = arrivalCalendar.timeInMillis

        // Calendar 지정가능한 날짜를 오늘로부터 7일간으로 설정
        calendarView.maxDate = maxCalendar.timeInMillis

        // 이전에 선택한 날짜를 calendarView에 지정
        calendarView.date = intent.getLongExtra(ARRIVAL_DATE, calendarView.minDate)
        arrivalCalendar.timeInMillis = intent.getLongExtra(ARRIVAL_DATE, calendarView.minDate)

        // 24시간
//        val times = listOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23) // 시간 리스트
        val timeToPosition = intent.getIntExtra(SELECTED_TIME, 0)
        timeSelectAdapter =
            TimeSelectAdapter(this, timeToPosition, this) // this : Calendar Activity 클래스 인스턴스, this:
        val timeSelectRecyclerView: RecyclerView = findViewById(R.id.arrivalTimeSelectRecyclerView)
        timeSelectRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        timeSelectRecyclerView.adapter = timeSelectAdapter

        // calendarView dateChangeListener 설정
        calendarView.setOnDateChangeListener { view, year, month, dayOfMonth ->
            arrivalCalendar.set(year, month, dayOfMonth)
        }

        // 날짜와 시간 값 확정
        btnGet.setOnClickListener {
            selectedDateMillis = arrivalCalendar.timeInMillis

            val dayOfWeekNumber: Int = arrivalCalendar.get(Calendar.DAY_OF_WEEK)

            when (dayOfWeekNumber) {
                1 -> dayOfWeek = "일"
                2 -> dayOfWeek = "월"
                3 -> dayOfWeek = "화"
                4 -> dayOfWeek = "수"
                5 -> dayOfWeek = "목"
                6 -> dayOfWeek = "금"
                7 -> dayOfWeek = "토"
            }

            selectedYear = arrivalCalendar.get(Calendar.YEAR).toString()
            selectedMonth = (arrivalCalendar.get(Calendar.MONTH) + 1).toString()
            selectedDay = (arrivalCalendar.get(Calendar.DAY_OF_MONTH)).toString()
            selectedDayOfWeek = dayOfWeek

            if (::timeSelectAdapter.isInitialized) {
                sendResult()
            }
        }
    }

    // TimeNumberClick
    override fun onTimeClick(time: Int) {
        selectedTime = time
    }

    private fun sendResult() {
        val resultIntent = Intent()
        resultIntent.putExtra(SELECTED_TIMESTAMP, selectedDateMillis)
        resultIntent.putExtra(SELECTED_YEAR, selectedYear)
        resultIntent.putExtra(SELECTED_MONTH, selectedMonth)
        resultIntent.putExtra(SELECTED_DAY, selectedDay)
        resultIntent.putExtra(SELECTED_DAYOFWEEK, selectedDayOfWeek)
        resultIntent.putExtra(SELECTED_TIME, selectedTime)

        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }

}
