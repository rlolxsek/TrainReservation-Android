package com.sdt.trproject

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.GridView
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class StationSelectActivity: AppCompatActivity()  {

    companion object {
        const val DEPARTURE_STATION = "DEPARTURE_STATION"
        const val ARRIVAL_STATION = "ARRIVAL_STATION"
    }

    // appbar 작업
    private lateinit var appbarTitle : TextView
    private lateinit var clearBtn : ImageView

    private lateinit var stationSelectAdapter: GridView
    private var stationGridView: GridView? = null
    private lateinit var tvDepartureStation: TextView
    private lateinit var tvArrivalStation: TextView
    private var currentSelectedTextView: TextView? = null

    val resultIntent = Intent()

    private var isDeparture = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_station_select)

        // appbar 작업
        appbarTitle = findViewById<TextView?>(R.id.appbarTitle).apply {
            setText("역선택")
        }
        clearBtn = findViewById<ImageView?>(R.id.clearBtn).apply {
            setOnClickListener(){
                finish()
            }
        }

        stationGridView = findViewById(R.id.stationGridview)
        tvDepartureStation = findViewById(R.id.tvPreSelectedDepartureStation)
        tvArrivalStation = findViewById(R.id.tvPreSelectedArrivalStation)
        val btnStationSelect: Button = findViewById(R.id.btnStationSelect)

        isDeparture = intent.getBooleanExtra("isDeparture", true)

//        val resultIntent = Intent()
        val defaultDepartureStation = intent.getStringExtra(DEPARTURE_STATION) ?: ""
        val defaultArrivalStation = intent.getStringExtra(ARRIVAL_STATION) ?: ""
        resultIntent.putExtra(DEPARTURE_STATION, defaultDepartureStation)
        resultIntent.putExtra(ARRIVAL_STATION, defaultArrivalStation)

        tvDepartureStation.text = defaultDepartureStation
        tvArrivalStation.text = defaultArrivalStation


        if (isDeparture) {
            currentSelectedTextView = tvDepartureStation
        } else {
            currentSelectedTextView = tvArrivalStation
        }

        // TextView 에 클릭 이벤트 설정
        tvDepartureStation?.setOnClickListener {
            currentSelectedTextView = tvDepartureStation
        }

        tvArrivalStation?.setOnClickListener {
            currentSelectedTextView = tvArrivalStation
        }

        val stations = arrayOf("수서", "동탄", "평택지제", "천안아산", "오송", "수서", "김천구미", "서대구", "동대구", "신경주", "울산(통도사)", "부산", "공주", "익산", "정읍", "광주송정", "나주", "목포")
        val adapter = StationAdapter(this, stations)
        stationGridView?.adapter = adapter

        btnStationSelect.setOnClickListener {
            finish()
        }
    }

    private inner class StationAdapter(context: Context, stations: Array<String>) : ArrayAdapter<String>(context, 0, stations) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val button: Button
            if (convertView == null) {
                button = Button(context)
                button.setOnClickListener {
                    // Update the text of the currently selected TextView.
                    currentSelectedTextView?.text = getItem(position)
                    val selectedStation = getItem(position)
                    resultIntent.apply {
                        if ( currentSelectedTextView == tvDepartureStation ) {
                            putExtra(DEPARTURE_STATION, selectedStation)
                        } else {
                            putExtra(ARRIVAL_STATION, selectedStation)
                        }
                        putExtra("isDeparture", isDeparture)
                    }
                    setResult(Activity.RESULT_OK, resultIntent)
                }
            } else {
                button = convertView as Button
            }
            button.text = getItem(position)
            return button
        }
    }
}
