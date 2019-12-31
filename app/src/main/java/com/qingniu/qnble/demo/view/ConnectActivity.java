package com.qingniu.qnble.demo.view;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.qingniu.qnble.demo.R;
import com.qingniu.qnble.demo.adapter.ListAdapter;
import com.qingniu.qnble.demo.bean.User;
import com.qingniu.qnble.demo.util.AndroidPermissionCenter;
import com.qingniu.qnble.demo.util.ToastMaker;
import com.qingniu.qnble.demo.util.UserConst;
import com.qingniu.qnble.utils.QNLogUtils;
import com.qingniu.scale.constant.DecoderConst;
import com.yolanda.health.qnblesdk.constant.CheckStatus;
import com.yolanda.health.qnblesdk.constant.QNIndicator;
import com.yolanda.health.qnblesdk.constant.QNScaleStatus;
import com.yolanda.health.qnblesdk.constant.UserGoal;
import com.yolanda.health.qnblesdk.constant.UserShape;
import com.yolanda.health.qnblesdk.listener.QNBleConnectionChangeListener;
import com.yolanda.health.qnblesdk.listener.QNLogListener;
import com.yolanda.health.qnblesdk.listener.QNResultCallback;
import com.yolanda.health.qnblesdk.listener.QNScaleDataListener;
import com.yolanda.health.qnblesdk.out.QNBleApi;
import com.yolanda.health.qnblesdk.out.QNBleDevice;
import com.yolanda.health.qnblesdk.out.QNScaleData;
import com.yolanda.health.qnblesdk.out.QNScaleItemData;
import com.yolanda.health.qnblesdk.out.QNScaleStoreData;
import com.yolanda.health.qnblesdk.out.QNUser;
import com.yolanda.health.qnblesdk.out.QNWiFiConfig;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.qingniu.qnble.demo.R.id.connectBtn;

/**
 * Created by ch on 2019/12/31.
 */

public class ConnectActivity extends AppCompatActivity implements View.OnClickListener {


    public static Intent getCallIntent(Context context, User user, QNBleDevice device) {
        return new Intent(context, ConnectActivity.class)
                .putExtra(UserConst.USER, user)
                .putExtra(UserConst.DEVICE, device);
    }

    public static Intent getCallIntent(Context context, User user, QNBleDevice device, QNWiFiConfig qnWiFiConfig) {
        return new Intent(context, ConnectActivity.class)
                .putExtra(UserConst.USER, user)
                .putExtra(UserConst.DEVICE, device)
                .putExtra(UserConst.WIFI_CONFIG, qnWiFiConfig);
    }

    @BindView(connectBtn)
    Button mConnectBtn;
    @BindView(R.id.statusTv)
    TextView mStatusTv;
    @BindView(R.id.weightTv)
    TextView mWeightTv;
    @BindView(R.id.back_tv)
    TextView mBackTv;
    @BindView(R.id.listView)
    ListView mListView;
    @BindView(R.id.stroteDataTest)
    Button stroteDataTest;
    @BindView(R.id.threshold)
    EditText threshold;
    @BindView(R.id.setThreshold)
    Button setThreshold;
    @BindView(R.id.hmacTest)
    TextView hmacTest;
    @BindView(R.id.testHmac)
    Button testHmac;

    private QNBleDevice mBleDevice;
    private List<QNScaleItemData> mDatas = new ArrayList<>();
    private QNBleApi mQNBleApi;

    private User mUser;
    private QNWiFiConfig mQnWiFiConfig;

    private boolean mIsConnected;

