package com.sdt.trproject.ksh;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sdt.trproject.BuildConfig;
import com.sdt.trproject.SharedPrefKeys;


import java.io.IOException;


import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitService {
    private static SharedPreferences mSharedPreferences;

    public RetrofitService() {

    }

    private static final String BASE_URL = BuildConfig.SERVER_ADDR;

    public static InfoService getApiService(Context context, Option option){
        return getInstance(context, option).create(InfoService.class);
    }

    private static retrofit2.Retrofit getInstance(Context context, Option option){
        if(mSharedPreferences == null) {
            mSharedPreferences = context.getSharedPreferences(SharedPrefKeys.PREF_NAME, Context.MODE_PRIVATE);

        }

        Gson gson = new GsonBuilder().setLenient().create();
        OkHttpClient.Builder builder = new OkHttpClient.Builder();

        if(option.isSendCookie()) {
            builder = builder.addInterceptor(new CookieManagementInterceptor());
        }

        return new retrofit2.Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(builder.build())
                .build();
    }

    public static class Option {
        // 약식 빌더 패턴
        private boolean sendCookie = false;
        public boolean isSendCookie() {
            return sendCookie;
        }

        public Option setSendCookie(boolean sendCookie) {
            this.sendCookie = sendCookie;
            return this;
        }

        // 약식 팩토리 패턴
        public static Option createInstance() {
            return new Option();
        }
        private Option() {}

    }

    public static class CookieManagementInterceptor implements Interceptor {
        @Override
        public Response intercept(Chain chain) throws IOException {
            String cookie = mSharedPreferences.getString(SharedPrefKeys.SET_COOKIE, "");
            Request originalRequest = chain.request();
            Request.Builder builder = originalRequest.newBuilder()
                    .addHeader(SharedPrefKeys.COOKIE, cookie); // 쿠키 값을 헤더에 추가
            Request newRequest = builder.build();
            return chain.proceed(newRequest);
        }
    }
}
