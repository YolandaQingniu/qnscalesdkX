package com.qingniu.qnble.demo.dialog;

/**
 * @author: yxb
 * @description:
 * @date: 2026/1/20
 */

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.qingniu.qnble.demo.R;


public class InputDialog extends Dialog {

    private EditText etInput;
    private TextView tvTitle;
    private Button btnConfirm;
    private Button btnCancel;

    private String title = "请输入";
    private String hint = "请输入内容...";
    private String confirmText = "确定";
    private String cancelText = "取消";

    private OnInputListener onInputListener;

    public interface OnInputListener {
        void onConfirm(String inputText);
        void onCancel();
    }

    public InputDialog(@NonNull Context context) {
        super(context, R.style.CustomDialog);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_input);

        initView();
        setupListeners();
        setupWindow();
    }

    private void initView() {
        etInput = findViewById(R.id.et_input);
        tvTitle = findViewById(R.id.tv_title);
        btnConfirm = findViewById(R.id.btn_confirm);
        btnCancel = findViewById(R.id.btn_cancel);

        // 设置初始值
        tvTitle.setText(title);
        etInput.setHint(hint);
        btnConfirm.setText(confirmText);
        btnCancel.setText(cancelText);
    }

    private void setupListeners() {
        // 确定按钮点击
        btnConfirm.setOnClickListener(v -> {
            String inputText = etInput.getText().toString().trim();

            if (onInputListener != null) {
                onInputListener.onConfirm(inputText);
            }

            dismiss(); // Dialog消失
        });

        // 取消按钮点击
        btnCancel.setOnClickListener(v -> {
            if (onInputListener != null) {
                onInputListener.onCancel();
            }
            dismiss(); // Dialog消失
        });

        // 点击外部不消失，但可以按返回键取消
        setCanceledOnTouchOutside(false);
    }

    private void setupWindow() {
        Window window = getWindow();
        if (window != null) {
            // 设置窗口大小和位置
            WindowManager.LayoutParams params = window.getAttributes();
            params.width = WindowManager.LayoutParams.MATCH_PARENT;
            params.height = WindowManager.LayoutParams.WRAP_CONTENT;
            params.gravity = Gravity.CENTER;
            window.setAttributes(params);

            // 设置背景透明
            window.setBackgroundDrawableResource(android.R.color.transparent);
        }
    }

    // ========== 链式调用设置方法 ==========

    public InputDialog setTitle(String title) {
        this.title = title;
        if (tvTitle != null) {
            tvTitle.setText(title);
        }
        return this;
    }

    public InputDialog setHint(String hint) {
        this.hint = hint;
        if (etInput != null) {
            etInput.setHint(hint);
        }
        return this;
    }

    public InputDialog setInitialText(String text) {
        if (etInput != null && text != null) {
            etInput.setText(text);
            etInput.setSelection(text.length()); // 光标移到末尾
        }
        return this;
    }

    public InputDialog setConfirmText(String text) {
        this.confirmText = text;
        if (btnConfirm != null) {
            btnConfirm.setText(text);
        }
        return this;
    }

    public InputDialog setCancelText(String text) {
        this.cancelText = text;
        if (btnCancel != null) {
            btnCancel.setText(text);
        }
        return this;
    }

    public InputDialog setOnInputListener(OnInputListener listener) {
        this.onInputListener = listener;
        return this;
    }

    // 设置输入类型
    public InputDialog setInputType(int inputType) {
        if (etInput != null) {
            etInput.setInputType(inputType);
        }
        return this;
    }

    // 设置最大行数
    public InputDialog setMaxLines(int maxLines) {
        if (etInput != null) {
            etInput.setMaxLines(maxLines);
        }
        return this;
    }

    // 显示软键盘
    public void showWithKeyboard() {
        show();
        etInput.requestFocus();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    }
}
