package com.qingniu.qnble.demo.view;

import android.annotation.SuppressLint;
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
import android.os.Build;
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
import com.qingniu.qnble.demo.nativeble.BleStatusAction;
import com.qingniu.qnble.demo.nativeble.NativeBleHelper;
import com.qingniu.qnble.demo.util.UserConst;
import com.qingniu.scale.constant.DecoderConst;
import com.qn.device.constant.QNBleConst;
import com.qn.device.constant.QNIndicator;
import com.qn.device.constant.QNScaleStatus;
import com.qn.device.constant.UserGoal;
import com.qn.device.constant.UserShape;
import com.qn.device.listener.QNBleProtocolDelegate;
import com.qn.device.listener.QNResultCallback;
import com.qn.device.listener.QNScaleDataListener;
import com.qn.device.out.QNBleDevice;
import com.qn.device.out.QNBleProtocolHandler;
import com.qn.device.out.QNScaleData;
import com.qn.device.out.QNScaleItemData;
import com.qn.device.out.QNScaleStoreData;
import com.qn.device.out.QNUser;

import java.util.List;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author: hyr
 * @date: 2021/11/4 10:57
 * @desc: 身高一体机蓝牙自主管理界面
 */
public class SelfHeightScaleActivity extends AppCompatActivity implements View.OnClickListener {


    private static final String TAG = "SelfHeightScaleActivity";

    @BindView(R.id.connectBtn)
    Button mConnectBtn;
    @BindView(R.id.statusTv)
    TextView mStatusTv;
    @BindView(R.id.weightTv)
    TextView mWeightTv;
    @BindView(R.id.back_tv)
    TextView mBackTv;
    @BindView(R.id.height_scale_store_size_tv)
    TextView heightScaleStoreSizeTv;
    @BindView(R.id.listView)
    ListView mListView;

    private NativeBleHelper mNativeBleHelper;

    private ListAdapter listAdapter;

    private Handler mHandler = new Handler(Looper.myLooper());

    private BluetoothGattCharacteristic qnNotifyBgc, qnWriteBgc;

    public static Intent getCallIntent(Context context, User user,QNBleDevice device) {
        return new Intent(context, SelfHeightScaleActivity.class)
                .putExtra(UserConst.USER, user)
                .putExtra(UserConst.DEVICE, device);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_height_scale);

        mNativeBleHelper = new NativeBleHelper(
                this,
                TAG,
                getIntent(),
                new BleStatusAction() {
                    @Override
                    public void onBleStatus(int bleStatus) {
                        onGetBleStatus(bleStatus);
                    }
                },
                mQNScaleDataListener);

        ButterKnife.bind(this);
        initView();
        initData();
    }

    private BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);

            Log.d(TAG, "onConnectionStateChange: " + newState);

            if (status != BluetoothGatt.GATT_SUCCESS) {
                String err = "Cannot connect device with error status: " + status;
                // 当尝试连接失败的时候调用 disconnect 方法是不会引起这个方法回调的，所以这里直接回调就可以了
                gatt.close();
                if (mNativeBleHelper.getBluetoothGatt() != null) {
                    mNativeBleHelper.getBluetoothGatt().disconnect();
                    mNativeBleHelper.getBluetoothGatt().close();
                    mNativeBleHelper.setBluetoothGatt(null);
                }
                mHandler.post(() -> onGetBleStatus(QNScaleStatus.STATE_DISCONNECTED));
                mNativeBleHelper.setIsConnected(false);
                Log.e(TAG, err);
                return;
            }
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                mNativeBleHelper.setIsConnected(true);

                //当蓝牙设备已经接
                mHandler.post(() -> {
                    onGetBleStatus(QNScaleStatus.STATE_CONNECTED);
                    Toast.makeText(SelfHeightScaleActivity.this, getResources().getString(R.string.connect_successfully), Toast.LENGTH_SHORT).show();
                });

                // TODO: 2019/9/7  某些手机可能存在无法发现服务问题,此处可做延时操作
                if (mNativeBleHelper.getBluetoothGatt() != null) {
                    mNativeBleHelper.getBluetoothGatt().discoverServices();
                }

                Log.d(TAG, "onConnectionStateChange: " + "连接成功");

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                mNativeBleHelper.setIsConnected(false);
                //当设备无法连接
                if (mNativeBleHelper.getBluetoothGatt()  != null) {
                    mNativeBleHelper.getBluetoothGatt().disconnect();
                    mNativeBleHelper.getBluetoothGatt().close();
                    mNativeBleHelper.setBluetoothGatt(null);
                }

                qnNotifyBgc = null;
                qnWriteBgc = null;
                gatt.close();
                //TODO 实际运用中可发起重新连接
