package com.qingniu.qnble.demo.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.qingniu.qnble.demo.R;
import com.qingniu.qnble.utils.QNLogUtils;
import com.yl.pack.YLPacker;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author: hyr
 * @date: 2022/7/22 13:47
 * @desc:
 */
public class DebugSettingActivity extends AppCompatActivity {

    private static String DEBUG_HMAC = "";
    private EditText weightEt;
    private EditText rh20et;
    private EditText lh20et;
    private EditText t20et;
    private EditText rf20et;
    private EditText lf20et;
    private EditText rh100et;
    private EditText lh100et;
    private EditText t100et;
    private EditText rf100et;
    private EditText lf100et;
    private Button okBtn;

    public static String getDebugHmac() {
        if (TextUtils.isEmpty(DEBUG_HMAC)) {
            QNLogUtils.logAndWrite("八电极数据计算", "没有上次的测量hmac");
        } else {
            try {
                JSONObject jsonObject = new JSONObject(YLPacker.unpack(DEBUG_HMAC));
                double lastWeight = jsonObject.getDouble("weight");
                String measure_time = jsonObject.getString("measure_time");
                double lastResistanceRH20 = jsonObject.getDouble("res20_right_arm");
                double lastResistanceLH20 = jsonObject.getDouble("res20_left_arm");
                double lastResistanceT20 = jsonObject.getDouble("res20_trunk");
                double lastResistanceRF20 = jsonObject.getDouble("res20_right_leg");
                double lastResistanceLF20 = jsonObject.getDouble("res20_left_leg");

                double lastResistanceRH100 = jsonObject.getDouble("res100_right_arm");
                double lastResistanceLH100 = jsonObject.getDouble("res100_left_arm");
                double lastResistanceT100 = jsonObject.getDouble("res100_trunk");
                double lastResistanceRF100 = jsonObject.getDouble("res100_right_leg");
                double lastResistanceLF100 = jsonObject.getDouble("res100_left_leg");

                QNLogUtils.logAndWrite("八电极数据计算", "上次测量体重 lastWeight:" + lastWeight);
                QNLogUtils.logAndWrite("八电极数据计算", "上次测量时间 measure_time:" + measure_time);
                QNLogUtils.logAndWrite("八电极数据计算", "上次测量右上20 lastResistanceRH20:" + lastResistanceRH20);
                QNLogUtils.logAndWrite("八电极数据计算", "上次测量左上20 lastResistanceLH20:" + lastResistanceLH20);
                QNLogUtils.logAndWrite("八电极数据计算", "上次测量躯干20 lastResistanceT20:" + lastResistanceT20);
                QNLogUtils.logAndWrite("八电极数据计算", "上次测量右下20 lastResistanceRF20:" + lastResistanceRF20);
                QNLogUtils.logAndWrite("八电极数据计算", "上次测量左下20 lastResistanceLF20:" + lastResistanceLF20);
                QNLogUtils.logAndWrite("八电极数据计算", "上次测量右上100 lastResistanceRH100:" + lastResistanceRH100);
                QNLogUtils.logAndWrite("八电极数据计算", "上次测量左上100 lastResistanceLH100:" + lastResistanceLH100);
                QNLogUtils.logAndWrite("八电极数据计算", "上次测量躯干100 lastResistanceT100:" + lastResistanceT100);
                QNLogUtils.logAndWrite("八电极数据计算", "上次测量右下100 lastResistanceRF100:" + lastResistanceRF100);
                QNLogUtils.logAndWrite("八电极数据计算", "上次测量左下100 lastResistanceLF100:" + lastResistanceLF100);
            } catch (Exception e) {
                QNLogUtils.logAndWrite("八电极数据计算", "解析上次测量的hmac出错");
            }
        }
        return DEBUG_HMAC;
    }

    public static Intent getCallIntent(Context context) {
        return new Intent(context, DebugSettingActivity.class);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug_setting);

