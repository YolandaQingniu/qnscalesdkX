package com.qingniu.qnble.demo.picker;

import android.app.AlertDialog;
import android.content.Context;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: yxb
 * @description:
 * @date: 2025/11/6
 */
public class MultiSelectGridDialog {

    private Context context;
    private OnMultiSelectListener listener;
    private List<Integer> selectedItems = new ArrayList<>();
    private String title = "请选择";

    public MultiSelectGridDialog(Context context, OnMultiSelectListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public interface OnMultiSelectListener {
        void onSelected(List<Integer> selectedItems);
    }

    public void show() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);

        // 创建复选框数组
        final String[] items = {"1", "2", "3", "4", "5", "6", "7", "8"};
        final boolean[] checkedItems = new boolean[items.length];

        // 初始化选中状态
        for (int i = 0; i < checkedItems.length; i++) {
            checkedItems[i] = selectedItems.contains(i + 1);
        }

        builder.setMultiChoiceItems(items, checkedItems, (dialog, which, isChecked) -> {
            if (isChecked) {
                if (!selectedItems.contains(which + 1)) {
                    selectedItems.add(which + 1);
                }
            } else {
                selectedItems.remove((Integer) (which + 1));
            }
        });

        builder.setPositiveButton("确定", (dialog, which) -> {
            if (listener != null) {
                listener.onSelected(selectedItems);
            }
        });

        builder.setNegativeButton("取消", null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
