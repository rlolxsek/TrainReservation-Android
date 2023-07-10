package com.sdt.trproject.utils

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sdt.trproject.services.RequestTrainReservationResponse
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.HttpException
import retrofit2.Response
import retrofit2.http.POST
import retrofit2.http.Path

fun JSONArray.requestBody(): RequestBody =
    this.toString().requestBody("application/json; charset=utf-8".toMediaTypeOrNull())

fun JSONObject.requestBody(): RequestBody =
    toString().requestBody("application/json; charset=utf-8".toMediaTypeOrNull())

fun String.requestBody(mediaType: MediaType? = null): RequestBody =
    toRequestBody(mediaType)


//interface A {
//    fun a() {}
//}
//
//interface B {
//    fun b() {}
//}
//
//abstract class AC {
//    fun ac() {}
//}
//
//
//
//fun call(a: A) {
//    if(a is AC) {
//
//    }
//}
//
//inline fun <reified G, T> callG(g: T) where T: A, T: AC, T: B{
//    g.a()
//    g.ac()
//    g.b()
//
//    var gg: G = G::class.java.newInstance()
//}

//fun <T,R> List<T>.fn(): List<R> {
//
//}

// 파일명: RetrofitExt.kt

object RetrofitModule {

//    interface ApiResponseCallback<T> {
//        fun onSuccess(response: T)
//        fun onFailure(errorMessage: String)
//    }

    // inline 함수로 정의. 제네릭 타입 T를 사용
    // 인자로 Retrofit의 Call<T> 객체, onFailure 람다 함수, onSuccess 람다 함수를 받음.
    fun <T> executeCall(
        call: Call<T>, // Retrofit의 Call<T> 객체
        // OnFailure 람다 함수, 문자열 메시지와 http 상태 코드를 인자로 받음. 기본값은 null
        onFailure: ((message: String, httpCode: Int) -> Unit)? = null,
        // onSuccess 람다 함수, 타입 T의 응답 객체를 인자로 받음.
        onSuccess: (response: T) -> Unit
    ) {
        // Retrofit의 enqueue 메소드를 사용하여 네트워크 요청을 비동기로 실행.
        call.enqueue(object : Callback<T> {
            override fun onResponse(call: Call<T>, response: Response<T>) {
                // 응답이 성공적인 경우 (200 ~ 299 상태 코드)
                if (response.isSuccessful) {
                    // 응답 객체가 존재하는지 확인 후 onSuccess 람다 함수를 호출
                    response.body()?.let {
                        onSuccess(it)
                    } ?: this.onFailure(
                        call, t = HttpException(
                            Response.error<T>(
                                response.code(),
                                "응답 값 없음".toResponseBody("text/plain".toMediaType())
                            )
                        )
                    ) // 응답 객체가 없는 경우 onFailure 람다 함수를 호출.
                } else {
                    // 응답이 실패할 경우 onFailure 람다 함수를 호출.
                    this.onFailure(
                        call, t = HttpException(
                            Response.error<T>(
                                response.code(),
                                "응답이 올바르지 않음.".toResponseBody("text/plain".toMediaType())
                            )
                        )
                    )
                }
            }

            // onFailure는 네트워크 요청이 실패했을 때 호출. (인터넷 연결 문제 등)
            override fun onFailure(call: Call<T>, t: Throwable) {
                // onFailure 람다 함수를 호출하고 Throwable 객체로부터 메시지를 전달.
                // HttpException 의 경우 상태 코드를 전달하고, 그렇지 않은 경우 0을 전달.
                onFailure?.invoke(t.message ?: "알 수 없는 에러", if (t is HttpException) t.code() else 0)
            }
        })
    }

//    fun <T> executeCall(call: Call<T>, callback: ApiResponseCallback<T>) {
//        call.enqueue(object : Callback<T> {
//            override fun onResponse(call: Call<T>, response: Response<T>) {
//                if (response.isSuccessful) {
//                    response.body()?.let {
//                        callback.onSuccess(it)
//                    } ?: callback.onFailure("Response body is null")
//                } else {
//                    callback.onFailure("Response error ${response.code()}")
//                }
//            }
//
//            override fun onFailure(call: Call<T>, t: Throwable) {
//                callback.onFailure(t.message ?: "Unknown error")
//            }
//        })
//    }
}

