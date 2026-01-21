package com.qingniu.qnble.demo.util;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;

/**
 * @author: yxb
 * @description:
 * @date: 2026/1/20
 */
public class Utils {

    /**
     * 复制文本到剪贴板
     */
    public static void copyToClipboard(Context context, String text) {
        ClipboardManager clipboard = (ClipboardManager)
                context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("hmac数据", text);
        clipboard.setPrimaryClip(clip);
    }

}
