package com.qingniu.qnble.demo.picker;

import android.app.AlertDialog;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.qingniu.qnble.demo.R;

/**
 * @author: yxb
 * @description:
 * @date: 2025/11/6
 */
public class UserOperationDialog {

    private Context context;
    private OnOperationSelectedListener listener;

    private int selectedOperation = 0; // 0: 注册, 1: 切换
    private int selectedIndex = 1; // 默认选择第1位
    private EditText userSecretEt;

    public UserOperationDialog(Context context, OnOperationSelectedListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public interface OnOperationSelectedListener {
        void onOperationSelected(boolean isRegister, Integer index, int secret);
    }

    public void show() {
        View dialogView = createDialogView();

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("用户操作")
                .setView(dialogView)
                .setPositiveButton("确定", (dialog, which) -> {
                    boolean isRegister = selectedOperation == 0;
                    Integer index = isRegister ? null : selectedIndex;
                    String secretStr = userSecretEt.getText().toString();
                    int secret = 0;
                    if (!TextUtils.isEmpty(secretStr)) {
                        secret = Integer.parseInt(secretStr);
                    }
                    if (listener != null) {
                        listener.onOperationSelected(isRegister, index, secret);
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private View createDialogView() {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.dialog_user_operation, null);

        RadioGroup radioGroup = view.findViewById(R.id.radioGroup);
        RadioButton radioRegister = view.findViewById(R.id.radioRegister);
        RadioButton radioSwitch = view.findViewById(R.id.radioSwitch);
        LinearLayout indexContainer = view.findViewById(R.id.indexContainer);
        RadioGroup indexRadioGroup = view.findViewById(R.id.indexRadioGroup);
        TextView descriptionText = view.findViewById(R.id.descriptionText);

        userSecretEt = view.findViewById(R.id.user_secret_et);

        // 初始化索引选择
        setupIndexRadioGroup(indexRadioGroup);

        // 操作类型选择监听
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.radioRegister) {
                    selectedOperation = 0;
                    indexContainer.setVisibility(View.GONE);
                    descriptionText.setText("使用当前用户信息新注册用户");
                } else if (checkedId == R.id.radioSwitch) {
                    selectedOperation = 1;
                    indexContainer.setVisibility(View.VISIBLE);
                    updateSwitchDescription(descriptionText, selectedIndex);
                }
            }
        });

        // 索引选择监听
        indexRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.index1:
                        selectedIndex = 1;
                        break;
                    case R.id.index2:
                        selectedIndex = 2;
                        break;
                    case R.id.index3:
                        selectedIndex = 3;
                        break;
                    case R.id.index4:
                        selectedIndex = 4;
                        break;
                    case R.id.index5:
                        selectedIndex = 5;
                        break;
                    case R.id.index6:
                        selectedIndex = 6;
                        break;
                    case R.id.index7:
                        selectedIndex = 7;
                        break;
                    case R.id.index8:
                        selectedIndex = 8;
                        break;
                    default:
                        selectedIndex = 1;
                }

                if (selectedOperation == 1) {
                    updateSwitchDescription(descriptionText, selectedIndex);
                }
            }
        });

        // 默认选择注册
        radioRegister.setChecked(true);

        return view;
    }

    private void setupIndexRadioGroup(RadioGroup radioGroup) {
        // 默认选择第一个索引
        radioGroup.check(R.id.index1);
    }

    private void updateSwitchDescription(TextView textView, int index) {
        textView.setText("使用当前用户信息覆盖对应index " + index);
    }
}
