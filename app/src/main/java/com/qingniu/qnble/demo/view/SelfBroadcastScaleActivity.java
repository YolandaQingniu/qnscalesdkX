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
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.qingniu.qnble.demo.R;
import com.qingniu.qnble.demo.adapter.ListAdapter;
import com.qingniu.qnble.demo.bean.User;
import com.qingniu.qnble.demo.util.ToastMaker;
import com.qingniu.qnble.demo.util.UserConst;
import com.qingniu.qnble.utils.QNLogUtils;
import com.yolanda.health.qnblesdk.constant.CheckStatus;
import com.yolanda.health.qnblesdk.constant.QNDeviceType;
import com.yolanda.health.qnblesdk.constant.UserGoal;
import com.yolanda.health.qnblesdk.constant.UserShape;
import com.yolanda.health.qnblesdk.listener.QNResultCallback;
import com.yolanda.health.qnblesdk.out.QNBleApi;
import com.yolanda.health.qnblesdk.out.QNBleBroadcastDevice;
import com.yolanda.health.qnblesdk.out.QNBleDevice;
import com.yolanda.health.qnblesdk.out.QNScaleData;
import com.yolanda.health.qnblesdk.out.QNScaleItemData;
import com.yolanda.health.qnblesdk.out.QNUser;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by ch on 2019/12/31.
 */

public class SelfBroadcastScaleActivity extends AppCompatActivity {

    @BindView(R.id.setUnit)
    Button setUnit;
    @BindView(R.id.weightTv)
    TextView weightTv;
    @BindView(R.id.listView)
    ListView listView;
    @BindView(R.id.unit_edit)
    EditText unitEdit;

    private QNBleApi mQnbleApi;
    private QNBleDevice mBleDevice;
    private User mUser;
    private QNUser qnUser;
    private QNBleBroadcastDevice currentDevice;
    private List<QNScaleItemData> mDatas = new ArrayList<>();
    private int currentMeasureCode;
    private boolean isScanning;
    private ListAdapter listAdapter;

    private Handler mHandler = new Handler(Looper.myLooper());

    public static Intent getCallIntent(Context context, User user, QNBleDevice device) {
        return new Intent(context, SelfBroadcastScaleActivity.class)
                .putExtra(UserConst.USER, user)
                .putExtra(UserConst.DEVICE, device);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_broadcast_scale);
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

            if (qnBleDevice.getDeviceType() != QNDeviceType.SCALE_BROADCAST) {
                //非广播秤，直接返回
                return;
            }

            final QNBleBroadcastDevice broadcastDevice = mQnbleApi.buildBroadcastDevice(device, rssi, scanRecord, new QNResultCallback() {
                @Override
                public void onResult(int code, String msg) {

                    Log.e("buildBroadcastDevice", "结果--" + code + ",msg:---" + msg);

                }
            });

