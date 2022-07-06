package com.qingniu.qnble.demo.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.qingniu.qnble.demo.R;
import com.qingniu.qnble.utils.QNLogUtils;
import com.qn.device.constant.QNBleRulerUnit;
import com.qn.device.listener.QNBleRulerListener;
import com.qn.device.listener.QNResultCallback;
import com.qn.device.out.QNBleApi;
import com.qn.device.out.QNBleRulerData;
import com.qn.device.out.QNBleRulerDevice;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author: hyr
 * @date: 2022/7/5 09:22
 * @desc:
 */
public class RulerActivity extends AppCompatActivity implements QNBleRulerListener {

    private final String TAG = "RulerActivity";

    @BindView(R.id.scanBtn)
    Button scanBtn;
    @BindView(R.id.macTv)
    TextView macTv;
    @BindView(R.id.lengthTv)
    TextView lengthTv;
    @BindView(R.id.unitTv)
    TextView unitTv;


    private boolean nowScaning;

    private boolean nowConnected;

    private QNBleRulerDevice qnBleRulerDevice;

    public static Intent getCallIntent(Context context) {
        return new Intent(context, RulerActivity.class);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ruler);
        ButterKnife.bind(this);
        QNBleApi.getInstance(this).setBleRulerListener(this);
        initData();
    }

    private void initData() {
        //扫描或断开
        scanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (nowScaning) {
                    return;
                }
                if (nowConnected) {
                    if (null != qnBleRulerDevice) {
                        QNBleApi.getInstance(RulerActivity.this).disconnectRulerDevice(qnBleRulerDevice, new QNResultCallback() {
                            @Override
                            public void onResult(int code, String msg) {

                            }
                        });
                    }
                } else {
                    QNBleApi.getInstance(RulerActivity.this).startBleDeviceDiscovery(new QNResultCallback() {
                        @Override
                        public void onResult(int code, String msg) {

                        }
                    });
                }
            }
        });
    }

    @Override
    public void onRulerDeviceDiscover(QNBleRulerDevice device) {
        QNLogUtils.logAndWrite(TAG, "onRulerDeviceDiscover "+device.toString());
        if (nowConnected && !nowScaning) {
            return;
        }
        nowScaning = false;
        nowConnected = true;
        QNBleApi.getInstance(RulerActivity.this).stopBleDeviceDiscovery(new QNResultCallback() {
            @Override
            public void onResult(int code, String msg) {

            }
        });
        qnBleRulerDevice = device;
        QNBleApi.getInstance(RulerActivity.this).connectRulerDevice(qnBleRulerDevice, new QNResultCallback() {
            @Override
            public void onResult(int code, String msg) {

            }
        });
    }

    @Override
    public void onRulerConnecting(QNBleRulerDevice device) {
        QNLogUtils.logAndWrite(TAG, "onRulerConnecting");
        scanBtn.setText(R.string.connecting);
    }

    @Override
    public void onRulerConnected(QNBleRulerDevice device) {
        QNLogUtils.logAndWrite(TAG, "onRulerConnected");
        macTv.setText(qnBleRulerDevice.getMac());
        nowConnected = true;
        scanBtn.setText(R.string.disconnected);
    }

    @Override
    public void onGetReceiveRealTimeData(QNBleRulerData data, QNBleRulerDevice device) {
        //QNLogUtils.logAndWrite(TAG, "onGetReceiveRealTimeData");
        lengthTv.setText("实时数据: "+ data.getValue());
        unitTv.setText((data.getUnit() == QNBleRulerUnit.QNBleRulerUnitCM) ? "cm" : "inch");
    }

    @Override
    public void onGetReceiveResultData(QNBleRulerData data, QNBleRulerDevice device) {
        //QNLogUtils.logAndWrite(TAG, "onGetReceiveResultData");
        lengthTv.setText("稳定数据: "+ data.getValue());
        unitTv.setText((data.getUnit() == QNBleRulerUnit.QNBleRulerUnitCM) ? "cm" : "inch");
    }

    @Override
    public void onRulerDisconnected(QNBleRulerDevice device) {
        QNLogUtils.logAndWrite(TAG, "onRulerDisconnected");
        nowConnected = false;
        scanBtn.setText(R.string.scan);
        macTv.setText("");
        lengthTv.setText("");
        unitTv.setText("");
    }

    @Override
    public void onRulerConnectFail(QNBleRulerDevice device) {
        QNLogUtils.logAndWrite(TAG, "onRulerConnectFail");
        nowConnected = false;
        scanBtn.setText(R.string.scan);
        macTv.setText("");
        lengthTv.setText("");
        unitTv.setText("");
    }

    @Override
    protected void onDestroy() {
        QNBleApi.getInstance(this).setBleRulerListener(null);
        if (null != qnBleRulerDevice) {
            QNBleApi.getInstance(this).disconnectRulerDevice(qnBleRulerDevice, new QNResultCallback() {
                @Override
                public void onResult(int code, String msg) {

                }
            });
        }
        super.onDestroy();
    }
}
