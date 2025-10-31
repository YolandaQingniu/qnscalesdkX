package com.qingniu.qnble.demo.view;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;

import androidx.appcompat.app.AppCompatActivity;

import com.qingniu.qnble.demo.R;

/**
 * @author: yxb
 * @description:
 * @date: 2025/10/31
 */
public class SlimUserConfigActivity extends AppCompatActivity {

    private static final String TAG = "SlimUserConfigActivity";
    private RadioGroup rgDayRule, rgWeightRule;
    private EditText etProgressDays, etStartWeight, etTargetWeight;
    private Button btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slim_user_config);

        rgDayRule = findViewById(R.id.rg_day_calculation_rule);
        rgWeightRule = findViewById(R.id.rg_weight_data_rule);
        etProgressDays = findViewById(R.id.et_progress_days);
        etStartWeight = findViewById(R.id.et_start_weight);
        etTargetWeight = findViewById(R.id.et_target_weight);
        btnSave = findViewById(R.id.btn_save);

        btnSave.setOnClickListener(v -> saveSettings());
    }

    private void saveSettings() {
        int dayRuleId = rgDayRule.getCheckedRadioButtonId();
        String dayRule = (dayRuleId == R.id.rb_auto_increment)
                ? "自动递增" : "按测量天数递增";

        int weightRuleId = rgWeightRule.getCheckedRadioButtonId();
        String weightRule = (weightRuleId == R.id.rb_last_measurement)
                ? "当天最后测量值" : "当天最小值";

        String progressDays = etProgressDays.getText().toString().trim();
        String startWeight = etStartWeight.getText().toString().trim();
        String targetWeight = etTargetWeight.getText().toString().trim();

        String summary = "减重天数规则：" + dayRule +
                "\n减重进度天数：" + progressDays +
                "\n体重曲线规则：" + weightRule +
                "\n初始体重：" + startWeight + " kg" +
                "\n目标体重：" + targetWeight + " kg";

        Log.i(TAG, "结果: " + summary);

    }
}

