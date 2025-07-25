package com.qingniu.qnble.demo.view;

import static com.qingniu.qnble.demo.view.FileUtile.fileToByteArray;
import static com.qingniu.qnble.demo.view.FileUtile.getUfwFile;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.qingniu.qnble.demo.R;
import com.qingniu.qnble.demo.adapter.ListAdapter;
import com.qingniu.qnble.demo.util.DateUtils;
import com.qingniu.qnble.demo.util.QNDemoLogger;
import com.qingniu.qnble.demo.util.ToastMaker;
import com.qingniu.qnble.demo.util.UserConst;
import com.qingniu.scale.constant.DecoderConst;
import com.qingniu.scale.measure.ble.va.ScaleVAManagerService;
import com.qingniu.scale.ota.jieli.JieLiOtaStep;
import com.qingniu.scale.ota.jieli.OtaListener;
import com.qingniu.scale.model.BleScale;
import com.qingniu.utils.QNVaLogger;
import com.qn.device.constant.QNIndicator;
import com.qn.device.constant.QNInfoConst;
import com.qn.device.constant.QNScaleStatus;
import com.qn.device.listener.QNBleConnectionChangeListener;
import com.qn.device.listener.QNBleOTAListener;
import com.qn.device.listener.QNResultCallback;
import com.qn.device.listener.QNUserScaleDataListener;
import com.qn.device.out.QNBleApi;
import com.qn.device.out.QNBleDevice;
import com.qn.device.out.QNBleOTAConfig;
import com.qn.device.out.QNScaleData;
import com.qn.device.out.QNScaleItemData;
import com.qn.device.out.QNScaleStoreData;
import com.qn.device.out.QNUser;
import com.qn.device.out.QNUserScaleConfig;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * wsp 双模秤连接界面
 */

public class UserScaleActivity extends AppCompatActivity implements View.OnClickListener, QNBleOTAListener {

    private static final String TAG = "UserScaleActivity";

    @BindView(R.id.snTextView)
    TextView snTextView;

    @BindView(R.id.bleVerTv)
    TextView bleVerView;

    @BindView(R.id.otaStatusTv)
    TextView otaStatusView;

    @BindView(R.id.batteryTv)
    TextView batteryTv;
    private int bleStatus;

    @BindView(R.id.hmacEt)
    EditText hmacEt;
    @BindView(R.id.hmacBtn)
    Button hmacBtn;


    public static Intent getCallIntent(Context context, QNBleDevice device, QNUserScaleConfig qnUserScaleConfig) {
        return new Intent(context, UserScaleActivity.class)
                .putExtra(UserConst.DEVICE, device)
                .putExtra(UserConst.WSPCONFIG, qnUserScaleConfig);
    }

    @BindView(R.id.connectBtn)
    Button mConnectBtn;
    @BindView(R.id.otaBtn)
    Button otaBtn;
    @BindView(R.id.resetBtn)
    Button resetBtn;

    @BindView(R.id.ota9Btn)
    Button ota9Btn;

    @BindView(R.id.ota10Btn)
    Button ota10Btn;

    @BindView(R.id.registerUserIndex)
    TextView registerUserIndex;
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


    private QNBleDevice mBleDevice;
    private final List<QNScaleItemData> mDatas = new ArrayList<>();
    private QNBleApi mQNBleApi;

    private QNUserScaleConfig mQnUserScaleConfig;

    private boolean mIsConnected;

    private ListAdapter listAdapter;

    private final QNBleOTAListener qnBleOTAListener = new QNBleOTAListener() {
        @Override
        public void onOTAStart(QNBleDevice device) {
            QNDemoLogger.d("UserScaleActivity", "onOtaStart");
            otaStatusView.setText("onOtaStart");
        }

        @Override
        public void onOTAUpgrading(QNBleDevice device) {
            QNDemoLogger.d("UserScaleActivity", "onOTAUpgrading");
            otaStatusView.setText("onOTAUpgrading");
        }

        @Override
        public void onOTACompleted(QNBleDevice device) {
            QNDemoLogger.d("UserScaleActivity", "onOTACompleted");
            otaStatusView.setText("onOTACompleted");
        }

        @Override
        public void onOTAFailed(QNBleDevice device, int errorCode) {
            QNDemoLogger.d("UserScaleActivity", "onOTAFailed " + errorCode);
            otaStatusView.setText("onOTAFailed " + errorCode);
        }

        @Override
        public void onOTAProgress(QNBleDevice device, int progress, int otaStep) {
            QNDemoLogger.d("UserScaleActivity", "onOTAProgress " + otaStep + "  " + progress);
            otaStatusView.setText("onOTAProgress 阶段 " + otaStep + " 进度 " + progress);
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wsp_scale);
        mQNBleApi = QNBleApi.getInstance(this);
        ButterKnife.bind(this);
        initIntent();
        initView();
        initData();
        QNBleApi.getInstance(this).setQNBleOTAListener(qnBleOTAListener);
    }

