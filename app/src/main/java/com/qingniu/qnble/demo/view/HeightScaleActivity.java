package com.qingniu.qnble.demo.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.qingniu.heightscale.ble.HeightScaleBleService;
import com.qingniu.qnble.demo.R;
import com.qingniu.qnble.demo.adapter.ListAdapter;
import com.qingniu.qnble.demo.bean.User;
import com.qingniu.qnble.demo.bean.WifiInfoModel;
import com.qingniu.qnble.demo.util.DateUtils;
import com.qingniu.qnble.demo.util.QNDemoLogger;
import com.qingniu.qnble.demo.util.UserConst;
import com.qingniu.scale.constant.DecoderConst;
import com.qingniu.scale.model.BleUser;
import com.qn.device.config.QNConfigManager;
import com.qn.device.constant.QNIndicator;
import com.qn.device.constant.QNScaleStatus;
import com.qn.device.constant.UserGoal;
import com.qn.device.constant.UserShape;
import com.qn.device.listener.QNBleConnectionChangeListener;
import com.qn.device.listener.QNResultCallback;
import com.qn.device.listener.QNScaleDataListener;
import com.qn.device.out.QNBleApi;
import com.qn.device.out.QNBleDevice;
import com.qn.device.out.QNConfig;
import com.qn.device.out.QNHeightDeviceConfig;
import com.qn.device.out.QNHeightDeviceFunction;
import com.qn.device.out.QNScaleData;
import com.qn.device.out.QNScaleItemData;
import com.qn.device.out.QNScaleStoreData;
import com.qn.device.out.QNUser;
import com.qn.device.out.QNWiFiConfig;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * author: ch
 * date: 2020/6/16
 * package_name: com.qingniu.qnble.demo
 * description:身高一体机界面
 */

