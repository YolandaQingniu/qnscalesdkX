package com.qingniu.qnble.demo.picker;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.qingniu.qnble.demo.R;
import com.qingniu.qnble.demo.util.UIUtils;

import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * author: yolanda-XY
 * date: 2018/3/30
 * package_name: com.qingniu.qnble.demo.picker
 * description: ${TODO}
 */

public class BaseDataPickerDialog extends Dialog implements NumberPicker.OnValueChangeListener, StringWheelView.OnWheelViewListener {


    @BindView(R.id.pickerContainer)
    protected LinearLayout pickerContainer;
    @BindView(R.id.confirmBtn)
    protected Button birthdayBtn;
    @BindView(R.id.dialogContainer)
    protected LinearLayout dialogContainer;
    @BindView(R.id.buttonBar)
    protected ViewGroup buttonBar;
    public int themeColor;
    protected View[] pickers;


    public static class PickerData {
        public String[] strings;

        public String unit="";
        public int maxValue;
        public int minValue;

        public int defaultValue;


        public PickerData() {
        }

        public PickerData(String[] strings) {
            this.strings = strings;
        }
    }

    protected int getValue(int index) {
        if (pickers[index] instanceof NumberPicker) {
            NumberPicker numberPicker = (NumberPicker) pickers[index];
            return numberPicker.getValue();
        } else {
            StringWheelView stringPicker = (StringWheelView) pickers[index];
            return stringPicker.getSelectedIndex();
        }
    }

    protected void setCurValue(int index, int value) {
        if (pickers[index] instanceof NumberPicker) {
            NumberPicker numberPicker = (NumberPicker) pickers[index];
            numberPicker.setCurValue(value);
        }
    }

    protected void setMaxValue(int index, int value) {
        if (pickers[index] instanceof NumberPicker) {
            NumberPicker numberPicker = (NumberPicker) pickers[index];
            numberPicker.setMaxValue(value);
        }
    }

    protected void setMinValue(int index, int value) {
        if (pickers[index] instanceof NumberPicker) {
            NumberPicker numberPicker = (NumberPicker) pickers[index];
            numberPicker.setMinValue(value);
        }
    }

    @OnClick(R.id.cancelBtn)
    public void onCancelClick() {
        dismiss();
    }


    public BaseDataPickerDialog(Context context) {
        super(context, R.style.pickerDialog);
        setContentView(R.layout.data_picker_base_dialog);

        ButterKnife.bind(this);

        Window window = getWindow();
        window.setGravity(Gravity.BOTTOM);
        window.setWindowAnimations(R.style.bottomDialogAnimation);
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public void onValueChange(NumberPicker picker, int oldVal, int newVal, EditText editText) {
        for (int i = 0; i < pickers.length; i++) {
            if (pickers[i] == picker) {
                onValueChange(i, newVal);
                return;
            }
        }
    }

    @Override
    public void onSelected(StringWheelView picker, int selectedIndex, String item) {
        for (int i = 0; i < pickers.length; i++) {
            if (pickers[i] == picker) {
                onValueChange(i, selectedIndex);
                return;
            }
        }
    }

    protected void onValueChange(int index, int value) {
    }

    protected void resetStringPick(int index, PickerData pk) {
        StringWheelView stringPicker = (StringWheelView) pickers[index];
        stringPicker.setSelectedColor(themeColor);
        stringPicker.setItems(Arrays.asList(pk.strings));
        stringPicker.setSelection(pk.defaultValue);
    }

    protected void init(PickerData... pickerDatas) {
        buttonBar.setBackgroundColor(themeColor);
        pickers = new View[pickerDatas.length];
        for (int i = 0; i < pickerDatas.length; i++) {
            if (i != 0) {
                pickerContainer.addView(getDividerView(getContext()));
            }
            View picker;
            PickerData pk = pickerDatas[i];
            if (pk.strings != null) {
                StringWheelView stringPicker = new StringWheelView(getContext());
                picker = stringPicker;

                stringPicker.setOnWheelViewListener(this);
                stringPicker.setSelectedColor(themeColor);
                stringPicker.setItems(Arrays.asList(pk.strings));
                stringPicker.setSelection(pk.defaultValue);
            } else {
                NumberPicker numberPicker = new NumberPicker(getContext());
                picker = numberPicker;
                numberPicker.setFocusable(true);
                numberPicker.setFocusableInTouchMode(true);

                numberPicker.setLabel(pk.unit);
                numberPicker.setMaxValue(pk.maxValue);
                numberPicker.setMinValue(pk.minValue);
                numberPicker.setCurValue(pk.defaultValue);
                numberPicker.initThemeColor(themeColor);

                numberPicker.setOnValueChangedListener(this);
                numberPicker.setBackgroundID(0);
            }

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, UIUtils.dpToPx(getContext(),200));
            lp.weight = 1;
            picker.setLayoutParams(lp);

            pickerContainer.addView(picker);
            pickers[i] = picker;
        }
    }

    View getDividerView(Context context) {
        ImageView imageView = new ImageView(context);
        imageView.setImageResource(R.drawable.picker_line);
        imageView.setBackgroundColor(themeColor);
        return imageView;
    }

    public static abstract class Builder<D extends BaseDataPickerDialog> {
        protected int themeColor;
        protected Context context;


        public Builder themeColor(int themeColor) {
            this.themeColor = themeColor;
            return this;
        }

        public Builder context(Context context) {
            this.context = context;
            return this;
        }

        public abstract D build();

    }

}
