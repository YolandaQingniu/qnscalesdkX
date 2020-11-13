package com.qingniu.qnble.demo.util;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.Toast;


/**
 * Created by hdr on 15/9/1.
 */
public class ToastMaker {

    static Toast toast;
    private final static Object synObj = new Object();

    public static void show(Context context, String text) {

        synchronized (synObj) {
            if (toast != null) {
                toast.cancel();

            }
            toast = new Toast(context);

            TextView textView = newTv(context);
            textView.setText(text);

            toast.setView(textView);
            toast.setDuration(Toast.LENGTH_SHORT);
            toast.show();

        }
    }

    static TextView newTv(Context context) {
        TextView textView = new TextView(context);
        textView.setPaddingRelative(UIUtils.dpToPx(context,5), UIUtils.dpToPx(context,5), UIUtils.dpToPx(context,5), UIUtils.dpToPx(context,5));
        textView.setGravity(Gravity.CENTER);

        GradientDrawable gd = new GradientDrawable();
        gd.setColor(0xFF5d5b5b);
        gd.setCornerRadius(UIUtils.dpToPx(context,5));
        textView.setBackground(gd);

        textView.setTextSize(14);
        textView.setTextColor(Color.WHITE);
        return textView;
    }


    public static void show(Context context, int textResId) {
        show(context, context.getString(textResId));
    }
}
