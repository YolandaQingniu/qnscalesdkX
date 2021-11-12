package com.qingniu.qnble.demo.nativeble;

/**
 * @author: hyr
 * @date: 2021/11/12 14:39
 * @desc: 用于NativeBleHelper将蓝牙状态回调给引用界面
 */
public interface BleStatusAction {
    void onBleStatus(int bleStatus);
}
