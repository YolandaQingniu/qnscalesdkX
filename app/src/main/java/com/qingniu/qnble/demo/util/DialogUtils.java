package com.qingniu.qnble.demo.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.yl.pack.YLPacker;

/**
 * @author: hyr
 * @date: 2023/8/31 14:39
 * @desc:
 */
public class DialogUtils {
    public static void showScrollableDialog(Activity activity, String message) {
        JsonElement jsonElement = new JsonParser().parse(message);
        String json = new GsonBuilder().setPrettyPrinting().create().toJson(jsonElement);
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("")
                .setMessage(json)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // 用户点击确定按钮的处理
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
