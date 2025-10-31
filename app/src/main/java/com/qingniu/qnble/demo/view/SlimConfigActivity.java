package com.qingniu.qnble.demo.view;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.qingniu.qnble.demo.R;
import com.qingniu.qnble.demo.bean.SoundConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: yxb
 * @description: 减重秤设备配置页面
 * @date: 2025/10/30
 */
public class SlimConfigActivity extends AppCompatActivity {

    private static final String TAG = "AlarmSettingActivity";

    // 主体控件
    private Spinner spinnerAction;
    private TimePicker timePicker;
    private Spinner spinnerVolume;

    private CheckBox[] dayChecks;

    private EditText curveET;
    private CheckBox curveCB;

    // 提示音配置块
    private View itemAlarmSound, itemWeightSound, itemMeasureDoneSound, itemGoalDoneSound;

    // 四种提示音数据
    private final List<SoundConfig> soundConfigs = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slim_config);

        initViews();
        initSoundConfig(itemAlarmSound, "闹钟提醒提示音");
        initSoundConfig(itemWeightSound, "上秤测量提示音");
        initSoundConfig(itemMeasureDoneSound, "测量完成提示音");
        initSoundConfig(itemGoalDoneSound, "完成目标提示音");

        // 示例：保存按钮
        Button btnSave = new Button(this);
        btnSave.setText("保存设置");
        ((LinearLayout) findViewById(R.id.container)).addView(btnSave);
        btnSave.setOnClickListener(v -> saveSettings());
    }

    private void initViews() {
        spinnerAction = findViewById(R.id.spinner_alarm_action);
        timePicker = findViewById(R.id.time_picker);
        spinnerVolume = findViewById(R.id.spinner_volume);

        timePicker.setIs24HourView(true);

        dayChecks = new CheckBox[]{
                findViewById(R.id.cb_mon),
                findViewById(R.id.cb_tue),
                findViewById(R.id.cb_wed),
                findViewById(R.id.cb_thu),
                findViewById(R.id.cb_fri),
                findViewById(R.id.cb_sat),
                findViewById(R.id.cb_sun)
        };

        itemAlarmSound = findViewById(R.id.item_alarm_sound);
        itemWeightSound = findViewById(R.id.item_weight_sound);
        itemMeasureDoneSound = findViewById(R.id.item_measure_done_sound);
        itemGoalDoneSound = findViewById(R.id.item_goal_done_sound);

        curveET = findViewById(R.id.curve_et);
        curveCB = findViewById(R.id.curve_cb);
    }

    /**
     * 初始化通用的提示音配置布局
     */
    private void initSoundConfig(View view, String title) {
        TextView tvTitle = view.findViewById(R.id.tv_sound_title);
        Switch swEnable = view.findViewById(R.id.switch_sound_enable);
        SeekBar seekBar = view.findViewById(R.id.seekbar_volume);
        TextView tvValue = view.findViewById(R.id.tv_volume_value);

        tvTitle.setText(title);

        // 默认启用
        swEnable.setChecked(true);
        seekBar.setEnabled(true);

        // SeekBar 更新数值显示
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvValue.setText(progress + "/8");
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // 开关控制可用状态
        swEnable.setOnCheckedChangeListener((buttonView, isChecked) -> {
            seekBar.setEnabled(isChecked);
            tvValue.setEnabled(isChecked);
        });

        // 存放引用
        soundConfigs.add(new SoundConfig(title, swEnable.isChecked(), seekBar.getProgress()));

        // 可同步监听状态变化更新 model
        swEnable.setOnCheckedChangeListener((buttonView, isChecked) -> {
            findConfigByName(title).enabled = isChecked;
        });
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                tvValue.setText(progress + "/8");
                findConfigByName(title).volume = progress;
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private SoundConfig findConfigByName(String name) {
        for (SoundConfig c : soundConfigs) {
            if (c.name.equals(name)) return c;
        }
        return null;
    }

    private void saveSettings() {
        String actionType = (String) spinnerAction.getSelectedItem();
        String volumeLevel = (String) spinnerVolume.getSelectedItem();

        int hour = timePicker.getHour();
        int minute = timePicker.getMinute();

        List<String> activeDays = new ArrayList<>();
        String[] dayNames = {"一", "二", "三", "四", "五", "六", "日"};
        for (int i = 0; i < dayChecks.length; i++) {
            if (dayChecks[i].isChecked()) activeDays.add(dayNames[i]);
        }

        String etSplit = curveET.getText().toString();
        String[] split = etSplit.split(",");
        ArrayList<String> curveList = new ArrayList<>();
        for (int i = 0; i < split.length; i++) {
            if (i <= 13) {
                curveList.add(split[i]);
            }
        }

        boolean curveFlag = curveCB.isChecked();

        Log.i(TAG, "操作类型: " + actionType);
        Log.i(TAG, "提醒时间: " + hour + ":" + minute);
        Log.i(TAG, "生效日: " + activeDays);
        Log.i(TAG, "音量设置: " + volumeLevel);
        Log.i(TAG, "体重曲线: " + curveList);
        Log.i(TAG, "是否今日数据: " + curveFlag);

        for (SoundConfig config : soundConfigs) {
            Log.i(TAG, "提示音配置: " + config);
        }

        Toast.makeText(this, "设置已保存", Toast.LENGTH_SHORT).show();
    }
}
