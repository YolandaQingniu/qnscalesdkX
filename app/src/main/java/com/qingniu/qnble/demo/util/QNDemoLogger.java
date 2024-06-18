package com.qingniu.qnble.demo.util;

import com.qingniu.qnble.utils.QNBleLogger;

/**
 * @author: hyr
 * @date: 2023/11/2 15:47
 * @desc:
 */
public class QNDemoLogger {
    public static final String TAG = "Demo";

    public static void d(String... msg) {
        QNBleLogger.d(buildMsgWithTag(TAG, msg));
    }

    public static void i(String... msg) {
        QNBleLogger.i(buildMsgWithTag(TAG, msg));
    }

    public static void e(String... msg) {
        QNBleLogger.e(buildMsgWithTag(TAG, msg));
    }


    private static String[] buildMsgWithTag(String tag, String... msg) {
        String[] newMsg = new String[msg.length + 1];
        newMsg[0] = tag;
        System.arraycopy(msg, 0, newMsg, 1, msg.length);
        return newMsg;
    }
}
