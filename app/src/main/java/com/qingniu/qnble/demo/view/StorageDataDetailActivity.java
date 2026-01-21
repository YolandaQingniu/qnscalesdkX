package com.qingniu.qnble.demo.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.qingniu.qnble.demo.R;
import com.qingniu.qnble.demo.adapter.ListAdapter;
import com.qingniu.qnble.demo.util.QNDataUtils;
import com.qn.device.out.QNBleApi;
import com.qn.device.out.QNScaleData;
import com.qn.device.out.QNScaleItemData;
import com.qn.device.out.QNScaleStoreData;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author: yxb
 * @description:
 * @date: 2026/1/20
 */
public class StorageDataDetailActivity extends AppCompatActivity {

    public static Intent getCallIntent(Context context) {
        Intent intent = new Intent(context, StorageDataDetailActivity.class);
        return intent;
    }

    private final List<QNScaleItemData> mDatas = new ArrayList<>();

    private ListAdapter listAdapter;

    private QNBleApi mQNBleApi;

    @BindView(R.id.listView)
    ListView mListView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_storage_data_detail);

        ButterKnife.bind(this);

        mQNBleApi = QNBleApi.getInstance(this);

        listAdapter = new ListAdapter(mDatas, mQNBleApi, QNDataUtils.mQnUserScaleConfig.getCurUser(), QNDataUtils.mBleDevice);
        mListView.setAdapter(listAdapter);
        listAdapter.notifyDataSetChanged();

        if (QNDataUtils.qnScaleStoreData != null) {
            generateScaleData(QNDataUtils.qnScaleStoreData);
        }
    }

    private void generateScaleData(QNScaleStoreData storeData) {
        if (storeData.isDataComplete()) {
            onReceiveScaleData(storeData.generateScaleData());
        } else {
            //未知存储数据，需要确认用户信息
            storeData.setUser(QNDataUtils.mQnUserScaleConfig.getCurUser());
            onReceiveScaleData(storeData.generateScaleData());
        }
    }

    private void onReceiveScaleData(QNScaleData md) {
        mDatas.clear();
        mDatas.addAll(md.getAllItem());
        listAdapter.notifyDataSetChanged();
    }
}
