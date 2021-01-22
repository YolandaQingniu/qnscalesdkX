package com.qingniu.qnble.demo.view;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
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
import com.qingniu.qnble.demo.picker.WIFISetDialog;
import com.qingniu.qnble.demo.util.AndroidPermissionCenter;
import com.qingniu.qnble.demo.util.ToastMaker;
import com.qingniu.qnble.demo.util.UserConst;
import com.qingniu.qnble.utils.QNLogUtils;
import com.qn.device.constant.CheckStatus;
import com.qn.device.constant.QNDeviceType;
import com.qn.device.listener.QNResultCallback;
import com.qn.device.out.QNBleApi;
import com.qn.device.out.QNBleDevice;
import com.qn.device.out.QNConfig;
import com.qn.device.out.QNWiFiConfig;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SelfManagementActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private final String TAG = SelfManagementActivity.class.getSimpleName();

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

    private QNBleApi mQNBleApi;
    private User mUser;
    private Config mConfig;
    private boolean isScanning;
    private WIFISetDialog wifiSetDialog;

    public static Intent getCallIntent(Context context, User user, Config mConfig) {
        return new Intent(context, SelfManagementActivity.class)
                .putExtra(UserConst.CONFIG, mConfig)
                .putExtra(UserConst.USER, user);
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
            TextView rssiTv = (TextView) convertView.findViewById(R.id.rssiTv);
            ImageView deviceType = convertView.findViewById(R.id.deviceType);

            QNBleDevice scanResult = devices.get(position);

            nameTv.setText(scanResult.getName());
            modelTv.setText(scanResult.getModeId());
            macTv.setText(scanResult.getMac());
            rssiTv.setText(String.valueOf(scanResult.getRssi()));
            if (scanResult.isSupportWifi()) {
                deviceType.setImageResource(R.drawable.wifi_icon);
            } else {
                deviceType.setImageResource(R.drawable.system_item_arrow);
            }

            return convertView;
        }
    };

    private List<String> macList = new ArrayList<>();
    private List<QNBleDevice> devices = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_self_management);
        ButterKnife.bind(this);

        mQNBleApi = QNBleApi.getInstance(this);

        mUser = getIntent().getParcelableExtra(UserConst.USER);
        mConfig = getIntent().getParcelableExtra(UserConst.CONFIG);
        initData();
        //动态申请权限(Android6.0以后需要)
        AndroidPermissionCenter.verifyPermissions(this);

        mListView.setAdapter(this.listAdapter);

        mListView.setOnItemClickListener(this);
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
        mQnConfig.setOnlyScreenOn(mConfig.isOnlyScreenOn());
        //设置扫描对象
        mQnConfig.save(new QNResultCallback() {
            @Override
            public void onResult(int i, String s) {
                Log.d("ScanActivity", "initData:" + s);
            }
        });
        wifiSetDialog = new WIFISetDialog(SelfManagementActivity.this);
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
                        QNLogUtils.log("LeScanCallback", msg);
                    }
                }
            });

            if (qnBleDevice != null && !macList.contains(qnBleDevice.getMac())) {
                QNLogUtils.log("LeScanCallback", qnBleDevice.getMac());
                macList.add(qnBleDevice.getMac());
                devices.add(qnBleDevice);
                listAdapter.notifyDataSetChanged();
            }
        }
    };

    private void startScan() {
        BluetoothAdapter bluetoothAdapter = getBluetoothAdapter();

        if (bluetoothAdapter == null) {
            Toast.makeText(SelfManagementActivity.this,getResources().getString(R.string.device_not_support), Toast.LENGTH_SHORT).show();
            return;
        }
        if (bluetoothAdapter.getState() != BluetoothAdapter.STATE_ON) {
            Toast.makeText(SelfManagementActivity.this,getResources().getString(R.string.open_ble), Toast.LENGTH_SHORT).show();
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
            Toast.makeText(SelfManagementActivity.this, getResources().getString(R.string.device_not_support), Toast.LENGTH_SHORT).show();
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

        if (device.getDeviceType() == QNDeviceType.SCALE_BLE_DEFAULT) {

            if (device.isSupportWifi()) {
                //双模秤
                wifiSetDialog.setDialogClickListener(new WIFISetDialog.DialogClickListener() {
                    @Override
                    public void confirmClick(String ssid, String pwd) {
                        Log.e(TAG, "ssid：" + ssid);
                        startActivity(SelfConnectActivity.getCallIntent(SelfManagementActivity.this, mUser, device, new QNWiFiConfig(ssid, pwd)));
                        wifiSetDialog.dismiss();
                    }

                    @Override
                    public void cancelClick() {

                    }
                });
                wifiSetDialog.show();
            } else {
                startActivity(SelfConnectActivity.getCallIntent(SelfManagementActivity.this, mUser, device));
            }

        } else if (device.getDeviceType() == QNDeviceType.SCALE_BROADCAST) {
            //广播秤
            startActivity(SelfBroadcastScaleActivity.getCallIntent(SelfManagementActivity.this, mUser, device));
        }else if (device.getDeviceType() == QNDeviceType.SCALE_KITCHEN) {
            //厨房秤
            startActivity(SelfKitchenScaleActivity.getCallIntent(SelfManagementActivity.this,device));
        } else {
            Toast.makeText(this, getResources().getString(R.string.device_not_support), Toast.LENGTH_SHORT).show();
        }

    }


    @OnClick({R.id.scan_setting, R.id.scanBtn, R.id.stopBtn})
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
                    ToastMaker.show(this,getResources().getString(R.string.scanning));
                }
                break;
            case R.id.stopBtn:
                if (isScanning) {
                    stopScan();
                } else {
                    ToastMaker.show(this, getResources().getString(R.string.scan_stopped));
                }
                break;
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == AndroidPermissionCenter.REQUEST_EXTERNAL_STORAGE) {
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "" + getResources().getString(R.string.permission) + permissions[i] +getResources().getString(R.string.apply_for_to_success), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "" + getResources().getString(R.string.permission) + permissions[i] + getResources().getString(R.string.apply_for_to_fail), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
