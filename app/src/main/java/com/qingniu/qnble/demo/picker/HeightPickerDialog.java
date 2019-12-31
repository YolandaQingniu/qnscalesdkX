package com.qingniu.qnble.demo.picker;

import android.content.Context;

import com.qingniu.qnble.demo.R;

import butterknife.OnClick;

/**
 * author: yolanda-XY
 * date: 2018/3/30
 * package_name: com.qingniu.qnble.demo.picker
 * description: ${TODO}
 */

public class HeightPickerDialog extends BaseDataPickerDialog {

    HeightChooseListener heightChooseListener;

    public HeightPickerDialog(Context context) {
        super(context);
    }

    public interface HeightChooseListener {
        void onChoose(int height);
    }

    @OnClick(R.id.confirmBtn)
    public void onConfirmClick() {
        if (heightChooseListener != null) {
            int height = getValue(0);
            heightChooseListener.onChoose(height);
        }
        dismiss();
    }

    public static class Builder extends BaseDataPickerDialog.Builder<HeightPickerDialog> {
        HeightChooseListener heightChooseListener;

        int defaultHeight = 165;
        int maxHeight = 220;
        int minHeight = 80;

        public Builder defaultHeight(int defaultValue) {
            this.defaultHeight = defaultValue;
            return this;
        }

        public Builder heightChooseListener(HeightChooseListener heightChooseListener) {
            this.heightChooseListener = heightChooseListener;
            return this;
        }

        public Builder maxHeight(int maxHeight) {
            this.maxHeight = maxHeight;
            return this;
        }

        public Builder minHeight(int minHeight) {
            this.minHeight = minHeight;
            return this;
        }

        @Override
        public HeightPickerDialog build() {
            HeightPickerDialog dialog = new HeightPickerDialog(context);
            dialog.themeColor = themeColor;
            dialog.heightChooseListener = heightChooseListener;

            PickerData pk = new PickerData();
            pk.unit = "cm";
            pk.maxValue = maxHeight;
            pk.defaultValue = defaultHeight;
            pk.minValue = minHeight;

            dialog.init(pk);

            return dialog;
        }
    }
}

