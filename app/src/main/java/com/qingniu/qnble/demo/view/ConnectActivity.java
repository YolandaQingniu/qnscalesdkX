package com.qingniu.qnble.demo.view;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.qingniu.qnble.demo.R;
import com.qingniu.qnble.demo.adapter.ListAdapter;
import com.qingniu.qnble.demo.bean.User;
import com.qingniu.qnble.demo.util.AndroidPermissionCenter;
import com.qingniu.qnble.demo.util.ToastMaker;
import com.qingniu.qnble.demo.util.UserConst;
import com.qingniu.scale.constant.DecoderConst;
import com.qn.device.constant.CheckStatus;
import com.qn.device.constant.QNIndicator;
import com.qn.device.constant.QNScaleEvent;
import com.qn.device.constant.QNScaleStatus;
import com.qn.device.constant.UserGoal;
import com.qn.device.constant.UserShape;
import com.qn.device.listener.QNBleConnectionChangeListener;
import com.qn.device.listener.QNScaleDataListener;
import com.qn.device.listener.QNLogListener;
import com.qn.device.listener.QNResultCallback;
import com.qn.device.out.QNBleApi;
import com.qn.device.out.QNBleDevice;
import com.qn.device.out.QNScaleData;
import com.qn.device.out.QNScaleItemData;
import com.qn.device.out.QNScaleStoreData;
import com.qn.device.out.QNUser;
import com.qn.device.out.QNWiFiConfig;
import com.yl.pack.YLPacker;

import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


