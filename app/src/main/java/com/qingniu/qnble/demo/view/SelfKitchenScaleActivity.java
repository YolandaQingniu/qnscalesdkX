package com.qingniu.qnble.demo.view;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.qingniu.qnble.demo.R;
import com.qingniu.qnble.demo.util.UserConst;
import com.qingniu.qnble.utils.QNLogUtils;
import com.qn.device.constant.CheckStatus;
import com.qn.device.constant.QNDeviceType;
import com.qn.device.listener.QNResultCallback;
import com.qn.device.out.QNBleApi;
import com.qn.device.out.QNBleDevice;
import com.qn.device.out.QNBleKitchenDevice;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by ch on 2019/10/23.
 * 自主厨房秤数据解析界面
 */

public class SelfKitchenScaleActivity extends AppCompatActivity {

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
    private QNBleDevice mBleDevice;
    private int currentMeasureCode;
    private boolean isScanning;

    private Handler mHandler = new Handler(Looper.myLooper());


    public static Intent getCallIntent(Context context, QNBleDevice device) {
        return new Intent(context, SelfKitchenScaleActivity.class)
                .putExtra(UserConst.DEVICE, device);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kitchen_scale);
        ButterKnife.bind(this);
        initData();
    }

    /**
     * 系统蓝牙扫描对象回调
     */
    private BluetoothAdapter.LeScanCallback scanCallback = new BluetoothAdapter.LeScanCallback() {

        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {

            if (device == null) {
                return;
            }

            QNBleDevice qnBleDevice = mQnbleApi.buildDevice(device, rssi, scanRecord, new QNResultCallback() {
                @Override
                public void onResult(int code, String msg) {
                    if (code != CheckStatus.OK.getCode()) {
                        QNLogUtils.log("LeScanCallback", msg);
                    }
                }
            });

            if (qnBleDevice == null) {
                //非公司秤直接返回
                return;
            }

            if (qnBleDevice.getDeviceType() != QNDeviceType.SCALE_KITCHEN) {
                //非厨房秤，直接返回
                return;
            }

            final QNBleKitchenDevice kitchenDevice = mQnbleApi.buildKitchenDevice(device, rssi, scanRecord, new QNResultCallback() {
                @Override
                public void onResult(int code, String msg) {

                    Log.e("buildKitchenDevice", "结果--" + code + ",msg:---" + msg);

                }
            });

            if (null != kitchenDevice && kitchenDevice.getMac().equals(mBleDevice.getMac())) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        macTv.setText(kitchenDevice.getMac());
                        modeIdTv.setText(kitchenDevice.getModeId());
                        peelTv.setText(kitchenDevice.isPeel() + "");
                        negativeTv.setText(kitchenDevice.isNegative() + "");
                        overloadTv.setText(kitchenDevice.isOverload() + "");
                        if (kitchenDevice.isNegative()) {
                            weightTv.setText("-" + mQnbleApi.convertWeightWithTargetUnit(kitchenDevice.getWeight(), kitchenDevice.getUnit()));
                        } else {
                            weightTv.setText(mQnbleApi.convertWeightWithTargetUnit(kitchenDevice.getWeight(), kitchenDevice.getUnit()));
                        }
                    }
                });

            }

        }
    };

    private void startScan() {
        BluetoothAdapter bluetoothAdapter = getBluetoothAdapter();

        if (bluetoothAdapter == null) {
            Toast.makeText(SelfKitchenScaleActivity.this, getResources().getString(R.string.device_not_support), Toast.LENGTH_SHORT).show();
            return;
        }
        if (bluetoothAdapter.getState() != BluetoothAdapter.STATE_ON) {
            Toast.makeText(SelfKitchenScaleActivity.this, getResources().getString(R.string.open_ble), Toast.LENGTH_SHORT).show();
            return;
        }

        isScanning = bluetoothAdapter.startLeScan(scanCallback);
    }

    private BluetoothAdapter getBluetoothAdapter() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            return bluetoothManager == null ? null : bluetoothManager.getAdapter();
        } else {
            return BluetoothAdapter.getDefaultAdapter();
        }
    }

    private void stopScan() {
        BluetoothAdapter bluetoothAdapter = getBluetoothAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(SelfKitchenScaleActivity.this, getResources().getString(R.string.device_not_support), Toast.LENGTH_SHORT).show();
            return;
        }
        bluetoothAdapter.stopLeScan(scanCallback);
        isScanning = false;
    }

    private void initData() {
        mQnbleApi = QNBleApi.getInstance(this);
        Intent intent = getIntent();
        if (intent != null) {
            mBleDevice = intent.getParcelableExtra(UserConst.DEVICE);
        }
        //开启扫描
        startScan();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopScan();
    }


}

