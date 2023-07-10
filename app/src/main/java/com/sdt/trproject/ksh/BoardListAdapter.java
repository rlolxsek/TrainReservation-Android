package com.sdt.trproject.ksh;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sdt.trproject.R;
import com.sdt.trproject.ksh.vo.BoardVo;

import java.util.List;

public class BoardListAdapter extends RecyclerView.Adapter<BoardListAdapter.ViewHolder> {
    private List<BoardVo> mDataSet;

    public interface OnItemClickListener {
        void onItemClicked(int position,BoardVo data);
    }

    // OnItemClickListener 참조 변수 선언
    private OnItemClickListener itemClickListener;

    // OnItemClickListener 전달 메소드
    public void setOnItemClickListener (OnItemClickListener listener) {
        itemClickListener = listener;
    }
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView title;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);


        }

        public TextView getTitle() {
            return title;
        }
        public void setTitle(TextView title) {
            this.title = title;
        }
    }
    public BoardListAdapter() {
    }

    @NonNull
    @Override // ViewHolder 객체를 생성하여 리턴한다.
    public BoardListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(
                parent.getContext()
        ).inflate(R.layout.item_board, parent, false);
        BoardListAdapter.ViewHolder viewHolder = new BoardListAdapter.ViewHolder(view);

        //===== [Click 이벤트 구현을 위해 추가된 코드] =====================
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = viewHolder.getAdapterPosition();
                if (position == RecyclerView.NO_POSITION) {
                    return;
                }
                BoardVo boardVo = mDataSet.get(position);
                itemClickListener.onItemClicked(position,boardVo);
            }
        });
        //==================================================================

        return viewHolder;
    }

    @Override  // ViewHolder안의 내용을 position에 해당되는 데이터로 교체한다.
    public void onBindViewHolder(@NonNull BoardListAdapter.ViewHolder holder, int position) {
        if (mDataSet == null) {
            return;
        }

        BoardVo context = mDataSet.get(position);
        holder.title.setText(context.getTitle());

    }

    @Override // 전체 데이터의 갯수를 리턴한다.
    public int getItemCount() {
        return mDataSet != null ? mDataSet.size() : 0;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void addAll(List<BoardVo> dataSet) {
        this.mDataSet = dataSet;
        notifyDataSetChanged();
    }
    @SuppressLint("NotifyDataSetChanged")
    public void clear() {
        if (!mDataSet.isEmpty()) {
            this.mDataSet.clear();
        }
        notifyDataSetChanged();
    }
}