/**
 * author: yolanda-XY
 * date: 2018/3/23
 * package_name: com.qingniu.qnble.demo
 * description: ${设置用户信息界面}
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



    @BindView(R.id.eight_hamc_test_layout)
    LinearLayout eightHmacTestLayout;
    @BindView(R.id.resistance_20k_et)
    EditText resistance20kEt;
    @BindView(R.id.resistance_100k_et)
    EditText resistance100kEt;
    @BindView(R.id.eight_weight_et)
    EditText eightWeightEt;
    @BindView(R.id.generate_hmac_btn)
    Button generateHmacBtn;
    @BindView(R.id.use_last_hmac_btn)
    Button useLastHmacBtn;
    @BindView(R.id.cur_resistance_20k_tv)
    TextView curResistance20kTv;
    @BindView(R.id.cur_resistance_100k_tv)
    TextView curResistance100kTv;
    @BindView(R.id.snTextView)
    TextView snTextView;
    @BindView(R.id.turnOnMeasureFatBtn)
    Button turnOnMeasureFatBtn;
    @BindView(R.id.turnOffMeasureFatBtn)
    Button turnOffMeasureFatBtn;

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

            @Override
            public void onStartInteracting(QNBleDevice device) {

            }
        });
    }

    private void connectQnDevice(QNBleDevice device) {
        if (null != mQnWiFiConfig) {
            mQNBleApi.connectDeviceSetWiFi(device, createQNUser(), mQnWiFiConfig, new QNResultCallback() {
                @Override
                public void onResult(int code, String msg) {
                    Log.e("ConnectActivity", "wifi 配置code:" + code + ",msg:" + msg);
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
                boolean isEightData = data.getItemValue(QNIndicator.TYPE_LEFT_ARM_MUSCLE_WEIGHT_INDEX) > 0;
                //增加八电极指标适配
                listAdapter.setEight(isEightData);
                if (isEightData && false){
                    eightHmacTestLayout.setVisibility(View.VISIBLE);
                }
                else {
                    eightHmacTestLayout.setVisibility(View.GONE);
                }

                QNScaleItemData fatValue = data.getItem(QNIndicator.TYPE_SUBFAT);
                if (fatValue != null) {
                    String value = fatValue.getValue() + "";
                    Log.d("ConnectActivity", "收到皮下脂肪数据:" + value);
                }
                currentQNScaleData = data;
                historyQNScaleData.add(data);
                Log.d("ConnectActivity", "加密hmac为:" + data.getHmac());
//                Log.d("ConnectActivity", "收到体脂肪:"+data.getItem(QNIndicator.TYPE_BODYFAT).getValue());
//                doDisconnect();


                onReceiveScaleData(currentQNScaleData);
            }

            @Override
            public void onGetStoredScale(QNBleDevice device, List<QNScaleStoreData> storedDataList) {
                Log.d("ConnectActivity", "收到存储数据 " + storedDataList.size() + "条");
                if (storedDataList != null && storedDataList.size() > 0) {
                    QNScaleStoreData data = storedDataList.get(0);
                    for (int i = 0; i < storedDataList.size(); i++) {
                        Log.d("ConnectActivity", "收到存储数据:" + storedDataList.get(i).getWeight());
                    }
                    QNUser qnUser = createQNUser();
                    data.setUser(qnUser);
                    QNScaleData qnScaleData = data.generateScaleData();

                    boolean isEightData = qnScaleData.getItemValue(QNIndicator.TYPE_LEFT_ARM_MUSCLE_WEIGHT_INDEX) > 0;
                    //增加八电极指标适配
                    listAdapter.setEight(isEightData);
                    if (isEightData  && false){
                        eightHmacTestLayout.setVisibility(View.VISIBLE);
                    }
                    else {
                        eightHmacTestLayout.setVisibility(View.GONE);
                    }

                    Log.d("ConnectActivity", "存储数据加密hmac为:" + data.getHmac());

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

            @Override
            public void onScaleEventChange(QNBleDevice device, int scaleEvent) {
                Log.d("ConnectActivity", "秤返回的事件是:" + scaleEvent);
                if (scaleEvent == QNScaleEvent.EVENT_UPDATE_SCALE_CONFIG_SUCCESS) {
                    Log.e("UserScaleActivity", "更新秤端设置成功");
                    ToastMaker.show(ConnectActivity.this, "更新秤端设置成功");
                } else if (scaleEvent == QNScaleEvent.EVENT_UPDATE_SCALE_CONFIG_FAIL) {
                    Log.e("UserScaleActivity", "更新秤端设置失败");
                    ToastMaker.show(ConnectActivity.this, "更新秤端设置失败");
                }
            }

            @Override
            public void readSnComplete(QNBleDevice device, String sn) {
                snTextView.setText("SN码: "+sn);
            }
        });
    }


    /**
     * 后续需删除，显示收到的八电极测量数据和存储数据的阻抗
     */
    private void initEightDataTv(@NonNull String hmac){
        try {
            JSONObject jsonObject = new JSONObject(YLPacker.unpack(hmac));
            int eightFlag  = jsonObject.optInt("eight_flag");

            if (eightFlag == 1){
                String lastResistanceRH20 = String.format("%.2f",jsonObject.getDouble("res20_right_arm"));
                String lastResistanceLH20 = String.format("%.2f",jsonObject.getDouble("res20_left_arm"));
                String lastResistanceT20 = String.format("%.2f",jsonObject.getDouble("res20_trunk"));
                String lastResistanceRF20 = String.format("%.2f",jsonObject.getDouble("res20_right_leg"));
                String lastResistanceLF20 = String.format("%.2f",jsonObject.getDouble("res20_left_leg"));

                String lastResistanceRH100 = String.format("%.2f",jsonObject.getDouble("res100_right_arm"));
                String lastResistanceLH100 = String.format("%.2f",jsonObject.getDouble("res100_left_arm"));
                String lastResistanceT100 = String.format("%.2f",jsonObject.getDouble("res100_trunk"));
                String lastResistanceRF100 = String.format("%.2f",jsonObject.getDouble("res100_right_leg"));
                String lastResistanceLF100 = String.format("%.2f",jsonObject.getDouble("res100_left_leg"));

                String cur20kString = lastResistanceRH20+" "+lastResistanceLH20+" "+lastResistanceT20+" "+lastResistanceRF20+" "+lastResistanceLF20;
                String cur100kString = lastResistanceRH100+" "+lastResistanceLH100+" "+lastResistanceT100+" "+lastResistanceRF100+" "+lastResistanceLF100;
                curResistance20kTv.setText(cur20kString);
                curResistance100kTv.setText(cur100kString);
            }
            else {
                curResistance20kTv.setText("");
                curResistance100kTv.setText("");
            }
        }
        catch (Exception e){
            Toast.makeText(ConnectActivity.this, "当前数据传入的hmac错误！", Toast.LENGTH_SHORT).show();
        }
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
        if (mBleDevice.isNormalPregnantScale()){
            turnOnMeasureFatBtn.setVisibility(View.VISIBLE);
            turnOffMeasureFatBtn.setVisibility(View.VISIBLE);
        }else {
            turnOnMeasureFatBtn.setVisibility(View.GONE);
            turnOffMeasureFatBtn.setVisibility(View.GONE);
        }
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
        mDatas.addAll(md.getAllItem());
        listAdapter.notifyDataSetChanged();
        initEightDataTv(md.getHmac());
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
                hmacTest.setText("");
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
        if (null != mQnWiFiConfig) {
            mQNBleApi.connectDeviceSetWiFi(mBleDevice, createQNUser(), mQnWiFiConfig, new QNResultCallback() {
                @Override
                public void onResult(int code, String msg) {
                    Log.e("ConnectActivity", "wifi 配置code:" + code + ",msg:" + msg);
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
        mIsConnected = false;
        mQNBleApi.disconnectDevice(mBleDevice, new QNResultCallback() {
            @Override
            public void onResult(int code, String msg) {
                Log.d("ConnectActivity", "断开连接设备返回:" + msg);
            }
        });
    }

    @OnClick({R.id.stroteDataTest, R.id.setThreshold, R.id.testHmac, R.id.generate_hmac_btn, R.id.use_last_hmac_btn, R.id.turnOnMeasureFatBtn, R.id.turnOffMeasureFatBtn})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.stroteDataTest:
                //{"weight"=>"25.35", "measure_time"=>"2019-05-06 14:02:51", "mac"=>"F0:FE:6B:CB:75:6A", "model_id"=>"0005", "sign"=>"3F828A0207EB762F0D12E1ED5345AF7D6907304A74A45990B254256AC08DAA76EEA778E4B50ACE92D47DA72DD7257F82734C33A56721D797FD932B3741E5C730F2901F7EFAA1755DD0683BABD0959BB1E82201C3B50B3E8A5360A3D57550CF446DC834B8FA2F0D16DA4C0797CC1C308E4253413D4AB90DC4093F8065199ABE8AB0C9D06E3172E511C54C7E5095BB92C753070DC0CEB5D64785C4577952B50465"}
                //注释的代码为从服务器收到的下发数据，通过下面的方法生成测量数据。（此方法只针对双模秤有效）
                QNScaleStoreData qnScaleStoreData = new QNScaleStoreData();
                qnScaleStoreData.setUser(createQNUser());
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                try {
                    Date date = simpleDateFormat.parse("2022-12-13 11:34:45");
                    qnScaleStoreData.buildStoreData(17.35, date, "ED:67:39:57:26:43",
                            "1da47865046748e3768bd10131c2516402817c1d5614d9b0f2bdfe1c9ed5d606307c068a31c77cced9327ab52a38a42026b4ac4bef7404d5de631b1c3780ea08d3cfcfe4bdf22602edc3fd4f2c490125afd341df68b2bc534dda393ee7f37271db495ac09a930870c9a73071a5139dfe61c8898ca52c59b09b32a5613867753abb4a6549d121f3e9e8854a7572fc4b40ddc91e9911dff53cdafb8545bc2a3510",
                            new QNResultCallback() {
                                @Override
                                public void onResult(int code, String msg) {
                                    Log.e("buildStoreData", "code=" + code + ",msg=" + msg);
                                }
                            });
                    QNScaleData qnScaleData = qnScaleStoreData.generateScaleData();
                    onReceiveScaleData(qnScaleData);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                break;
            case R.id.setThreshold:
                if (TextUtils.isEmpty(threshold.getText().toString())) {
                    ToastMaker.show(this, getResources().getString(R.string.please_enter_body_fat_change_control));
                    return;
                }
                if (null == currentQNScaleData) {
                    ToastMaker.show(this, getResources().getString(R.string.set_body_fat_change_hint));
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
            case R.id.turnOnMeasureFatBtn:
                mQNBleApi.setFatMeasurementSwitch(true, false, new QNResultCallback() {
                    @Override
                    public void onResult(int i, String s) {
                        ToastMaker.show(ConnectActivity.this, "秤端设置调用结果 "+s);
                    }
                });
                break;
            case R.id.turnOffMeasureFatBtn:
                mQNBleApi.setFatMeasurementSwitch(false, false, new QNResultCallback() {
                    @Override
                    public void onResult(int i, String s) {
                        ToastMaker.show(ConnectActivity.this, "秤端设置调用结果 "+s);
                    }
                });
                break;
            case R.id.generate_hmac_btn:
                try {
                    ArrayList<Double> list20k = new ArrayList<>();
                    for (String s : resistance20kEt.getText().toString().split(" ")) {
                        list20k.add(Double.valueOf(s));
                    }
                    ArrayList<Double> list100k = new ArrayList<>();
                    for (String s : resistance100kEt.getText().toString().split(" ")) {
                        list100k.add(Double.valueOf(s));
                    }
                    Double eightWeight = Double.valueOf(eightWeightEt.getText().toString());
                    if (list20k.size() != 5 || list100k.size() != 5 || eightWeight<=0){
                        Toast.makeText(ConnectActivity.this, getString(R.string.please_input_correct_resistance_and_weight), Toast.LENGTH_SHORT).show();
                    }
                    else {
                        JSONObject jsonObject = new JSONObject();

                        jsonObject.put("resistance_50_adjust", list20k.get(4)+list20k.get(3));
                        jsonObject.put("resistance_500_adjust", list100k.get(4)+list100k.get(3));

                        jsonObject.put("weight", eightWeight);
                        jsonObject.put("eight_flag", 1);

                        jsonObject.put("res20_left_arm", list20k.get(1));
                        jsonObject.put("res20_left_leg", list20k.get(4));
                        jsonObject.put("res20_right_arm", list20k.get(0));
                        jsonObject.put("res20_right_leg", list20k.get(3));
                        jsonObject.put("res20_trunk", list20k.get(2));

                        jsonObject.put("res100_left_arm", list100k.get(1));
                        jsonObject.put("res100_left_leg", list100k.get(4));
                        jsonObject.put("res100_right_arm", list100k.get(0));
                        jsonObject.put("res100_right_leg", list100k.get(3));
                        jsonObject.put("res100_trunk", list100k.get(2));
                        String hmac = YLPacker.pack(jsonObject.toString());

                        if (null == currentQNScaleData) {
                            ToastMaker.show(this, getResources().getString(R.string.set_body_fat_change_hint));
                            return;
                        }
                        if (!TextUtils.isEmpty(hmac)){
                            currentQNScaleData.setFatThreshold(hmac, 0.0,
                                    new QNResultCallback() {
                                        @Override
                                        public void onResult(int code, String msg) {
                                            Log.e("setEightFatThreshold", "code=" + code + ",msg=" + msg);
                                            if (code == CheckStatus.OK.getCode()) {
                                                //设置完后得到调整后数据并进行显示
                                                onReceiveScaleData(currentQNScaleData);
                                                Toast.makeText(ConnectActivity.this, getResources().getString(R.string.set_eight_fat_threshold_success), Toast.LENGTH_SHORT).show();
                                            }
                                            else {
                                                Toast.makeText(ConnectActivity.this, "error "+"code=" + code + ",msg=" + msg, Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        }
                    }
                }
                catch (Exception e){
                    Toast.makeText(ConnectActivity.this, getString(R.string.please_input_correct_resistance_and_weight), Toast.LENGTH_SHORT).show();
                }
                finally {
                    break;
                }
            case R.id.use_last_hmac_btn:
                if (historyQNScaleData.size() < 2) {
                    ToastMaker.show(this, getResources().getString(R.string.set_body_fat_change_hint1));
                    return;
                }
                QNScaleData preData = historyQNScaleData.get(historyQNScaleData.size() - 2);
                if (preData.getItemValue(QNIndicator.TYPE_LEFT_ARM_FAT_INDEX)< 0 ){
                    ToastMaker.show(this, getResources().getString(R.string.set_body_fat_change_hint1));
                    return;
                }
                else {
                    if (null == currentQNScaleData) {
                        ToastMaker.show(this, getResources().getString(R.string.set_body_fat_change_hint));
                        return;
                    }
                    if (!TextUtils.isEmpty(preData.getHmac())){
                        currentQNScaleData.setFatThreshold(preData.getHmac(), 0.0,
                                new QNResultCallback() {
                                    @Override
                                    public void onResult(int code, String msg) {
                                        Log.e("setEightFatThreshold", "code=" + code + ",msg=" + msg);
                                        if (code == CheckStatus.OK.getCode()) {
                                            //设置完后得到调整后数据并进行显示
                                            onReceiveScaleData(currentQNScaleData);
                                            Toast.makeText(ConnectActivity.this, getResources().getString(R.string.set_eight_fat_threshold_success), Toast.LENGTH_SHORT).show();
                                        }
                                        else {
                                            Toast.makeText(ConnectActivity.this, "error "+"code=" + code + ",msg=" + msg, Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                    }
                }
                break;
            default:
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
