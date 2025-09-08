package com.qingniu.qnble.demo.view;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import com.qingniu.qnble.demo.util.QNDemoLogger;
import com.qingniu.qnble.demo.util.UserConst;
import com.qingniu.scale.config.DecoderAdapterManager;
import com.qingniu.scale.config.DoubleDecoderAdapter;
import com.qingniu.scale.constant.DecoderConst;
import com.qn.device.constant.QNBleConst;
import com.qn.device.constant.QNIndicator;
import com.qn.device.constant.QNScaleStatus;
import com.qn.device.constant.UserGoal;
import com.qn.device.constant.UserShape;
import com.qn.device.listener.QNBleProtocolDelegate;
import com.qn.device.listener.QNLogListener;
import com.qn.device.listener.QNResultCallback;
import com.qn.device.listener.QNScaleDataListener;
import com.qn.device.out.QNBleApi;
import com.qn.device.out.QNBleDevice;
import com.qn.device.out.QNBleProtocolHandler;
import com.qn.device.out.QNScaleData;
import com.qn.device.out.QNScaleItemData;
import com.qn.device.out.QNScaleStoreData;
import com.qn.device.out.QNUser;
import com.qn.device.out.QNWiFiConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * author: yolanda-zhao
 * description:自主普通秤连接测量界面(单设备连接)
 * date: 2019/9/6
 */

public class SelfConnectActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "SelfConnectActivity";

    public static Intent getCallIntent(Context context, User user, QNBleDevice device) {
        return new Intent(context, SelfConnectActivity.class)
                .putExtra(UserConst.USER, user)
                .putExtra(UserConst.DEVICE, device);
    }

    public static Intent getCallIntent(Context context, User user, QNBleDevice device, QNWiFiConfig qnWiFiConfig) {
        return new Intent(context, SelfConnectActivity.class)
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


    private Handler mHandler = new Handler(Looper.myLooper());

    private BluetoothGattCharacteristic qnReadBgc, qnWriteBgc, qnBleReadBgc, qnBleWriteBgc;

    private QNBleDevice mBleDevice;
    private List<QNScaleItemData> mDatas = new ArrayList<>();
    private QNBleApi mQNBleApi;

    private User mUser;
    private QNWiFiConfig mQnWiFiConfig;

    private boolean mIsConnected;

    private BluetoothGatt mBluetoothGatt;

    private boolean isFirstService;

    private QNBleProtocolHandler mProtocolhandler;

    private QNScaleData currentQNScaleData;
    private List<QNScaleData> historyQNScaleData = new ArrayList<>();
    private ListAdapter listAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_self_connect);
        mQNBleApi = QNBleApi.getInstance(this);
        ButterKnife.bind(this);
        initIntent();
        initView();
        initData();
    }

    private BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);

            QNDemoLogger.d(TAG, "onConnectionStateChange: " + newState);

            if (status != BluetoothGatt.GATT_SUCCESS) {
                String err = "Cannot connect device with error status: " + status;
                // 当尝试连接失败的时候调用 disconnect 方法是不会引起这个方法回调的，所以这里直接回调就可以了
                gatt.close();
                if (mBluetoothGatt != null) {
                    mBluetoothGatt.disconnect();
                    mBluetoothGatt.close();
                    mBluetoothGatt = null;
                }
                setBleStatus(QNScaleStatus.STATE_DISCONNECTED);
                mIsConnected = false;
                QNDemoLogger.e(TAG, err);
                return;
            }
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                mIsConnected = true;

                //当蓝牙设备已经接
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        setBleStatus(QNScaleStatus.STATE_CONNECTED);
                        Toast.makeText(SelfConnectActivity.this, getResources().getString(R.string.connect_successfully), Toast.LENGTH_SHORT).show();
                    }
                });

                // TODO: 2019/9/7  某些手机可能存在无法发现服务问题,此处可做延时操作
                if (mBluetoothGatt != null) {
                    mBluetoothGatt.discoverServices();
                }

                QNDemoLogger.d(TAG, "onConnectionStateChange: " + "连接成功");

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                mIsConnected = false;
                //当设备无法连接
                if (mBluetoothGatt != null) {
                    mBluetoothGatt.disconnect();
                    mBluetoothGatt.close();
                    mBluetoothGatt = null;
                }

                qnReadBgc = null;
                qnWriteBgc = null;
                qnBleReadBgc = null;
                qnBleWriteBgc = null;

                gatt.close();
                //TODO 实际运用中可发起重新连接
