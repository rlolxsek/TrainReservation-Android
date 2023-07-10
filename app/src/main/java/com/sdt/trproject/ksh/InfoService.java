package com.sdt.trproject.ksh;

import com.sdt.trproject.ksh.vo.AnswerVo;
import com.sdt.trproject.ksh.vo.BoardVo;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;


public interface InfoService {


    @Headers(value = {
        "accept: application/json" ,
        "content-type: application/json"
    })

    @POST("/info/board/select")
    Call<ResponseVo<BoardVo>> get_board() ;

    @POST("/info/board/select/search")
    Call<ResponseVo<BoardVo>> get_board_search(@Body BoardVo search);


    @POST("/info/board/select/index")
    Call<ResponseVo<BoardVo>> get_board_index(@Body BoardVo index);

    @POST("/info/enquiry/select")
    Call<ResponseVo<BoardVo>> get_enquiry() ;

    @POST("/info/enquiry/insert")
    Call<ResponseVo<BoardVo>> insert_enquiry(@Body BoardVo input);

    @POST("/info/enquiry/select/index")
    Call<ResponseVo<BoardVo>> select_enquiry(@Body BoardVo input);

    @POST("/info/answer/select/index")
    Call<ResponseVo<AnswerVo>> select_answer(@Body AnswerVo input);


    @POST("/info/answer/insert")
    Call<ResponseVo<AnswerVo>> insertAnswer(@Body AnswerVo input);

    @POST("/info/answer/update")
    Call<ResponseVo<AnswerVo>> updateAnswer(@Body AnswerVo input);

    @POST("/member/isAdmin")
    Call<ResponseVo> isAdmin();


}
