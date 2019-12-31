package com.qingniu.qnble.demo.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.qingniu.qnble.demo.R;
import com.qingniu.qnble.scanner.ScanResult;

import java.util.LinkedHashMap;

/**
 * @author: hekang
 * @description:展示设备的适配器
 * @date: 2018/6/13 10:31
 */
public class DeviceAdapter extends BaseAdapter {
    private LinkedHashMap<String, ScanResult> resultMap;
    private Context mContext;

    public DeviceAdapter(Context context, LinkedHashMap<String, ScanResult> scanResultMap) {
        this.mContext = context;
        this.resultMap = scanResultMap;
    }

    public void refresh(LinkedHashMap<String, ScanResult> scanResultList) {
//        this.resultMap.clear();
//        resultMap.putAll(scanResultList);
        this.resultMap = scanResultList;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return resultMap.size();
    }

    @Override
    public Object getItem(int position) {
        return resultMap.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_device, null);
        }
        TextView nameTv = (TextView) convertView.findViewById(R.id.nameTv);
        TextView macTv = (TextView) convertView.findViewById(R.id.macTv);
        TextView rssiTv = (TextView) convertView.findViewById(R.id.rssiTv);

        String key = (String) resultMap.keySet().toArray()[position];
        ScanResult scanResult = resultMap.get(key);

        nameTv.setText(scanResult.getLocalName());
        macTv.setText(scanResult.getMac());
        rssiTv.setText(String.valueOf(scanResult.getRssi()));


        return convertView;
    }

}