public class HeightScaleActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int WIFI_INFO_REQUEST_CODE = 1000;
    private QNWiFiConfig pairWifiConfig;

    public static Intent getCallIntent(Context context, User user, QNBleDevice device) {
        return new Intent(context, HeightScaleActivity.class)
                .putExtra(UserConst.USER, user)
                .putExtra(UserConst.DEVICE, device);
    }

    public static Intent getCallIntent(Context context, User user, QNBleDevice device, QNWiFiConfig qnWiFiConfig) {
        return new Intent(context, HeightScaleActivity.class)
                .putExtra(UserConst.USER, user)
                .putExtra(UserConst.WIFI_CONFIG, qnWiFiConfig)
                .putExtra(UserConst.DEVICE, device);
    }

    @BindView(R.id.switch_user_btn)
    Button switchUserBtn;
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

    @BindView(R.id.bar_code_tv)
    TextView barCodeTv;

    @BindView(R.id.storage_tip_tv)
    TextView storageTipTv;

    @BindView(R.id.scan_wifi_btn)
    Button scanWifiBtn;

    @BindView(R.id.pair_wifi_btn)
    Button pairWifiBtn;

    @BindView(R.id.wifi_info_btn)
    Button wifiInfoBtn;

    @BindView(R.id.wifi_clean_btn)
    Button wifiCleanBtn;

    @BindView(R.id.device_info_btn)
    Button deviceInfoBtn;

    @BindView(R.id.reset_btn)
    Button resetBtn;

    @BindView(R.id.setting_btn)
    Button settingBtn;

    private QNBleDevice mBleDevice;
    private final List<QNScaleItemData> mDatas = new ArrayList<>();
    private QNBleApi mQNBleApi;

    private User mUser;

    private QNWiFiConfig qnWiFiConfig;

    private boolean mIsConnected;

    private ListAdapter listAdapter;

    private ArrayList<WifiInfoModel> wifiInfoModels = new ArrayList<>();

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
            //连接当前设备
            doConnect();
        }
    }

    private void initBleConnectStatus() {
        mQNBleApi.setBleConnectionChangeListener(new QNBleConnectionChangeListener() {
            //正在连接
            @Override
            public void onConnecting(QNBleDevice device) {
                setBleStatus(QNScaleStatus.STATE_CONNECTING);
            }

            //已连接，注意此时还不能进行其他蓝牙操作
            @Override
            public void onConnected(QNBleDevice device) {
                setBleStatus(QNScaleStatus.STATE_CONNECTED);
            }

            //发现服务
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
                QNDemoLogger.d("HeightScaleActivity", "onConnectError:" + errorCode);
                setBleStatus(QNScaleStatus.STATE_DISCONNECTED);
            }

            //可以执行其他蓝牙操作API方法
            @Override
            public void onStartInteracting(QNBleDevice device) {

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

        QNUser result = mQNBleApi.buildUser(mUser.getUserId(),
                mUser.getHeight(), mUser.getGender(), mUser.getBirthDay(), mUser.getAthleteType(),
                userShape, userGoal, mUser.getClothesWeight(), new QNResultCallback() {
                    @Override
                    public void onResult(int code, String msg) {
                        QNDemoLogger.d("HeightScaleActivity", "创建用户信息返回:" + msg);
                    }
                });
        return result;
    }


    private void initUserData() {
        mQNBleApi.setDataListener(new QNScaleDataListener() {

            @Override
            public void onSetHeightScaleConfigState(QNBleDevice device, boolean isLanguageSuccess, boolean isWeightUnitSuccess,
                                                    boolean isHeightUnitSuccess,
                                                    boolean isVolumeSuccess) {
                String msg = "isLanguageSuccess:" + isLanguageSuccess + "   isWeightUnitSuccess:" + isWeightUnitSuccess + "   isHeightUnitSuccess:" + isHeightUnitSuccess + "   isVolumeSuccess:" + isVolumeSuccess;
                QNDemoLogger.d("HeightScaleActivity", msg);

                if (isLanguageSuccess && isWeightUnitSuccess && isHeightUnitSuccess && isVolumeSuccess) {
                    msg = "Success";
                } else {
                    msg = "Fail";
                }
                Toast.makeText(HeightScaleActivity.this, msg, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onGetHeightScaleConfig(QNBleDevice device, QNHeightDeviceFunction function) {

                new AlertDialog.Builder(HeightScaleActivity.this)
                        .setTitle("身高秤设置信息")
                        .setMessage(function.toString())
                        .setPositiveButton(R.string.confirm, null)
                        .show();

            }

            @Override
            public void onResetHeightScaleState(QNBleDevice device, boolean isSuccess) {
                String msg = "onResetHeightScaleState:" + isSuccess;
                QNDemoLogger.d("HeightScaleActivity", msg);
                Toast.makeText(HeightScaleActivity.this, msg, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onClearHeightScaleWifiConfigState(QNBleDevice device, boolean isSuccess) {
                String msg = "onClearHeightScaleWifiConfigState:" + isSuccess;
                QNDemoLogger.d("HeightScaleActivity", msg);
                Toast.makeText(HeightScaleActivity.this, msg, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onGetHeightScaleWifiConfig(QNBleDevice device, boolean isSuccess, String ssid) {
                String msg = "onGetHeightScaleWifiConfig:" + isSuccess + "   ssid:" + ssid;
                QNDemoLogger.d("HeightScaleActivity", msg);
                new AlertDialog.Builder(HeightScaleActivity.this)
                        .setTitle("身高秤wifi配置信息")
                        .setMessage(msg)
                        .setPositiveButton(R.string.confirm, null)
                        .show();
            }

            @Override
            public void onScanHeightScaleWifiSsidResult(QNBleDevice device, String ssid, int rssi) {

                String msg = "onScanHeightScaleWifiSsidResult:" + ssid + "   rssi:" + rssi;
                QNDemoLogger.d("HeightScaleActivity", msg);
                Toast.makeText(HeightScaleActivity.this, "scanning", Toast.LENGTH_SHORT).show();

                WifiInfoModel wifiInfoModel = new WifiInfoModel(ssid, rssi);
                wifiInfoModels.add(wifiInfoModel);

            }

            @Override
            public void onScanHeightScaleWifiSsidFinish(QNBleDevice device, int resultCode) {
                String msg = "";
                if (resultCode == 1) {
                    msg = "onScanHeightScaleWifiSsidFinish:扫描结束";
                    ArrayList<WifiInfoModel> models = new ArrayList<>(wifiInfoModels);
                    wifiInfoModels.clear();
                    startActivityForResult(WifiInfoActivity.getCallIntent(HeightScaleActivity.this, models), WIFI_INFO_REQUEST_CODE);
                } else {
                    msg = "onScanHeightScaleWifiSsidFinish:扫描失败";
                }
                QNDemoLogger.d("HeightScaleActivity", msg);

            }

            @Override
            public void onGetBarCode(String devMac, String barCode) {
                String msg = "扫码枪获取扫描结果: " + barCode + "   mac: " + devMac;
                QNDemoLogger.d("HeightScaleActivity onGetBarCode", msg);
                Toast.makeText(HeightScaleActivity.this, msg, Toast.LENGTH_SHORT).show();

                barCodeTv.setVisibility(View.VISIBLE);
                barCodeTv.setText(barCode);
            }

            @Override
            public void onGetBarCodeFail(String devMac) {
                String msg = "扫码枪获取扫描结果失败: " + devMac;
                QNDemoLogger.d("HeightScaleActivity onGetBarCodeFail", msg);
                Toast.makeText(HeightScaleActivity.this, msg, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onGetBarCodeGunState(String devMac, boolean isConnected) {
                String msg = "扫码枪连接状态变化: " + isConnected + "   mac: " + devMac;
                QNDemoLogger.d("HeightScaleActivity onGetBarCodeGunState", msg);
                Toast.makeText(HeightScaleActivity.this, msg, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onGetUnsteadyWeight(QNBleDevice device, double weight) {
                QNDemoLogger.d("HeightScaleActivity", "体重是:" + weight);
                mWeightTv.setText(initWeight(weight));
            }

            @Override
            public void onGetScaleData(QNBleDevice device, QNScaleData data) {
                QNDemoLogger.d("HeightScaleActivity", "测量数据hmac:" + data.getHmac());
                storageTipTv.setText("测量数据barCode: " + data.getBarCode());
                onReceiveScaleData(data);
                QNScaleItemData fatValue = data.getItem(QNIndicator.TYPE_SUBFAT);
                if (fatValue != null) {
                    String value = String.valueOf(fatValue.getValue());
                    QNDemoLogger.d("HeightScaleActivity", "收到皮下脂肪数据:" + value);
                }
            }

            @Override
            public void onGetStoredScale(QNBleDevice device, List<QNScaleStoreData> storedDataList) {
                String text = "收到存储数据条数：" + storedDataList.size();
                QNDemoLogger.d("HeightScaleActivity", text);
                if (storedDataList != null && storedDataList.size() > 0) {
                    QNScaleStoreData data = storedDataList.get(0);
                    for (int i = 0; i < storedDataList.size(); i++) {
                        QNDemoLogger.d("HeightScaleActivity", "存储数据hamc:" + storedDataList.get(i).getHmac());
                    }
                    QNUser qnUser = createQNUser();
                    data.setUser(qnUser);
                    QNScaleData qnScaleData = data.generateScaleData();
                    QNDemoLogger.d("HeightScaleActivity", "存储数据 barCode:" + qnScaleData.getBarCode());
                    text = text + "\nbarCode: " + qnScaleData.getBarCode();
                    onReceiveScaleData(qnScaleData);
                }

                storageTipTv.setText(text);

            }

            @Override
            public void onGetElectric(QNBleDevice device, int electric) {
                String text = "收到电池电量百分比:" + electric;
                QNDemoLogger.d("HeightScaleActivity", text);
                if (electric == DecoderConst.NONE_BATTERY_VALUE) {//获取电池信息失败
                    return;
                }
                Toast.makeText(HeightScaleActivity.this, text, Toast.LENGTH_SHORT).show();
            }

            //测量过程中的连接状态
            @Override
            public void onScaleStateChange(QNBleDevice device, int status) {
                QNDemoLogger.d("HeightScaleActivity", "秤的连接状态是:" + status);
                setBleStatus(status);
            }

            @Override
            public void onScaleEventChange(QNBleDevice device, int scaleEvent) {
                QNDemoLogger.d("HeightScaleActivity", "秤返回的事件是:" + scaleEvent);
            }

            @Override
            public void readSnComplete(QNBleDevice device, String sn) {

            }

            @Override
            public void onGetBleVer(QNBleDevice device, int bleVer) {

            }

            @Override
            public void onGetBatteryLevel(QNBleDevice device, int batteryLevel, boolean isLowLevel) {

            }
        });
    }

    private void initIntent() {
        Intent intent = getIntent();
        if (intent != null) {
            mBleDevice = intent.getParcelableExtra(UserConst.DEVICE);
            mUser = intent.getParcelableExtra(UserConst.USER);
            qnWiFiConfig = intent.getParcelableExtra(UserConst.WIFI_CONFIG);
        }
    }

    private String initWeight(double weight) {
        int unit = mQNBleApi.getConfig().getUnit();
        return mQNBleApi.convertWeightWithTargetUnit(weight, unit);
    }

    private void initView() {
        mConnectBtn.setOnClickListener(this);
        switchUserBtn.setOnClickListener(this);
        scanWifiBtn.setOnClickListener(this);
        pairWifiBtn.setOnClickListener(this);
        wifiInfoBtn.setOnClickListener(this);
        wifiCleanBtn.setOnClickListener(this);
        deviceInfoBtn.setOnClickListener(this);
        resetBtn.setOnClickListener(this);
        settingBtn.setOnClickListener(this);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == WIFI_INFO_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                getPairWifiConfig();
                pairWifiConfig.setSsid(WifiInfoActivity.ssid);
                pairWifiConfig.setPwd(WifiInfoActivity.pwd);
            }
        }
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
                QNDemoLogger.d("HeightScaleActivity", "开始设置WiFi");
                break;
            case QNScaleStatus.STATE_WIFI_BLE_NETWORK_FAIL:
                stateString = getResources().getString(R.string.failed_to_set_wifi);
                btnString = getResources().getString(R.string.disconnected);
                QNDemoLogger.d("HeightScaleActivity", "设置WiFi失败");
                break;
            case QNScaleStatus.STATE_WIFI_BLE_NETWORK_SUCCESS:
                stateString = getResources().getString(R.string.success_to_set_wifi);
                btnString = getResources().getString(R.string.disconnected);
                QNDemoLogger.d("HeightScaleActivity", "设置WiFi成功");
                break;
            case QNScaleStatus.STATE_HEIGH_SCALE_MEASURE_FAIL:
                stateString = getResources().getString(R.string.measure_fail);
                btnString = getResources().getString(R.string.disconnected);
                QNDemoLogger.d("HeightScaleActivity", "身高体重秤测量失败");
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
            case R.id.setting_btn:
                showSettingDialog();
                break;
            case R.id.device_info_btn:
                mQNBleApi.getHeightScaleConfig((code, msg) -> {
                    QNDemoLogger.d("HeightScaleActivity", "获取身高体重秤信息操作:" + msg);
                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                });
                break;
            case R.id.reset_btn:
                mQNBleApi.resetHeightScale((code, msg) -> {
                    QNDemoLogger.d("HeightScaleActivity", "重置操作:" + msg);
                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                });
                break;
            case R.id.wifi_clean_btn:
                mQNBleApi.clearHeightScaleWifiConfig((code, msg) -> {
                    QNDemoLogger.d("HeightScaleActivity", "清除WIFI信息操作:" + msg);
                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                });
                break;
            case R.id.wifi_info_btn:

                mQNBleApi.getHeightScaleWifiConfig((code, msg) -> {
                    QNDemoLogger.d("HeightScaleActivity", "获取WIFI信息操作:" + msg);
                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                });

                break;
            case R.id.pair_wifi_btn:

                getPairWifiConfig();

                new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.distribution_network_information))
                        .setMessage("SSID:" + pairWifiConfig.getSsid() + "\n" + "pwd:" + pairWifiConfig.getPwd())
                        .setNegativeButton(R.string.cancel, null)
                        .setPositiveButton(R.string.confirm, (dialog, which) -> {
                            mQNBleApi.startPairHeightScaleWifi(pairWifiConfig, (code, msg) -> {
                                QNDemoLogger.d("HeightScaleActivity", "配对WIFI操作:" + msg);
                                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                            });
                        })
                        .create()
                        .show();

                break;
            case R.id.scan_wifi_btn:
                mQNBleApi.scanHeightScaleWifiSsid((code, msg) -> {
                    QNDemoLogger.d("HeightScaleActivity", "扫描WIFI操作:" + msg);
                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                });
                break;
            case R.id.switch_user_btn:
                BleUser bleUser = new BleUser();
                bleUser.setHeight(165);
                bleUser.setBirthday(DateUtils.stringToDate("1993-01-01"));
                bleUser.setGender(1);
                HeightScaleBleService.switchHeightScaleUser(this, bleUser);
                break;
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

    private void getPairWifiConfig() {
        if (pairWifiConfig == null) {
            pairWifiConfig = new QNWiFiConfig();
//            qnWiFiConfig.setSsid("King");
//            qnWiFiConfig.setPwd("987654321");
            pairWifiConfig.setSsid("yxb-mac-test-1234567891011121314");
            pairWifiConfig.setPwd("yxb666666");
            pairWifiConfig.setServeUrl("http://wsp-lite.yolanda.hk/yolanda/aios?code=");
            pairWifiConfig.setEncryptionKey("yolandakitnewhdr");
            pairWifiConfig.setFotaUrl("https://ota.volanda.hk");
        }
    }


    private void doConnect() {
        if (mBleDevice == null || mUser == null) {
            return;
        }

        QNHeightDeviceConfig deviceConfig = new QNHeightDeviceConfig();

        QNConfig qnConfig = QNConfigManager.getInstance().getQNConfig();
        deviceConfig.setWeightUnit(qnConfig.getUnit());
        deviceConfig.setHeightUnit(qnConfig.getHeightUnit());
        deviceConfig.setVoiceLanguage(qnConfig.getLanguage());

        deviceConfig.setUser(createQNUser());
        if (qnWiFiConfig != null) {
            deviceConfig.setWiFiConfig(qnWiFiConfig);
        }

        mQNBleApi.connectHeightScaleDevice(mBleDevice, deviceConfig, (code, msg) -> {
            QNDemoLogger.d("HeightScaleActivity", "连接设备返回:" + msg);
            if (code == 0) {
                mIsConnected = true;
            }
        });

    }

    private void doDisconnect() {
        mQNBleApi.disconnectDevice(mBleDevice, new QNResultCallback() {
            @Override
            public void onResult(int code, String msg) {
                QNDemoLogger.d("HeightScaleActivity", "断开连接设备返回:" + msg);
            }
        });
    }

    private void showSettingDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_settings, null);

        RadioGroup rgLanguage = view.findViewById(R.id.rgLanguage);
        RadioGroup rgWeight = view.findViewById(R.id.rgWeight);
        RadioGroup rgHeight = view.findViewById(R.id.rgHeight);
        RadioGroup rgVolume = view.findViewById(R.id.rgVolume);

        selectRadio(rgLanguage, KEY_LANGUAGE);
        selectRadio(rgWeight, KEY_WEIGHT_UNIT);
        selectRadio(rgHeight, KEY_HEIGHT_UNIT);
        selectRadio(rgVolume, KEY_VOLUME);

        new AlertDialog.Builder(this)
                .setTitle("语音播报设置")
                .setView(view)
                .setPositiveButton("确认", (dialog, which) -> {
                    int selectedLang = getSelectedValue(rgLanguage);
                    int selectedWeight = getSelectedValue(rgWeight);
                    int selectedHeight = getSelectedValue(rgHeight);
                    int selectedVolume = getSelectedValue(rgVolume);

                    QNHeightDeviceFunction function = saveSettings(selectedLang, selectedWeight, selectedHeight, selectedVolume);

                    mQNBleApi.setHeightScaleConfig(function, (code, msg) -> {

                    });

                })
                .setNegativeButton("取消", (dialog, which) -> dialog.dismiss())
                .show();
    }

    /**
     * 根据保存值选中Radio
     */
    private void selectRadio(RadioGroup group, int value) {
        for (int i = 0; i < group.getChildCount(); i++) {
            View v = group.getChildAt(i);
            if (v.getTag() != null && Integer.parseInt(v.getTag().toString()) == value) {
                ((android.widget.RadioButton) v).setChecked(true);
                break;
            }
        }
    }

    /**
     * 获取选中的值
     */
    private int getSelectedValue(RadioGroup group) {
        int id = group.getCheckedRadioButtonId();
        if (id != -1) {
            View v = group.findViewById(id);
            if (v.getTag() != null) {
                return Integer.parseInt(v.getTag().toString());
            }
        }
        return -1;
    }

    int KEY_LANGUAGE = -1;
    int KEY_WEIGHT_UNIT = -1;
    int KEY_HEIGHT_UNIT = -1;
    int KEY_VOLUME = -1;

    /**
     * 保存设置
     *
     * @return
     */
    private QNHeightDeviceFunction saveSettings(int lang, int weight, int height, int volume) {
        KEY_LANGUAGE = lang;
        KEY_WEIGHT_UNIT = weight;
        KEY_HEIGHT_UNIT = height;
        KEY_VOLUME = volume;

        QNHeightDeviceFunction function = new QNHeightDeviceFunction();
        function.setLanguage(lang);
        function.setWeightUnit(weight);
        function.setHeightUnit(height);
        function.setVolume(volume);
        return function;
    }
}