//                if (mBleDevice != null) {
//                    connectQnDevice(mBleDevice);
//                }
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        setBleStatus(QNScaleStatus.STATE_LINK_LOSS);
                    }
                });

            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            QNDemoLogger.d(TAG, "onServicesDiscovered------: " + "发现服务----" + status);

            if (status == BluetoothGatt.GATT_SUCCESS) {
                //发现服务,并遍历服务,找到公司对于的设备服务
                List<BluetoothGattService> services = gatt.getServices();

                for (BluetoothGattService service : services) {
                    //第一套
                    if (service.getUuid().equals(UUID.fromString(QNBleConst.UUID_IBT_SERVICES))) {
                        if (mProtocolhandler != null) {
                            //使能所有特征值
                            initCharacteristic(gatt, true);
                            QNDemoLogger.d(TAG, "onServicesDiscovered------: " + "发现服务为第一套");
                            mProtocolhandler.prepare(QNBleConst.UUID_IBT_SERVICES);
                        }
                        break;
                    }
                    //第二套
                    if (service.getUuid().equals(UUID.fromString(QNBleConst.UUID_IBT_SERVICES_1))) {
                        if (mProtocolhandler != null) {
                            //使能所有特征值
                            QNDemoLogger.d(TAG, "onServicesDiscovered------: " + "发现服务为第二套");
                            initCharacteristic(gatt, false);
                            mProtocolhandler.prepare(QNBleConst.UUID_IBT_SERVICES_1);
                        }
                        break;
                    }

                }

            } else {
                QNDemoLogger.d(TAG, "onServicesDiscovered---error: " + status);
            }
        }


        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            QNDemoLogger.d(TAG, "onCharacteristicRead---收到数据:  ");
            if (status == BluetoothGatt.GATT_SUCCESS) {
                //获取到数据
                if (mProtocolhandler != null) {
                    mProtocolhandler.onGetBleData(getService(), characteristic.getUuid().toString(), characteristic.getValue());
                }
            } else {
                QNDemoLogger.d(TAG, "onCharacteristicRead---error: " + status);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);

            QNDemoLogger.d(TAG, "onCharacteristicChanged---收到数据:  ");
            //获取到数据
            if (mProtocolhandler != null) {
                mProtocolhandler.onGetBleData(getService(), characteristic.getUuid().toString(), characteristic.getValue());
            }

        }

    };

    private void initCharacteristic(BluetoothGatt gatt, boolean isFirstService) {

        this.isFirstService = isFirstService;

        //第一套服务
        if (isFirstService) {
            qnReadBgc = getCharacteristic(gatt, QNBleConst.UUID_IBT_SERVICES, QNBleConst.UUID_IBT_READ);
            qnWriteBgc = getCharacteristic(gatt, QNBleConst.UUID_IBT_SERVICES, QNBleConst.UUID_IBT_WRITE);
            qnBleReadBgc = getCharacteristic(gatt, QNBleConst.UUID_IBT_SERVICES, QNBleConst.UUID_IBT_BLE_READER);
            qnBleWriteBgc = getCharacteristic(gatt, QNBleConst.UUID_IBT_SERVICES, QNBleConst.UUID_IBT_BLE_WRITER);
        } else {
            qnReadBgc = getCharacteristic(gatt, QNBleConst.UUID_IBT_SERVICES_1, QNBleConst.UUID_IBT_READ_1);
            qnWriteBgc = getCharacteristic(gatt, QNBleConst.UUID_IBT_SERVICES_1, QNBleConst.UUID_IBT_WRITE_1);
        }

        enableNotifications(qnReadBgc);
        enableIndications(qnBleReadBgc);
    }

    private String getService() {
        if (isFirstService) {
            return QNBleConst.UUID_IBT_SERVICES;
        } else {
            return QNBleConst.UUID_IBT_SERVICES_1;
        }
    }

    private BluetoothGattCharacteristic getCharacteristic(final BluetoothGatt gatt, String serviceUuid, String characteristicUuid) {
        BluetoothGattService service = gatt.getService(UUID.fromString(serviceUuid));
        if (service == null) {
            return null;
        }
        return service.getCharacteristic(UUID.fromString(characteristicUuid));
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

    private void buildHandler() {
        mProtocolhandler = mQNBleApi.buildProtocolHandler(mBleDevice, createQNUser(), new QNBleProtocolDelegate() {
            @Override
            public void writeCharacteristicValue(String service_uuid, String characteristic_uuid, byte[] data, QNBleDevice qnBleDevice) {
                writeCharacteristicData(service_uuid, characteristic_uuid, data, qnBleDevice.getMac());
            }

            @Override
            public void readCharacteristic(String service_uuid, String characteristic_uuid, QNBleDevice qnBleDevice) {
                readCharacteristicData(service_uuid, characteristic_uuid, qnBleDevice.getMac());

            }
        }, new QNResultCallback() {
            @Override
            public void onResult(int code, String msg) {
                QNDemoLogger.d(TAG, "创建结果----" + code + " ------------- " + msg);
            }
        });
    }

    private void readCharacteristicData(String service_uuid, String characteristic_uuid, String mac) {

        switch (characteristic_uuid) {
            case QNBleConst.UUID_IBT_READ:

                if (mBluetoothGatt != null && qnReadBgc != null) {
                    mBluetoothGatt.readCharacteristic(qnReadBgc);
                }

                break;
            case QNBleConst.UUID_IBT_BLE_READER:

                if (mBluetoothGatt != null && qnBleReadBgc != null) {
                    mBluetoothGatt.readCharacteristic(qnBleReadBgc);
                }

                break;
            case QNBleConst.UUID_IBT_READ_1:

                if (mBluetoothGatt != null && qnReadBgc != null) {
                    mBluetoothGatt.readCharacteristic(qnReadBgc);
                }

                break;

        }

    }

    private void writeCharacteristicData(String service_uuid, String characteristic_uuid, byte[] data, String mac) {
        switch (characteristic_uuid) {
            case QNBleConst.UUID_IBT_WRITE:

                if (mBluetoothGatt != null && qnWriteBgc != null) {
                    qnWriteBgc.setValue(data);
                    mBluetoothGatt.writeCharacteristic(qnWriteBgc);
                }

                break;
            case QNBleConst.UUID_IBT_BLE_WRITER:

                if (mBluetoothGatt != null && qnBleWriteBgc != null) {
                    qnBleWriteBgc.setValue(data);
                    mBluetoothGatt.writeCharacteristic(qnBleWriteBgc);
                }

                break;
            case QNBleConst.UUID_IBT_WRITE_1:

                if (mBluetoothGatt != null && qnWriteBgc != null) {
                    qnWriteBgc.setValue(data);
                    mBluetoothGatt.writeCharacteristic(qnWriteBgc);
                }

                break;
        }


    }

    private boolean enableNotifications(BluetoothGattCharacteristic characteristic) {

        final BluetoothGatt gatt = mBluetoothGatt;

        if (gatt == null || characteristic == null)
            return false;


        int properties = characteristic.getProperties();
        if ((properties & BluetoothGattCharacteristic.PROPERTY_NOTIFY) == 0)
            return false;

        boolean isSuccess = gatt.setCharacteristicNotification(characteristic, true);

        final BluetoothGattDescriptor descriptor = characteristic.getDescriptor(QNBleConst.CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID);
        if (descriptor != null) {
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            return gatt.writeDescriptor(descriptor);
        }

        return false;
    }


    private boolean enableIndications(BluetoothGattCharacteristic characteristic) {

        final BluetoothGatt gatt = mBluetoothGatt;

        if (gatt == null || characteristic == null)
            return false;

        int properties = characteristic.getProperties();
        if ((properties & BluetoothGattCharacteristic.PROPERTY_INDICATE) == 0)
            return false;

        boolean isSuccess = gatt.setCharacteristicNotification(characteristic, true);

        final BluetoothGattDescriptor descriptor = characteristic.getDescriptor(QNBleConst.CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID);
        if (descriptor != null) {
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
            QNDemoLogger.d(TAG, "enableIndications----------" + characteristic.getUuid());
            return gatt.writeDescriptor(descriptor);
        }
        return false;
    }

    /**
     * 获取设备与蓝牙的连接状态
     */
    private void initBleConnectStatus() {

    }

    /**
     * @param device 连接设备
     */
    private void connectQnDevice(QNBleDevice device) {
        setBleStatus(QNScaleStatus.STATE_CONNECTING);
        buildHandler();

        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        BluetoothAdapter adapter = bluetoothManager.getAdapter();
        BluetoothDevice mDevice = adapter.getRemoteDevice(device.getMac());

        if (mDevice != null) {
            QNDemoLogger.d(TAG, "connectQnDevice------: " + mDevice.getAddress());
            mBluetoothGatt = mDevice.connectGatt(SelfConnectActivity.this, false, mGattCallback);
        }

        //是双模秤需要配置wifi配置项(此处需要在连接以后读取数据才会用到)
        if (mQnWiFiConfig != null && mBleDevice.isSupportWifi()) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    DoubleDecoderAdapter doubleDecoderAdapter = DecoderAdapterManager.getInstance().getDoubleDecoderAdapter();
                    QNDemoLogger.d(TAG, "connectQnDevice------: " + doubleDecoderAdapter);
                    doubleDecoderAdapter.sendWIFIInfo(mQnWiFiConfig.getSsid(), mQnWiFiConfig.getPwd(), QNBleConst.WIFI_INFO_AUTHMODE, QNBleConst.WIFI_INFO_TYPE);
                    //设置完后需要初始化,避免重连设备时重复配网
                    mQnWiFiConfig = null;
                }
            }, 2000);
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
                        QNDemoLogger.d(TAG, "创建用户信息返回:" + msg);
                    }
                });
    }


    private void initUserData() {
        mQNBleApi.setDataListener(new QNScaleDataListener() {

            @Override
            public void onGetBarCode(String devMac, String barCode) {

            }

            @Override
            public void onGetBarCodeFail(String devMac) {

            }

            @Override
            public void onGetBarCodeGunState(String devMac, boolean isConnected) {

            }

            @Override
            public void onGetUnsteadyWeight(QNBleDevice device, double weight) {
                QNDemoLogger.d(TAG, "体重是:" + weight);
                mWeightTv.setText(initWeight(weight));
            }

            @Override
            public void onGetScaleData(QNBleDevice device, QNScaleData data) {
                QNDemoLogger.d(TAG, "收到测量数据");
                onReceiveScaleData(data);
                QNScaleItemData fatValue = data.getItem(QNIndicator.TYPE_SUBFAT);
                if (fatValue != null) {
                    String value = fatValue.getValue() + "";
                    QNDemoLogger.d(TAG, "收到皮下脂肪数据:" + value);
                }
                currentQNScaleData = data;
                historyQNScaleData.add(data);
                //测量结束,断开连接
                doDisconnect();
                QNDemoLogger.d(TAG, "加密hmac为:" + data.getHmac());
//                QNDemoLogger.d(TAG, "收到体脂肪:"+data.getItem(QNIndicator.TYPE_BODYFAT).getValue());
            }

            @Override
            public void onGetStoredScale(QNBleDevice device, List<QNScaleStoreData> storedDataList) {
                QNDemoLogger.d(TAG, "收到存储数据");
                if (storedDataList != null && storedDataList.size() > 0) {
                    QNScaleStoreData data = storedDataList.get(0);
                    QNDemoLogger.d(TAG, "收到存储数据:" + data.getWeight());
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
                String text = getResources().getString(R.string.percentage_of_battery_received) + electric;
                QNDemoLogger.d(TAG, text);
                if (electric == DecoderConst.NONE_BATTERY_VALUE) {//获取电池信息失败
                    return;
                }
                Toast.makeText(SelfConnectActivity.this, text, Toast.LENGTH_SHORT).show();
            }

            //测量过程中的连接状态
            @Override
            public void onScaleStateChange(QNBleDevice device, int status) {
                QNDemoLogger.d(TAG, "秤的连接状态是:" + status);
                setBleStatus(status);
            }

            @Override
            public void onScaleEventChange(QNBleDevice device, int scaleEvent) {
                QNDemoLogger.d("ConnectActivity", "秤返回的事件是:" + scaleEvent);
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
            mQnWiFiConfig = intent.getParcelableExtra(UserConst.WIFI_CONFIG);

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
                QNDemoLogger.d(TAG, "开始设置WiFi");
                break;
            case QNScaleStatus.STATE_WIFI_BLE_NETWORK_FAIL:
                stateString = getResources().getString(R.string.failed_to_set_wifi);
                btnString = getResources().getString(R.string.disconnected);
                QNDemoLogger.d(TAG, "设置WiFi失败");
                break;
            case QNScaleStatus.STATE_WIFI_BLE_NETWORK_SUCCESS:
                stateString = getResources().getString(R.string.success_to_set_wifi);
                btnString = getResources().getString(R.string.disconnected);
                QNDemoLogger.d(TAG, "设置WiFi成功");
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
        connectQnDevice(mBleDevice);
    }

    /**
     * 断开连接
     */
    private void doDisconnect() {
        if (mBluetoothGatt != null) {
            mBluetoothGatt.disconnect();
        }

        if (mProtocolhandler != null) {
            mProtocolhandler = null;
        }
    }

}

