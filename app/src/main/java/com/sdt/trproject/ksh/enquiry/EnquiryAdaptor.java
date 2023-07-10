package com.sdt.trproject.ksh.enquiry;

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

public class EnquiryAdaptor extends RecyclerView.Adapter<EnquiryAdaptor.ViewHolder>{
    private List<BoardVo> mDataSet;

    public interface OnItemClickListener {
        void onItemClicked(BoardVo data);
    }


    private OnItemClickListener itemClickListener;


    public void setOnItemClickListener(OnItemClickListener listener) {
        itemClickListener = listener;
    }
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView title;
        private TextView isAnswered;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.enquiryTitle);
            isAnswered = itemView.findViewById(R.id.isAnswered);

        }
        public TextView getTitle() {
            return title;
        }
        public void setTitle(TextView title) {
            this.title = title;
        }


    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(
                parent.getContext()
        ).inflate(R.layout.item_enquriy, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = viewHolder.getAdapterPosition();
                if (position == RecyclerView.NO_POSITION) {
                    return;
                }
                BoardVo boardVo = mDataSet.get(position);
                itemClickListener.onItemClicked(boardVo);
            }
        });

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BoardVo context = mDataSet.get(position);
        holder.title.setText("제목 : " + context.getTitle());

        holder.isAnswered.setVisibility(context.getAnswered() != 0? View.VISIBLE : View.GONE);
    }

    // 어뎁터 생성시 mDataSet 초기화 안함
    public EnquiryAdaptor() {
    }

    //addAll 메서드로를 이용하여 mDataSet 초기화
    @SuppressLint("NotifyDataSetChanged")
    public void addAll(List<BoardVo> dataSet) {
        this.mDataSet = dataSet;
        notifyDataSetChanged();
    }
    public void clear(){
        this.mDataSet = null;
    }

    @Override
    public int getItemCount() {
        return mDataSet != null? mDataSet.size() : 0;
    }

}