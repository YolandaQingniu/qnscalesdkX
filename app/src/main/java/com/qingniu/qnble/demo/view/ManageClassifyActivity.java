package com.qingniu.qnble.demo.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.qingniu.qnble.demo.R;
import com.yl.pack.YLPacker;

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
                //startActivity(SettingActivity.getCallIntent(this));
                String hamc = "70D57038CC39FEA98FBB9A4132B35422156F71C275603AD8DEF64198096F20C83C083970A4524B7FF1E7409EF8E4B3F17C71BAEA571E6FC67DDB590B3917628E9E403DE06E0BDC8E30B2895E830E0C576B162F23EEB183D2622AA9399DBD85C17060238509129EE6710BF9AB89492D63CCDF86E32F1C9AFF8B639889F0B0D01EC3F14C68E5557EB6B6BC6A74526DE6232F848E13889B4C8D9368ACF9AFC397AE01B472212F195B3CF29B75E4D53C885B8AAA713DA6F6E8645B83BDE6904775DC01B472212F195B3CF29B75E4D53C885B9E3110237DE52527B62088B24AA01CD20EBA57561F8644F1DE658C975CD43BA0D307D30880D259827BC19819B283E60545552513BF77A5B00AE2C60F2ECAAE3A60F4668E264CDD8F426AC1B3E7C86835F4AE05E506D7398D9524594DB26119848DB4BA08928D6F23ADC6673B5901A7DF7F871283BE261220FEC2C5D59118DDA80E4B45CC50F252627F4EA4E1D3C4D740EEC277BBA82065332AF0D24DA7E33CEB4E080A6D0DCC4C781C0076D85949D08C35819E4F8DDA119A99DFCCE5E7560D7EA0EB6E40419247EA7DF1120F3E325EBADDC91E9911DFF53CDAFB8545BC2A3510";
                Log.e("hyrrrr","hamc解密:\n"+ YLPacker.unpack(hamc));
                String lpl = "LPL";
                String lpljiami = YLPacker.pack(lpl);
                Log.e("hyrrrr","lpl加密: "+ lpljiami);
                Log.e("hyrrrr","lpljiami解密: "+ YLPacker.unpack(lpljiami));
                break;
            case R.id.self_manage:
                startActivity(CustomSettingActivity.getCallIntent(this));
                break;
        }
    }
}
