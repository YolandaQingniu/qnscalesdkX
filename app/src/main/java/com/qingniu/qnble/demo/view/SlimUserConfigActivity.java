package com.qingniu.qnble.demo.view;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.qingniu.qnble.demo.R;
import com.qingniu.qnble.demo.util.SlimUtils;
import com.qn.device.constant.QNSlimCurveWeightSelection;
import com.qn.device.constant.QNSlimDayCountRule;
import com.qn.device.out.QNSlimUserSlimConfig;

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

        QNSlimUserSlimConfig userSlimConfig = new QNSlimUserSlimConfig();

        int dayRuleId = rgDayRule.getCheckedRadioButtonId();
        QNSlimDayCountRule slimDayCountRule = (dayRuleId == R.id.rb_auto_increment)
                ? QNSlimDayCountRule.QNSlimDayCountRuleAutoIncrement : QNSlimDayCountRule.QNSlimDayCountRuleByMeasurement;
        userSlimConfig.setSlimDayCountRule(slimDayCountRule);

        int weightRuleId = rgWeightRule.getCheckedRadioButtonId();
        QNSlimCurveWeightSelection curveWeightSelection = (weightRuleId == R.id.rb_last_measurement)
                ? QNSlimCurveWeightSelection.QNSlimCurveWeightSelectionLastOfDay : QNSlimCurveWeightSelection.QNSlimCurveWeightSelectionMinOfDay;
        userSlimConfig.setCurveWeightSelection(curveWeightSelection);

        String progressDays = etProgressDays.getText().toString().trim();
        String startWeight = etStartWeight.getText().toString().trim();
        String targetWeight = etTargetWeight.getText().toString().trim();

        userSlimConfig.setSlimDays(Integer.parseInt(progressDays));
        userSlimConfig.setInitialWeight(Double.parseDouble(startWeight));
        userSlimConfig.setTargetWeight(Double.parseDouble(targetWeight));

        SlimUtils.qnSlimUserSlimConfig = userSlimConfig;

        Log.i(TAG, "结果: " + userSlimConfig.toString());
        Toast.makeText(this, "设置已保存，可返回上级页面", Toast.LENGTH_SHORT).show();

    }
}

