package com.qingniu.qnble.demo.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.qingniu.qnble.demo.R;
import com.qingniu.qnble.demo.adapter.ListAdapter;
import com.qingniu.qnble.demo.bean.User;
import com.qingniu.qnble.demo.util.ToastMaker;
import com.qingniu.qnble.demo.util.UserConst;
import com.yolanda.health.qnblesdk.constant.UserGoal;
import com.yolanda.health.qnblesdk.constant.UserShape;
import com.yolanda.health.qnblesdk.listener.QNBleDeviceDiscoveryListener;
import com.yolanda.health.qnblesdk.listener.QNResultCallback;
import com.yolanda.health.qnblesdk.out.QNBleApi;
import com.yolanda.health.qnblesdk.out.QNBleBroadcastDevice;
import com.yolanda.health.qnblesdk.out.QNBleDevice;
import com.yolanda.health.qnblesdk.out.QNBleKitchenDevice;
import com.yolanda.health.qnblesdk.out.QNConfig;
import com.yolanda.health.qnblesdk.out.QNScaleData;
import com.yolanda.health.qnblesdk.out.QNScaleItemData;
import com.yolanda.health.qnblesdk.out.QNUser;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 广播秤数据解析界面
 */
public class BroadcastScaleActivity extends AppCompatActivity {

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
    private ListAdapter listAdapter;

    public static Intent getCallIntent(Context context, User user, QNBleDevice device) {
        return new Intent(context, BroadcastScaleActivity.class)
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

    private void initData() {
        mQnbleApi = QNBleApi.getInstance(this);
        Intent intent = getIntent();
        if (intent != null) {
            mBleDevice = intent.getParcelableExtra(UserConst.DEVICE);
            mUser = intent.getParcelableExtra(UserConst.USER);
            qnUser = createQNUser();
        }
        listAdapter = new ListAdapter(mDatas, mQnbleApi,qnUser);
        listView.setAdapter(listAdapter);
        listAdapter.notifyDataSetChanged();
        QNConfig mQnConfig = mQnbleApi.getConfig();
        mQnConfig.setAllowDuplicates(false);
        mQnConfig.setDuration(0);
        mQnConfig.setOnlyScreenOn(false);
        /**
         * 强化广播秤信号，这个只对广播秤有效
         */
        mQnConfig.setEnhanceBleBroadcast(true);
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
                if (null != device && device.getMac().equals(mBleDevice.getMac())) {
                    currentDevice = device;
                    weightTv.setText(initWeight(device.getWeight()));
                    if (device.isComplete()) {
                        QNScaleData qnScaleData = device.generateScaleData(qnUser, new QNResultCallback() {
                            @Override
                            public void onResult(int code, String msg) {
                                Log.e("generateScaleData", "结果" + code + ",msg:" + msg);
                            }
                        });
                        //此处用来去重
                        if (currentMeasureCode != device.getMeasureCode()) {
                            onReceiveScaleData(qnScaleData);
                        }
                        currentMeasureCode = device.getMeasureCode();
                    }
                }
            }

            @Override
            public void onKitchenDeviceDiscover(QNBleKitchenDevice device) {
                //厨房秤专用
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
                ToastMaker.show(BroadcastScaleActivity.this, code + ":" + msg);
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
