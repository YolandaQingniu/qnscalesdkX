package com.qingniu.qnble.demo.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.qingniu.qnble.demo.R;
import com.qingniu.qnble.demo.adapter.ListAdapter;
import com.qingniu.qnble.demo.bean.User;
import com.qingniu.qnble.demo.util.UserConst;
import com.qingniu.scale.constant.DecoderConst;
import com.qn.device.constant.QNIndicator;
import com.qn.device.constant.QNScaleStatus;
import com.qn.device.constant.UserGoal;
import com.qn.device.constant.UserShape;
import com.qn.device.listener.QNBleConnectionChangeListener;
import com.qn.device.listener.QNLogListener;
import com.qn.device.listener.QNResultCallback;
import com.qn.device.listener.QNScaleDataListener;
import com.qn.device.out.QNBleApi;
import com.qn.device.out.QNBleDevice;
import com.qn.device.out.QNScaleData;
import com.qn.device.out.QNScaleItemData;
import com.qn.device.out.QNScaleStoreData;
import com.qn.device.out.QNUser;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


/**
 * author: ch
 * date: 2020/6/16
 * package_name: com.qingniu.qnble.demo
 * description:身高一体机界面
 */

public class HeightScaleActivity extends AppCompatActivity implements View.OnClickListener {


    public static Intent getCallIntent(Context context, User user, QNBleDevice device) {
        return new Intent(context, HeightScaleActivity.class)
                .putExtra(UserConst.USER, user)
                .putExtra(UserConst.DEVICE, device);
    }


    @BindView(R.id.connectBtn)
    Button mConnectBtn;
    @BindView(R.id.statusTv)
    TextView mStatusTv;
    @BindView(R.id.weightTv)
    TextView mWeightTv;
    @BindView(R.id.back_tv)
    TextView mBackTv;
    @BindView(R.id.listView)
    ListView mListView;

    private QNBleDevice mBleDevice;
    private List<QNScaleItemData> mDatas = new ArrayList<>();
    private QNBleApi mQNBleApi;

    private User mUser;

    private boolean mIsConnected;

