package com.sdt.trproject

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.RecyclerView
import com.sdt.trproject.adapters.TrainReservationTicketAdapter
import com.sdt.trproject.services.RequestTrainReservationListItem
import com.sdt.trproject.services.RequestTrainReservationListResponse
import com.sdt.trproject.services.TrainApiService
import com.sdt.trproject.utils.RetrofitModule
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ReservationTicketListActivity: AppCompatActivity() {

//    override val layoutRes: Int
//        get() = R.layout.activity_reservation_ticket_list

    // appbar 추가
    private lateinit var appbarTitle : TextView
    private lateinit var clearBtn : ImageView


    private lateinit var recyclerView: RecyclerView

    @Inject
    lateinit var adapter: TrainReservationTicketAdapter

    @Inject
    lateinit var trainApiService: TrainApiService

    private lateinit var tvEmptyReservationTicketList: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reservation_ticket_list)

        recyclerView = findViewById(R.id.reservationTicketRecyclerView)
        tvEmptyReservationTicketList = findViewById(R.id.tvEmptyReservationTicketList)

        // appbar 추가
        appbarTitle = findViewById<TextView?>(R.id.appbarTitle).apply {
            setText("승차권 확인")
        }
        clearBtn = findViewById<ImageView?>(R.id.clearBtn).apply {
            setOnClickListener(){
                finish()
            }
        }

        adapter = TrainReservationTicketAdapter(this, trainApiService)
        recyclerView.adapter = adapter

        // 로그인한 유저의 승차권 목록 가져오기
        val call = trainApiService.requestTrainReservationList()

        RetrofitModule.executeCall(
            call,
            onFailure = { message, httpCode ->
                println("TrainReservationList 요청 실패 : Message = $message, HttpCode = $httpCode")
            },
            onSuccess = { response ->
                println("TrainReservationList 요청 성공 : Response = $response")
                handleRequestTrainReservationListResponse(response)
            }
        )
    }

    private fun handleRequestTrainReservationListResponse(response: RequestTrainReservationListResponse?) {
        val responseData: List<RequestTrainReservationListItem>? = response?.data

        if ( responseData != null) {
            tvEmptyReservationTicketList.visibility = View.GONE
            adapter.setData(responseData)
        } else {
            tvEmptyReservationTicketList.visibility = View.VISIBLE
        }

    }

    private fun loginFun(loginBtnInAppbarFooter: TextView) {

        when (loginBtnInAppbarFooter.text.toString()) {

            AppbarKeys.LOG_IN -> {
                finish()
                val intent = Intent(this@ReservationTicketListActivity, LoginActivity::class.java)
                startActivity(intent)
            }


            else -> {
                Log.d("goLoginButton", "코드오류")

                val intent = Intent(this@ReservationTicketListActivity,LoginActivity::class.java)
                startActivity(intent)
            }
        }
    }
}
