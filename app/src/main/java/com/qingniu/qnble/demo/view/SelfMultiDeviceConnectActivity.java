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
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.qingniu.qnble.demo.R;
import com.qingniu.qnble.demo.bean.Config;
import com.qingniu.qnble.demo.bean.User;
import com.qingniu.qnble.demo.util.AndroidPermissionCenter;
import com.qingniu.qnble.demo.util.QNDemoLogger;
import com.qingniu.qnble.demo.util.ToastMaker;
import com.qingniu.qnble.demo.util.UserConst;
import com.qingniu.scale.constant.DecoderConst;
import com.qn.device.constant.CheckStatus;
import com.qn.device.constant.QNBleConst;
import com.qn.device.constant.QNDeviceType;
import com.qn.device.constant.QNScaleStatus;
import com.qn.device.constant.UserGoal;
import com.qn.device.constant.UserShape;
import com.qn.device.listener.QNBleProtocolDelegate;
import com.qn.device.listener.QNResultCallback;
import com.qn.device.listener.QNScaleDataListener;
import com.qn.device.out.QNBleApi;
import com.qn.device.out.QNBleDevice;
import com.qn.device.out.QNBleProtocolHandler;
import com.qn.device.out.QNConfig;
import com.qn.device.out.QNScaleData;
import com.qn.device.out.QNScaleItemData;
import com.qn.device.out.QNScaleStoreData;
import com.qn.device.out.QNUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * author: ch
 * description:自主普通秤连接测量界面(多设备连接)
 * date: 2020.10.20
 */

public class SelfMultiDeviceConnectActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private static final String TAG = "SelfMultiDeviceConnectActivity";

    @BindView(R.id.scan_measuring)
    TextView mScanMeasuring;
    @BindView(R.id.scan_setting)
    TextView mScanSetting;
    @BindView(R.id.scan_appid)
    TextView mScanAppid;
    @BindView(R.id.scanBtn)
    Button mScanBtn;
    @BindView(R.id.stopBtn)
    Button mStopBtn;
    @BindView(R.id.scan_measuring_info)
    TextView mScanMeasuringInfo;
    @BindView(R.id.listView)
    ListView mListView;
    @BindView(R.id.measureDateShow)
    TextView measureDateShow;
    @BindView(R.id.clearBtn)
    Button clearBtn;

    private QNBleApi mQNBleApi;
    private User mUser;
    private Config mConfig;
    private boolean isScanning;
    private List<String> macList = new ArrayList<>();
    private List<QNBleDevice> devices = new ArrayList<>();


    private Map<String, BluetoothGatt> mBluetoothGatts = new HashMap<>();
    private Map<String, Boolean> connectStatus = new HashMap<>();

    private Map<String, QNBleProtocolHandler> mProtocolhandlers = new HashMap<>();
    ;


    public static Intent getCallIntent(Context context, User user, Config mConfig) {
        return new Intent(context, SelfMultiDeviceConnectActivity.class)
                .putExtra(UserConst.CONFIG, mConfig)
                .putExtra(UserConst.USER, user);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_self_multi_device_connect);
        ButterKnife.bind(this);

        mQNBleApi = QNBleApi.getInstance(this);

        mUser = getIntent().getParcelableExtra(UserConst.USER);
        mConfig = getIntent().getParcelableExtra(UserConst.CONFIG);
        initData();
        //动态申请权限(Android6.0以后需要)
        AndroidPermissionCenter.verifyPermissions(this);

        mListView.setAdapter(this.listAdapter);

        mListView.setOnItemClickListener(this);

        measureDateShow.setMovementMethod(ScrollingMovementMethod.getInstance());
    }

    private void initData() {
        mScanAppid.setText("UserId : " + mUser.getUserId());
        QNConfig mQnConfig = mQNBleApi.getConfig();//获取上次设置的对象,未设置获取的是默认对象
        mQnConfig.setAllowDuplicates(mConfig.isAllowDuplicates());
        mQnConfig.setDuration(mConfig.getDuration());
        //此API已废弃
        //mQnConfig.setScanOutTime(mConfig.getScanOutTime());
        mQnConfig.setConnectOutTime(mConfig.getConnectOutTime());
        mQnConfig.setUnit(mConfig.getUnit());
        mQnConfig.setHeightUnit(mConfig.getHeightUnit());
        mQnConfig.setOnlyScreenOn(mConfig.isOnlyScreenOn());
        //设置扫描对象
        mQnConfig.save(new QNResultCallback() {
            @Override
            public void onResult(int i, String s) {
                QNDemoLogger.d("ScanActivity", "initData:" + s);
            }
        });


        initUserData(); //设置数据监听器,返回数据,需在连接当前设备前设置

    }

    private String initWeight(double weight) {
        int unit = mQNBleApi.getConfig().getUnit();
        return mQNBleApi.convertWeightWithTargetUnit(weight, unit);
    }

    private void initUserData() {
        mQNBleApi.setDataListener(new QNScaleDataListener() {
            @Override
            public void onGetUnsteadyWeight(QNBleDevice device, double weight) {
                QNDemoLogger.d(TAG, "体重是:" + weight);
                //mWeightTv.setText(initWeight(weight));
                StringBuilder builder = new StringBuilder();
                builder.append("mac:").append(device.getMac()).append(",")
                        .append(initWeight(weight)).append("\n");
                measureDateShow.append(builder.toString());
            }

            @Override
            public void onGetScaleData(QNBleDevice device, QNScaleData data) {
                QNDemoLogger.d(TAG, "收到测量数据");
                onReceiveScaleData(data, device.getMac());
                //测量结束,断开连接
                doDisconnect(device.getMac());
                QNDemoLogger.d(TAG, "加密hmac为:" + data.getHmac());
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
                    //存储数据暂不在界面上显示数据，处理逻辑可参照测量数据
                }
            }

            @Override
            public void onGetElectric(QNBleDevice device, int electric) {
                String text = getResources().getString(R.string.percentage_of_battery_received) + electric;
                QNDemoLogger.d(TAG, text);
                if (electric == DecoderConst.NONE_BATTERY_VALUE) {//获取电池信息失败
                    return;
                }
                Toast.makeText(SelfMultiDeviceConnectActivity.this, text, Toast.LENGTH_SHORT).show();
            }

            //测量过程中的连接状态
            @Override
            public void onScaleStateChange(QNBleDevice device, int status) {
                QNDemoLogger.d(TAG, "秤的连接状态是:" + status);
                setBleStatus(device.getMac(), status);
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
        });
    }

    private void onReceiveScaleData(QNScaleData data, String mac) {
        List<QNScaleItemData> allItem = data.getAllItem();
        StringBuilder builder = new StringBuilder();
        builder.append("mac:").append(mac).append(",");
        for (QNScaleItemData itemData : allItem) {
            builder.append(itemData.getName())
                    .append(":")
                    .append(itemData.getValue())
                    .append(",");
        }
        builder.append("\n");

        measureDateShow.append(builder.toString());

    }


    private BaseAdapter listAdapter = new BaseAdapter() {
        @Override
        public int getCount() {
            return devices.size();
        }

        @Override
        public Object getItem(int position) {
            return devices.get(position);
        }

        @Override
        public long getItemId(int position) {
            return devices.get(position).hashCode();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_device, null);
            }
            TextView nameTv = (TextView) convertView.findViewById(R.id.nameTv);
            TextView modelTv = (TextView) convertView.findViewById(R.id.modelTv);
            TextView macTv = (TextView) convertView.findViewById(R.id.macTv);
            TextView screenStateTv = (TextView) convertView.findViewById(R.id.screenStateTv);
            ImageView deviceType = convertView.findViewById(R.id.deviceType);

            QNBleDevice scanResult = devices.get(position);

            nameTv.setText(scanResult.getName());
            modelTv.setText(scanResult.getModeId());
            macTv.setText(scanResult.getMac());
            screenStateTv.setText(String.valueOf(scanResult.getScreenState().getCode()));
            if (scanResult.isSupportWifi()) {
                deviceType.setImageResource(R.drawable.wifi_icon);
            } else {
                deviceType.setImageResource(R.drawable.system_item_arrow);
            }

            return convertView;
        }
    };

    private void setBleStatus(final String mac, int bleStatus) {
        String stateString = "";
        switch (bleStatus) {
            case QNScaleStatus.STATE_CONNECTING: {
                stateString = getResources().getString(R.string.connecting);
                break;
            }
            case QNScaleStatus.STATE_CONNECTED: {
                stateString = getResources().getString(R.string.connected);
                break;
            }
            case QNScaleStatus.STATE_DISCONNECTING: {
                stateString = getResources().getString(R.string.disconnect_in_progress);
                break;
            }
            case QNScaleStatus.STATE_LINK_LOSS:
            case QNScaleStatus.STATE_DISCONNECTED:{
                stateString = getResources().getString(R.string.connection_disconnected);
                break;
            }
            case QNScaleStatus.STATE_START_MEASURE: {
                stateString = getResources().getString(R.string.measuring);
                break;
            }
            case QNScaleStatus.STATE_REAL_TIME: {
                stateString = getResources().getString(R.string.real_time_weight_measurement);
                break;
            }
            case QNScaleStatus.STATE_BODYFAT: {
                stateString = getResources().getString(R.string.impedance_measured);
                break;
            }
            case QNScaleStatus.STATE_HEART_RATE: {
                stateString = getResources().getString(R.string.measuring_heart_rate);
                break;
            }
            case QNScaleStatus.STATE_MEASURE_COMPLETED: {
                stateString = getResources().getString(R.string.measure_complete);
                break;
            }
        }
        if (!TextUtils.isEmpty(stateString)) {

            final String finalStateString = stateString;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    StringBuilder builder = new StringBuilder();
                    builder.append("mac:").append(mac).append(",")
                            .append(finalStateString).append("\n");
                    measureDateShow.append(builder.toString());
                }
            });

        }

    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopScan();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!mBluetoothGatts.isEmpty()) {
            for (String mac : mBluetoothGatts.keySet()) {
                doDisconnect(mac);
            }
        }
        mBluetoothGatts.clear();
        connectStatus.clear();
        mQNBleApi.setDataListener(null);
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
            QNBleDevice qnBleDevice = mQNBleApi.buildDevice(device, rssi, scanRecord, new QNResultCallback() {
                @Override
                public void onResult(int code, String msg) {
                    if (code != CheckStatus.OK.getCode()) {
                    }
                }
            });

            /**
             *  注意：仅普通蓝牙秤支持多设备连接
             */
            if (null != qnBleDevice && qnBleDevice.getDeviceType() == QNDeviceType.SCALE_BLE_DEFAULT) {

                if (qnBleDevice != null && !macList.contains(qnBleDevice.getMac())) {
                    macList.add(qnBleDevice.getMac());
                    devices.add(qnBleDevice);
                    listAdapter.notifyDataSetChanged();
                }
            }
        }
    };

    private void startScan() {
        BluetoothAdapter bluetoothAdapter = getBluetoothAdapter();

        if (bluetoothAdapter == null) {
            Toast.makeText(SelfMultiDeviceConnectActivity.this, getResources().getString(R.string.device_not_support), Toast.LENGTH_SHORT).show();
            return;
        }
        if (bluetoothAdapter.getState() != BluetoothAdapter.STATE_ON) {
            Toast.makeText(SelfMultiDeviceConnectActivity.this, getResources().getString(R.string.open_ble), Toast.LENGTH_SHORT).show();
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
            Toast.makeText(SelfMultiDeviceConnectActivity.this, getResources().getString(R.string.device_not_support), Toast.LENGTH_SHORT).show();
            return;
        }
        bluetoothAdapter.stopLeScan(scanCallback);
        isScanning = false;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (position < 0 || position >= this.devices.size()) {
            return;
        }
        stopScan();
        final QNBleDevice device = this.devices.get(position);

        // 连接设备
        doConnect(device);
    }


    @OnClick({R.id.scan_setting, R.id.scanBtn, R.id.stopBtn, R.id.clearBtn})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.scan_setting:
                startActivity(CustomSettingActivity.getCallIntent(this));
                finish();
                break;
            case R.id.scanBtn:
                if (!isScanning) {
                    this.devices.clear();
                    this.macList.clear();

                    listAdapter.notifyDataSetChanged();
                    startScan();
                } else {
                    ToastMaker.show(this, getResources().getString(R.string.scanning));
                }
                break;
            case R.id.stopBtn:
                if (isScanning) {
                    stopScan();
                } else {
                    ToastMaker.show(this, getResources().getString(R.string.scan_stopped));
                }
                break;
            case R.id.clearBtn:
                measureDateShow.setText("");
                break;
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == AndroidPermissionCenter.REQUEST_EXTERNAL_STORAGE) {
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "" + getResources().getString(R.string.permission) + permissions[i] + getResources().getString(R.string.apply_for_to_success), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "" + getResources().getString(R.string.permission) + permissions[i] + getResources().getString(R.string.apply_for_to_fail), Toast.LENGTH_SHORT).show();
                }
            }
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


    /**
     * @param mBleDevice 连接设备
     */
    private void connectQnDevice(QNBleDevice mBleDevice) {

        /**
         *  缓存协议解析对象
         */
        mProtocolhandlers.put(mBleDevice.getMac(), buildHandler(mBleDevice));

        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        BluetoothAdapter adapter = bluetoothManager.getAdapter();
        BluetoothDevice mDevice = adapter.getRemoteDevice(mBleDevice.getMac());

        if (mDevice != null) {
            QNDemoLogger.d(TAG, "connectQnDevice------: " + mDevice.getAddress());
            if (!connectStatus.isEmpty() && null != connectStatus.get(mBleDevice.getMac()) && connectStatus.get(mBleDevice.getMac())) {
                ToastMaker.show(this, getResources().getString(R.string.current_device_connected));
            } else {
                BluetoothGatt mBluetoothGatt;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    mBluetoothGatt = mDevice.connectGatt(SelfMultiDeviceConnectActivity.this, false, mGattCallback, BluetoothDevice.TRANSPORT_LE);
                } else {
                    mBluetoothGatt = mDevice.connectGatt(SelfMultiDeviceConnectActivity.this, false, mGattCallback);
                }

                // 缓存连接的设备
                mBluetoothGatts.put(mBleDevice.getMac(), mBluetoothGatt);
            }
        }

    }


    private void initCharacteristic(BluetoothGatt gatt, boolean isFirstService) {

        BluetoothGattCharacteristic qnReadBgc, qnBleReadBgc = null;
        //第一套服务
        if (isFirstService) {
            qnReadBgc = getCharacteristic(gatt, QNBleConst.UUID_IBT_SERVICES, QNBleConst.UUID_IBT_READ);
            qnBleReadBgc = getCharacteristic(gatt, QNBleConst.UUID_IBT_SERVICES, QNBleConst.UUID_IBT_BLE_READER);
        } else {
            qnReadBgc = getCharacteristic(gatt, QNBleConst.UUID_IBT_SERVICES_1, QNBleConst.UUID_IBT_READ_1);
        }

        enableNotifications(gatt, qnReadBgc);
        enableIndications(gatt, qnBleReadBgc);
    }


    private boolean enableNotifications(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {


        if (gatt == null || characteristic == null)
            return false;


        int properties = characteristic.getProperties();
        if ((properties & BluetoothGattCharacteristic.PROPERTY_NOTIFY) == 0)
            return false;

        gatt.setCharacteristicNotification(characteristic, true);

        final BluetoothGattDescriptor descriptor = characteristic.getDescriptor(QNBleConst.CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID);
        if (descriptor != null) {
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            return gatt.writeDescriptor(descriptor);
        }

        return false;
    }


    private boolean enableIndications(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {


        if (gatt == null || characteristic == null)
            return false;

        int properties = characteristic.getProperties();
        if ((properties & BluetoothGattCharacteristic.PROPERTY_INDICATE) == 0)
            return false;

        gatt.setCharacteristicNotification(characteristic, true);

        final BluetoothGattDescriptor descriptor = characteristic.getDescriptor(QNBleConst.CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID);
        if (descriptor != null) {
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
            QNDemoLogger.d(TAG, "enableIndications----------" + characteristic.getUuid());
            return gatt.writeDescriptor(descriptor);
        }
        return false;
    }

    private BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);

            QNDemoLogger.d(TAG, "onConnectionStateChange: " + newState);
            BluetoothGatt mBluetoothGatt = gatt;

            String mac = gatt.getDevice().getAddress();

            if (status != BluetoothGatt.GATT_SUCCESS) {
                String err = "Cannot connect device with error status: " + status;
                // 当尝试连接失败的时候调用 disconnect 方法是不会引起这个方法回调的，所以这里直接回调就可以了
                gatt.close();

                if (mBluetoothGatt != null) {
                    mBluetoothGatt.disconnect();
                    mBluetoothGatt.close();
                }
                setBleStatus(mac, QNScaleStatus.STATE_DISCONNECTED);
                connectStatus.put(mac, false);
                QNDemoLogger.e(TAG, err);
                return;
            }
            if (newState == BluetoothProfile.STATE_CONNECTED) {

                //缓存该设备连接状态
                connectStatus.put(mac, true);
                setBleStatus(mac, QNScaleStatus.STATE_CONNECTING);
                // TODO: 2019/9/7  某些手机可能存在无法发现服务问题,此处可做延时操作
                if (gatt != null) {
                    gatt.discoverServices();
                }

                QNDemoLogger.d(TAG, "onConnectionStateChange: " + "连接成功");

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                //缓存该设备连接状态
                connectStatus.put(mac, false);
                setBleStatus(mac, QNScaleStatus.STATE_DISCONNECTED);
                //当设备无法连接
                if (mBluetoothGatt != null) {
                    mBluetoothGatt.disconnect();
                    mBluetoothGatt.close();
                }
                gatt.close();

            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            QNDemoLogger.d(TAG, "onServicesDiscovered------: " + "发现服务----" + status);
            if (mProtocolhandlers.isEmpty()) {
                QNDemoLogger.d(TAG, "未建立协议解析");
                return;
            }
           QNBleProtocolHandler  mProtocolhandler = mProtocolhandlers.get(gatt.getDevice().getAddress());
            if (status == BluetoothGatt.GATT_SUCCESS) {

                if (isFirstService(gatt)) {
                    if (mProtocolhandler != null) {
                        //使能所有特征值
                        initCharacteristic(gatt, true);
                        QNDemoLogger.d(TAG, "onServicesDiscovered------: " + "发现服务为第一套");
                        mProtocolhandler.prepare(QNBleConst.UUID_IBT_SERVICES);
                    }else {
                        QNDemoLogger.d(TAG, "当前协议解析对象为空");
                    }
                } else {
                    if (mProtocolhandler != null) {
                        //使能所有特征值
                        QNDemoLogger.d(TAG, "onServicesDiscovered------: " + "发现服务为第二套");
                        initCharacteristic(gatt, false);
                        mProtocolhandler.prepare(QNBleConst.UUID_IBT_SERVICES_1);
                    }else {
                        QNDemoLogger.d(TAG, "当前协议解析对象为空");
                    }
                }
                setBleStatus(gatt.getDevice().getAddress(), QNScaleStatus.STATE_CONNECTED);
            } else {
                QNDemoLogger.d(TAG, "onServicesDiscovered---error: " + status);
            }
        }


        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            QNDemoLogger.d(TAG, "onCharacteristicRead---收到数据:  ");
            QNBleProtocolHandler  mProtocolhandler = mProtocolhandlers.get(gatt.getDevice().getAddress());
            if (status == BluetoothGatt.GATT_SUCCESS) {
                //获取到数据
                if (mProtocolhandler != null) {
                    mProtocolhandler.onGetBleData(getService(gatt), characteristic.getUuid().toString(), characteristic.getValue());
                }
            } else {
                QNDemoLogger.d(TAG, "onCharacteristicRead---error: " + status);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            QNBleProtocolHandler  mProtocolhandler = mProtocolhandlers.get(gatt.getDevice().getAddress());
            QNDemoLogger.d(TAG, "onCharacteristicChanged---收到数据:  ");
            //获取到数据
            if (mProtocolhandler != null) {
                mProtocolhandler.onGetBleData(getService(gatt), characteristic.getUuid().toString(), characteristic.getValue());
            }

        }

    };

    private boolean isFirstService(BluetoothGatt gatt) {
        List<BluetoothGattService> services = gatt.getServices();
        boolean result = false;
        for (BluetoothGattService service : services) {
            //第一套
            if (service.getUuid().equals(UUID.fromString(QNBleConst.UUID_IBT_SERVICES))) {
                result = true;
                break;
            }
            //第二套
            if (service.getUuid().equals(UUID.fromString(QNBleConst.UUID_IBT_SERVICES_1))) {
                result = false;
                break;
            }

        }
        return result;
    }


    private boolean isFirstService(String service_uuid) {
        boolean result;
        if (service_uuid.equalsIgnoreCase(QNBleConst.UUID_IBT_SERVICES)) {
            result = true;

        } else {
            result = false;

        }
        return result;
    }


    private String getService(BluetoothGatt gatt) {
        if (isFirstService(gatt)) {
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


    private QNBleProtocolHandler buildHandler(QNBleDevice mBleDevice) {
        QNBleProtocolHandler mProtocolhandler = mQNBleApi.buildProtocolHandler(mBleDevice, createQNUser(), qnBleProDelegate, new QNResultCallback() {
            @Override
            public void onResult(int code, String msg) {
                QNDemoLogger.d(TAG, "创建结果----" + code + " ------------- " + msg);
            }
        });

        return mProtocolhandler;
    }


    private QNBleProtocolDelegate qnBleProDelegate = new QNBleProtocolDelegate() {
        @Override
        public void writeCharacteristicValue(String service_uuid, String characteristic_uuid, byte[] data, QNBleDevice qnBleDevice) {
            writeCharacteristicData(service_uuid, characteristic_uuid, data, qnBleDevice.getMac());
        }

        @Override
        public void readCharacteristic(String service_uuid, String characteristic_uuid,QNBleDevice qnBleDevice) {
            readCharacteristicData(service_uuid, characteristic_uuid, qnBleDevice.getMac());
        }
    };


    private void readCharacteristicData(String service_uuid, String characteristic_uuid, String mac) {
        //未有连接设备，直接返回
        if (mBluetoothGatts.isEmpty()) return;

        //获取连接的设备
        BluetoothGatt mBluetoothGatt = mBluetoothGatts.get(mac);

        BluetoothGattCharacteristic qnReadBgc, qnBleReadBgc = null;
        //第一套服务
        if (isFirstService(service_uuid)) {
            qnReadBgc = getCharacteristic(mBluetoothGatt, QNBleConst.UUID_IBT_SERVICES, QNBleConst.UUID_IBT_READ);
            qnBleReadBgc = getCharacteristic(mBluetoothGatt, QNBleConst.UUID_IBT_SERVICES, QNBleConst.UUID_IBT_BLE_READER);
        } else {
            qnReadBgc = getCharacteristic(mBluetoothGatt, QNBleConst.UUID_IBT_SERVICES_1, QNBleConst.UUID_IBT_READ_1);
        }
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

        //未有连接设备，直接返回
        if (mBluetoothGatts.isEmpty()) return;

        //获取连接的设备
        BluetoothGatt mBluetoothGatt = mBluetoothGatts.get(mac);

        BluetoothGattCharacteristic qnWriteBgc, qnBleWriteBgc = null;
        //第一套服务
        if (isFirstService(service_uuid)) {
            qnWriteBgc = getCharacteristic(mBluetoothGatt, QNBleConst.UUID_IBT_SERVICES, QNBleConst.UUID_IBT_WRITE);
            qnBleWriteBgc = getCharacteristic(mBluetoothGatt, QNBleConst.UUID_IBT_SERVICES, QNBleConst.UUID_IBT_BLE_WRITER);
        } else {
            qnWriteBgc = getCharacteristic(mBluetoothGatt, QNBleConst.UUID_IBT_SERVICES_1, QNBleConst.UUID_IBT_WRITE_1);
        }

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


    private void doConnect(QNBleDevice mBleDevice) {
        if (mBleDevice == null || mUser == null) {
            return;
        }
        connectQnDevice(mBleDevice);
    }

    /**
     * 断开连接
     */
    private void doDisconnect(String mac) {
        if (!mBluetoothGatts.isEmpty() && mBluetoothGatts.get(mac) != null) {
            mBluetoothGatts.get(mac).disconnect();
        }

    }

}

