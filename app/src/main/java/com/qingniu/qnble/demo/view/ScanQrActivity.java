package com.qingniu.qnble.demo.view;

import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.qingniu.qnble.demo.R;
import com.qingniu.qnble.demo.util.QNDemoLogger;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.bingoogolapple.qrcode.core.QRCodeView;

/**
 * Created by CHENHUA on 2018/4/3.
 */
public class ScanQrActivity extends AppCompatActivity {

    @BindView(R.id.preview_view)
    SurfaceView previewView;
    @BindView(R.id.back_text)
    ImageView imageBack;
    @BindView(R.id.flashlight_off)
    ImageView flashlightOff;
    @BindView(R.id.scan_unlock_flashlight)
    LinearLayout scanUnlockFlashlight;
    @BindView(R.id.zbarview)
    cn.bingoogolapple.qrcode.zxing.ZXingView mZBarView;


    private boolean isOpen;//判断手电筒打没打开


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zxing_code_scan);
        ButterKnife.bind(this);
        mZBarView.setDelegate(delegate);
    }

    private QRCodeView.Delegate delegate = new QRCodeView.Delegate() {
        @Override
        public void onScanQRCodeSuccess(String result) {
            vibrate();
            mZBarView.stopSpot(); // 停止识别
            setResult(200, getIntent().putExtra("code", result));
            QNDemoLogger.e("二维码", "二维码：" + result);
            finish();
        }

        @Override
        public void onCameraAmbientBrightnessChanged(boolean isDark) {

        }

        @Override
        public void onScanQRCodeOpenCameraError() {

        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        mZBarView.startCamera(); // 打开后置摄像头开始预览，但是并未开始识别
//      mZBarView.startCamera(Camera.CameraInfo.CAMERA_FACING_FRONT); // 打开前置摄像头开始预览，但是并未开始识别
        mZBarView.startSpotAndShowRect(); // 显示扫描框，并且延迟0.1秒后开始识别
    }


   /* @Override
    public void handleResult(String resultString) {
        setResult(200, getIntent().putExtra("code", resultString));
        finish();
    }*/

    @OnClick({R.id.back_text, R.id.flashlight_off, R.id.scan_unlock_flashlight})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.back_text:
                //startActivity(new Intent(this, InputDeviceNumActivity.class));
                ScanQrActivity.this.finish();
                break;
            case R.id.flashlight_off:
            case R.id.scan_unlock_flashlight:
                isOpen = !isOpen;
                if (isOpen) {
                    flashlightOff.setImageResource(R.drawable.icon_flashlight_on);
                    mZBarView.openFlashlight(); // 打开闪光灯
                } else {
                    mZBarView.closeFlashlight(); // 关闭闪光灯
                    flashlightOff.setImageResource(R.drawable.icon_flashlight_off);
                }

                break;
        }
    }

    @Override
    protected void onStop() {
        mZBarView.stopCamera(); // 关闭摄像头预览，并且隐藏扫描框
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        mZBarView.onDestroy(); // 销毁二维码扫描控件
        super.onDestroy();
    }

    private void vibrate() {
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        vibrator.vibrate(200);
    }
}
