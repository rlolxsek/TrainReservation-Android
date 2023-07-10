package com.sdt.trproject.adapters

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.Button
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sdt.trproject.R

class TimeSelectAdapter(
    private val context: Context,

    private var selectedPosition: Int = RecyclerView.NO_POSITION, // 이전에 선택한 버튼의 위치를 추적하기 위한 변수
    private val onTimeClickListener: OnTimeClickListener
) : RecyclerView.Adapter<TimeSelectAdapter.TimeViewHolder>() {

    private var recyclerView: RecyclerView? = null
    private val times: List<Int> = listOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23) // 시간 리스트
    private var clickedPosition: Int = selectedPosition ?: 0

    // ViewHolder 클래스 정의
    inner class TimeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val buttonItem: Button = itemView.findViewById(R.id.buttonItem)
        private val departureRecyclerView: RecyclerView? = (context as Activity).findViewById(R.id.departureTimeSelectRecyclerView)
        private val arrivalRecyclerView: RecyclerView? = (context as Activity).findViewById(R.id.departureTimeSelectRecyclerView)
//        private val times: List<Int> = timeList

        fun bind(time: Int) {
            if (time < 10) {
                buttonItem.text = "0${time}시"
            } else {
                buttonItem.text = "${time}시"
            }

            onTimeClickListener.onTimeClick(clickedPosition)

            buttonItem.setOnClickListener {
                // 버튼 클릭 시 동작할 코드
                clickedPosition = adapterPosition
                if (clickedPosition != RecyclerView.NO_POSITION) {
                    updateButtonColor(clickedPosition)
                    onTimeClickListener.onTimeClick(time)
                }
            }

            // 이전에 선택한 버튼인 경우 색상을 업데이트
            if (adapterPosition == selectedPosition) {
                changeButtonColor(buttonItem, isSelected = true)
            } else {
                changeButtonColor(buttonItem, isSelected = false)
            }

            // 스크롤할 아이템을 가운데로 정렬
            buttonItem.viewTreeObserver.addOnGlobalLayoutListener(object :
                ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {

                    val layoutManager = departureRecyclerView?.layoutManager as? LinearLayoutManager
                    val offset = (layoutManager?.width ?: 0) / 2 - (buttonItem.width / 2)

                    layoutManager?.scrollToPositionWithOffset(selectedPosition, offset)

                    buttonItem.viewTreeObserver.removeOnGlobalLayoutListener(this)
                }
            })

//            buttonItem.post {
//                Log.d("버튼너비" , buttonItem.width.toString())
//            }
        }
    }


    // 버튼의 색상을 변경하는 함수
    private fun changeButtonColor(button: Button, isSelected: Boolean) {
        val color = if (isSelected) {
            Color.rgb(224, 212, 255)
        } else {
            // 선택하지 않은 버튼의 색상
            Color.TRANSPARENT
        }
        button.setBackgroundColor(color)
    }

    // 버튼의 색상을 업데이트하는 함수
    private fun updateButtonColor(position: Int) {
        val previousPosition = selectedPosition
        selectedPosition = position // 선택한 버튼의 위치 업데이트

        if (previousPosition != RecyclerView.NO_POSITION) {
            // 이전에 선택한 버튼이 있는 경우에만 해당 버튼의 색상 초기화
            notifyItemChanged(previousPosition)
        }

        if (selectedPosition != RecyclerView.NO_POSITION) {
            // 선택한 버튼의 색상 변경
            notifyItemChanged(selectedPosition)
        }
    }


    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        this.recyclerView = null
    }

    // 숫자 클릭 콜백을 정의하는 인터페이스
    interface OnTimeClickListener {
        fun onTimeClick(time: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.calendar_time_list_item, parent, false)
        return TimeViewHolder(view)
    }

    override fun onBindViewHolder(holder: TimeViewHolder, position: Int) {
        val time = times[position]
        holder.bind(time)
    }

    override fun getItemCount(): Int {
        return times.size
    }


}