package com.qingniu.qnble.demo.view;

import android.os.Bundle;
import android.text.TextUtils;
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
import com.qingniu.qnble.demo.util.SlimUtils;
import com.qn.device.constant.QNSlimAlarmOperation;
import com.qn.device.constant.QNSlimAlarmWeekDays;
import com.qn.device.constant.QNSlimVoiceOperation;
import com.qn.device.constant.QNSlimVoiceSource;
import com.qn.device.constant.QNSlimVoiceVolume;
import com.qn.device.out.QNSlimDeviceConfig;
import com.qn.device.out.QNSlimUserCurveData;
import com.qn.device.out.QNSlimVoiceConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: yxb
 * @description: 减重秤设备配置页面
 * @date: 2025/10/30
 */
public class SlimDeviceConfigActivity extends AppCompatActivity {

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
        setContentView(R.layout.activity_slim_device_config);

        initViews();
        initSoundConfig(itemAlarmSound, getString(R.string.voice_alarm_tip));
        initSoundConfig(itemWeightSound, getString(R.string.voice_measure_start_tip));
        initSoundConfig(itemMeasureDoneSound, getString(R.string.voice_measure_finish_tip));
        initSoundConfig(itemGoalDoneSound, getString(R.string.voice_goal_tip));

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
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvValue.setText(progress + "/8");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
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
            @Override
            public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                tvValue.setText(progress + "/8");
                findConfigByName(title).volume = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    private SoundConfig findConfigByName(String name) {
        for (SoundConfig c : soundConfigs) {
            if (c.name.equals(name)) return c;
        }
        return null;
    }