//                if (mBleDevice != null) {
//                    connectQnDevice(mBleDevice);
//                }
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        onGetBleStatus(QNScaleStatus.STATE_LINK_LOSS);
                    }
                });

            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            Log.d(TAG, "onServicesDiscovered------: " + "发现服务----" + status);

            if (status == BluetoothGatt.GATT_SUCCESS) {
                //发现服务,并遍历服务,找到公司对于的设备服务
                List<BluetoothGattService> services = gatt.getServices();

                for (BluetoothGattService service : services) {
                    //是身高一体机的服务UUID
                    if (service.getUuid().toString().equals(QNBleConst.UUID_HEIGHT_SCALE_SERVICES)) {
                        if (mNativeBleHelper.getProtocolhandler() != null) {
                            //使能所有特征值
                            initCharacteristic(gatt);
                            Log.d(TAG, "onServicesDiscovered------: " + "发现身高一体机服务");
                            mNativeBleHelper.getProtocolhandler().prepare(QNBleConst.UUID_HEIGHT_SCALE_SERVICES);
                        }
                        break;
                    }
                }

            } else {
                Log.d(TAG, "onServicesDiscovered---error: " + status);
            }
        }


        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            Log.d(TAG, "onCharacteristicRead---收到数据:  ");
            if (status == BluetoothGatt.GATT_SUCCESS) {
                //获取到数据
                if (mNativeBleHelper.getProtocolhandler() != null) {
                    mNativeBleHelper.getProtocolhandler().onGetBleData(QNBleConst.UUID_HEIGHT_SCALE_SERVICES, characteristic.getUuid().toString(), characteristic.getValue());
                }
            } else {
                Log.d(TAG, "onCharacteristicRead---error: " + status);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);

            Log.d(TAG, "onCharacteristicChanged---收到数据:  ");
            //获取到数据
            if (mNativeBleHelper.getProtocolhandler() != null) {
                mNativeBleHelper.getProtocolhandler().onGetBleData(QNBleConst.UUID_HEIGHT_SCALE_SERVICES, characteristic.getUuid().toString(), characteristic.getValue());
            }

        }

    };


    private QNScaleDataListener mQNScaleDataListener  = new QNScaleDataListener() {
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
        }

        @Override
        public void onGetStoredScale(QNBleDevice device, List<QNScaleStoreData> storedDataList) {
            Log.d("ConnectActivity", "收到存储数据");
            if (storedDataList != null && storedDataList.size() > 0) {
                heightScaleStoreSizeTv.setText(storedDataList.size() + "");
                QNScaleStoreData data = storedDataList.get(0);
                for (int i = 0; i < storedDataList.size(); i++) {
                    Log.d("ConnectActivity", "收到存储数据:" + storedDataList.get(i).getWeight());
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
            Log.d("ConnectActivity", text);
            if (electric == DecoderConst.NONE_BATTERY_VALUE) {//获取电池信息失败
                return;
            }
            Toast.makeText(SelfHeightScaleActivity.this, text, Toast.LENGTH_SHORT).show();
        }

        //测量过程中的连接状态
        @Override
        public void onScaleStateChange(QNBleDevice device, int status) {
            Log.d("ConnectActivity", "秤的连接状态是:" + status);
        }

        @Override
        public void onScaleEventChange(QNBleDevice device, int scaleEvent) {
            Log.d("ConnectActivity", "秤返回的事件是:" + scaleEvent);
        }

        @Override
        public void readSnComplete(QNBleDevice device, String sn) {

        }
    };

    private void initCharacteristic(BluetoothGatt gatt) {

        qnNotifyBgc = getCharacteristic(gatt, QNBleConst.UUID_HEIGHT_SCALE_SERVICES, QNBleConst.UUID_HEIGHT_SCALE_READ);
        qnWriteBgc = getCharacteristic(gatt, QNBleConst.UUID_HEIGHT_SCALE_SERVICES, QNBleConst.UUID_HEIGHT_SCALE_WRITE);
        enableNotifications(qnNotifyBgc);
    }

    private BluetoothGattCharacteristic getCharacteristic(final BluetoothGatt gatt, String serviceUuid, String characteristicUuid) {
        BluetoothGattService service = gatt.getService(UUID.fromString(serviceUuid));
        if (service == null) {
            return null;
        }
        return service.getCharacteristic(UUID.fromString(characteristicUuid));
    }

    private boolean enableNotifications(BluetoothGattCharacteristic characteristic) {

        final BluetoothGatt gatt = mNativeBleHelper.getBluetoothGatt();

        if (gatt == null || characteristic == null){
            return false;
        }



        int properties = characteristic.getProperties();
        if ((properties & BluetoothGattCharacteristic.PROPERTY_NOTIFY) == 0){
            return false;
        }


        boolean isSuccess = gatt.setCharacteristicNotification(characteristic, true);

        final BluetoothGattDescriptor descriptor = characteristic.getDescriptor(QNBleConst.CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID);
        if (descriptor != null) {
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            return gatt.writeDescriptor(descriptor);
        }

        return false;
    }

    private void  initView(){
        mConnectBtn.setOnClickListener(this);
        mBackTv.setOnClickListener(this);
        listAdapter = new ListAdapter(mNativeBleHelper.getDatas(), mNativeBleHelper.getQNBleApi(), createQNUser());
        mListView.setAdapter(listAdapter);
        listAdapter.notifyDataSetChanged();
    }

    private QNUser createQNUser() {
        UserShape userShape;
        switch (mNativeBleHelper.getUser().getChoseShape()) {
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
        switch (mNativeBleHelper.getUser().getChoseGoal()) {
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

        return mNativeBleHelper.getQNBleApi().buildUser(
                mNativeBleHelper.getUser().getUserId(),
                mNativeBleHelper.getUser().getHeight(),
                mNativeBleHelper.getUser().getGender(),
                mNativeBleHelper.getUser().getBirthDay(),
                mNativeBleHelper.getUser().getAthleteType(),
                userShape,
                userGoal,
                mNativeBleHelper.getUser().getClothesWeight(),
                new QNResultCallback() {
                    @Override
                    public void onResult(int code, String msg) {
                        Log.d(TAG, "创建用户信息返回:" + msg);
                    }
                });
    }

    private void  initData(){
        //已经连接设备先断开设备,再连接
        if (mNativeBleHelper.getIsConnected()) {
            doDisconnect();
        } else {
            connectQnDevice(mNativeBleHelper.getBleDevice()); //连接当前设备
        }
    }

    private String initWeight(double weight) {
        int unit = mNativeBleHelper.getQNBleApi().getConfig().getUnit();
        return mNativeBleHelper.getQNBleApi().convertWeightWithTargetUnit(weight, unit);
    }

    private void onReceiveScaleData(QNScaleData md) {
        mNativeBleHelper.getDatas().clear();
        /**
         * 增加身高显示
         */
        QNScaleItemData qnScaleItemData = new QNScaleItemData();
        qnScaleItemData.setName(getString(R.string.height));
        qnScaleItemData.setValue(md.getHeight());
        mNativeBleHelper.getDatas().add(qnScaleItemData);

        mNativeBleHelper.getDatas().addAll(md.getAllItem());

        listAdapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.connectBtn:
                if (mNativeBleHelper.getIsConnected()) {
                    //已经连接,断开连接
                    this.doDisconnect();
                } else {
                    //断开连接,就开始连接
                    mNativeBleHelper.getDatas().clear();
                    listAdapter.notifyDataSetChanged();
                    this.doConnect();
                }
                break;
            case R.id.back_tv:
                doDisconnect();
                finish();
                break;
            default:
        }
    }

    private void doConnect() {
        if (mNativeBleHelper.getBleDevice() == null || mNativeBleHelper.getUser() == null) {
            return;
        }
        connectQnDevice(mNativeBleHelper.getBleDevice());
    }

    /**
     * 断开连接
     */
    private void doDisconnect() {
        if (mNativeBleHelper.getBluetoothGatt() != null) {
            mNativeBleHelper.getBluetoothGatt().disconnect();
        }

        if (mNativeBleHelper.getProtocolhandler() != null) {
            mNativeBleHelper.setProtocolhandler(null);
        }
    }

    /**
     * @param device 连接设备
     */
    @SuppressLint("MissingPermission")
    private void connectQnDevice(QNBleDevice device) {
        onGetBleStatus(QNScaleStatus.STATE_CONNECTING);
        buildHandler();

        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        BluetoothAdapter adapter = bluetoothManager.getAdapter();
        BluetoothDevice mDevice = adapter.getRemoteDevice(device.getMac());

        if (mDevice != null) {
            Log.d(TAG, "connectQnDevice------: " + mDevice.getAddress());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mNativeBleHelper.setBluetoothGatt(mDevice.connectGatt(SelfHeightScaleActivity.this, false, mGattCallback, BluetoothDevice.TRANSPORT_LE));
            } else {
                mNativeBleHelper.setBluetoothGatt(mDevice.connectGatt(SelfHeightScaleActivity.this, false, mGattCallback));
            }
        }
    }

    private void onGetBleStatus(int bleStatus) {
        String stateString;
        String btnString;
        switch (bleStatus) {
            case QNScaleStatus.STATE_CONNECTING: {
                stateString = getResources().getString(R.string.connecting);
                btnString = getResources().getString(R.string.disconnected);
                break;
            }
            case QNScaleStatus.STATE_CONNECTED: {
                stateString = getResources().getString(R.string.connected);
                btnString = getResources().getString(R.string.disconnected);
                break;
            }
            case QNScaleStatus.STATE_DISCONNECTING: {
                stateString = getResources().getString(R.string.disconnect_in_progress);
                btnString = getResources().getString(R.string.connect);

                break;
            }
            case QNScaleStatus.STATE_LINK_LOSS: {
                stateString = getResources().getString(R.string.connection_disconnected);
                btnString = getResources().getString(R.string.connect);
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
                break;
            case QNScaleStatus.STATE_WIFI_BLE_NETWORK_FAIL:
                stateString = getResources().getString(R.string.failed_to_set_wifi);
                btnString = getResources().getString(R.string.disconnected);
                break;
            case QNScaleStatus.STATE_WIFI_BLE_NETWORK_SUCCESS:
                stateString = getResources().getString(R.string.success_to_set_wifi);
                btnString = getResources().getString(R.string.disconnected);
                break;
            case QNScaleStatus.STATE_HEIGH_SCALE_MEASURE_FAIL:
                stateString = getResources().getString(R.string.measure_fail);
                btnString = getResources().getString(R.string.disconnected);
                break;
            default: {
                stateString = getResources().getString(R.string.connection_disconnected);
                btnString = getResources().getString(R.string.connect);
                break;
            }
        }
        mStatusTv.setText(stateString);
        mConnectBtn.setText(btnString);
    }

    private void buildHandler() {
        QNBleProtocolHandler curProtocolhandler = mNativeBleHelper.getQNBleApi().buildProtocolHandler(mNativeBleHelper.getBleDevice(), createQNUser(), new QNBleProtocolDelegate() {
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
                Log.d(TAG, "创建结果----" + code + " ------------- " + msg);
            }
        });

        mNativeBleHelper.setProtocolhandler(curProtocolhandler);
    }

    private void writeCharacteristicData(String service_uuid, String characteristic_uuid, byte[] data, String mac) {
        if (characteristic_uuid.equals(QNBleConst.UUID_HEIGHT_SCALE_WRITE)){
            if (mNativeBleHelper.getBluetoothGatt() != null && qnWriteBgc != null) {
                qnWriteBgc.setValue(data);
                mNativeBleHelper.getBluetoothGatt().writeCharacteristic(qnWriteBgc);
            }
        }
    }

    private void readCharacteristicData(String service_uuid, String characteristic_uuid, String mac) {
        if (characteristic_uuid.equals(QNBleConst.UUID_HEIGHT_SCALE_READ)){
            if (mNativeBleHelper.getBluetoothGatt() != null && qnNotifyBgc != null) {
                mNativeBleHelper.getBluetoothGatt().readCharacteristic(qnNotifyBgc);
            }
        }
    }
}
