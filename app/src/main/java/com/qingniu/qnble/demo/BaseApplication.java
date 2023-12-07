package com.qingniu.qnble.demo;

import android.app.Application;
import android.util.Log;

import com.qn.device.listener.QNLogListener;
import com.qn.device.listener.QNResultCallback;
import com.qn.device.out.QNBleApi;

public class BaseApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        String encryptPath = "file:///android_asset/123456789.qn";
        QNBleApi mQNBleApi = QNBleApi.getInstance(this);

        mQNBleApi.initSdk("123456789", encryptPath, new QNResultCallback() {
            @Override
            public void onResult(int code, String msg) {
                Log.d("BaseApplication", "初始化文件" + msg);
            }
        });

        mQNBleApi.setLogListener(new QNLogListener() {
            @Override
            public void onLog(String log) {
                Log.e("TTTTTT", log);
            }
        });
    }
}
