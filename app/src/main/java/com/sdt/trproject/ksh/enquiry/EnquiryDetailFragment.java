package com.sdt.trproject.ksh.enquiry;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.sdt.trproject.R;
import com.sdt.trproject.ksh.InfoService;
import com.sdt.trproject.ksh.ResponseVo;
import com.sdt.trproject.ksh.RetrofitService;
import com.sdt.trproject.ksh.vo.AnswerVo;
import com.sdt.trproject.ksh.vo.BoardVo;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EnquiryDetailFragment extends Fragment {
    private static final String Index = "enquiryIndex";
    private int enquiryIndex;

    private TextView enquiryTitle;
    private TextView enquiryContent;
    private TextView enquiryWriter;
    private TextView enquiryCreatedDate;

    private ConstraintLayout answer;
    private EditText answerAnswer;
    private TextView answerWriter;
    private TextView answerCreateDate;
    private AppCompatButton btn;





    public EnquiryDetailFragment() {
    }

    public static EnquiryDetailFragment newInstance(int index) {
        EnquiryDetailFragment fragment = new EnquiryDetailFragment();
        Bundle args = new Bundle();
        args.putInt(Index, index);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            enquiryIndex = getArguments().getInt(Index);
            isAdmin();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_enquiry_answer_detail, container, false);
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        enquiryTitle = view.findViewById(R.id.enquiry_detail_title_textView);
        enquiryContent = view.findViewById(R.id.enquiry_detail_content);
        enquiryWriter = view.findViewById(R.id.enquiry_writer_id);
        enquiryCreatedDate = view.findViewById(R.id.enquiry_create_date);
        answer =view.findViewById(R.id.enquiry_answer_constraint);
        answerAnswer = view.findViewById(R.id.answer_answer);
        answerWriter = view.findViewById(R.id.answer_writer_id);
        answerCreateDate = view.findViewById(R.id.answer_created_date);


        btn = view.findViewById(R.id.enquiry_detail_insert);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(btn.getText().toString().equals("답변하기")){
                    btn.setText("입력하기");
                    answer.setVisibility(View.VISIBLE);
                    answerWriter.setVisibility(View.GONE);
                    answerCreateDate.setVisibility(View.GONE);
                }else if(btn.getText().toString().equals("입력하기")) {
                    String input = answerAnswer.getText().toString();
                    AnswerVo inputVo = new AnswerVo();
                    inputVo.setEnquiryIndex(enquiryIndex);
                    inputVo.setAnswer(input);
                    insertAnswer(inputVo);
                }else {
                    String input = answerAnswer.getText().toString();
                    updateAnswer(input);
                }
            }
        });
        getEnquiry();
    }

    public void getEnquiry() {

        InfoService apiService = RetrofitService.getApiService(
                requireContext(),
                RetrofitService.Option.createInstance().setSendCookie(true)
        );
        BoardVo vo = new BoardVo();
        vo.setIndex(enquiryIndex);
        Call<ResponseVo<BoardVo>> mCall =  apiService.select_enquiry(vo);

        mCall.enqueue(new Callback<>() {
            //콜백 받는 부분
            @Override
            public void onResponse(@NonNull Call<ResponseVo<BoardVo>> call, @NonNull Response<ResponseVo<BoardVo>> response) {
                ResponseVo<BoardVo> responses = response.body();
                if (responses.getResult().equals("success")) {
                    List<BoardVo> dataList = responses.getDataList();

                    enquiryTitle.setText(dataList.get(0).getTitle());
                    enquiryContent.setText(dataList.get(0).getContent());
                    enquiryCreatedDate.setText("작성일 : " + dataList.get(0).getFormattedCreatedDate().substring(0,10));
                    enquiryWriter.setText("작성자 : "+ dataList.get(0).getAuthor().toString());

                    if(dataList.get(0).getAnswered()==0){
                        answer.setVisibility(View.GONE);
                    }else {
                        getAnswer();
                    }
                } else {
                    Toast.makeText(requireContext(), "실패 ㅎ", Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onFailure(Call<ResponseVo<BoardVo>> call, Throwable t) {
                failureMessage(t);
            }
        });
    }


    public void getAnswer() {
        InfoService apiService = RetrofitService.getApiService(
                requireContext(),
                RetrofitService.Option.createInstance().setSendCookie(true)
        );
        AnswerVo vo = new AnswerVo();
        vo.setEnquiryIndex(enquiryIndex);
        Call<ResponseVo<AnswerVo>> mCall =  apiService.select_answer(vo);

        mCall.enqueue(new Callback<>() {
            //콜백 받는 부분
            @Override
            public void onResponse(@NonNull Call<ResponseVo<AnswerVo>> call, @NonNull Response<ResponseVo<AnswerVo>> response) {
                ResponseVo<AnswerVo> responses = response.body();
                if (responses.getResult().equals("success")) {
                    List<AnswerVo> dataList = responses.getDataList();
                    answerAnswer.setText(dataList.get(0).getAnswer());
                    answerWriter.setText("작성자 : " +dataList.get(0).getAuthor().toString());
                    if (dataList.get(0).getUpdatedDate() != null) {
                        answerCreateDate.setText("수정일 : " +dataList.get(0).getUpdatedDate().substring(0,10));
                    }
                    else{
                        answerCreateDate.setText("작성일 : " +dataList.get(0).getCreatedDate().substring(0,10));
                    }
                    btn.setText("수정하기");


                } else {
                    Toast.makeText(requireContext(), "실패 ㅎ", Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onFailure(Call<ResponseVo<AnswerVo>> call, Throwable t) {
                failureMessage(t);
            }
        });
    }
    public void updateAnswer(String inputAnswer) {

        InfoService apiService = RetrofitService.getApiService(
                requireContext(),
                RetrofitService.Option.createInstance().setSendCookie(true)
        );
        AnswerVo vo = new AnswerVo();
        vo.setEnquiryIndex(enquiryIndex);
        vo.setAnswer(inputAnswer);
        Call<ResponseVo<AnswerVo>> mCall =  apiService.updateAnswer(vo);

        mCall.enqueue(new Callback<>() {
            //콜백 받는 부분
            @Override
            public void onResponse(@NonNull Call<ResponseVo<AnswerVo>> call, @NonNull Response<ResponseVo<AnswerVo>> response) {
                ResponseVo<AnswerVo> responses = response.body();
                if (responses.getResult().equals("success")) {
                    Activity activity = requireActivity();
                    if (!(activity instanceof EnquiryActivity)) {
                        return;
                    }
                    ((EnquiryActivity) activity).navigateToBack();
                }
            }
            @Override
            public void onFailure(Call<ResponseVo<AnswerVo>> call, Throwable t) {
                failureMessage(t);
            }
        });
    }
    public void isAdmin() {

        InfoService apiService = RetrofitService.getApiService(
                requireContext(),
                RetrofitService.Option.createInstance().setSendCookie(true)
        );
        AnswerVo vo = new AnswerVo();
        vo.setEnquiryIndex(enquiryIndex);
        Call<ResponseVo> mCall =  apiService.isAdmin();

        mCall.enqueue(new Callback<>() {
            //콜백 받는 부분
            @Override
            public void onResponse(@NonNull Call<ResponseVo> call, @NonNull Response<ResponseVo> response) {
                ResponseVo responses = response.body();
                if (responses.getResult().equals("success")) {
                    btn.setVisibility(View.VISIBLE);
                } else {
                    btn.setVisibility(View.GONE);
                }
            }
            @Override
            public void onFailure(Call<ResponseVo> call, Throwable t) {
                failureMessage(t);
            }
        });


    }
    public void insertAnswer(AnswerVo vo) {

        InfoService apiService = RetrofitService.getApiService(
                requireContext(),
                RetrofitService.Option.createInstance().setSendCookie(true)
        );

        Call<ResponseVo<AnswerVo>> mCall =  apiService.insertAnswer(vo);

        mCall.enqueue(new Callback<>() {
            //콜백 받는 부분
            @Override
            public void onResponse(@NonNull Call<ResponseVo<AnswerVo>> call, @NonNull Response<ResponseVo<AnswerVo>> response) {
                ResponseVo<AnswerVo> responses = response.body();
                if (responses.getResult().equals("success")) {
                    Toast.makeText(requireContext(), "답변 완료", Toast.LENGTH_LONG).show();

                    Activity activity = requireActivity();
                    if (!(activity instanceof EnquiryActivity)) {
                        return;
                    }
                    ((EnquiryActivity) activity).navigateToBack();
                }

            }

            @Override
            public void onFailure(Call<ResponseVo<AnswerVo>> call, Throwable t) {
                failureMessage(t);
            }
        });


    }
    public void failureMessage(Throwable t) {
        Toast.makeText(requireContext(), "알수 없는 오류가 발생했습니다.\n어플리케이션을 새로 시작합니다", Toast.LENGTH_LONG).show();
        t.printStackTrace();
    }

}