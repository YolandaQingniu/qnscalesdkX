package com.qingniu.qnble.demo.nativeble;

import android.bluetooth.BluetoothGatt;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;

import com.qn.device.listener.QNLogListener;
import com.qn.device.out.QNBleApi;

/**
 * @author: hyr
 * @date: 2021/11/10 14:48
 * @desc: 统一封装原生蓝牙扫描、连接、收发数据的逻辑
 */
public class NativeBleHelper {

    //对外的Api支持类
    private QNBleApi mQNBleApi;

    public QNBleApi getQNBleApi() {
        return mQNBleApi;
    }

    //是否已连接
    private boolean mIsConnected;

    public boolean isConnected() { return mIsConnected; }

    //蓝牙连接管理类
    private BluetoothGatt mBluetoothGatt;

    public BluetoothGatt getBluetoothGatt() {
        return mBluetoothGatt;
    }

    /**
     * 构造方法
     * @param tag 记录日志的tag，不传则不记录
     */
    public NativeBleHelper(Context ctx, @Nullable String tag){
        init(ctx,tag);
    }

    /**
     * 初始化
     * @param tag 记录日志的tag，不传则不记录
     */
    private void init(Context ctx, @Nullable String tag){

        mQNBleApi = QNBleApi.getInstance(ctx);

        if (!TextUtils.isEmpty(tag)){
            //此API是用来监听日志的，如果需要上传日志到服务器则可以使用，否则不需要设置
            mQNBleApi.setLogListener(new QNLogListener() {
                @Override
                public void onLog(String log) {
                    Log.e(tag, log);
                }
            });
        }
    }


}
