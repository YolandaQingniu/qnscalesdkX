package com.qingniu.qnble.demo;

import android.app.Application;
import android.util.Log;

import com.qingniu.qnble.utils.QNLogUtils;
import com.qn.device.listener.QNResultCallback;
import com.qn.device.out.QNBleApi;

public class BaseApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        String encryptPath = "file:///android_asset/123456789.qn";
        QNLogUtils.setLogEnable(BuildConfig.DEBUG);//设置日志打印开关，默认关闭
//        QNLogUtils.setWriteEnable(true);//设置日志写入文件开关，默认关闭
        QNBleApi mQNBleApi = QNBleApi.getInstance(this);

        mQNBleApi.initSdk("123456789", encryptPath, new QNResultCallback() {
            @Override
            public void onResult(int code, String msg) {
                Log.d("BaseApplication", "初始化文件" + msg);
            }
        });
    }
}