        weightEt = findViewById(R.id.weightEt);
        rh20et = findViewById(R.id.rh20et);
        lh20et = findViewById(R.id.lh20et);
        t20et = findViewById(R.id.t20et);
        rf20et = findViewById(R.id.rf20et);
        lf20et = findViewById(R.id.lf20et);
        rh100et = findViewById(R.id.rh100et);
        lh100et = findViewById(R.id.lh100et);
        t100et = findViewById(R.id.t100et);
        rf100et = findViewById(R.id.rf100et);
        lf100et = findViewById(R.id.lf100et);
        okBtn = findViewById(R.id.okBtn);

        try {
            JSONObject jsonObject = new JSONObject(YLPacker.unpack(DEBUG_HMAC));
            double lastWeight = jsonObject.getDouble("weight");
            double lastResistanceRH20 = jsonObject.getDouble("res20_right_arm");
            double lastResistanceLH20 = jsonObject.getDouble("res20_left_arm");
            double lastResistanceT20 = jsonObject.getDouble("res20_trunk");
            double lastResistanceRF20 = jsonObject.getDouble("res20_right_leg");
            double lastResistanceLF20 = jsonObject.getDouble("res20_left_leg");

            double lastResistanceRH100 = jsonObject.getDouble("res100_right_arm");
            double lastResistanceLH100 = jsonObject.getDouble("res100_left_arm");
            double lastResistanceT100 = jsonObject.getDouble("res100_trunk");
            double lastResistanceRF100 = jsonObject.getDouble("res100_right_leg");
            double lastResistanceLF100 = jsonObject.getDouble("res100_left_leg");

            weightEt.setText(String.valueOf(lastWeight));

            rh20et.setText(String.valueOf(lastResistanceRH20));
            lh20et.setText(String.valueOf(lastResistanceLH20));
            t20et.setText(String.valueOf(lastResistanceT20));
            rf20et.setText(String.valueOf(lastResistanceRF20));
            lf20et.setText(String.valueOf(lastResistanceLF20));
            rh100et.setText(String.valueOf(lastResistanceRH100));
            lh100et.setText(String.valueOf(lastResistanceLH100));
            t100et.setText(String.valueOf(lastResistanceT100));
            rf100et.setText(String.valueOf(lastResistanceRF100));
            lf100et.setText(String.valueOf(lastResistanceLF100));
        } catch (Exception e) {
        }

        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    JSONObject jsonObject = new JSONObject();
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    jsonObject.put("weight", Double.valueOf(weightEt.getText().toString()));
                    jsonObject.put("measure_time", simpleDateFormat.format(new Date()));
                    jsonObject.put("res20_left_arm", Double.valueOf(lh20et.getText().toString()));
                    jsonObject.put("res20_left_leg", Double.valueOf(lf20et.getText().toString()));
                    jsonObject.put("res20_right_arm", Double.valueOf(rh20et.getText().toString()));
                    jsonObject.put("res20_right_leg", Double.valueOf(rf20et.getText().toString()));
                    jsonObject.put("res20_trunk", Double.valueOf(t20et.getText().toString()));

                    jsonObject.put("res100_left_arm", Double.valueOf(lh100et.getText().toString()));
                    jsonObject.put("res100_left_leg", Double.valueOf(lf100et.getText().toString()));
                    jsonObject.put("res100_right_arm", Double.valueOf(rh100et.getText().toString()));
                    jsonObject.put("res100_right_leg", Double.valueOf(rf100et.getText().toString()));
                    jsonObject.put("res100_trunk", Double.valueOf(t100et.getText().toString()));

                    jsonObject.put("resistance_50_adjust",500);
                    jsonObject.put("resistance_500_adjust",500);
                    jsonObject.put("eight_flag",1);

                    String hmac = YLPacker.pack(jsonObject.toString());
                    DEBUG_HMAC = hmac;
                    QNLogUtils.logAndWrite("八电极数据计算", "调试用hmac:\n" + DEBUG_HMAC);
                    finish();
                } catch (Exception e) {
                    DEBUG_HMAC = "";
                    Toast.makeText(DebugSettingActivity.this, "输入非数字，已清空当前HMAC", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
