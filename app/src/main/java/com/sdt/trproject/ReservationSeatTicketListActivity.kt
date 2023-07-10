package com.sdt.trproject

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sdt.trproject.adapters.TrainReservationSeatTicketAdapter
import com.sdt.trproject.services.RequestTrainReservationRefundResponse
import com.sdt.trproject.services.RequestTrainReservationSeatTicketListItem
import com.sdt.trproject.services.TrainApiService
import com.sdt.trproject.utils.RetrofitModule
import com.sdt.trproject.utils.requestBody
import dagger.hilt.android.AndroidEntryPoint
import org.json.JSONObject
import org.w3c.dom.Text
import javax.inject.Inject

@AndroidEntryPoint
class ReservationSeatTicketListActivity : AppCompatActivity() {

    private lateinit var btnReservationSeatTicketRefund: Button
    private lateinit var recyclerView: RecyclerView

    //appbar
    private lateinit var appbarTitle : TextView
    private lateinit var backBtn : ImageView

    @Inject
    lateinit var adapter: TrainReservationSeatTicketAdapter

    @Inject
    lateinit var trainApiService: TrainApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reservation_seat_ticket_list)

        appbarTitle = findViewById<TextView?>(R.id.appbarTitle).apply {
            setText("조회결과")
        }
        backBtn = findViewById<ImageView?>(R.id.back_btn).apply {
            setOnClickListener(){
                onBackPressed()
            }
        }

        val data: String? = intent.getStringExtra("DATA")
        val gson = Gson()
        val responseData: List<RequestTrainReservationSeatTicketListItem> =
            gson.fromJson(
                data,
                object : TypeToken<List<RequestTrainReservationSeatTicketListItem>>() {}.type
            )

        btnReservationSeatTicketRefund = findViewById(R.id.btnReservationSeatTicketRefund)

        recyclerView = findViewById(R.id.trainReservationSeatTicketRecyclerView)
        adapter = TrainReservationSeatTicketAdapter(this, trainApiService)
        recyclerView.adapter = adapter

        recyclerView.layoutManager = LinearLayoutManager(this) // LinearLayoutManager
        adapter.setData(responseData)


        // dialog
        val refundDialogBuilder = AlertDialog.Builder(this)
        val refundInflater = layoutInflater
        val refundDialogView = refundInflater.inflate(R.layout.dialog_refund_agree, null)
        val refundConfirmButton = refundDialogView.findViewById<TextView>(R.id.tvRefundConfirm)
        val refundCancelButton = refundDialogView.findViewById<TextView>(R.id.tvRefundCancel)

        refundDialogBuilder.setView(refundDialogView)
        val refundAlertDialog = refundDialogBuilder.create()



        btnReservationSeatTicketRefund.setOnClickListener {
            refundAlertDialog.show()

            refundConfirmButton.setOnClickListener {
                // 확인 버튼 동작 처리

                requestTrainReservationRefund(responseData[0].reservationId)
                refundAlertDialog.dismiss()

            }

            refundCancelButton.setOnClickListener {
                // 취소 버튼 동작 처리
                refundAlertDialog.dismiss()
            }
        }
    }

    private fun requestTrainReservationRefund(id: String) {
        // 승차권(reservationId) 환불 요청
        val jsonObject = JSONObject()
        jsonObject.put("reservationId", id)
        val requestBody = jsonObject.requestBody()
        val call = trainApiService.requestTrainReservationRefund(requestBody)

        RetrofitModule.executeCall(
            call,
            onFailure = { message, httpCode ->
                println("TrainReservationRefund 요청 실패 : Message = $message, HttpCode = $httpCode")
            },
            onSuccess = { response ->
                println("TrainReservationRefund 요청 성공 : Response = $response")
                handleRequestTrainReservationRefundResponse(response)
            }
        )
    }

    private fun handleRequestTrainReservationRefundResponse(response: RequestTrainReservationRefundResponse) {
        val intent = Intent(this, ReservationTicketListActivity::class.java)
        // dialog notice
        val noticeDialogBuilder = AlertDialog.Builder(this)
        val noticeInflater = layoutInflater
        val noticeDialogView = noticeInflater.inflate(R.layout.dialog_refund_notice, null)
        val noticeConfirmButton = noticeDialogView.findViewById<TextView>(R.id.tvNoticeConfirmation)

        noticeDialogBuilder.setView(noticeDialogView)
        val noticeAlertDialog = noticeDialogBuilder.create()

        noticeAlertDialog.show()

        noticeConfirmButton.setOnClickListener {
            // 확인 버튼 동작 처리
            noticeAlertDialog.dismiss()
            startActivity(intent)
            finish()
        }

    }
}