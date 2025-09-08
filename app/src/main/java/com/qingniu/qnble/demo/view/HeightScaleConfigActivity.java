package com.qingniu.qnble.demo.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.qingniu.heightscale.model.HeightScaleWiFIInfo;
import com.qingniu.qnble.demo.R;
import com.qingniu.qnble.demo.bean.User;
import com.qingniu.qnble.demo.util.ToastMaker;
import com.qingniu.qnble.demo.util.UserConst;
import com.qn.device.out.QNBleDevice;
import com.qn.device.out.QNWiFiConfig;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author: yxb
 * @description: 身高体重一体机配置页面
 * @date: 2025/9/8
 */
public class HeightScaleConfigActivity extends AppCompatActivity {

    @BindView(R.id.ssidEdit)
    EditText ssidEdit;
    @BindView(R.id.wifiPwdEd)
    EditText wifiPwdEd;
    @BindView(R.id.serverUrlEd)
    EditText serverUrlEd;
    @BindView(R.id.OTAUrlEd)
    EditText OTAUrlEd;
    @BindView(R.id.secretKeyEd)
    EditText secretKeyEd;

    @BindView(R.id.debug_btn)
    Button debugBtn;
    @BindView(R.id.ok_btn)
    Button okBtn;

    public static Intent getCallIntent(Context context, User user, QNBleDevice device) {
        return new Intent(context, HeightScaleConfigActivity.class)
                .putExtra(UserConst.USER, user)
                .putExtra(UserConst.DEVICE, device);
    }

    private QNBleDevice mBleDevice;
    private User mUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_height_scale_config);
        ButterKnife.bind(this);
        initIntent();

        debugBtn.setOnClickListener(v -> {
//            ssidEdit.setText("King");
//            wifiPwdEd.setText("987654321");
            ssidEdit.setText("yxb-mac");
            wifiPwdEd.setText("yxb666666");
            serverUrlEd.setText("http://wsp-lite.yolanda.hk/yolanda/wsp?code=");
            secretKeyEd.setText("yolandakitnewhdr");
            OTAUrlEd.setText("https://ota.volanda.hk");
        });

        okBtn.setOnClickListener(v -> {
            if (!TextUtils.isEmpty(ssidEdit.getText().toString())) {
                if (TextUtils.isEmpty(serverUrlEd.getText().toString())) {
                    ToastMaker.show(this, getResources().getString(R.string.wifi_config_request_server));
                    return;
                }
                String otaUrl = OTAUrlEd.getText().toString();
                String encryption = secretKeyEd.getText().toString();

                QNWiFiConfig qnWiFiConfig = new QNWiFiConfig();
                qnWiFiConfig.setSsid(ssidEdit.getText().toString());
                qnWiFiConfig.setPwd(wifiPwdEd.getText().toString());
                qnWiFiConfig.setServeUrl(serverUrlEd.getText().toString());
                qnWiFiConfig.setFotaUrl(otaUrl);
                qnWiFiConfig.setEncryptionKey(encryption);

                startActivity(HeightScaleActivity.getCallIntent(HeightScaleConfigActivity.this, mUser, mBleDevice, qnWiFiConfig));
            } else {
                startActivity(HeightScaleActivity.getCallIntent(HeightScaleConfigActivity.this, mUser, mBleDevice, null));
            }
            finish();
        });
    }

    private void initIntent() {
        Intent intent = getIntent();
        if (intent != null) {
            mBleDevice = intent.getParcelableExtra(UserConst.DEVICE);
            mUser = intent.getParcelableExtra(UserConst.USER);
        }
    }

}