    private ListAdapter listAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_height_scale);
        mQNBleApi = QNBleApi.getInstance(this);
        ButterKnife.bind(this);
        initIntent();
        initView();
        initData();
    }

    private void initData() {
        initBleConnectStatus();
        initUserData(); //设置数据监听器,返回数据,需在连接当前设备前设置
        //已经连接设备先断开设备,再连接
        if (mIsConnected) {
            doDisconnect();
        } else {
            connectQnDevice(mBleDevice); //连接当前设备
        }
    }

    private void initBleConnectStatus() {
        mQNBleApi.setBleConnectionChangeListener(new QNBleConnectionChangeListener() {
            //正在连接
            @Override
            public void onConnecting(QNBleDevice device) {
                setBleStatus(QNScaleStatus.STATE_CONNECTING);
            }

            //已连接
            @Override
            public void onConnected(QNBleDevice device) {
                setBleStatus(QNScaleStatus.STATE_CONNECTED);
            }

            @Override
            public void onServiceSearchComplete(QNBleDevice device) {

            }

            //正在断开连接，调用断开连接时，会马上回调
            @Override
            public void onDisconnecting(QNBleDevice device) {
                setBleStatus(QNScaleStatus.STATE_DISCONNECTING);
            }

            // 断开连接，断开连接后回调
            @Override
            public void onDisconnected(QNBleDevice device) {
                setBleStatus(QNScaleStatus.STATE_DISCONNECTED);
            }

            //出现了连接错误，错误码参考附表
            @Override
            public void onConnectError(QNBleDevice device, int errorCode) {
                Log.d("HeightScaleActivity", "onConnectError:" + errorCode);
                setBleStatus(QNScaleStatus.STATE_DISCONNECTED);
            }

            @Override
            public void onStartInteracting(QNBleDevice device) {

            }
        });
    }

    private void connectQnDevice(QNBleDevice device) {
        mQNBleApi.connectDevice(device, createQNUser(), new QNResultCallback() {
            @Override
            public void onResult(int code, String msg) {
                Log.d("HeightScaleActivity", "连接设备返回:" + msg);
            }
        });

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

        return mQNBleApi.buildUser(mUser.getUserId(),
                mUser.getHeight(), mUser.getGender(), mUser.getBirthDay(), mUser.getAthleteType(),
                userShape, userGoal, mUser.getClothesWeight(), new QNResultCallback() {
                    @Override
                    public void onResult(int code, String msg) {
                        Log.d("HeightScaleActivity", "创建用户信息返回:" + msg);
                    }
                });
    }


    private void initUserData() {
        mQNBleApi.setDataListener(new QNScaleDataListener() {
            @Override
            public void onGetUnsteadyWeight(QNBleDevice device, double weight) {
                Log.d("HeightScaleActivity", "体重是:" + weight);
                mWeightTv.setText(initWeight(weight));
            }

            @Override
            public void onGetScaleData(QNBleDevice device, QNScaleData data) {
                Log.d("HeightScaleActivity", "收到测量数据");
                onReceiveScaleData(data);
                QNScaleItemData fatValue = data.getItem(QNIndicator.TYPE_SUBFAT);
                if (fatValue != null) {
                    String value = fatValue.getValue() + "";
                    Log.d("HeightScaleActivity", "收到皮下脂肪数据:" + value);
                }
            }

            @Override
            public void onGetStoredScale(QNBleDevice device, List<QNScaleStoreData> storedDataList) {
                Log.d("HeightScaleActivity", "收到存储数据 " + storedDataList.size() + "条");
                if (storedDataList != null && storedDataList.size() > 0) {
                    QNScaleStoreData data = storedDataList.get(0);
                    for (int i = 0; i < storedDataList.size(); i++) {
                        Log.d("HeightScaleActivity", "收到存储数据:" + storedDataList.get(i).getWeight());
                    }
                    QNUser qnUser = createQNUser();
                    data.setUser(qnUser);
                    QNScaleData qnScaleData = data.generateScaleData();
                    onReceiveScaleData(qnScaleData);
                }
            }

            @Override
            public void onGetElectric(QNBleDevice device, int electric) {
                String text = "收到电池电量百分比:" + electric;
                Log.d("HeightScaleActivity", text);
                if (electric == DecoderConst.NONE_BATTERY_VALUE) {//获取电池信息失败
                    return;
                }
                Toast.makeText(HeightScaleActivity.this, text, Toast.LENGTH_SHORT).show();
            }

            //测量过程中的连接状态
            @Override
            public void onScaleStateChange(QNBleDevice device, int status) {
                Log.d("HeightScaleActivity", "秤的连接状态是:" + status);
                setBleStatus(status);
            }

            @Override
            public void onScaleEventChange(QNBleDevice device, int scaleEvent) {
                Log.d("HeightScaleActivity", "秤返回的事件是:" + scaleEvent);
            }

            @Override
            public void readSnComplete(QNBleDevice device, String sn) {

            }
        });
    }

    private void initIntent() {
        Intent intent = getIntent();
        if (intent != null) {
            mBleDevice = intent.getParcelableExtra(UserConst.DEVICE);
            mUser = intent.getParcelableExtra(UserConst.USER);
        }
    }

    private String initWeight(double weight) {
        int unit = mQNBleApi.getConfig().getUnit();
        return mQNBleApi.convertWeightWithTargetUnit(weight, unit);
    }

    private void initView() {
        mConnectBtn.setOnClickListener(this);
        mBackTv.setOnClickListener(this);
        listAdapter = new ListAdapter(mDatas, mQNBleApi, createQNUser());
        mListView.setAdapter(listAdapter);
        listAdapter.notifyDataSetChanged();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        doDisconnect();
        mQNBleApi.setBleConnectionChangeListener(null);
        mQNBleApi.setDataListener(null);
    }


    private void onReceiveScaleData(QNScaleData md) {
        mDatas.clear();
        /**
         * 增加身高显示
         */
        QNScaleItemData qnScaleItemData = new QNScaleItemData();
        qnScaleItemData.setName(getString(R.string.height));
        qnScaleItemData.setValue(md.getHeight());
        mDatas.add(qnScaleItemData);

        mDatas.addAll(md.getAllItem());

        listAdapter.notifyDataSetChanged();
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
                Log.d("HeightScaleActivity", "开始设置WiFi");
                break;
            case QNScaleStatus.STATE_WIFI_BLE_NETWORK_FAIL:
                stateString = getResources().getString(R.string.failed_to_set_wifi);
                btnString = getResources().getString(R.string.disconnected);
                Log.d("HeightScaleActivity", "设置WiFi失败");
                break;
            case QNScaleStatus.STATE_WIFI_BLE_NETWORK_SUCCESS:
                stateString = getResources().getString(R.string.success_to_set_wifi);
                btnString = getResources().getString(R.string.disconnected);
                Log.d("HeightScaleActivity", "设置WiFi成功");
                break;
            case QNScaleStatus.STATE_HEIGH_SCALE_MEASURE_FAIL:
                stateString = getResources().getString(R.string.measure_fail);
                btnString = getResources().getString(R.string.disconnected);
                Log.d("HeightScaleActivity","身高体重秤测量失败");
                break;
            default: {
                stateString = getResources().getString(R.string.connection_disconnected);
                btnString = getResources().getString(R.string.connect);
                mIsConnected = false;
                break;
            }
        }
        mStatusTv.setText(stateString);
        mConnectBtn.setText(btnString);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.connectBtn:
                if (mIsConnected) {
                    //已经连接,断开连接
                    this.doDisconnect();
                } else {
                    //断开连接,就开始连接
                    mDatas.clear();
                    listAdapter.notifyDataSetChanged();
                    this.doConnect();
                }
                break;
            case R.id.back_tv:
                doDisconnect();
                finish();
                break;
        }
    }


    private void doConnect() {
        if (mBleDevice == null || mUser == null) {
            return;
        }

        mQNBleApi.connectDevice(mBleDevice, createQNUser(), new QNResultCallback() {
            @Override
            public void onResult(int code, String msg) {
                Log.d("HeightScaleActivity", "连接设备返回:" + msg);
                if (code == 0) {
                    mIsConnected = true;
                }
            }
        });

    }

    private void doDisconnect() {
        mQNBleApi.disconnectDevice(mBleDevice, new QNResultCallback() {
            @Override
            public void onResult(int code, String msg) {
                Log.d("HeightScaleActivity", "断开连接设备返回:" + msg);
            }
        });
    }

}
