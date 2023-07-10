package com.sdt.trproject.adapters

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.sdt.trproject.R
import com.sdt.trproject.ReservationDetailActivity
import com.sdt.trproject.ReservationSeatTicketListActivity
import com.sdt.trproject.services.*
import com.sdt.trproject.utils.RetrofitModule
import com.sdt.trproject.utils.requestBody
import dagger.hilt.android.qualifiers.ActivityContext
import org.json.JSONObject
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import javax.inject.Inject

class TrainReservationSeatTicketAdapter @Inject constructor(
    @ActivityContext private val context: Context,
    private var trainApiService: TrainApiService
) : RecyclerView.Adapter<TrainReservationSeatTicketAdapter.ViewHolder>(){

    private val reservationSeatTicketListItems: MutableList<RequestTrainReservationSeatTicketListItem> = mutableListOf()

    @SuppressLint("NotifyDataSetChanged")
    fun setData(data: List<RequestTrainReservationSeatTicketListItem>) {
        reservationSeatTicketListItems.clear()
        reservationSeatTicketListItems.addAll(data)
        println("ReservationList data : $data")
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TrainReservationSeatTicketAdapter.ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.list_item_reservation_seat_ticket, parent, false
        )
        return ViewHolder(itemView)    }

    override fun onBindViewHolder(holder: TrainReservationSeatTicketAdapter.ViewHolder, position: Int) {
        val item = reservationSeatTicketListItems[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int {
        return reservationSeatTicketListItems.size
    }

    inner class ViewHolder(
        itemView: View,
    ): RecyclerView.ViewHolder(itemView) {
        private val tvSeatTicketDepartDate: TextView = itemView.findViewById(R.id.tvSeatTicketDepartDate)
        private val tvSeatTicketDepartStation: TextView = itemView.findViewById(R.id.tvSeatTicketDepartStation)
        private val tvSeatTicketArriveStation: TextView = itemView.findViewById(R.id.tvSeatTicketArriveStation)
        private val tvSeatTicketDepartTime: TextView = itemView.findViewById(R.id.tvSeatTicketDepartTime)
        private val tvSeatTicketArriveTime: TextView = itemView.findViewById(R.id.tvSeatTicketArriveTime)
        private val tvSeatTicketTrainType: TextView = itemView.findViewById(R.id.tvSeatTicketTrainType)
        private val tvSeatTicketTrainNo: TextView = itemView.findViewById(R.id.tvSeatTicketTrainNo)
        private val tvSeatTicketAge: TextView = itemView.findViewById(R.id.tvSeatTicketAge)
        private val tvSeatTicketCarriageGrade: TextView = itemView.findViewById(R.id.tvSeatTicketCarriageGrade)
        private val tvSeatTicketCarriageNumber: TextView = itemView.findViewById(R.id.tvSeatTicketCarriageNumber)
        private val tvSeatTicketSeat: TextView = itemView.findViewById(R.id.tvSeatTicketSeat)
        private val tvSeatTicketReservationId: TextView = itemView.findViewById(R.id.tvSeatTicketReservationId)
        private val tvSeatTicketCurrentDate: TextView = itemView.findViewById(R.id.tvSeatTicketCurrentDate)
        private val tvSeatTicketCurrentTime: TextView = itemView.findViewById(R.id.tvSeatTicketCurrentTime)
        private val tvSeatTicketCharge: TextView = itemView.findViewById(R.id.tvSeatTicketCharge)
        private val tvSeatTicketDiscountCharge: TextView = itemView.findViewById(R.id.tvSeatTicketDiscountCharge)
        private val tvSeatTicketFinalCharge: TextView = itemView.findViewById(R.id.tvSeatTicketFinalCharge)

        private val currentDateFormat = SimpleDateFormat("yyyy년 MM월 dd일", Locale.getDefault())
        private val currentTimeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

        private val updateInterval = 1000L // 업데이트 간격 (1초)

        private val updateTimeRunnable = object : Runnable {
            override fun run() {
                updateCurrentTime()
                handler.postDelayed(this, updateInterval)
            }
        }

        private val handler = Handler(Looper.getMainLooper())

        init {
            // 현재 시간을 초기화
            updateCurrentTime()

            // 현재 시간을 실시간으로 업데이트하기 위해 Handler를 사용
            handler.postDelayed(updateTimeRunnable, updateInterval)
        }

        @SuppressLint("SetTextI18n")
        fun bind(item: RequestTrainReservationSeatTicketListItem) {
            val reservationId: String = item.reservationId
            val ticketId: String = item.ticketId
            val date: String = item.date
            val arriveTime: String = item.arriveTime
            val arriveStation: String = item.arriveStation
            val departTime: String = item.departTime
            val departStation: String = item.departStation
            val trainNo: String = item.trainNo
            val carriage: String = item.carriage
            val seat: String = item.seat
            val price: Int = item.price
            val discountedPrice: Int = item.discountedPrice
            val age: String = item.age
            val currentDate: String
            val currentTime: String
            val ageText = if (age == "adult") {
                "어른"
            } else if (age == "child") {
                "어린이"
            } else {
                "경로"
            }

            val discountCharge: Int = price - discountedPrice

            val formattedCharge = NumberFormat.getNumberInstance(Locale.getDefault()).format(price)
            val formattedTotalDiscountCharge = NumberFormat.getNumberInstance(Locale.getDefault()).format(discountCharge)
            val formattedFinalCharge = NumberFormat.getNumberInstance(Locale.getDefault()).format(discountedPrice)



            // 출발 일자 포맷 변경
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
            val originalTimeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            val targetTimeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            val parsedOriginalDepartTime = originalTimeFormat.parse(departTime)
            val parsedOriginalArriveTime = originalTimeFormat.parse(arriveTime)
            val formattedDepartTime = parsedOriginalDepartTime?.let { targetTimeFormat.format(it) }
            val formattedArriveTime = parsedOriginalArriveTime?.let { targetTimeFormat.format(it) }

            // TextView에 표시
            tvSeatTicketDepartDate.text = "${formattedDate}($dayOfWeek)"
            tvSeatTicketDepartStation.text = departStation
            tvSeatTicketDepartTime.text = formattedDepartTime
            tvSeatTicketArriveStation.text = arriveStation
            tvSeatTicketArriveTime.text = formattedArriveTime
            tvSeatTicketTrainNo.text = trainNo
            tvSeatTicketAge.text = ageText
            tvSeatTicketCarriageGrade.text =
                when(carriage) {
                    "1" -> {
                        "일반실"
                    }
                    "2" -> {
                        "특실"
                    }
                    else -> "-"
                }
            tvSeatTicketCarriageNumber.text =
                when(carriage) {
                    "1" -> {
                        "1호차"
                    }
                    "2" -> {
                        "2호차"
                    }
                    else -> "-호차"
                }
            tvSeatTicketSeat.text = seat
            tvSeatTicketCharge.text = formattedCharge
            tvSeatTicketDiscountCharge.text = formattedTotalDiscountCharge
            tvSeatTicketFinalCharge.text = formattedFinalCharge

        }

        // 현재시간 업데이트
        private fun updateCurrentTime() {
            val currentTime = Calendar.getInstance().time
            val currentDate = currentDateFormat.format(currentTime)
            val currentTimeString = currentTimeFormat.format(currentTime)

            tvSeatTicketCurrentDate.text = currentDate
            tvSeatTicketCurrentTime.text = currentTimeString
        }
    }
}