    private void initData() {
        initBleConnectStatus();
        initUserData(); //设置数据监听器,返回数据,需在连接当前设备前设置
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
                QNDemoLogger.d("UserScaleActivity", "onConnectError:" + errorCode);
                setBleStatus(QNScaleStatus.STATE_DISCONNECTED);
            }


            @Override
            public void onStartInteracting(QNBleDevice device) {
                QNDemoLogger.d("UserScaleActivity", "onStartInteracting");
            }
        });
    }

    private void connectQnWspDevice(QNBleDevice device) {
        mQNBleApi.connectUserScaleDevice(device, mQnUserScaleConfig, new QNResultCallback() {
            @Override
            public void onResult(int code, String msg) {
                QNDemoLogger.e("UserScaleActivity", "用户模式连接 wifi 配置code:" + code + ",msg:" + msg);
            }
        });
    }


    private void initUserData() {
        mQNBleApi.setDataListener(new QNUserScaleDataListener() {
            @Override
            public void registerUserComplete(QNBleDevice device, QNUser user) {
                QNDemoLogger.d("UserScaleActivity", "注册返回的用户索引：" + user.getIndex());
                registerUserIndex.setText(getResources().getString(R.string.register_user_index) + user.getIndex());
            }

            @Override
            public void onGetUnsteadyWeight(QNBleDevice device, double weight) {
                QNDemoLogger.d("UserScaleActivity", "体重是:" + weight);
                mWeightTv.setText(initWeight(weight));
            }

            @Override
            public void onGetScaleData(QNBleDevice device, QNScaleData data) {
                QNDemoLogger.d("UserScaleActivity", "收到测量数据");
                boolean isEightData = data.getItemValue(QNIndicator.TYPE_LEFT_ARM_MUSCLE_WEIGHT_INDEX) > 0;
                listAdapter.setEight(isEightData);

                hmacEt.setText(data.getHmac());

                onReceiveScaleData(data);
                QNScaleItemData fatValue = data.getItem(QNIndicator.TYPE_SUBFAT);
                if (fatValue != null) {
                    String value = String.valueOf(fatValue.getValue());
                    QNDemoLogger.d("UserScaleActivity", "收到皮下脂肪数据:" + value);
                }
                QNDemoLogger.d("UserScaleActivity", "加密hmac为:" + data.getHmac());
//                QNDemoLogger.d("UserScaleActivity", "收到体脂肪:"+data.getItem(QNIndicator.TYPE_BODYFAT).getValue());

                if (data.getItemValue(QNIndicator.TYPE_LEFT_ARM_MUSCLE_WEIGHT_INDEX) > 0) {

                    QNUser qnUser = data.getQnUser();


                    int gender = 0;
                    if (QNInfoConst.GENDER_MAN.equals(qnUser.getGender())) {
                        gender = 1;
                    }

                    JSONObject object = new JSONObject();

                    try {
                        object.putOpt("gender", gender);
                        object.putOpt("birthday", DateUtils.dateToString(qnUser.getBirthDay()));
                        object.putOpt("height", qnUser.getHeight());
                        object.putOpt("bmi", data.getItemValue(QNIndicator.TYPE_BMI));
                        object.putOpt("bmr", data.getItemValue(QNIndicator.TYPE_BMR));
                        object.putOpt("body_age", data.getItemValue(QNIndicator.TYPE_BODY_AGE));
                        object.putOpt("bodyfat", data.getItemValue(QNIndicator.TYPE_BODYFAT));
                        object.putOpt("bodyfat_left_arm", data.getItemValue(QNIndicator.TYPE_LEFT_ARM_FAT_INDEX));
                        object.putOpt("bodyfat_left_leg", data.getItemValue(QNIndicator.TYPE_LEFT_LEG_FAT_INDEX));
                        object.putOpt("bodyfat_right_arm", data.getItemValue(QNIndicator.TYPE_RIGHT_ARM_FAT_INDEX));
                        object.putOpt("bodyfat_right_leg", data.getItemValue(QNIndicator.TYPE_RIGHT_LEG_FAT_INDEX));
                        object.putOpt("bodyfat_trunk", data.getItemValue(QNIndicator.TYPE_TRUNK_FAT_INDEX));
                        object.putOpt("bone", data.getItemValue(QNIndicator.TYPE_BONE));
                        object.putOpt("lbm", data.getItemValue(QNIndicator.TYPE_LBM));
                        object.putOpt("muscle", data.getItemValue(QNIndicator.TYPE_MUSCLE));
                        object.putOpt("protein", data.getItemValue(QNIndicator.TYPE_PROTEIN));
                        object.putOpt("subfat", data.getItemValue(QNIndicator.TYPE_SUBFAT));
                        object.putOpt("visfat", data.getItemValue(QNIndicator.TYPE_VISFAT));
                        object.putOpt("water", data.getItemValue(QNIndicator.TYPE_WATER));
                        object.putOpt("weight", data.getItemValue(QNIndicator.TYPE_WEIGHT));
                        object.putOpt("sinew", data.getItemValue(QNIndicator.TYPE_MUSCLE_MASS));
                        object.putOpt("sinew_left_arm", data.getItemValue(QNIndicator.TYPE_LEFT_ARM_MUSCLE_WEIGHT_INDEX));
                        object.putOpt("sinew_left_leg", data.getItemValue(QNIndicator.TYPE_LEFT_LEG_MUSCLE_WEIGHT_INDEX));
                        object.putOpt("sinew_right_arm", data.getItemValue(QNIndicator.TYPE_RIGHT_ARM_MUSCLE_WEIGHT_INDEX));
                        object.putOpt("sinew_right_leg", data.getItemValue(QNIndicator.TYPE_RIGHT_LEG_MUSCLE_WEIGHT_INDEX));
                        object.putOpt("sinew_trunk", data.getItemValue(QNIndicator.TYPE_TRUNK_MUSCLE_WEIGHT_INDEX));
                        object.putOpt("score", data.getItemValue(QNIndicator.TYPE_SCORE));

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                    startActivity(WebEightElectroActivity.getCallIntent(UserScaleActivity.this, object));
                }
            }

            @Override
            public void onGetStoredScale(QNBleDevice device, List<QNScaleStoreData> storedDataList) {
                QNDemoLogger.d("UserScaleActivity", "收到存储数据 " + storedDataList.size() + "条");
                if (storedDataList != null && storedDataList.size() > 0) {
                    QNScaleStoreData data = storedDataList.get(0);
                    for (int i = 0; i < storedDataList.size(); i++) {
                        QNDemoLogger.d("UserScaleActivity", "收到存储数据:" + storedDataList.get(i).getWeight());
                    }
                    data.setUser(mQnUserScaleConfig.getCurUser());
                    QNScaleData qnScaleData = data.generateScaleData();
                    onReceiveScaleData(qnScaleData);


                    QNDemoLogger.d("UserScaleActivity", "存储数据 加密hmac为:" + data.getHmac());
                }
            }

            @Override
            public void onGetElectric(QNBleDevice device, int electric) {
                String text = "收到电池电量百分比:" + electric;
                QNDemoLogger.d("UserScaleActivity", text);
                if (electric == DecoderConst.NONE_BATTERY_VALUE) {//获取电池信息失败
                    return;
                }
                Toast.makeText(UserScaleActivity.this, text, Toast.LENGTH_SHORT).show();
            }

            //测量过程中的连接状态
            @Override
            public void onScaleStateChange(QNBleDevice device, int status) {
                QNDemoLogger.d("UserScaleActivity", "秤的连接状态是:" + status);
                if (status == QNScaleStatus.EVENT_SCALE_NOW_NEED_OTA) {
                    otaStatusView.setText("秤需要下发升级数据!");
                    Toast.makeText(UserScaleActivity.this, "秤需要下发升级数据!",Toast.LENGTH_SHORT).show();
                }
                setBleStatus(status);
            }

            @Override
            public void onScaleEventChange(QNBleDevice device, int scaleEvent) {
                QNDemoLogger.d("UserScaleActivity", "秤的事件是:" + scaleEvent);
            }

            @Override
            public String getLastDataHmac(QNBleDevice qnBleDevice, QNUser qnUser) {
                String hmac = hmacEt.getText().toString();
                if (TextUtils.isEmpty(hmac)) {
                    return null;
                } else {
                    return hmac;
                }
            }

            @Override
            public void readSnComplete(QNBleDevice qnBleDevice, String s) {

            }

            @Override
            public void onGetBleVer(QNBleDevice device, int bleVer) {
                bleVerView.setText("当前固件版本 "+bleVer);
            }

            @Override
            public void onGetBatteryLevel(QNBleDevice device, int batteryLevel, boolean isLowLevel) {
                batteryTv.setText("当前电量 " + batteryLevel + " 是否低电 " + isLowLevel);
            }
        });
    }

    private void initIntent() {
        Intent intent = getIntent();
        if (intent != null) {
            mBleDevice = intent.getParcelableExtra(UserConst.DEVICE);
            mQnUserScaleConfig = intent.getParcelableExtra(UserConst.WSPCONFIG);
        }
    }

    private String initWeight(double weight) {
        int unit = mQNBleApi.getConfig().getUnit();
        return mQNBleApi.convertWeightWithTargetUnit(weight, unit);
    }

    private void initView() {
        hmacBtn.setOnClickListener(this);
        mConnectBtn.setOnClickListener(this);
        resetBtn.setOnClickListener(this);
        otaBtn.setOnClickListener(this);
        ota9Btn.setOnClickListener(this);
        ota10Btn.setOnClickListener(this);
        mBackTv.setOnClickListener(this);
        listAdapter = new ListAdapter(mDatas, mQNBleApi, mQnUserScaleConfig.getCurUser(), mBleDevice);
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
        this.bleStatus = bleStatus;
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
                QNDemoLogger.d("UserScaleActivity", "开始设置WiFi");
                break;
            case QNScaleStatus.STATE_WIFI_BLE_NETWORK_FAIL:
                stateString = getResources().getString(R.string.failed_to_set_wifi);
                btnString = getResources().getString(R.string.disconnected);
                QNDemoLogger.d("UserScaleActivity", "设置WiFi失败");
                //配网成功或失败后都立刻断开连接
                doDisconnect();
                break;
            case QNScaleStatus.STATE_WIFI_BLE_NETWORK_SUCCESS:
                stateString = getResources().getString(R.string.success_to_set_wifi);
                btnString = getResources().getString(R.string.disconnected);
                QNDemoLogger.d("UserScaleActivity", "设置WiFi成功");
                //配网成功或失败后都立刻断开连接
                doDisconnect();
                break;
            case QNScaleStatus.EVENT_SCALE_NOW_NEED_OTA:
                stateString = mStatusTv.getText().toString();
                btnString = mConnectBtn.getText().toString();
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
            case R.id.resetBtn:
                if (bleStatus != QNScaleStatus.STATE_DISCONNECTED) {
                    mQNBleApi.restoreFactorySettingsCallback(new QNResultCallback() {
                        @Override
                        public void onResult(int code, String msg) {

                        }
                    });
                } else {
                    QNDemoLogger.d("UserScaleActivity", "请连接秤");
                    ToastMaker.show(this, "请连接秤");
                }
                break;
            case R.id.otaBtn:

                File filesDir = getExternalFilesDir(null);
                if (filesDir != null) {
                    File[] files = filesDir.listFiles();
                    String[] strings = new String[files.length];

                    for (int i = 0; i < files.length; i++) {
                        strings[i] = files[i].getName();
                    }

                    new AlertDialog.Builder(this)
                            .setNegativeButton("取消", null)
                            .setItems(strings, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    File file = files[which];
                                    BleScale bleScale = new BleScale();
                                    bleScale.setMac(mBleDevice.getMac());
//                                        OTAServiceManager.getInstance(WspScaleActivity.this).startConnect(WspScaleActivity.this, bleScale, path);

                                    QNBleOTAConfig otaConfig = new QNBleOTAConfig();
                                    otaConfig.setOTAData(file2buf(file));
                                    otaConfig.setOTAVer(which + 1);

                                    mQnUserScaleConfig.setOtaConfig(otaConfig);
                                    mQNBleApi.setQNBleOTAListener(UserScaleActivity.this);
                                    mQNBleApi.connectUserScaleDevice(mBleDevice, mQnUserScaleConfig, new QNResultCallback() {
                                        @Override
                                        public void onResult(int code, String msg) {
                                            QNDemoLogger.e("UserScaleActivity", "wifi 配置code:" + code + ",msg:" + msg);
                                        }
                                    });

                                }
                            })
                            .create()
                            .show();
                }

                break;
            case R.id.hmacBtn:
                hmacEt.setText("");
                break;
            case R.id.connectBtn:
                if (mIsConnected) {
                    //已经连接,断开连接
                    this.doDisconnect();
                } else {
                    this.bleVerView.setText("");
                    this.otaStatusView.setText("");
                    //断开连接,就开始连接
                    mDatas.clear();
                    listAdapter.notifyDataSetChanged();
                    connectQnWspDevice(mBleDevice);
                }
                break;
            case R.id.back_tv:
                doDisconnect();
                finish();
                break;
            case R.id.ota9Btn: {
                QNBleApi.getInstance(UserScaleActivity.this).applyOta(fileToByteArray(getUfwFile(UserScaleActivity.this, "v09.ufw")), new QNResultCallback() {
                    @Override
                    public void onResult(int code, String msg) {
                        QNDemoLogger.d("UserScaleActivity", "调用ota9Btn " + code + " " + msg);
                        otaStatusView.setText("调用ota9Btn " + code + " " + msg);
                    }
                });
                break;
            }
            case R.id.ota10Btn: {
                QNBleApi.getInstance(UserScaleActivity.this).applyOta(fileToByteArray(getUfwFile(UserScaleActivity.this, "v10.ufw")), new QNResultCallback() {
                    @Override
                    public void onResult(int code, String msg) {
                        QNDemoLogger.d("UserScaleActivity", "调用ota10Btn " + code + " " + msg);
                        otaStatusView.setText("调用ota10Btn " + code + " " + msg);
                    }
                });
                break;
            }
        }
    }

    public static byte[] file2buf(File file) {
        byte[] buffer = null;
        try {
            if (!file.exists()) {
                return null;
            }

            FileInputStream fis = new FileInputStream(file);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] b = new byte[1024];
            int len = -1;
            while ((len = fis.read(b)) != -1) {
                bos.write(b, 0, len);
            }
            fis.close();
            bos.close();
            buffer = bos.toByteArray();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return buffer;
    }

    private void doDisconnect() {
        mQNBleApi.disconnectDevice(mBleDevice, new QNResultCallback() {
            @Override
            public void onResult(int code, String msg) {
                QNDemoLogger.d("UserScaleActivity", "断开连接设备返回:" + msg);
            }
        });
    }

    @OnClick({R.id.stroteDataTest})
    public void onViewClicked(View view) {
        if (view.getId() == R.id.stroteDataTest) {/**
         *  用户获取到服务器的数据用此API进行测量数据的生成（The data acquired by the user to the server is generated using this API for measurement data）
         */

               /* hmac="3F828A0207EB762F0D12E1ED5345AF7D6907304A74A45990B254256AC08DAA76EEA778E4B50ACE92D47DA72DD7257F82734C33A56721D797FD932B3741E5C730F2901F7EFAA1755DD0683BABD0959BB1E82201C3B50B3E8A5360A3D57550CF446DC834B8FA2F0D16DA4C0797CC1C308E4253413D4AB90DC4093F8065199ABE8AB0C9D06E3172E511C54C7E5095BB92C753070DC0CEB5D64785C4577952B50465"
        // 解密后{"weight":25.35,"measure_time":"2019-05-06 14:02:51","mac":"F0:FE:6B:CB:75:6A","heart_rate":93,"resistance_50":1601,"resistance_500":65474,"model_id":"0005"}*/
            String hmac = "3F828A0207EB762F0D12E1ED5345AF7D6907304A74A45990B254256AC08DAA76EEA778E4B50ACE92D47DA72DD7257F82734C33A56721D797FD932B3741E5C730F2901F7EFAA1755DD0683BABD0959BB1E82201C3B50B3E8A5360A3D57550CF446DC834B8FA2F0D16DA4C0797CC1C308E4253413D4AB90DC4093F8065199ABE8AB0C9D06E3172E511C54C7E5095BB92C753070DC0CEB5D64785C4577952B50465";
            QNScaleData scaleData = mQNBleApi.generateScaleData(mQnUserScaleConfig.getCurUser(), "0005",
                    25.35, 1601, 65474, 93, hmac, new Date(1557122571000L));
            if (null != scaleData) {
                onReceiveScaleData(scaleData);
            }
        }
    }


    @Override
    public void onOTAStart(QNBleDevice device) {
        QNDemoLogger.d("UserScaleActivity", "onOTAStart:" + device.getMac());
    }

    @Override
    public void onOTAUpgrading(QNBleDevice device) {
        QNDemoLogger.d("UserScaleActivity", "onOTAUpgrading:" + device.getMac());
    }

    @Override
    public void onOTACompleted(QNBleDevice device) {
        QNDemoLogger.d("UserScaleActivity", "onOTACompleted:" + device.getMac());
    }

    @Override
    public void onOTAFailed(QNBleDevice device, int errorCode) {
        QNDemoLogger.d("UserScaleActivity", "onOTAFailed:" + device.getMac());
    }

    @Override
    public void onOTAProgress(QNBleDevice device, int progress, int otaStep) {
        QNDemoLogger.d("UserScaleActivity", "onOTAProgress:" + progress);
    }
}
