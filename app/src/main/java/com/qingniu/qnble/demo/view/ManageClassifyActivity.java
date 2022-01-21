package com.qingniu.qnble.demo.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.qingniu.qnble.demo.R;

/**
 * author: yolanda-zhao
 * description:分类管理界面
 * date: 2019/9/6
 */

public class ManageClassifyActivity extends AppCompatActivity implements View.OnClickListener {

    public static Intent getCallIntent(Context context) {
        return new Intent(context, ManageClassifyActivity.class);

    }

    private TextView mSdkManage;
    private TextView mSelfManage;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manageclassify);

        mSdkManage = (TextView) findViewById(R.id.sdk_manage);
        mSelfManage = (TextView) findViewById(R.id.self_manage);

        getExternalFilesDir(null);

        initData();
    }

    private void initData() {
        mSelfManage.setOnClickListener(this);
        mSdkManage.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.sdk_manage:
                startActivity(SettingActivity.getCallIntent(this));
                break;
            case R.id.self_manage:
                startActivity(CustomSettingActivity.getCallIntent(this));
                break;
        }
    }
}
