package com.qingniu.qnble.demo.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.qingniu.qnble.demo.R;
import com.qingniu.qnble.demo.adapter.ListAdapter;
import com.qingniu.qnble.demo.dialog.InputDialog;
import com.qingniu.qnble.demo.util.QNDataUtils;
import com.qingniu.qnble.demo.util.Utils;
import com.qn.device.out.QNBleApi;
import com.qn.device.out.QNScaleData;
import com.qn.device.out.QNScaleItemData;
import com.qn.device.out.QNScaleStoreData;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

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

    private String mHmac;

    @BindView(R.id.listView)
    ListView mListView;

    @BindView(R.id.reCalcBtn)
    Button reCalcBtn;

    @OnClick(R.id.reCalcBtn)
    public void onReCalcBtnClick() {
        InputDialog dialog = new InputDialog(this);
        dialog.setTitle("请输入上一条Hmac，点击确认后，当前数据将重算");
        dialog.setOnInputListener(new InputDialog.OnInputListener() {
            @Override
            public void onConfirm(String inputText) {
                // 处理输入内容
                if (!TextUtils.isEmpty(inputText)) {

                    if (QNDataUtils.qnScaleStoreData != null) {
                        generateScaleData(QNDataUtils.qnScaleStoreData, inputText);
                        Toast.makeText(StorageDataDetailActivity.this,
                                "重算" + inputText, Toast.LENGTH_SHORT).show();
                    }

                } else {
                    Toast.makeText(StorageDataDetailActivity.this,
                            "输入内容为空", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancel() {
                Toast.makeText(StorageDataDetailActivity.this, "已取消", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    @BindView(R.id.copyHmacBtn)
    Button copyHmacBtn;

    @OnClick(R.id.copyHmacBtn)
    public void onCopyHmacBtnClick() {
        Utils.copyToClipboard(this, mHmac);
        Toast.makeText(StorageDataDetailActivity.this, "复制成功", Toast.LENGTH_SHORT).show();
    }

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
        generateScaleData(storeData, null);
    }

    private void generateScaleData(QNScaleStoreData storeData, String lastHmac) {
        if (storeData.isDataComplete()) {
            reCalcBtn.setVisibility(View.GONE);
            onReceiveScaleData(storeData.generateScaleData(lastHmac));
        } else {
            //未知存储数据，需要确认用户信息
            storeData.setUser(QNDataUtils.mQnUserScaleConfig.getCurUser());
            onReceiveScaleData(storeData.generateScaleData(lastHmac));
        }
    }

    private void onReceiveScaleData(QNScaleData md) {
        mDatas.clear();
        mDatas.addAll(md.getAllItem());
        listAdapter.notifyDataSetChanged();

        mHmac = md.getHmac();
    }

}
