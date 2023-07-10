package com.sdt.trproject.ksh.enquiry;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;

import com.sdt.trproject.R;
import com.sdt.trproject.ksh.InfoService;
import com.sdt.trproject.ksh.ResponseVo;
import com.sdt.trproject.ksh.RetrofitService;
import com.sdt.trproject.ksh.vo.BoardVo;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EnquiryInsertEnquiryFragment extends Fragment {


    private EditText insertEnquiryTitle;
    private EditText insertEnquiryContent;
    private AppCompatButton insertEnquiryButton;
    public EnquiryInsertEnquiryFragment() {
        // Required empty public constructor
    }


    public static EnquiryInsertEnquiryFragment newInstance() {
        EnquiryInsertEnquiryFragment fragment = new EnquiryInsertEnquiryFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_enquiry_insert, container, false);
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        insertEnquiryTitle = view.findViewById(R.id.enquiry_detail_title_textView);
        insertEnquiryContent = view.findViewById(R.id.enquiry_detail_content);
        insertEnquiryButton = view.findViewById(R.id.insertEnquiryBtn);

        insertEnquiryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = insertEnquiryTitle.getText().toString();
                String content = insertEnquiryContent.getText().toString();
                BoardVo input = new BoardVo();
                input.setTitle(title);
                input.setContent(content);
                selectEnquiry(input);
            }
        });
    }


    public void selectEnquiry(BoardVo input){
        InfoService apiService = RetrofitService.getApiService(
                requireContext(),
                RetrofitService.Option.createInstance().setSendCookie(true)
        );

        Call<ResponseVo<BoardVo>> mCall =  apiService.insert_enquiry(input);

        mCall.enqueue(new Callback<>() {
            //콜백 받는 부분
            @Override
            public void onResponse(@NonNull Call<ResponseVo<BoardVo>> call, @NonNull Response<ResponseVo<BoardVo>> response) {
                ResponseVo responses = response.body();
                if (responses.getResult().equals("success")) {
                    Toast.makeText(requireContext(), "문의 완료", Toast.LENGTH_LONG).show();

                    Activity activity = requireActivity();
                    if (!(activity instanceof EnquiryActivity)) {
                        return;
                    }
                    ((EnquiryActivity) activity).navigateToBack();

                }
            }

            @Override
            public void onFailure(Call<ResponseVo<BoardVo>> call, Throwable t) {
                failureMessage(t);

            }
        });
    }
    public void failureMessage(Throwable t) {
        Toast.makeText(requireContext(), "알수 없는 오류가 발생했습니다.\n어플리케이션을 새로 시작합니다", Toast.LENGTH_LONG).show();
        t.printStackTrace();
    }

}