//interface RetrofitRequestService {
//    @POST("{path}")
//    fun onRequest(@Path("path") requestPath: String, requestBody: RequestBody): Call<ResponseBody>
//}


//inline fun <reified RequestTrainReservationResponse> Call<RequestTrainReservationResponse>.handle(
//    context: Context,
//    noinline onFail: ((message: String, httpCode: Int) -> Unit)? = null,
//    crossinline onSuccess: (response: RequestTrainReservationResponse) -> Unit
//) {
//    enqueue(object : Callback<RequestTrainReservationResponse> {
//        override fun onResponse(
//            call: Call<RequestTrainReservationResponse>,
//            response: Response<RequestTrainReservationResponse>
//        ) {
//            println("response??? : $response ")
//            if (!response.isSuccessful) {
//                onFailure(
//                    call, t = HttpException(
//                        Response.error<RequestTrainReservationResponse>(
//                            1000 + response.code(),
//                            "요청 실패".toResponseBody("text/plain".toMediaType())
//                        )
//                    )
//                )
//                return
//            }
//
//            val resp = response.body()
//            if (resp == null) {
//                onFailure(
//                    call, t = HttpException(
//                        Response.error<RequestTrainReservationResponse>(
//                            1000 + 500,
//                            "응답 없음".toResponseBody("text/plain".toMediaType())
//                        )
//                    )
//                )
//                return
//            }
//
//            val gson = Gson()
//            val jsonString = gson.toJson(resp)
//            println("jsonString123: ${jsonString}")
////            println("resp : ${resp is ResponseBody}")
////            val converted: RESPONSE = if(resp is ResponseBody) {
////                //val respBody = resp as ResponseBody
////                val type = object : TypeToken<RESPONSE>() {}.type
////                Gson().fromJson(resp.string(), RESPONSE::class.java)
////            } else {
////                RESPONSE::class.java.cast(resp)
////            }
//            onSuccess.invoke(resp)
//        }
//
//        override fun onFailure(call: Call<RequestTrainReservationResponse>, t: Throwable) {
//            onFail?.invoke(t.message ?: "", if (t is HttpException) t.code() else 0)
//                ?: context.showToast(t.message ?: "")
//        }
//    })
//}

//inline fun <reified RESPONSE, CONVERT> Call<RESPONSE>.handle(
//    context: Context,
//    noinline onFail: ((message: String, httpCode: Int) -> Unit)? = null,
//    crossinline onSuccess: (response: CONVERT) -> Unit
//) {
//    enqueue(object : Callback<RESPONSE> {
//        override fun onResponse(
//            call: Call<RESPONSE>,
//            response: Response<RESPONSE>
//        ) {
//            println("response.body() : ${response.body()}")
//            if (!response.isSuccessful) {
//                onFailure(
//                    call, t = HttpException(
//                        Response.error<CONVERT>(
//                            1000 + response.code(),
//                            "요청 실패".toResponseBody("text/plain".toMediaType())
//                        )
//                    )
//                )
//                return
//            }
//
//            val resp = response.body()
//            if (resp == null) {
//                onFailure(
//                    call, t = HttpException(
//                        Response.error<CONVERT>(
//                            1000 + 500,
//                            "응답 없음".toResponseBody("text/plain".toMediaType())
//                        )
//                    )
//                )
//                return
//            }
//
//            val gson = Gson()
//            val jsonString = gson.toJson(resp)
//            println("jsonString : $jsonString")
//            val converted: CONVERT = gson.fromJson(jsonString, object : TypeToken<CONVERT>() {}.type)
////            val converted: CONVERT = if(resp is ResponseBody) {
////                //val respBody = resp as ResponseBody
////                val type = object : TypeToken<CONVERT>() {}.type
////                val jsonString = resp.string()
////                gson.fromJson(jsonString, type)
////            } else {
////                gson.fromJson(gson.toJson(resp), CONVERT::class.java)
////            }
//            onSuccess.invoke(converted)
//        }
//
//        override fun onFailure(call: Call<RESPONSE>, t: Throwable) {
//            onFail?.invoke(t.message ?: "", if (t is HttpException) t.code() else 0)
//                ?: context.showToast(t.message ?: "")
//        }
//    })
//}