            if (broadcastDevice != null && broadcastDevice.getMac().equals(mBleDevice.getMac())) {
                currentDevice = broadcastDevice;
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        weightTv.setText(initWeight(broadcastDevice.getWeight()));
                        if (broadcastDevice.isComplete()) {
                            QNScaleData qnScaleData = broadcastDevice.generateScaleData(qnUser, new QNResultCallback() {
                                @Override
                                public void onResult(int code, String msg) {
                                    Log.e("generateScaleData", "结果" + code + ",msg:" + msg);
                                }
                            });
                            //此处用来去重
                            if (currentMeasureCode != broadcastDevice.getMeasureCode()) {
                                onReceiveScaleData(qnScaleData);
                            }
                            currentMeasureCode = broadcastDevice.getMeasureCode();
                            //停止扫描
                            stopScan();
                        }
                    }
                });
            }

        }
    };

    private void startScan() {
        BluetoothAdapter bluetoothAdapter = getBluetoothAdapter();

        if (bluetoothAdapter == null) {
            Toast.makeText(SelfBroadcastScaleActivity.this, getResources().getString(R.string.device_not_support), Toast.LENGTH_SHORT).show();
            return;
        }
        if (bluetoothAdapter.getState() != BluetoothAdapter.STATE_ON) {
            Toast.makeText(SelfBroadcastScaleActivity.this, getResources().getString(R.string.open_ble), Toast.LENGTH_SHORT).show();
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
            Toast.makeText(SelfBroadcastScaleActivity.this, getResources().getString(R.string.device_not_support), Toast.LENGTH_SHORT).show();
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
            mUser = intent.getParcelableExtra(UserConst.USER);
            qnUser = createQNUser();
        }
        listAdapter = new ListAdapter(mDatas,mQnbleApi,qnUser);
        listView.setAdapter(listAdapter);
        listAdapter.notifyDataSetChanged();
        //开启扫描
        startScan();

    }

    @OnClick(R.id.setUnit)
    public void onViewClicked() {
        int unit;
        if (TextUtils.isEmpty(unitEdit.getText().toString())) {
            ToastMaker.show(this, getResources().getString(R.string.set_unit_is_empty));
            return;
        } else {
            try {
                unit = Integer.parseInt(unitEdit.getText().toString());
            } catch (Exception e) {
                ToastMaker.show(this, getResources().getString(R.string.input_int));
                return;
            }
        }
        if (null == currentDevice) {
            ToastMaker.show(this, getResources().getString(R.string.device_set_empty));
            return;
        }
        currentDevice.syncUnit(unit, new QNResultCallback() {

            @Override
            public void onResult(int code, String msg) {
                Log.e("syncUnit", "结果" + code + ",msg:" + msg);
                ToastMaker.show(SelfBroadcastScaleActivity.this, code + ":" + msg);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopScan();
    }


    private String initWeight(double weight) {
        int unit = mQnbleApi.getConfig().getUnit();
        return mQnbleApi.convertWeightWithTargetUnit(weight, unit);
    }

    private void onReceiveScaleData(QNScaleData md) {
        mDatas.clear();
        mDatas.addAll(md.getAllItem());
        listAdapter.notifyDataSetChanged();
    }

    private QNUser createQNUser() {
        UserShape userShape;
        switch (mUser.getChoseShape()) {
            case 0:
                userShape = UserShape.SHAPE_NONE;
                break;
            case 1:
                userShape = UserShape.SHAPE_SLIM;
                break;
            case 2:
                userShape = UserShape.SHAPE_NORMAL;
                break;
            case 3:
                userShape = UserShape.SHAPE_STRONG;
                break;
            case 4:
                userShape = UserShape.SHAPE_PLIM;
                break;
            default:
                userShape = UserShape.SHAPE_NONE;
                break;
        }

        UserGoal userGoal;
        switch (mUser.getChoseGoal()) {
            case 0:
                userGoal = UserGoal.GOAL_NONE;
                break;
            case 1:
                userGoal = UserGoal.GOAL_LOSE_FAT;
                break;
            case 2:
                userGoal = UserGoal.GOAL_STAY_HEALTH;
                break;
            case 3:
                userGoal = UserGoal.GOAL_GAIN_MUSCLE;
                break;
            case 4:
                userGoal = UserGoal.POWER_OFTEN_EXERCISE;
                break;
            case 5:
                userGoal = UserGoal.POWER_LITTLE_EXERCISE;
                break;
            case 6:
                userGoal = UserGoal.POWER_OFTEN_RUN;
                break;
            default:
                userGoal = UserGoal.GOAL_NONE;
                break;
        }

        return mQnbleApi.buildUser(mUser.getUserId(),
                mUser.getHeight(), mUser.getGender(), mUser.getBirthDay(), mUser.getAthleteType(),
                userShape, userGoal, mUser.getClothesWeight(), new QNResultCallback() {
                    @Override
                    public void onResult(int code, String msg) {
                        Log.d("createQNUser", "创建用户信息返回:" + msg);
                    }
                });
    }
}

