package com.qingniu.qnble.demo.view;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.qingniu.qnble.demo.R;
import com.qingniu.qnble.demo.bean.Config;
import com.qingniu.qnble.demo.bean.User;
import com.qingniu.qnble.demo.picker.WIFISetDialog;
import com.qingniu.qnble.demo.util.AndroidPermissionCenter;
import com.qingniu.qnble.demo.util.ToastMaker;
import com.qingniu.qnble.demo.util.UserConst;
import com.qingniu.qnble.utils.QNLogUtils;
import com.qn.device.constant.CheckStatus;
import com.qn.device.constant.QNDeviceType;
import com.qn.device.constant.QNIndicator;
import com.qn.device.constant.UserGoal;
import com.qn.device.constant.UserShape;
import com.qn.device.listener.QNBleDeviceDiscoveryListener;
import com.qn.device.listener.QNResultCallback;
import com.qn.device.out.QNBleApi;
import com.qn.device.out.QNBleBroadcastDevice;
import com.qn.device.out.QNBleDevice;
import com.qn.device.out.QNBleKitchenDevice;
import com.qn.device.out.QNConfig;
import com.qn.device.out.QNShareData;
import com.qn.device.out.QNUser;
import com.qn.device.out.QNUtils;
import com.qn.device.out.QNWiFiConfig;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ScanActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

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

    @BindView(R.id.qr_data_et)
    EditText qr_data_et;
    @BindView(R.id.qr_time_et)
    EditText qr_time_et;
    @BindView(R.id.qr_data_tv)
    TextView qr_data_tv;
    @BindView(R.id.scanQrcode)
    Button scanQrcode;
    @BindView(R.id.nameTv)
    TextView nameTv;
    @BindView(R.id.modelTv)
    TextView modelTv;
    @BindView(R.id.macTv)
    TextView macTv;
    @BindView(R.id.rssiTv)
    TextView rssiTv;
    @BindView(R.id.lvHeadLay)
    LinearLayout lvHeadLay;
    @BindView(R.id.qr_test_btn)
    Button qrTestBtn;
    @BindView(R.id.kitchenBtn)
    Button kitchenBtn;

    private QNBleApi mQNBleApi;
    private User mUser;
    private Config mConfig;
    private boolean isScanning;
    private WIFISetDialog wifiSetDialog;

    private List<Object> adapterList = new ArrayList<>();

    public static Intent getCallIntent(Context context, User user, Config mConfig) {
        return new Intent(context, ScanActivity.class)
                .putExtra(UserConst.CONFIG, mConfig)
                .putExtra(UserConst.USER, user);
    }

    private static final String TAG = "ScanActivity";

    private BaseAdapter listAdapter = new BaseAdapter() {
        @Override
        public int getCount() {
            return adapterList.size();
        }

        @Override
        public Object getItem(int position) {
            return adapterList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return adapterList.get(position).hashCode();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_device, null);
            }
            TextView nameTv = (TextView) convertView.findViewById(R.id.nameTv);
            TextView modelTv = (TextView) convertView.findViewById(R.id.modelTv);
            TextView macTv = (TextView) convertView.findViewById(R.id.macTv);
            TextView rssiTv = (TextView) convertView.findViewById(R.id.rssiTv);
            ImageView deviceType = convertView.findViewById(R.id.deviceType);

            Object obj = adapterList.get(position);
            if (obj instanceof QNBleDevice) {
                QNBleDevice scanResult = (QNBleDevice) obj;
                nameTv.setText(scanResult.getName());
                modelTv.setText(scanResult.getModeId());
                macTv.setText(scanResult.getMac());
                rssiTv.setText(String.valueOf(scanResult.getRssi()));
                if (scanResult.isSupportWifi()) {
                    deviceType.setImageResource(R.drawable.wifi_icon);
                } else {
                    deviceType.setImageResource(R.drawable.system_item_arrow);
                }
            } else if (obj instanceof QNBleKitchenDevice) {
                QNBleKitchenDevice kitchenDevice = (QNBleKitchenDevice) obj;
                nameTv.setText(kitchenDevice.getName());
                modelTv.setText(kitchenDevice.getModeId());
                macTv.setText(kitchenDevice.getMac());
                rssiTv.setText(String.valueOf(kitchenDevice.getRSSI()));
            }

            return convertView;
        }
    };

//    private List<QNBleDevice> devices = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mQNBleApi = QNBleApi.getInstance(this);
        //动态申请权限(Android6.0以后需要)
