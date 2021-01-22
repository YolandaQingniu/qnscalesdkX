package com.qingniu.qnble.demo.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.qingniu.qnble.demo.R;
import com.qn.device.listener.QNBleDeviceDiscoveryListener;
import com.qn.device.listener.QNResultCallback;
import com.qn.device.out.QNBleApi;
import com.qn.device.out.QNBleBroadcastDevice;
import com.qn.device.out.QNBleDevice;
import com.qn.device.out.QNBleKitchenDevice;
import com.qn.device.out.QNConfig;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by ch on 2019/10/23.
 * 厨房秤数据解析界面
 */

public class kitchenScaleActivity extends AppCompatActivity {

    @BindView(R.id.macTv)
    TextView macTv;
    @BindView(R.id.modeIdTv)
    TextView modeIdTv;
    @BindView(R.id.weightTv)
    TextView weightTv;
    @BindView(R.id.peelTv)
    TextView peelTv;
    @BindView(R.id.negativeTv)
    TextView negativeTv;
    @BindView(R.id.overloadTv)
    TextView overloadTv;


    private QNBleApi mQnbleApi;
    private QNBleKitchenDevice mBleDevice;


    public static Intent getCallIntent(Context context) {
        return new Intent(context, kitchenScaleActivity.class);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kitchen_scale);
        ButterKnife.bind(this);
        initData();
    }

    private void initData() {
        mQnbleApi = QNBleApi.getInstance(this);
        QNConfig mQnConfig = mQnbleApi.getConfig();
        mQnConfig.setAllowDuplicates(false);
        mQnConfig.setDuration(0);
        mQnConfig.setOnlyScreenOn(false);
        //设置扫描对象
        mQnConfig.save(new QNResultCallback() {
            @Override
            public void onResult(int i, String s) {
                Log.d("ScanActivity", "initData:" + s);
            }
        });

        mQnbleApi.setBleDeviceDiscoveryListener(new QNBleDeviceDiscoveryListener() {
            @Override
            public void onDeviceDiscover(QNBleDevice device) {
                //发现设备
            }

            @Override
            public void onStartScan() {
                //开始扫描
            }

            @Override
            public void onStopScan() {
                //结束扫描
            }

            @Override
            public void onScanFail(int code) {
                //扫描失败
                Log.e("onScanFail", "反馈码" + code);
            }

            //蓝牙广播秤专用数据
            @Override
            public void onBroadcastDeviceDiscover(QNBleBroadcastDevice device) {

            }

            @Override
            public void onKitchenDeviceDiscover(QNBleKitchenDevice device) {
                if (null == mBleDevice) {
                    mBleDevice = device;
                }
                //厨房秤专用
                if (null != device && device.getMac().equals(mBleDevice.getMac())) {
                    macTv.setText(device.getMac());
                    modeIdTv.setText(device.getModeId());
                    peelTv.setText(device.isPeel() + "");
                    negativeTv.setText(device.isNegative() + "");
                    overloadTv.setText(device.isOverload() + "");
                    if (device.isNegative()) {
                        weightTv.setText("-" + mQnbleApi.convertWeightWithTargetUnit(device.getWeight(), device.getUnit()));
                    } else {
                        weightTv.setText(mQnbleApi.convertWeightWithTargetUnit(device.getWeight(), device.getUnit()));
                    }
                }
            }
        });
        mQnbleApi.startBleDeviceDiscovery(new QNResultCallback() {
            @Override
            public void onResult(int code, String msg) {
                //开启扫描
                Log.e("startBleDeviceDiscovery", "结果" + code + ",msg:" + msg);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mQnbleApi.stopBleDeviceDiscovery(new QNResultCallback() {
            @Override
            public void onResult(int code, String msg) {
                //结束扫描
            }
        });
    }
}
