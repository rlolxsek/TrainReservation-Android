package com.sdt.trproject.ksh;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.sdt.trproject.R;
import com.sdt.trproject.ksh.vo.BoardVo;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ListFragment extends Fragment implements View.OnClickListener {
    private View mRootView;
    private BoardListAdapter mBoardAdapter;
    private EditText mSearchText;
    private List<BoardVo> boardVo;
    private ArrayList<Integer> integerArrayList;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if(mRootView != null) {
            return mRootView;
        }
        mRootView = inflater.inflate(R.layout.fragment_list, container, false);

        RecyclerView mRecyclerView = mRootView.findViewById(R.id.recyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mRecyclerView.getContext());
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mBoardAdapter = new BoardListAdapter();
        mRecyclerView.setAdapter(mBoardAdapter);

        ImageButton mSearchButton = mRootView.findViewById(R.id.search_btn);
        mSearchText = mRootView.findViewById(R.id.search_edt);
        mSearchButton.setOnClickListener(this);

        mBoardAdapter.setOnItemClickListener(new BoardListAdapter.OnItemClickListener() {
            @Override
            public void onItemClicked(int position, BoardVo data) {
                Activity activity = requireActivity();
                if (!(activity instanceof BoardActivity)) {
                    return;
                }
                ((BoardActivity) activity).navigate(DetailFragment.newInstance(position,integerArrayList));

            }
        });

        refresh();

        return mRootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        if (mRootView.getParent() != null) {
            ((ViewGroup)mRootView.getParent()).removeView(mRootView);
        }
        super.onDestroyView();
    }

    public void refresh() {

        InfoService apiService = RetrofitService.getApiService(
                requireContext(),
                RetrofitService.Option.createInstance()
        );
        String searchText = mSearchText.getText().toString();

        Call<ResponseVo<BoardVo>> mCall = searchText.length() == 0 ? apiService.get_board() : apiService.get_board_search(new BoardVo(searchText));

        mCall.enqueue(new Callback<>() {
            //콜백 받는 부분
            @Override
            public void onResponse(@NonNull Call<ResponseVo<BoardVo>> call, @NonNull Response<ResponseVo<BoardVo>> response) {
                ResponseVo<BoardVo> responses = response.body();
                if (responses.getResult().equals("success") && responses.getDataList().size() != 0) {
                    boardVo = responses.getDataList();

                    integerArrayList = new ArrayList<>();
                    for (BoardVo vo : boardVo) {
                        integerArrayList.add(vo.getIndex());
                    }
                    mBoardAdapter.addAll(boardVo);

                } else {
                    Toast.makeText(requireContext(), "정보 없다", Toast.LENGTH_LONG).show();
                    mBoardAdapter.clear();
                }
            }

            @Override
            public void onFailure(Call<ResponseVo<BoardVo>> call, Throwable t) {
                failureMessage(t);
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (v == null) {
            return;
        }
        switch (v.getId()) {
            case R.id.search_btn:
                refresh();
                break;
        }
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        mRootView = null;
    }
    public void failureMessage(Throwable t) {
        Toast.makeText(requireContext(), "알수 없는 오류가 발생했습니다.\n어플리케이션을 새로 시작합니다", Toast.LENGTH_LONG).show();
        t.printStackTrace();
    }
}