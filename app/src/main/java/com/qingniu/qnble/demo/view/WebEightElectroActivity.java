package com.qingniu.qnble.demo.view;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.qingniu.qnble.demo.R;
import com.qingniu.qnble.utils.QNLogUtils;

import org.json.JSONObject;

/**
 * Created by yangxiaobo
 * on 2021/7/14 7:16 PM
 * desc:
 */
public class WebEightElectroActivity extends AppCompatActivity {

    private static final String EXTRA_DATA = "extra_data";
    private static final String TAG = "WebEightElectroActivity";

    public static Intent getCallIntent(Context context, JSONObject jsonObject) {
        Intent intent = new Intent(context, WebEightElectroActivity.class);
        intent.putExtra(EXTRA_DATA, jsonObject.toString());
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_eight_electro);

        ProgressBar progressView = findViewById(R.id.progressView);

        WebView webView = findViewById(R.id.webViewRl);
        WebSettings webSettings = webView.getSettings();

        //如果访问的页面中要与Javascript交互，则webview必须设置支持Javascript
        webSettings.setJavaScriptEnabled(true);

        //设置自适应屏幕，两者合用
        webSettings.setUseWideViewPort(true); //将图片调整到适合webview的大小
        webSettings.setLoadWithOverviewMode(true); // 缩放至屏幕的大小
        webSettings.setAllowUniversalAccessFromFileURLs(true);
        webSettings.setAllowFileAccessFromFileURLs(true);
        //缩放操作
        webSettings.setSupportZoom(true); //支持缩放，默认为true。是下面那个的前提。
        webSettings.setBuiltInZoomControls(true); //设置内置的缩放控件。若为false，则该WebView不可缩放
        webSettings.setDisplayZoomControls(false); //隐藏原生的缩放控件

        //其他细节操作
        webSettings.setRenderPriority(WebSettings.RenderPriority.HIGH);

        // 开启 DOM storage API 功能
        webSettings.setDomStorageEnabled(true);
        //开启 database storage API 功能
        webSettings.setDatabaseEnabled(true);
        //开启 Application Caches 功能
        webSettings.setAppCacheEnabled(true);

        webSettings.setAllowFileAccess(true); //设置可以访问文件
        webSettings.setLoadsImagesAutomatically(true); //支持自动加载图片
        webSettings.setDefaultTextEncodingName("utf-8");//设置编码格式
        if (Build.VERSION.SDK_INT >= 21) {
            //解决链接是https的，但是里面的图片是http的，所以导致图片加载不出
            webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        webView.setWebChromeClient(new WebChromeClient() {

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                QNLogUtils.log(TAG, "onProgressChanged => " + newProgress);
                if (newProgress == 100) {
                    //加载完毕进度条消失
                    progressView.setVisibility(View.GONE);
                } else {
                    //更新进度
                    if (progressView.getVisibility() == View.GONE) {
                        progressView.setVisibility(View.VISIBLE);
                    }
                    progressView.setProgress(newProgress);
                }
            }
        });

        String object = getIntent().getStringExtra(EXTRA_DATA);


        Uri h5Url = Uri.parse("https://app-h5.yolanda.hk/h5-business-demo/eight_electrodes_report.html");
//        Uri h5Url = Uri.parse("http://192.168.2.24:4040/h5-business-demo/eight_electrodes_report.html");
        h5Url = h5Url.buildUpon().appendQueryParameter("measureData",object).build();

        QNLogUtils.log(TAG, String.valueOf(h5Url));

        webView.loadUrl(String.valueOf(h5Url));
    }
}