    private QNScaleData currentQNScaleData;
    private List<QNScaleData> historyQNScaleData = new ArrayList<>();
    private ListAdapter listAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);
        mQNBleApi = QNBleApi.getInstance(this);
        //此API是用来监听日志的，如果需要上传日志到服务器则可以使用，否则不需要设置
        mQNBleApi.setLogListener(new QNLogListener() {
            @Override
            public void onLog(String log) {
                Log.e("test",log);
            }
        });
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
                Log.d("ConnectActivity", "onConnectError:" + errorCode);
                setBleStatus(QNScaleStatus.STATE_DISCONNECTED);
            }

        });
    }

    private void connectQnDevice(QNBleDevice device) {
        if (null != mQnWiFiConfig) {
            mQNBleApi.connectDeviceSetWiFi(device, createQNUser(), mQnWiFiConfig, new QNResultCallback() {
                @Override
                public void onResult(int code, String msg) {
                    QNLogUtils.log("ConnectActivity", "wifi 配置code:" + code + ",msg:" + msg);
                    // ToastMaker.show(ConnectActivity.this, code + ":" + msg);
                }
            });
        } else {
            mQNBleApi.connectDevice(device, createQNUser(), new QNResultCallback() {
                @Override
                public void onResult(int code, String msg) {
                    Log.d("ConnectActivity", "连接设备返回:" + msg);
                }
            });
        }
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
                        Log.d("ConnectActivity", "创建用户信息返回:" + msg);
                    }
                });
    }


    private void initUserData() {
        mQNBleApi.setDataListener(new QNScaleDataListener() {
            @Override
            public void onGetUnsteadyWeight(QNBleDevice device, double weight) {
                Log.d("ConnectActivity", "体重是:" + weight);
                mWeightTv.setText(initWeight(weight));
            }

            @Override
            public void onGetScaleData(QNBleDevice device, QNScaleData data) {
                Log.d("ConnectActivity", "收到测量数据");
                onReceiveScaleData(data);
                QNScaleItemData fatValue = data.getItem(QNIndicator.TYPE_SUBFAT);
                if (fatValue != null) {
                    String value = fatValue.getValue() + "";
                    Log.d("ConnectActivity", "收到皮下脂肪数据:" + value);
                }
                currentQNScaleData = data;
                historyQNScaleData.add(data);
                Log.d("ConnectActivity", "加密hmac为:" + data.getHmac());
//                Log.d("ConnectActivity", "收到体脂肪:"+data.getItem(QNIndicator.TYPE_BODYFAT).getValue());
            }

            @Override
            public void onGetStoredScale(QNBleDevice device, List<QNScaleStoreData> storedDataList) {
                Log.d("ConnectActivity", "收到存储数据");
                if (storedDataList != null && storedDataList.size() > 0) {
                    QNScaleStoreData data = storedDataList.get(0);
                    for(int i =0;i<storedDataList.size();i++){
                        Log.d("ConnectActivity", "收到存储数据:" + storedDataList.get(i).getWeight());
                    }
                    QNUser qnUser = createQNUser();
                    data.setUser(qnUser);
                    QNScaleData qnScaleData = data.generateScaleData();
                    onReceiveScaleData(qnScaleData);
                    currentQNScaleData = qnScaleData;
                    historyQNScaleData.add(qnScaleData);
                }
            }

            @Override
            public void onGetElectric(QNBleDevice device, int electric) {
                String text = "收到电池电量百分比:" + electric;
                Log.d("ConnectActivity", text);
                if (electric == DecoderConst.NONE_BATTERY_VALUE) {//获取电池信息失败
                    return;
                }
                Toast.makeText(ConnectActivity.this, text, Toast.LENGTH_SHORT).show();
            }

            //测量过程中的连接状态
            @Override
            public void onScaleStateChange(QNBleDevice device, int status) {
                Log.d("ConnectActivity", "秤的连接状态是:" + status);
                setBleStatus(status);
            }
        });
    }

    private void initIntent() {
        Intent intent = getIntent();
        if (intent != null) {
            mBleDevice = intent.getParcelableExtra(UserConst.DEVICE);
            mUser = intent.getParcelableExtra(UserConst.USER);
            mQnWiFiConfig = intent.getParcelableExtra(UserConst.WIFI_CONFIG);
            if (null == mQnWiFiConfig) {
                stroteDataTest.setVisibility(View.GONE);
            }
        }
    }

    private String initWeight(double weight) {
        int unit = mQNBleApi.getConfig().getUnit();
        return mQNBleApi.convertWeightWithTargetUnit(weight, unit);
    }

    private void initView() {
        mConnectBtn.setOnClickListener(this);
        mBackTv.setOnClickListener(this);
        listAdapter = new ListAdapter(mDatas,mQNBleApi,createQNUser());
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
            case QNScaleStatus.STATE_LINK_LOSS:{
                stateString = getResources().getString(R.string.connection_disconnected);
                btnString = getResources().getString(R.string.connect);
                mIsConnected = false;
                break;
            }
            case QNScaleStatus.STATE_START_MEASURE: {
                stateString = getResources().getString(R.string.measuring);
                btnString =  getResources().getString(R.string.disconnected);
                break;
            }
            case QNScaleStatus.STATE_REAL_TIME: {
                stateString = getResources().getString(R.string.real_time_weight_measurement);
                btnString =  getResources().getString(R.string.disconnected);
                break;
            }
            case QNScaleStatus.STATE_BODYFAT: {
                stateString = getResources().getString(R.string.impedance_measured);
                btnString =  getResources().getString(R.string.disconnected);
                break;
            }
            case QNScaleStatus.STATE_HEART_RATE: {
                stateString = getResources().getString(R.string.measuring_heart_rate);
                btnString =  getResources().getString(R.string.disconnected);
                break;
            }
            case QNScaleStatus.STATE_MEASURE_COMPLETED: {
                stateString = getResources().getString(R.string.measure_complete);
                hmacTest.setText("");
                btnString = getResources().getString(R.string.disconnected);
                break;
            }
            case QNScaleStatus.STATE_WIFI_BLE_START_NETWORK:
                stateString = getResources().getString(R.string.start_set_wifi);
                btnString =  getResources().getString(R.string.disconnected);
                Log.d("ConnectActivity", "开始设置WiFi");
                break;
            case QNScaleStatus.STATE_WIFI_BLE_NETWORK_FAIL:
                stateString = getResources().getString(R.string.failed_to_set_wifi);
                btnString =  getResources().getString(R.string.disconnected);
                Log.d("ConnectActivity", "设置WiFi失败");
                break;
            case QNScaleStatus.STATE_WIFI_BLE_NETWORK_SUCCESS:
                stateString = getResources().getString(R.string.success_to_set_wifi);
                btnString = getResources().getString(R.string.disconnected);
                Log.d("ConnectActivity", "设置WiFi成功");
                break;
            default: {
                stateString = getResources().getString(R.string.connection_disconnected);
                btnString =  getResources().getString(R.string.connect);
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
                finish();
                break;
        }
    }


    private void doConnect() {
        if (mBleDevice == null || mUser == null) {
            return;
        }
        if (null != mQnWiFiConfig) {
            mQNBleApi.connectDeviceSetWiFi(mBleDevice, createQNUser(), mQnWiFiConfig, new QNResultCallback() {
                @Override
                public void onResult(int code, String msg) {
                    QNLogUtils.log("ConnectActivity", "wifi 配置code:" + code + ",msg:" + msg);
                    if (code == 0) {
                        mIsConnected = true;
                    }
                    // ToastMaker.show(ConnectActivity.this, code + ":" + msg);
                }
            });
        } else {
            mQNBleApi.connectDevice(mBleDevice, createQNUser(), new QNResultCallback() {
                @Override
                public void onResult(int code, String msg) {
                    Log.d("ConnectActivity", "连接设备返回:" + msg);
                    if (code == 0) {
                        mIsConnected = true;
                    }
                }
            });
        }
    }

    private void doDisconnect() {
        mQNBleApi.disconnectDevice(mBleDevice, new QNResultCallback() {
            @Override
            public void onResult(int code, String msg) {
                Log.d("ConnectActivity", "断开连接设备返回:" + msg);
            }
        });
    }

    @OnClick({R.id.stroteDataTest, R.id.setThreshold, R.id.testHmac})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.stroteDataTest:
                //{"weight"=>"25.35", "measure_time"=>"2019-05-06 14:02:51", "mac"=>"F0:FE:6B:CB:75:6A", "model_id"=>"0005", "sign"=>"3F828A0207EB762F0D12E1ED5345AF7D6907304A74A45990B254256AC08DAA76EEA778E4B50ACE92D47DA72DD7257F82734C33A56721D797FD932B3741E5C730F2901F7EFAA1755DD0683BABD0959BB1E82201C3B50B3E8A5360A3D57550CF446DC834B8FA2F0D16DA4C0797CC1C308E4253413D4AB90DC4093F8065199ABE8AB0C9D06E3172E511C54C7E5095BB92C753070DC0CEB5D64785C4577952B50465"}
                /*QNScaleStoreData qnScaleStoreData =new QNScaleStoreData();
                qnScaleStoreData.setUser(createQNUser());
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                try {
                    Date date =simpleDateFormat.parse("2019-05-06 14:02:51");
                    qnScaleStoreData.buildStoreData(25.35,date , "F0:FE:6B:CB:75:6A",
                            "3F828A0207EB762F0D12E1ED5345AF7D6907304A74A45990B254256AC08DAA76EEA778E4B50ACE92D47DA72DD7257F82734C33A56721D797FD932B3741E5C730F2901F7EFAA1755DD0683BABD0959BB1E82201C3B50B3E8A5360A3D57550CF446DC834B8FA2F0D16DA4C0797CC1C308E4253413D4AB90DC4093F8065199ABE8AB0C9D06E3172E511C54C7E5095BB92C753070DC0CEB5D64785C4577952B50465",
                            new QNResultCallback() {
                                @Override
                                public void onResult(int code, String msg) {
                                    Log.e("buildStoreData","code="+code+",msg="+msg);
                                }
                            });
                    QNScaleData qnScaleData = qnScaleStoreData.generateScaleData();
                    onReceiveScaleData(qnScaleData);
                } catch (ParseException e) {
                    e.printStackTrace();
                }*/

                break;
            case R.id.setThreshold:
                if (TextUtils.isEmpty(threshold.getText().toString())) {
                    ToastMaker.show(this, getResources().getString(R.string.please_enter_body_fat_change_control));
                    return;
                }
                if (null == currentQNScaleData) {
                    ToastMaker.show(this,getResources().getString(R.string.set_body_fat_change_hint));
                    return;
                }
                if (!TextUtils.isEmpty(hmacTest.getText().toString())) {
                    currentQNScaleData.setFatThreshold(hmacTest.getText().toString(), Double.valueOf(threshold.getText().toString()),
                            new QNResultCallback() {
                                @Override
                                public void onResult(int code, String msg) {
                                    Log.e("setFatThreshold", "code=" + code + ",msg=" + msg);
                                    if (code == CheckStatus.OK.getCode()) {
                                        //设置完后得到调整后数据并进行显示
                                        onReceiveScaleData(currentQNScaleData);
                                    }
                                }
                            });

                } else {
                    if (historyQNScaleData.size() < 2) {
                        ToastMaker.show(this, getResources().getString(R.string.set_body_fat_change_hint1));
                        return;
                    }
                    //当前数据的前一条数据对应历史数据中的倒数第二条
                    currentQNScaleData.setFatThreshold(historyQNScaleData.get(historyQNScaleData.size() - 2).getHmac(), Double.valueOf(threshold.getText().toString()),
                            new QNResultCallback() {
                                @Override
                                public void onResult(int code, String msg) {
                                    Log.e("setFatThreshold", "code=" + code + ",msg=" + msg);
                                    if (code == CheckStatus.OK.getCode()) {
                                        //设置完后得到调整后数据并进行显示
                                        onReceiveScaleData(currentQNScaleData);
                                    }
                                }
                            });

                }

                break;
            case R.id.testHmac:
                if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.CAMERA)
                        == PackageManager.PERMISSION_GRANTED) {
                    startActivityForResult(new Intent(ConnectActivity.this, ScanQrActivity.class), 100);
                } else {
                    AndroidPermissionCenter.verifyCameraPermissions(this);
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100) {
            if (resultCode == 200) {
                String qrCode = data.getStringExtra("code").trim();
                Log.e("二维码：", qrCode);
                if (!TextUtils.isEmpty(qrCode)) {
                    hmacTest.setText(qrCode);
                } else {
                    //
                }

            }
        }
    }
}
