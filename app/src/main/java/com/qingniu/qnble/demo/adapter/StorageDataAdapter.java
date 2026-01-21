package com.qingniu.qnble.demo.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.qingniu.qnble.demo.R;
import com.qingniu.qnble.demo.util.QNDataUtils;
import com.qingniu.qnble.demo.util.Utils;
import com.qingniu.qnble.demo.view.StorageDataDetailActivity;
import com.qn.device.out.QNScaleStoreData;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

/**
 * @author: yxb
 * @description:
 * @date: 2026/1/19
 */
public class StorageDataAdapter extends RecyclerView.Adapter<StorageDataAdapter.ViewHolder> {

    private ArrayList<QNScaleStoreData> qnScaleStoreDataList;
    private Context context;
    private SimpleDateFormat dateFormat;

    public StorageDataAdapter(Context context, ArrayList<QNScaleStoreData> qnScaleStoreDataList) {
        this.context = context;
        this.qnScaleStoreDataList = qnScaleStoreDataList;
        this.dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_storage_data, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        QNScaleStoreData storeData = qnScaleStoreDataList.get(position);

        // 设置体重
        holder.tvWeight.setText(String.format(Locale.US, "%.2f kg", storeData.getWeight()));
//        holder.tvFat.setText(storeData);

        // 设置测量时间
        holder.tvTime.setText(dateFormat.format(storeData.getMeasureTime()));

        // 设置数据完整状态
        boolean isComplete = storeData.isDataComplete();
        if (isComplete) {
            holder.tvDataStatus.setText("已知存储数据");
            holder.tvDataStatus.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
        } else {
            holder.tvDataStatus.setText("未知存储数据");
            holder.tvDataStatus.setTextColor(context.getResources().getColor(android.R.color.holo_orange_dark));
        }

        // 设置备注（如果有）
        holder.tvNote.setVisibility(View.VISIBLE);

        // 复制按钮点击事件
        holder.btnCopy.setOnClickListener(v -> {
            String copyText = storeData.getHmac();
            Utils.copyToClipboard(context, copyText);

            // 显示复制成功的提示
            Toast.makeText(context, "已复制到剪贴板", Toast.LENGTH_SHORT).show();
        });

        // 整个item的点击事件（可选）
        holder.itemView.setOnClickListener(v -> {
            // 可以添加item点击逻辑，如跳转到详情页
            QNDataUtils.qnScaleStoreData = storeData;
            context.startActivity(StorageDataDetailActivity.getCallIntent(context));
        });
    }

    @Override
    public int getItemCount() {
        return qnScaleStoreDataList != null ? qnScaleStoreDataList.size() : 0;
    }

    /**
     * 更新数据
     */
    public void updateData(ArrayList<QNScaleStoreData> newData) {
        this.qnScaleStoreDataList = newData;
        notifyDataSetChanged();
    }

    /**
     * 添加新数据
     */
    public void addData(QNScaleStoreData weightData) {
        qnScaleStoreDataList.add(weightData); // 添加到开头
        notifyItemInserted(0);
    }

    /**
     * 移除数据
     */
    public void removeData(int position) {
        if (position >= 0 && position < qnScaleStoreDataList.size()) {
            qnScaleStoreDataList.remove(position);
            notifyItemRemoved(position);
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvWeight;
        TextView tvFat;
        TextView tvTime;
        TextView tvDataStatus;
        TextView tvNote;
        Button btnCopy;

        ViewHolder(View itemView) {
            super(itemView);
            tvWeight = itemView.findViewById(R.id.tv_weight);
            tvFat = itemView.findViewById(R.id.tv_fat);
            tvTime = itemView.findViewById(R.id.tv_time);
            tvDataStatus = itemView.findViewById(R.id.tv_data_status);
            tvNote = itemView.findViewById(R.id.tv_note);
            btnCopy = itemView.findViewById(R.id.btn_copy);
        }
    }
}
