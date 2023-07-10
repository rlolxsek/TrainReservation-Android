package com.sdt.trproject.services

import com.google.gson.annotations.SerializedName
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface TrainApiService {

    @Headers(
        "accept: application/json",
        "content-type: application/json",
    )

    @POST("/train/inquiry")
    fun requestTrainSchedule(@Body requestData: RequestBody): Call<RequestTrainScheduleResponse>

    @POST("/train/seat/list")
    fun requestTrainSeatList(): Call<RequestTrainSeatListResponse>

    @POST("/train/seat")
    fun requestTrainSeats(@Body requestData: RequestBody): Call<RequestTrainSeatsResponse>

    @POST("/train/time")
    fun requestTrainTimeTable(@Body requestData: RequestBody): Call<RequestTrainTimeTableResponse>

    @POST("/train/reservation")
    fun requestTrainReservation(@Body requestData: RequestBody): Call<RequestTrainReservationResponse>

    @POST("/train/reservation/detail")
    fun requestTrainReservationDetail(@Body requestData: RequestBody): Call<RequestTrainReservationDetailResponse>

    @POST("/train/reservation/payment")
    fun requestTrainReservationPayment(@Body requestData: RequestBody): Call<RequestTrainReservationPaymentResponse>

    @POST("/train/reservation/cancel")
    fun requestTrainReservationCancel(@Body requestData: RequestBody): Call<RequestTrainReservationCancelResponse>

    @POST("/train/reservation/list")
    fun requestTrainReservationList(): Call<RequestTrainReservationListResponse>

    @POST("/train/ticket")
    fun requestTrainSeatTicketList(@Body requestData: RequestBody): Call<RequestTrainReservationSeatTicketListResponse>

    @POST("/train/reservation/payment/refund")
    fun requestTrainReservationRefund(@Body requestData: RequestBody): Call<RequestTrainReservationRefundResponse>


//    @POST("{path}")
//    override fun onRequest(
//        @Path("path") requestPath: String,
//        @Body requestBody: RequestBody
//    ): Call<ResponseBody> {
//        println("requestPath : $requestPath")
//        println("requestBody : $requestBody")
//        return when (requestPath) {
//            TRAIN_RESERVATION -> {
//                println("TRAIN_RESERVATION")
//                requestTrainReservation(requestBody) as Call<ResponseBody>
//            }
//            else -> {
//                println("TRAIN_RESERVATION???")
//                requestTrainReservation(requestBody) as Call<ResponseBody>
//            }
//        }
//    }
}

data class RequestTrainScheduleItem(
    @SerializedName("adultcharge")
    val adultCharge: String,
    @SerializedName("arrplacename")
    val arrPlaceName: String,
    @SerializedName("arrplandtime")
    val arrPlandTime: String,
    @SerializedName("depplacename")
    val depPlaceName: String,
    @SerializedName("depplandtime")
    val depPlandTime: String,
    @SerializedName("traingradename")
    val trainGradeName: String,
    @SerializedName("trainno")
    val trainNo: Int,
    @SerializedName("date")
    val date: String,
    @SerializedName("common")
    val common: Boolean,
    @SerializedName("vip")
    val vip: Boolean
)

data class RequestTrainSeatsItem(
    @SerializedName("seat")
    val seat: String,
    @SerializedName("date")
    val date: String,
    @SerializedName("arriveTime")
    val arrivalTime: String,
    @SerializedName("arriveStation")
    val arrivalStation: String,
    @SerializedName("departTime")
    val departureTime: String,
    @SerializedName("departStation")
    val departureStation: String,
    @SerializedName("carriage")
    val carriage: Int,
    @SerializedName("trainNo")
    val trainNo: String
)

data class RequestTrainTime(
    @SerializedName("stationName")
    val stationName: String,
    @SerializedName("trainNo")
    val trainNo: Int,
    @SerializedName("departAt")
    val departAt: String
)

// 열차 시간표 조회 Response
data class RequestTrainScheduleResponse(
    // 응답 데이터 필드 정의
    @SerializedName("result")
    val result: String,
    @SerializedName("data")
    val data: List<RequestTrainScheduleItem>
)

// 열차 운행시간 표 Response
data class RequestTrainTimeTableResponse(
    @SerializedName("result")
    val result: String,
    @SerializedName("data")
    val data: List<RequestTrainTime>
)

// 열차 좌석 정보 Response
data class RequestTrainSeatsResponse(
    @SerializedName("result")
    val result: String,
    @SerializedName("data")
    val data: List<RequestTrainSeatsItem>
)

