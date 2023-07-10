package com.sdt.trproject

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.os.PersistableBundle
import android.util.TypedValue
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import com.sdt.trproject.services.RequestTrainReservationCancelResponse
import com.sdt.trproject.services.RequestTrainReservationDetailItem
import com.sdt.trproject.services.RequestTrainReservationPaymentResponse
import com.sdt.trproject.services.TrainApiService
import com.sdt.trproject.utils.RetrofitModule
import com.sdt.trproject.utils.requestBody
import dagger.hilt.android.AndroidEntryPoint
import org.json.JSONObject
import org.w3c.dom.Text
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class ReservationDetailActivity : AppCompatActivity() {

    private lateinit var appbarTitle : TextView
    private lateinit var backBtn : ImageView

    private lateinit var data: String
    private lateinit var seat: String
    private lateinit var date: String
    private lateinit var arriveTime: String
    private lateinit var arriveStation: String
    private lateinit var departTime: String
    private lateinit var departStation: String
    private lateinit var carriage: String
    private var price: Int = 0
    private var discountedPrice: Int = 0
    private lateinit var trainNo: String
    private lateinit var age: String
    private lateinit var reservationId: String

    private lateinit var tvDepartStation: TextView
    private lateinit var tvDepartTime: TextView
    private lateinit var tvArriveStation: TextView
    private lateinit var tvArriveTime: TextView
    private lateinit var tvDepartDateTrainNo: TextView
    private lateinit var tvTotalCharge: TextView
    private var totalCharge: Int = 0
    private lateinit var tvTotalDiscountCharge: TextView
    private var totalDiscountCharge: Int = 0
    private lateinit var tvFinalCharge: TextView
    private var finalCharge: Int = 0
    private var totalDiscountedCharge: Int = 0

    private lateinit var btnGoToReservationCancellation: Button
    private lateinit var btnReservationPaymentConfirm: Button

    @Inject
    lateinit var trainApiService: TrainApiService
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reservation_detail)

        // appbar 추가
        appbarTitle = findViewById<TextView?>(R.id.appbarTitle).apply {
            setText("예약내역")
        }
        backBtn = findViewById<ImageView?>(R.id.back_btn).apply {
            setOnClickListener(){
                onBackPressed()
            }
        }

        tvDepartStation = findViewById(R.id.tvReservationDetailDepartStation)
        tvDepartTime = findViewById(R.id.tvReservationDetailDepartTime)
        tvArriveStation = findViewById(R.id.tvReservationDetailArriveStation)
        tvArriveTime = findViewById(R.id.tvReservationDetailArriveTime)
        tvDepartDateTrainNo = findViewById(R.id.tvReservationDetailDateTrainNo)
        tvTotalCharge = findViewById(R.id.tvTotalChargeNumericText)
        tvTotalDiscountCharge = findViewById(R.id.tvTotalDiscountChargeNumericText)
        tvFinalCharge = findViewById(R.id.tvFinalChargeNumericText)
        btnGoToReservationCancellation = findViewById(R.id.btnGoToReservationCancellation)
        btnReservationPaymentConfirm = findViewById(R.id.btnReservationPaymentConfirm)

        val linearLayout: LinearLayout = findViewById(R.id.ticketListLl)

        // dialog
        val dialogBuilder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.confirm_dialog, null)
        val tvConfirmMessage = dialogView.findViewById<TextView>(R.id.tvConfirmMessage)
        val confirmButton = dialogView.findViewById<Button>(R.id.btnConfirm)
        val cancelButton = dialogView.findViewById<Button>(R.id.btnCancel)

        dialogBuilder.setView(dialogView)
        val alertDialog = dialogBuilder.create()


        data = intent.getStringExtra("DATA") ?: ""
        val gson = Gson()
        val dataArray = gson.fromJson(data, Array<RequestTrainReservationDetailItem>::class.java)

        date = dataArray[0].date
        arriveTime = dataArray[0].arriveTime
        arriveStation = dataArray[0].arriveStation
        departTime = dataArray[0].departTime
        departStation = dataArray[0].departStation
        trainNo = dataArray[0].trainNo
        reservationId = dataArray[0].reservationId

        println("date:::: $date")
        println("arriveTime::: $arriveTime")
        println("departTime::: $departTime")

        // 날짜 포맷 변경
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
        val formattedDepartTime = targetTimeFormat.format(parsedOriginalDepartTime)
        val formattedArriveTime = targetTimeFormat.format(parsedOriginalArriveTime)

        // 출발역, 출발시간, 도착역, 도착시간 세팅
        tvDepartStation.text = departStation
        tvDepartTime.text = formattedDepartTime
        tvArriveStation.text = arriveStation
        tvArriveTime.text = formattedArriveTime

        // 출발일자, 요일, 열차 번호 세팅
        tvDepartDateTrainNo.text = "${formattedDate}(${dayOfWeek}) SRT ${trainNo}"

        // ticket 개수만큼 TextView 생성
        dataArray.forEach {
            carriage = it.carriage
            age = it.age
            seat = it.seat
            price = it.price
            discountedPrice = it.discountedPrice

            val carriageText = if (carriage == "1") {
                "1호차(일반실)"
            } else {
                "2호차(특실)"
            }
            val ageText = if (age == "adult") {
                "어른"
            } else if (age == "child") {
                "어린이"
            } else {
                "경로"
            }
            val seatText = "($seat)"

            totalCharge += price // 총 원금 합계
            totalDiscountedCharge += discountedPrice // 총 할입된 금액

            // 예 : 1호차(특실)(1C) 어른
            val ticketMessage: String = "${carriageText}${seatText} $ageText"

            val textView = TextView(this) // 새로운 TextView 인스턴스를 생성. this는 현재 액티비티
            val layoutParams = LinearLayout.LayoutParams( // LinearLayout 내에서 TextView의 레이아웃 속성을 설정하기 위한 LayoutParams인스턴스 생성
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            textView.text = ticketMessage

            layoutParams.setMargins(0, resources.getDimensionPixelSize(R.dimen.reservation_detail_text_view_margin_top), 0, 0)
            textView.layoutParams = layoutParams // TextView의 레이아웃 속성을 이전에 설정한 LayoutParams로 설정
            textView.setTextColor(ContextCompat.getColor(this, R.color.dark_gray))
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimensionPixelSize(R.dimen.reservation_detail_text_view_text_size).toFloat())
            textView.setTypeface(null, Typeface.NORMAL)

            linearLayout.addView(textView)
        }

        totalDiscountCharge = totalCharge - totalDiscountedCharge

        val formattedTotalCharge = NumberFormat.getNumberInstance(Locale.getDefault()).format(totalCharge)
        val formattedTotalDiscountCharge = NumberFormat.getNumberInstance(Locale.getDefault()).format(totalDiscountCharge)
        val formattedFinalCharge = NumberFormat.getNumberInstance(Locale.getDefault()).format(totalDiscountedCharge)

        // 운임 요금 (totalCharge)
        tvTotalCharge.text = "${formattedTotalCharge}원"

        // 할인 금액 (totalCharge - totalDiscountedCharge)
        tvTotalDiscountCharge.text = "(-) ${formattedTotalDiscountCharge}원"

        // 총 결제 금액 (finalCharge)
        tvFinalCharge.text = "${formattedFinalCharge}원"


        /* 취소 확정 페이지로 넘어가기 */
        btnGoToReservationCancellation.setOnClickListener {
            val intent = Intent(this, ReservationCancelActivity::class.java).apply {
                putExtra("RESERVATION_ID", reservationId)
            }

            startActivity(intent)
//            sendRequestReservationCancel(reservationId)
        }



        // 결제 확정 하기
        btnReservationPaymentConfirm.setOnClickListener {
            tvConfirmMessage.text = "정말로 결제를 완료 하시겠습니까?" // 다이얼로그 텍스트 변경
            alertDialog.show()

            confirmButton.setOnClickListener {
                // 확인 버튼 동작 처리

                sendRequestReservationPayment(reservationId)
                alertDialog.dismiss()
            }

            cancelButton.setOnClickListener {
                // 취소 버튼 동작 처리
                alertDialog.dismiss()
            }
        }
    }

    /* 결제 진행 */
    private fun sendRequestReservationPayment(id: String) {
        val jsonObject = JSONObject()
        jsonObject.put("reservationId", reservationId)

        val requestBody = jsonObject.requestBody()
        val call = trainApiService.requestTrainReservationPayment(requestBody)

        RetrofitModule.executeCall(
            call,
            onFailure = { message, httpCode ->
                println("reservationPayment 요청 실패 : Message = $message, HttpCode = $httpCode")
            },
            onSuccess = { response ->
                println("reservationPayment 요청 성공 : Response = $response")
                val intent = Intent(this, ReservationTicketListActivity::class.java)
                startActivity(intent)
                finish()
            }
        )
    }
}