//        if ()
        AndroidPermissionCenter.verifyPermissions(this);
        mUser = getIntent().getParcelableExtra(UserConst.USER);
        mConfig = getIntent().getParcelableExtra(UserConst.CONFIG);
        initData();

        mListView.setAdapter(this.listAdapter);

        mListView.setOnItemClickListener(this);


    }

    private void initData() {
        mScanAppid.setText("UserId : " + mUser.getUserId());
        QNConfig mQnConfig = mQNBleApi.getConfig();//获取上次设置的对象,未设置获取的是默认对象
        mQnConfig.setAllowDuplicates(mConfig.isAllowDuplicates());
        mQnConfig.setDuration(mConfig.getDuration());
        //此API已废弃
        // mQnConfig.setScanOutTime(mConfig.getScanOutTime());
        mQnConfig.setConnectOutTime(mConfig.getConnectOutTime());
        mQnConfig.setUnit(mConfig.getUnit());
        mQnConfig.setOnlyScreenOn(mConfig.isOnlyScreenOn());
        /**
         * 强化广播秤信号，此选项只对广播秤有效
         */
        mQnConfig.setEnhanceBleBroadcast(mConfig.isEnhanceBleBroadcast());
        //设置扫描对象
        mQnConfig.save(new QNResultCallback() {
            @Override
            public void onResult(int i, String s) {
                Log.d("ScanActivity", "initData:" + s);
            }
        });
        wifiSetDialog = new WIFISetDialog(ScanActivity.this);

    }

    @Override
    protected void onResume() {
        super.onResume();
        mQNBleApi.setBleDeviceDiscoveryListener(new QNBleDeviceDiscoveryListener() {
            @Override
            public void onDeviceDiscover(QNBleDevice device) {
//                devices.add(device);
                adapterList.add(device);
                listAdapter.notifyDataSetChanged();
            }

            @Override
            public void onStartScan() {
                QNLogUtils.log("ScanActivity", "onStartScan");
                isScanning = true;
            }

            @Override
            public void onStopScan() {
                QNLogUtils.log("ScanActivity", "onStopScan");
                isScanning = false;
                ToastMaker.show(ScanActivity.this, getResources().getString(R.string.scan_stopped));
            }

            @Override
            public void onScanFail(int code) {
                isScanning = false;
                QNLogUtils.log("ScanActivity", "onScanFail:" + code);
                Toast.makeText(ScanActivity.this, getResources().getString(R.string.scan_exception) + ":" + code, Toast.LENGTH_SHORT).show();
            }


            @Override
            public void onBroadcastDeviceDiscover(QNBleBroadcastDevice device) {
                //广播秤专用,具体使用参考 BroadcastScaleActivity
            }

            @Override
            public void onKitchenDeviceDiscover(QNBleKitchenDevice device) {
                //厨房秤专用，具体使用参考 KitchenScaleActivity
                if (device.isBluetooth()) {
                    //蓝牙厨房秤返回的对象不同，这里简单处理
                    adapterList.add(device);
                    listAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopScan();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    private void startScan() {

        mQNBleApi.startBleDeviceDiscovery(new QNResultCallback() {
            @Override
            public void onResult(int code, String msg) {
                Log.d("ScanActivity", "code:" + code + ";msg:" + msg);
                if (code != CheckStatus.OK.getCode()) {
                    ToastMaker.show(ScanActivity.this, code + ":" + msg);
                }
            }
        });
    }

    private void stopScan() {
        mQNBleApi.stopBleDeviceDiscovery(new QNResultCallback() {
            @Override
            public void onResult(int code, String msg) {
                if (code == CheckStatus.OK.getCode()) {
                    isScanning = false;
                }
            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//        if (position < 0 || position >= this.devices.size()) {
        if (position < 0 || position >= this.adapterList.size()) {
            return;
        }
        stopScan();
//        final QNBleDevice device = this.devices.get(position);
        Object obj = this.adapterList.get(position);
        if (obj instanceof QNBleDevice) {
            QNBleDevice device = (QNBleDevice) obj;
            if (device.isSupportWifi()) {
                //普通双模秤
                if (device.getDeviceType() == QNDeviceType.SCALE_BLE_DEFAULT) {
                    wifiSetDialog.setDialogClickListener(new WIFISetDialog.DialogClickListener() {
                        @Override
                        public void confirmClick(String ssid, String pwd) {
                            Log.e(TAG, "ssid：" + ssid);
                            startActivity(ConnectActivity.getCallIntent(ScanActivity.this, mUser, device, new QNWiFiConfig(ssid, pwd)));
                            wifiSetDialog.dismiss();
                        }

                        @Override
                        public void cancelClick() {

                        }
                    });
                    wifiSetDialog.show();
                } else if (device.getDeviceType() == QNDeviceType.USER_SCALE) { // 支持wifi的用户秤，即wsp秤
                    startActivity(WspConfigActivity.getIntent(ScanActivity.this, mUser, device));
                } else if (device.getDeviceType() == QNDeviceType.HEIGHT_SCALE) { // 身高一体机
                    startActivity(HeightScaleActivity.getCallIntent(this, mUser, device));
                }
            } else {
                // SCALE_BROADCAST
                if (device.getDeviceType() == QNDeviceType.SCALE_BROADCAST && !device.getOneToOne()) {
                    startActivity(BroadcastScaleActivity.getCallIntent(ScanActivity.this, mUser, device));
                } else if (device.getDeviceType() == QNDeviceType.SCALE_KITCHEN) {// SCALE_KITCHEN
                    startActivity(kitchenScaleActivity.getCallIntent(ScanActivity.this));
                }
                else if (device.getDeviceType() == QNDeviceType.USER_SCALE) { // 不支持wifi的用户秤，目前有va秤
                    startActivity(WspConfigActivity.getIntent(ScanActivity.this, mUser, device));
                }
                else {//SCALE_BLE_DEFAULT
                    //连接设备
                    connectDevice(device);
                }
            }
        } else if (obj instanceof QNBleKitchenDevice) {
            QNBleKitchenDevice kitchenDevice = (QNBleKitchenDevice) obj;

            //这里因为蓝牙厨房秤回调的对象不一样，这里就简单处理UI做演示了
            startActivity(BleKitchenActivity.getCallIntent(this, kitchenDevice));
        }
    }

    private void connectDevice(QNBleDevice device) {
        startActivity(ConnectActivity.getCallIntent(this, mUser, device));
    }


    @OnClick({R.id.scan_setting, R.id.scanBtn, R.id.stopBtn, R.id.qr_test_btn, R.id.scanQrcode, R.id.kitchenBtn})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.scanQrcode:
                if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.CAMERA)
                        == PackageManager.PERMISSION_GRANTED) {
                    startActivityForResult(new Intent(ScanActivity.this, ScanQrActivity.class), 100);
                } else {
                    AndroidPermissionCenter.verifyCameraPermissions(this);
                }
                break;
            case R.id.scan_setting:
                startActivity(SettingActivity.getCallIntent(this));
                finish();
                break;
            case R.id.scanBtn:
                if (!isScanning) {
//                    this.devices.clear();
                    this.adapterList.clear();

                    listAdapter.notifyDataSetChanged();
                    startScan();
                } else {
                    ToastMaker.show(this, getResources().getString(R.string.scanning));
                }
                break;
            case R.id.stopBtn:
                stopScan();

                break;
            case R.id.qr_test_btn:
                String qrcode = qr_data_et.getText().toString().trim();
                long validSecond = -1L;
                try {
                    String qrValid = qr_time_et.getText().toString().trim();
                    validSecond = Long.parseLong(qrValid);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (validSecond == -1) {
                    ToastMaker.show(this, getString(R.string.input_date_time));
                    return;
                }
                QNShareData qnShareData = QNUtils.decodeShareData(qrcode, validSecond, createQNUser(), new QNResultCallback() {
                    @Override
                    public void onResult(int code, String msg) {
                        QNLogUtils.log(TAG, "code:" + code);
                    }
                });
                String result = getResources().getString(R.string.decode_fail);
                if (qnShareData != null) {
                    result = "qnShareData--sn：" + qnShareData.getSn() +
                            ";\nweight:" + qnShareData.getQNScaleData().getItemValue(QNIndicator.TYPE_WEIGHT) +
                            ";\nfat:" + qnShareData.getQNScaleData().getItemValue(QNIndicator.TYPE_BODYFAT);

                }
                qr_data_tv.setText(result);
                break;
            case R.id.kitchenBtn:
                startActivity(kitchenScaleActivity.getCallIntent(ScanActivity.this));
                break;
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
                mUser.getHeight(), mUser.getGender(), mUser.getBirthDay(), mUser.getAthleteType(), userShape, userGoal, new QNResultCallback() {
                    @Override
                    public void onResult(int code, String msg) {
                        Log.d("ConnectActivity", "创建用户信息返回:" + msg);
                    }
                });
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
        } else if (requestCode == AndroidPermissionCenter.REQUEST_CAMERA) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startActivityForResult(new Intent(ScanActivity.this, ScanQrActivity.class), 100);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100) {
            if (resultCode == 200) {
                String qrCode = data.getStringExtra("code").trim();
                Log.e(TAG, "二维码：" + qrCode);
                if (!TextUtils.isEmpty(qrCode)) {
                    qr_data_et.setText(qrCode);
                } else {
                    //
                }

            }
        }
    }

}
