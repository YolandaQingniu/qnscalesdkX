package com.qingniu.qnble.demo.util;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.qingniu.qnble.demo.BaseApplication;
import com.qingniu.qnble.utils.BleUtils;

/**
 * author: yolanda-XY
 * date: 2018/4/2
 * package_name: com.qingniu.qnble.demo.util
 * description: ${动态申请权限}
 */

public class AndroidPermissionCenter {

    private static String[] PERMISSIONS_STORAGE = {
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE",
            "android.permission.ACCESS_COARSE_LOCATION",
            "android.permission.ACCESS_FINE_LOCATION"};

    //安卓12需要申请的权限列表
    private static String[] ANDROID_S_PERMISSIONS_STORAGE = {
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE",
            "android.permission.BLUETOOTH_SCAN",
            "android.permission.BLUETOOTH_ADVERTISE",
            "android.permission.BLUETOOTH_CONNECT"
    };

    public static int REQUEST_EXTERNAL_STORAGE = 201;
    public static int REQUEST_CAMERA= 202;

    //API>=23时才需要判断权限，扫描时再判断权限
    public static void verifyPermissions(Activity activity) {
        //判断是否已经赋予权限
        if (BleUtils.isRunOnAndroid12Mode(activity)){
            //安卓12申请新的权限队列
            if (ContextCompat.checkSelfPermission(activity,
                    "android.permission.BLUETOOTH_SCAN")
                    != PackageManager.PERMISSION_GRANTED) {
                //如果应用之前请求过此权限但用户拒绝了请求，此方法将返回 true。
                if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                        "android.permission.BLUETOOTH_SCAN")) {
                    //这里可以写个对话框之类的项向用户解释为什么要申请权限，并在对话框的确认键后续再次申请权限

                } else {
                    //申请权限，字符串数组内是一个或多个要申请的权限，1是申请权限结果的返回参数，在onRequestPermissionsResult可以得知申请结果
                    ActivityCompat.requestPermissions(activity,
                            ANDROID_S_PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
                }
            }
        }
        else {
            if (ContextCompat.checkSelfPermission(activity,
                    Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                //如果应用之前请求过此权限但用户拒绝了请求，此方法将返回 true。
                if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                        Manifest.permission.ACCESS_COARSE_LOCATION)) {
                    //这里可以写个对话框之类的项向用户解释为什么要申请权限，并在对话框的确认键后续再次申请权限

                } else {
                    //申请权限，字符串数组内是一个或多个要申请的权限，1是申请权限结果的返回参数，在onRequestPermissionsResult可以得知申请结果
                    ActivityCompat.requestPermissions(activity,
                            PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
                }
            }
        }
    }

    //API>=23时才需要判断权限，扫描时再判断权限
    public static void verifyCameraPermissions(Activity activity) {
        //判断是否已经赋予权限
        if (ContextCompat.checkSelfPermission(activity,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            //如果应用之前请求过此权限但用户拒绝了请求，此方法将返回 true。
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                    Manifest.permission.CAMERA)) {
                //这里可以写个对话框之类的项向用户解释为什么要申请权限，并在对话框的确认键后续再次申请权限

            } else {
                //申请权限，字符串数组内是一个或多个要申请的权限，1是申请权限结果的返回参数，在onRequestPermissionsResult可以得知申请结果
                ActivityCompat.requestPermissions(activity,
                        new String[]{Manifest.permission.CAMERA,Manifest.permission.VIBRATE}, REQUEST_CAMERA);
            }
        }
    }

}
