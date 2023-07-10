package com.sdt.trproject.services

import com.google.gson.annotations.SerializedName
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface SearchTrainScheduleApiService {
    @Headers("accept: application/json",
        "content-type: application/json")
//    @POST("/train/search")
    @POST("/train/inquiry")
    fun searchTrain(@Body requestData: RequestBody): Call<ApiResponse>
}

data class TrainItem(
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

data class ApiResponse (
    // 응답 데이터 필드 정의
    @SerializedName("result")
    val result: String,
    @SerializedName("data")
    val data: List<TrainItem>
)