    private void saveSettings() {

        QNSlimDeviceConfig qnSlimDeviceConfig = new QNSlimDeviceConfig();
        QNSlimUserCurveData qnSlimUserCurveData = new QNSlimUserCurveData();


        String actionType = (String) spinnerAction.getSelectedItem();
        String volumeLevel = (String) spinnerVolume.getSelectedItem();

        int hour = timePicker.getHour();
        int minute = timePicker.getMinute();

        List<String> activeDays = new ArrayList<>();
        String[] dayNames = {"一", "二", "三", "四", "五", "六", "日"};
        for (int i = 0; i < dayChecks.length; i++) {
            if (dayChecks[i].isChecked()) activeDays.add(dayNames[i]);
        }

        ArrayList<Double> curveList = new ArrayList<>();
        String etSplit = curveET.getText().toString();
        if (!TextUtils.isEmpty(etSplit)) {
            String[] split = etSplit.split(",");
            for (int i = 0; i < split.length; i++) {
                if (i <= 13) {
                    curveList.add(Double.valueOf(split[i]));
                }
            }
            int size = curveList.size();
            if (size != 14) {
                for (int i = size; i < 14; i++) {
                    if (curveList.size() < 14) {
                        curveList.add(0.0);
                    }
                }
            }
        }

        boolean curveFlag = curveCB.isChecked();

        qnSlimUserCurveData.setTodayFlag(curveFlag);
        qnSlimUserCurveData.setCurveWeightArr(curveList);

        Log.i(TAG, "操作类型: " + actionType);
        Log.i(TAG, "提醒时间: " + hour + ":" + minute);
        Log.i(TAG, "生效日: " + activeDays);
        Log.i(TAG, "音量设置: " + volumeLevel);
        Log.i(TAG, "体重曲线: " + curveList);
        Log.i(TAG, "是否今日数据: " + curveFlag);

        for (SoundConfig config : soundConfigs) {
            Log.i(TAG, "提示音配置: " + config);
        }

        QNSlimAlarmOperation alarmOperation = QNSlimAlarmOperation.QNSlimAlarmOperationCloseAll;
        if ("打开".equals(actionType)) {
            alarmOperation = QNSlimAlarmOperation.QNSlimAlarmOperationSetDays;
        }
        qnSlimDeviceConfig.setAlarmOperation(alarmOperation);

        ArrayList<QNSlimAlarmWeekDays> qnSlimAlarmWeekDays = new ArrayList<>();
        if (activeDays.contains("一")) {
            qnSlimAlarmWeekDays.add(QNSlimAlarmWeekDays.QNSlimAlarmWeekDayMonday);
        }
        if (activeDays.contains("二")) {
            qnSlimAlarmWeekDays.add(QNSlimAlarmWeekDays.QNSlimAlarmWeekDayTuesday);
        }
        if (activeDays.contains("三")) {
            qnSlimAlarmWeekDays.add(QNSlimAlarmWeekDays.QNSlimAlarmWeekDayWednesday);
        }
        if (activeDays.contains("四")) {
            qnSlimAlarmWeekDays.add(QNSlimAlarmWeekDays.QNSlimAlarmWeekDayThursday);
        }
        if (activeDays.contains("五")) {
            qnSlimAlarmWeekDays.add(QNSlimAlarmWeekDays.QNSlimAlarmWeekDayFriday);
        }
        if (activeDays.contains("六")) {
            qnSlimAlarmWeekDays.add(QNSlimAlarmWeekDays.QNSlimAlarmWeekDaySaturday);
        }
        if (activeDays.contains("日")) {
            qnSlimAlarmWeekDays.add(QNSlimAlarmWeekDays.QNSlimAlarmWeekDaySunday);
        }
        qnSlimDeviceConfig.setAlarmWeekDays(qnSlimAlarmWeekDays);

        qnSlimDeviceConfig.setAlarmHour(hour);
        qnSlimDeviceConfig.setAlarmMinute(minute);

        QNSlimVoiceVolume voiceVolume = QNSlimVoiceVolume.QNSlimVoiceVolumeNoModify;
        if ("1级".equals(volumeLevel)) {
            voiceVolume = QNSlimVoiceVolume.QNSlimVoiceVolumeLevel1;
        }else if ("2级".equals(volumeLevel)) {
            voiceVolume = QNSlimVoiceVolume.QNSlimVoiceVolumeLevel2;
        }else if ("3级".equals(volumeLevel)) {
            voiceVolume = QNSlimVoiceVolume.QNSlimVoiceVolumeLevel3;
        }else if ("4级".equals(volumeLevel)) {
            voiceVolume = QNSlimVoiceVolume.QNSlimVoiceVolumeLevel4;
        }
        qnSlimDeviceConfig.setVoiceVolume(voiceVolume);

        qnSlimDeviceConfig.setAlarmVoice(fetchQNSlimVoiceConfig(getString(R.string.voice_alarm_tip)));
        qnSlimDeviceConfig.setMeasureStartVoice(fetchQNSlimVoiceConfig(getString(R.string.voice_measure_start_tip)));
        qnSlimDeviceConfig.setMeasureFinishVoice(fetchQNSlimVoiceConfig(getString(R.string.voice_measure_finish_tip)));
        qnSlimDeviceConfig.setCompleteGoalVoice(fetchQNSlimVoiceConfig(getString(R.string.voice_goal_tip)));

        SlimUtils.qnSlimDeviceConfig = qnSlimDeviceConfig;
        SlimUtils.qnSlimUserCurveData = qnSlimUserCurveData;

        Log.i(TAG, "qnSlimDeviceConfig: " + qnSlimDeviceConfig);
        Log.i(TAG, "qnSlimUserCurveData: " + qnSlimUserCurveData);


        Toast.makeText(this, "设置已保存，可返回上级页面", Toast.LENGTH_SHORT).show();
    }

    private QNSlimVoiceConfig fetchQNSlimVoiceConfig(String title) {
        SoundConfig soundConfig = findConfigByName(title);

        QNSlimVoiceConfig voiceConfig = new QNSlimVoiceConfig();
        if (soundConfig.enabled) {
            voiceConfig.setVoiceOperation(QNSlimVoiceOperation.QNSlimVoiceOperationOpen);
        } else {
            voiceConfig.setVoiceOperation(QNSlimVoiceOperation.QNSlimVoiceOperationClose);
        }
        voiceConfig.setVoiceSource(QNSlimVoiceSource.getQNSlimVoiceSource(soundConfig.volume));

        return voiceConfig;
    }
}