// 열차 예약하기 Response
data class RequestTrainReservationResponse(
    @SerializedName("result")
    val result: String,
    @SerializedName("data")
    val data: RequestTrainReservationItem,
    @SerializedName("message")
    val message: String
)

// 열차 예약하기 Response Data Item
data class RequestTrainReservationItem(
    @SerializedName("reservationId")
    val reservationId: String,
)

// 열차 호차별 좌석 정보 Response
data class RequestTrainSeatListResponse(
    @SerializedName("result")
    val result: String,
    @SerializedName("data")
    val data: RequestTrainSeatListItem,
    @SerializedName("message")
    val message: String
)

// 열차 호차별 좌석 정부 Response Data Item
data class RequestTrainSeatListItem(
    @SerializedName("standard")
    val standard: List<String>,
    @SerializedName("premium")
    val premium: List<String>
)

// 열차 예약 정보 Response
data class RequestTrainReservationDetailResponse(
    @SerializedName("result")
    val result: String,
    @SerializedName("data")
    val data: List<RequestTrainReservationDetailItem>,
    @SerializedName("message")
    val message: String
)

// 열차 예약 정보 Response Data Item
data class RequestTrainReservationDetailItem(
    @SerializedName("reservationId")
    val reservationId: String,
    @SerializedName("seat")
    val seat: String,
    @SerializedName("date")
    val date: String,
    @SerializedName("arriveTime")
    val arriveTime: String,
    @SerializedName("arriveStation")
    val arriveStation: String,
    @SerializedName("departTime")
    val departTime: String,
    @SerializedName("departStation")
    val departStation: String,
    @SerializedName("carriage")
    val carriage: String,
    @SerializedName("price")
    val price: Int,
    @SerializedName("discountedPrice")
    val discountedPrice: Int,
    @SerializedName("trainNo")
    val trainNo: String,
    @SerializedName("age")
    val age: String
)

// 열차 결제 Response
data class RequestTrainReservationPaymentResponse (
    @SerializedName("result")
    val result: String,
    @SerializedName("data")
    val data: String,
    @SerializedName("message")
    val message: String
)


// 열차 예약 취소 Response
data class RequestTrainReservationCancelResponse(
    @SerializedName("result")
    val result: String,
    @SerializedName("data")
    val data: String,
    @SerializedName("message")
    val message: String
)

// 승차권 확인 Response
data class RequestTrainReservationListResponse(
    @SerializedName("result")
    val result: String,
    @SerializedName("data")
    val data: List<RequestTrainReservationListItem>,
    @SerializedName("message")
    val message: String
)

// 승차권 확인 Response Data Item
data class RequestTrainReservationListItem(
    @SerializedName("reservationId")
    val reservationId: String,
    @SerializedName("date")
    val date: String,
    @SerializedName("arriveTime")
    val arriveTime: String,
    @SerializedName("arriveStation")
    val arriveStation: String,
    @SerializedName("departTime")
    val departTime: String,
    @SerializedName("departStation")
    val departStation: String,
    @SerializedName("trainNo")
    val trainNo: String,
    @SerializedName("ticketCnt")
    val ticketCnt: Int,
    @SerializedName("formattedCreatedDate")
    val formattedCreatedDate: String,
    @SerializedName("formattedExpiredDate")
    val formattedExpiredDate: String,
    @SerializedName("paymentId")
    val paymentId: String
)

// 승차권 좌석 티켓 Response
data class RequestTrainReservationSeatTicketListResponse(
    @SerializedName("result")
    val result: String,
    @SerializedName("data")
    val data: List<RequestTrainReservationSeatTicketListItem>,
    @SerializedName("message")
    val message: String
)

data class RequestTrainReservationSeatTicketListItem(
    @SerializedName("reservationId")
    val reservationId: String,
    @SerializedName("ticketId")
    val ticketId: String,
    @SerializedName("date")
    val date: String,
    @SerializedName("arriveTime")
    val arriveTime: String,
    @SerializedName("arriveStation")
    val arriveStation: String,
    @SerializedName("departTime")
    val departTime: String,
    @SerializedName("departStation")
    val departStation: String,
    @SerializedName("trainNo")
    val trainNo: String,
    @SerializedName("carriage")
    val carriage: String,
    @SerializedName("seat")
    val seat: String,
    @SerializedName("price")
    val price: Int,
    @SerializedName("discountedPrice")
    val discountedPrice: Int,
    @SerializedName("age")
    val age: String
)

// 승차권 환불 response
data class RequestTrainReservationRefundResponse(
    @SerializedName("result")
    val result: String,
    @SerializedName("message")
    val message: String,
)