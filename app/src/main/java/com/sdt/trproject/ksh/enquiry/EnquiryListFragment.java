package com.sdt.trproject.ksh.enquiry;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sdt.trproject.LoginActivity;
import com.sdt.trproject.R;
import com.sdt.trproject.ksh.InfoService;
import com.sdt.trproject.ksh.ResponseVo;
import com.sdt.trproject.ksh.RetrofitService;
import com.sdt.trproject.ksh.vo.BoardVo;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class EnquiryListFragment extends Fragment {

    private View mRootView;
    private RecyclerView mRecyclerView;
    private EnquiryAdaptor mEnquiryAdaptor;
    private List<BoardVo> mEnquiryList;
    private AppCompatButton goToEnquiryInsertBtn;


    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";


    private String mParam1;
    private String mParam2;

    public EnquiryListFragment() {
        // Required empty public constructor
    }

    public static EnquiryListFragment newInstance() {
        EnquiryListFragment fragment = new EnquiryListFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (mRootView != null) {
            return mRootView;
        }
        mRootView = inflater.inflate(R.layout.fragment_enquiry_list, container, false);

        goToEnquiryInsertBtn = mRootView.findViewById(R.id.goToEnquiryInsertBtn);
        goToEnquiryInsertBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Activity activity = requireActivity();
                if (!(activity instanceof EnquiryActivity)) {
                    return;
                }
                ((EnquiryActivity) activity).navigate(EnquiryInsertEnquiryFragment.newInstance());

            }
        });

        mRecyclerView = mRootView.findViewById(R.id.enquiry_recyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mRecyclerView.getContext());
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mEnquiryAdaptor = new EnquiryAdaptor();
        mRecyclerView.setAdapter(mEnquiryAdaptor);


        mEnquiryAdaptor.setOnItemClickListener(new EnquiryAdaptor.OnItemClickListener() {
            @Override
            public void onItemClicked(BoardVo data) {
                Activity activity = requireActivity();
                if (!(activity instanceof EnquiryActivity)) {
                    return;
                }
                ((EnquiryActivity) activity).navigate(EnquiryDetailFragment.newInstance(data.getIndex()));
            }
        });
        refresh();

        return mRootView;
    }


    @Override
    public void onStart() {
        super.onStart();
        refresh();
    }

    public void refresh() {

        InfoService apiService = RetrofitService.getApiService(
                requireContext(),
                RetrofitService.Option.createInstance().setSendCookie(true)
        );

        Call<ResponseVo<BoardVo>> mCall = apiService.get_enquiry();

        mCall.enqueue(new Callback<>() {
            //콜백 받는 부분
            @Override
            public void onResponse(@NonNull Call<ResponseVo<BoardVo>> call, @NonNull Response<ResponseVo<BoardVo>> response) {
                ResponseVo responses = response.body();
                if (responses.getResult().equals("success")) {
                    mEnquiryList = responses.getDataList();
                    mEnquiryAdaptor.addAll(mEnquiryList);

                } else {
                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                    startActivity(intent);
                    mEnquiryAdaptor.clear();
                }
            }

            @Override
            public void onFailure(Call<ResponseVo<BoardVo>> call, Throwable t) {
                Toast.makeText(requireContext(), "재요청", Toast.LENGTH_LONG).show();
                System.out.println(t.getMessage());
                t.printStackTrace();

            }
        });


    }
}