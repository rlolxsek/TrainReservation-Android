package com.sdt.trproject

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.sdt.trproject.services.RequestTrainReservationCancelResponse
import com.sdt.trproject.services.RequestTrainReservationListResponse
import com.sdt.trproject.services.TrainApiService
import com.sdt.trproject.utils.RetrofitModule
import com.sdt.trproject.utils.requestBody
import dagger.hilt.android.AndroidEntryPoint
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@AndroidEntryPoint
class ReservationCancelActivity : AppCompatActivity() {

    @Inject
    lateinit var trainApiService: TrainApiService

    //appbar
    private lateinit var appbarTitle : TextView
    private lateinit var clearBtn : ImageView

    private lateinit var tvReservationCancelDepartDate: TextView
    private lateinit var tvReservationCancelTicketCount: TextView
    private lateinit var tvReservationCancelTrainNo: TextView
    private lateinit var tvReservationCancelRouteInfo: TextView
    private lateinit var btnReservationCancelConfirm: Button

    private lateinit var reservationId: String
    private lateinit var date: String
    private lateinit var arriveTime: String
    private lateinit var arriveStation: String
    private lateinit var departTime: String
    private lateinit var departStation: String
    private lateinit var trainNo: String
    private var ticketCnt: Int = 0
    private lateinit var createdDate: String
    private lateinit var expiredDate: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reservation_cancel)

        // appbar 추가
        appbarTitle = findViewById<TextView?>(R.id.appbarTitle).apply {
            setText("승차권 확인")
        }
        clearBtn = findViewById<ImageView?>(R.id.clearBtn).apply {
            setOnClickListener(){
                finish()
            }
        }

        tvReservationCancelDepartDate = findViewById(R.id.tvReservationCancelDepartDate)
        tvReservationCancelTicketCount = findViewById(R.id.tvReservationCancelTicketCount)
        tvReservationCancelTrainNo = findViewById(R.id.tvReservationCancelTrainNo)
        tvReservationCancelRouteInfo = findViewById(R.id.tvReservationCancelRouteInfo)
        btnReservationCancelConfirm = findViewById(R.id.btnReservationCancelConfirm)

        val targetReservationId: String? = intent.getStringExtra("RESERVATION_ID")

        // dialog
        val dialogBuilder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.confirm_dialog, null)
        val tvConfirmMessage = dialogView.findViewById<TextView>(R.id.tvConfirmMessage)
        val confirmButton = dialogView.findViewById<Button>(R.id.btnConfirm)
        val cancelButton = dialogView.findViewById<Button>(R.id.btnCancel)

        dialogBuilder.setView(dialogView)
        val alertDialog = dialogBuilder.create()


        // 현재 로그인된 유저의 예약 목록 불러오기
        // 예약 목록 중 취소하고자 하는 대상 ReservationId 와 일치하는 것만 표시

        if (targetReservationId != null) {
            sendRequestReservationList(targetReservationId)
            btnReservationCancelConfirm.setOnClickListener {
                tvConfirmMessage.text = "정말로 예약취소 하시겠습니까?" // 다이얼로그 텍스트 변경
                alertDialog.show()

                confirmButton.setOnClickListener {
                    // 확인 버튼 동작 처리
                    sendRequestReservationCancel(targetReservationId)
                    alertDialog.dismiss()
                }

                cancelButton.setOnClickListener {
                    // 취소 버튼 동작 처리
                    alertDialog.dismiss()
                }
            }

        } else {
            Log.e("reservationId", "reservationId is null")
            return
        }




    }

    // 승차권 리스트 요청
    private fun sendRequestReservationList(id: String) {
        val call = trainApiService.requestTrainReservationList()

        RetrofitModule.executeCall(
            call,
            onFailure = { message, httpCode ->
                println("reservationList 요청 실패 : Message = $message, HttpCode = $httpCode")
            },
            onSuccess = { response ->
                println("reservationList 요청 성공 : Response = $response")
                handleRequestTrainReservationListResponse(response, id)
            }
        )
    }

    @SuppressLint("SetTextI18n", "SimpleDateFormat")
    private fun handleRequestTrainReservationListResponse(response: RequestTrainReservationListResponse, targetReservationId: String) {
        val dataList = response.data

        dataList.forEach {
            if(targetReservationId == it.reservationId) {
                reservationId = it.reservationId
                date = it.date
                arriveTime = it.arriveTime
                arriveStation = it.arriveStation
                departTime = it.departTime
                departStation = it.departStation
                trainNo = it.trainNo
                ticketCnt = it.ticketCnt
                createdDate = it.formattedCreatedDate
                expiredDate = it.formattedExpiredDate
                
                return@forEach
            }
        }
        
        // TextView에 표시
        // 날짜 포맷변경
        val originalDateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val parsedOriginalDate = LocalDate.parse(date, originalDateFormat)
        val targetDateFormat = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일")
        val formattedDate = parsedOriginalDate.format(targetDateFormat)
        val dayOfWeek = when (parsedOriginalDate.dayOfWeek.toString()) {
            "SUNDAY" -> "일"
            "MONDAY" -> "월"
            "TUESDAY" -> "화"
            "WEDNESDAY" -> "수"
            "THURSDAY" -> "목"
            "FRIDAY" -> "금"
            "SATURDAY" -> "토"
            else -> ""
        }

        // 시간 포맷 변경
        val originalTimeFormat = SimpleDateFormat("HH:mm:ss")
        val targetTimeFormat = SimpleDateFormat("HH:mm")
        val parsedOriginalDepartTime = originalTimeFormat.parse(departTime)
        val parsedOriginalArriveTime = originalTimeFormat.parse(arriveTime)
        val formattedDepartTime = parsedOriginalDepartTime?.let { targetTimeFormat.format(it) }
        val formattedArriveTime = parsedOriginalArriveTime?.let { targetTimeFormat.format(it) }

        // 출발 일자, 티켓 갯수 세팅
        tvReservationCancelDepartDate.text = "${formattedDate}(${dayOfWeek})"
        tvReservationCancelTicketCount.text = "${ticketCnt}매"

        // 열차 번호 세팅
        tvReservationCancelTrainNo.text = "[SRT $trainNo]"

        // 출발역, 출발시간, 도착역, 도착시간 세팅
        tvReservationCancelRouteInfo.text = "${departStation}(${formattedDepartTime}) -> ${arriveStation}(${formattedArriveTime})"
    }

    // 취소 요청
    private fun sendRequestReservationCancel(id: String) {
        val reservationId: String = id

        val jsonObject = JSONObject()
        jsonObject.put("reservationId", reservationId)

        val requestBody = jsonObject.requestBody()
        val call = trainApiService.requestTrainReservationCancel(requestBody)

        RetrofitModule.executeCall(
            call,
            onFailure = { message, httpCode ->
                println("reservationCancel 요청 실패 : Message = $message, HttpCode = $httpCode")
            },
            onSuccess = { response ->
                println("reservationCancel 요청 성공 : Response = $response")
                handleRequestTrainReservationCancelResponse(response)
            }
        )
    }

    // 취소 성공 처리
    private fun handleRequestTrainReservationCancelResponse(response: RequestTrainReservationCancelResponse) {
        val intent = Intent(this, ReservationTicketListActivity::class.java)

        startActivity(intent)
    }

}