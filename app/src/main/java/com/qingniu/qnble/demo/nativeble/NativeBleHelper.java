package com.qingniu.qnble.demo.nativeble;

import android.bluetooth.BluetoothGatt;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;

import com.qingniu.qnble.demo.bean.User;
import com.qingniu.qnble.demo.util.QNDemoLogger;
import com.qingniu.qnble.demo.util.UserConst;
import com.qn.device.constant.QNScaleStatus;
import com.qn.device.listener.QNBleConnectionChangeListener;
import com.qn.device.listener.QNLogListener;
import com.qn.device.listener.QNScaleDataListener;
import com.qn.device.out.QNBleApi;
import com.qn.device.out.QNBleDevice;
import com.qn.device.out.QNBleProtocolHandler;
import com.qn.device.out.QNScaleData;
import com.qn.device.out.QNScaleItemData;
import com.qn.device.out.QNScaleStoreData;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: hyr
 * @date: 2021/11/10 14:48
 * @desc: 统一封装原生蓝牙扫描、连接、收发数据的逻辑
 */
public class NativeBleHelper {

    String TAG = "NativeBleHelper";

    /**
     * 对外的Api支持类
     */
    private QNBleApi mQNBleApi;

    public QNBleApi getQNBleApi() {
        return mQNBleApi;
    }

    /**
     * 是否已连接
     */
    private boolean mIsConnected;

    public boolean getIsConnected() {
        return mIsConnected;
    }

    public void setIsConnected(boolean isConnected) {
        mIsConnected = isConnected;
    }

    /**
     * 蓝牙连接管理类
     */
    private BluetoothGatt mBluetoothGatt;

    public BluetoothGatt getBluetoothGatt() {
        return mBluetoothGatt;
    }

    public void setBluetoothGatt(BluetoothGatt bluetoothGatt) {
        this.mBluetoothGatt = bluetoothGatt;
    }

    /**
     * 连接的设备
     */
    private QNBleDevice mBleDevice;

    public QNBleDevice getBleDevice() {
        return mBleDevice;
    }

    /**
     * 传入SDK的用户
     */
    private User mUser;

    public User getUser() {
        return mUser;
    }

    /**
     * 由引用界面传入的处理蓝牙状态回调
     */
    private BleStatusAction mBleStatusAction;

    private QNScaleDataListener mQNScaleDataListener;


    /**
     * 单次测量数据的指标集合列表
     */
    private List<QNScaleItemData> mDatas = new ArrayList<>();

    public List<QNScaleItemData> getDatas() {
        return mDatas;
    }

    /**
     * 蓝牙协议代理类
     */
    private QNBleProtocolHandler mProtocolhandler;

    public QNBleProtocolHandler getProtocolhandler() {
        return mProtocolhandler;
    }

    public void setProtocolhandler(QNBleProtocolHandler protocolhandler) {
        this.mProtocolhandler = protocolhandler;
    }

    /**
     * 构造方法
     *
     * @param tag 记录日志的tag，不传则不记录
     * @param intent 由引用界面传入的Intent，包含QNBleDevice和User
     * @param bleStatusAction 由引用界面传入的处理蓝牙状态回调
     */
    public NativeBleHelper(Context ctx,
                           @Nullable String tag,
                           Intent intent,
                           BleStatusAction bleStatusAction,
                           QNScaleDataListener qnScaleDataListener) {
        init(ctx, tag,intent,bleStatusAction,qnScaleDataListener);
    }

    /**
     * 初始化
     * @param tag 记录日志的tag，不传则不记录
     * @param intent 包含QNBleDevice和User
     * @param bleStatusAction 由引用界面传入的处理蓝牙状态回调
     */
    private void init(Context ctx,
                      @Nullable String tag,
                      Intent intent,
                      BleStatusAction bleStatusAction,
                      QNScaleDataListener qnScaleDataListener) {

        mQNBleApi = QNBleApi.getInstance(ctx);
        mBleStatusAction = bleStatusAction;
        mQNScaleDataListener = qnScaleDataListener;

        initDeviceAndUser(intent);
        initBleConnectStatus();
        //设置数据监听器,返回数据,需在连接当前设备前设置
        initUserData();
    }

