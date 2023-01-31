package com.qingniu.qnble.demo.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.qingniu.qnble.demo.R;
import com.qingniu.qnble.demo.util.UserConst;
import com.qn.device.constant.QNScaleStatus;
import com.qn.device.listener.QNBleConnectionChangeListener;
import com.qn.device.listener.QNBleKitchenDataListener;
import com.qn.device.listener.QNResultCallback;
import com.qn.device.out.QNBleApi;
import com.qn.device.out.QNBleDevice;
import com.qn.device.out.QNBleKitchenDevice;

/**
 * Created by yangxiaobo
 * on 2021/9/9 11:00 AM
 * desc: 蓝牙厨房秤展示界面
 */
public class BleKitchenActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "BleKitchenActivity";
    private TextView macTv;
    private TextView stateTv;
    private TextView contentTv;

    public static Intent getCallIntent(Context context, QNBleKitchenDevice device) {
        return new Intent(context, BleKitchenActivity.class)
                .putExtra(UserConst.DEVICE, device);
    }

    private QNBleKitchenDevice device;

    private QNBleApi mQNBleApi;

    private boolean mIsConnected;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ble_kitchen);

        mQNBleApi = QNBleApi.getInstance(this);

        device = getIntent().getParcelableExtra(UserConst.DEVICE);

        macTv = findViewById(R.id.macTv);
        stateTv = findViewById(R.id.stateTv);
        contentTv = findViewById(R.id.contentTv);
        Button connectBtn = findViewById(R.id.connectBtn);
        Button disconnectBtn = findViewById(R.id.disconnectBtn);

        macTv.setText(device.getMac());
        connectBtn.setOnClickListener(this);
        disconnectBtn.setOnClickListener(this);

        initDataListener();
    }

    private void initDataListener() {
        mQNBleApi.setKitchenDataListener(new QNBleKitchenDataListener() {
            @Override
            public void onGetBleKitchenWeight(QNBleKitchenDevice device, double weight) {
                int unit = device.getUnit();
                double deviceWeight = device.getWeight();
                boolean peel = device.isPeel();
                boolean negative = device.isNegative();
                boolean overload = device.isOverload();
                boolean bluetooth = device.isBluetooth();
                boolean isStable = device.isStable();

                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("当前秤体显示的体重数值:")
                        .append(mQNBleApi.convertWeightWithTargetUnit(deviceWeight, unit)).append("\n")
                        .append(weight).append("(g)\n")
                        .append("是否去皮:")
                        .append(peel).append("\n")
                        .append("是否是负重量:")
                        .append(negative).append("\n")
                        .append("是否超载:")
                        .append(overload).append("\n")
                        .append("是否稳定:")
                        .append(isStable).append("\n")
                        .append("是否是蓝牙厨房秤:")
                        .append(bluetooth).append("\n");

                contentTv.setText(stringBuilder);
            }

            @Override
            public void onBleKitchenConnecting(QNBleKitchenDevice device) {
                setBleStatus(QNScaleStatus.STATE_CONNECTING);
            }

            @Override
            public void onBleKitchenConnected(QNBleKitchenDevice device) {
                setBleStatus(QNScaleStatus.STATE_CONNECTED);
            }

            @Override
            public void onBleKitchenDisconnected(QNBleKitchenDevice device) {
                setBleStatus(QNScaleStatus.STATE_DISCONNECTED);
            }

            @Override
            public void onBleKitchenError(QNBleKitchenDevice device, int errorCode) {
                Log.d("ConnectActivity", "onConnectError:" + errorCode);
                setBleStatus(QNScaleStatus.STATE_DISCONNECTED);
            }
        });
    }

    private void setBleStatus(int bleStatus) {
        String stateString;
        String btnString;
        switch (bleStatus) {
            case QNScaleStatus.STATE_CONNECTING: {
                stateString = getResources().getString(R.string.connecting);
                btnString = getResources().getString(R.string.disconnected);
                mIsConnected = true;
                break;
            }
            case QNScaleStatus.STATE_CONNECTED: {
                stateString = getResources().getString(R.string.connected);
                btnString = getResources().getString(R.string.disconnected);
                mIsConnected = true;
                break;
            }
            case QNScaleStatus.STATE_DISCONNECTING: {
                stateString = getResources().getString(R.string.disconnect_in_progress);
                btnString = getResources().getString(R.string.connect);
                mIsConnected = false;

                break;
            }
            case QNScaleStatus.STATE_LINK_LOSS: {
                stateString = getResources().getString(R.string.connection_disconnected);
                btnString = getResources().getString(R.string.connect);
                mIsConnected = false;
                break;
            }
            case QNScaleStatus.STATE_START_MEASURE: {
                stateString = getResources().getString(R.string.measuring);
                btnString = getResources().getString(R.string.disconnected);
                break;
            }
            case QNScaleStatus.STATE_REAL_TIME: {
                stateString = getResources().getString(R.string.real_time_weight_measurement);
                btnString = getResources().getString(R.string.disconnected);
                break;
            }
            case QNScaleStatus.STATE_BODYFAT: {
                stateString = getResources().getString(R.string.impedance_measured);
                btnString = getResources().getString(R.string.disconnected);
                break;
            }
            case QNScaleStatus.STATE_HEART_RATE: {
                stateString = getResources().getString(R.string.measuring_heart_rate);
                btnString = getResources().getString(R.string.disconnected);
                break;
            }
            case QNScaleStatus.STATE_MEASURE_COMPLETED: {
                stateString = getResources().getString(R.string.measure_complete);
                btnString = getResources().getString(R.string.disconnected);
                break;
            }
            case QNScaleStatus.STATE_WIFI_BLE_START_NETWORK:
                stateString = getResources().getString(R.string.start_set_wifi);
                btnString = getResources().getString(R.string.disconnected);
                Log.d("ConnectActivity", "开始设置WiFi");
                break;
            case QNScaleStatus.STATE_WIFI_BLE_NETWORK_FAIL:
                stateString = getResources().getString(R.string.failed_to_set_wifi);
                btnString = getResources().getString(R.string.disconnected);
                Log.d("ConnectActivity", "设置WiFi失败");
                break;
            case QNScaleStatus.STATE_WIFI_BLE_NETWORK_SUCCESS:
                stateString = getResources().getString(R.string.success_to_set_wifi);
                btnString = getResources().getString(R.string.disconnected);
                Log.d("ConnectActivity", "设置WiFi成功");
                break;
            default: {
                stateString = getResources().getString(R.string.connection_disconnected);
                btnString = getResources().getString(R.string.connect);
                mIsConnected = false;
                break;
            }
        }
        stateTv.setText(stateString);
//        mConnectBtn.setText(btnString);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mQNBleApi.disconnectDevice(device.getMac(), new QNResultCallback() {
            @Override
            public void onResult(int code, String msg) {
                Log.d(TAG, "断开连接");
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.connectBtn:
                mQNBleApi.connectBleKitchenDevice(device, new QNResultCallback() {
                    @Override
                    public void onResult(int code, String msg) {

                    }
                });
                break;
            case R.id.disconnectBtn:
                mQNBleApi.disconnectDevice(device.getMac(), new QNResultCallback() {
                    @Override
                    public void onResult(int code, String msg) {
                        Log.d(TAG, "断开连接");
                    }
                });
                break;
        }
    }
}
