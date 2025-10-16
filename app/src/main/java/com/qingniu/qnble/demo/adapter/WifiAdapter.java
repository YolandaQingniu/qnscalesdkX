package com.qingniu.qnble.demo.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.qingniu.qnble.demo.R;
import com.qingniu.qnble.demo.bean.WifiInfoModel;

import java.util.List;

public class WifiAdapter extends RecyclerView.Adapter<WifiAdapter.WifiViewHolder> {

    private final List<WifiInfoModel> wifiList;
    private OnItemClickListener listener;

    public WifiAdapter(List<WifiInfoModel> wifiList) {
        this.wifiList = wifiList;
    }

    @NonNull
    @Override
    public WifiViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_wifi, parent, false);
        return new WifiViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WifiViewHolder holder, int position) {
        WifiInfoModel model = wifiList.get(position);
        holder.tvName.setText(model.ssid);
        holder.tvLevel.setText(model.level + " dBm");

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onItemClick(model);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return wifiList.size();
    }

    static class WifiViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvLevel;

        WifiViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvLevel = itemView.findViewById(R.id.tvLevel);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(WifiInfoModel model);
    }

    public  void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

}