    /**
     * 初始化连接设备和传入SDK的用户
     */
    private void initDeviceAndUser(Intent intent){
        if (intent != null) {
            mBleDevice = intent.getParcelableExtra(UserConst.DEVICE);
            mUser = intent.getParcelableExtra(UserConst.USER);
        }
    }

    /**
     * 初始化蓝牙监听器
     */
    private void initBleConnectStatus(){
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
                QNDemoLogger.d(TAG, "onConnectError:" + errorCode);
                setBleStatus(QNScaleStatus.STATE_DISCONNECTED);
            }

            @Override
            public void onStartInteracting(QNBleDevice device) {

            }
        });
    }

    private void setBleStatus(int bleStatus) {
        switch (bleStatus) {
            case QNScaleStatus.STATE_CONNECTING:
                mIsConnected = true;
                break;

            case QNScaleStatus.STATE_CONNECTED:
                mIsConnected = true;
                break;

            case QNScaleStatus.STATE_DISCONNECTING:
                mIsConnected = false;
                break;

            case QNScaleStatus.STATE_LINK_LOSS:
                mIsConnected = false;
                break;

            case QNScaleStatus.STATE_START_MEASURE:
                break;

            case QNScaleStatus.STATE_REAL_TIME:
                break;

            case QNScaleStatus.STATE_BODYFAT:
                break;

            case QNScaleStatus.STATE_HEART_RATE:
                break;

            case QNScaleStatus.STATE_MEASURE_COMPLETED:
                break;

            case QNScaleStatus.STATE_WIFI_BLE_START_NETWORK:
                QNDemoLogger.d(TAG, "开始设置WiFi");
                break;

            case QNScaleStatus.STATE_WIFI_BLE_NETWORK_FAIL:
                QNDemoLogger.d(TAG, "设置WiFi失败");
                break;

            case QNScaleStatus.STATE_WIFI_BLE_NETWORK_SUCCESS:
                QNDemoLogger.d(TAG, "设置WiFi成功");
                break;

            case QNScaleStatus.STATE_HEIGH_SCALE_MEASURE_FAIL:
                QNDemoLogger.d(TAG,"身高体重秤测量失败");
                break;

            default:
                mIsConnected = false;
                break;
        }
        //回调蓝牙状态给引用界面
        if (mBleStatusAction != null){
            mBleStatusAction.onBleStatus(bleStatus);
        }
    }

    private void initUserData(){
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
                if (mQNScaleDataListener!=null){
                    mQNScaleDataListener.onGetUnsteadyWeight(device,weight);
                }
            }

            @Override
            public void onGetScaleData(QNBleDevice device, QNScaleData data) {
                if (mQNScaleDataListener!=null){
                    mQNScaleDataListener.onGetScaleData(device,data);
                }
            }

            @Override
            public void onGetStoredScale(QNBleDevice device, List<QNScaleStoreData> storedDataList) {
                if (mQNScaleDataListener!=null){
                    mQNScaleDataListener.onGetStoredScale(device,storedDataList);
                }
            }

            @Override
            public void onGetElectric(QNBleDevice device, int electric) {
                if (mQNScaleDataListener!=null){
                    mQNScaleDataListener.onGetElectric(device,electric);
                }
            }

            @Override
            public void onScaleStateChange(QNBleDevice device, int status) {
                if (mQNScaleDataListener!=null){
                    //setBleStatus结尾会调用引用界面的BleStatusAction，因此不执行onScaleStateChange
                    setBleStatus(status);
                    //mQNScaleDataListener.onScaleStateChange(device, status);
                }
            }

            @Override
            public void onScaleEventChange(QNBleDevice device, int scaleEvent) {
                if (mQNScaleDataListener!=null){
                    mQNScaleDataListener.onScaleEventChange(device,scaleEvent);
                }
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
}
