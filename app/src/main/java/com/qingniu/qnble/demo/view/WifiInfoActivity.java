package com.qingniu.qnble.demo.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.qingniu.qnble.demo.R;
import com.qingniu.qnble.demo.adapter.WifiAdapter;
import com.qingniu.qnble.demo.bean.WifiInfoModel;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author: yxb
 * @description:
 * @date: 2025/10/16
 */
public class WifiInfoActivity extends AppCompatActivity {

    private static final String EXTRA_LIST = "extra_list";

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    public static String ssid = "";
    public static String pwd = "";

    public static Intent getCallIntent(Context context, ArrayList<WifiInfoModel> list) {
        Intent intent = new Intent(context, WifiInfoActivity.class);
        intent.putParcelableArrayListExtra(EXTRA_LIST, list);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_info);
        ButterKnife.bind(this);

        ArrayList<WifiInfoModel> list = getIntent().getParcelableArrayListExtra(EXTRA_LIST);

        WifiAdapter wifiAdapter = new WifiAdapter(list);
        wifiAdapter.setOnItemClickListener(new WifiAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(WifiInfoModel model) {
                showInputDialog(model);
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(wifiAdapter);
    }

    private void showInputDialog(WifiInfoModel model) {
        final EditText editText = new EditText(this);
        editText.setHint("请输入内容" + model.ssid + "的密码");

        new AlertDialog.Builder(this)
                .setTitle("输入字符串")
                .setView(editText)
                .setPositiveButton("确认", (dialog, which) -> {
                    String input = editText.getText().toString().trim();
                    if (!input.isEmpty()) {
                        ssid = model.ssid;
                        pwd = input;
                        setResult(RESULT_OK);
                        finish();
                    }
                })
                .setNegativeButton("取消", (dialog, which) -> dialog.dismiss())
                .show();
    }
}
