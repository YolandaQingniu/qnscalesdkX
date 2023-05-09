package com.qingniu.qnble.demo.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.IdRes;
import androidx.appcompat.app.AppCompatActivity;

import com.qingniu.qnble.demo.R;
import com.qingniu.qnble.demo.bean.Config;
import com.qingniu.qnble.demo.bean.User;
import com.qingniu.qnble.demo.picker.DatePickerDialog;
import com.qingniu.qnble.demo.picker.HeightPickerDialog;
import com.qingniu.qnble.demo.util.DateUtils;
import com.qingniu.qnble.demo.util.ToastMaker;
import com.qn.device.constant.QNHeightUnit;
import com.qn.device.constant.QNInfoConst;
import com.qn.device.constant.QNUnit;
import com.qn.device.constant.UserGoal;
import com.qn.device.constant.UserShape;

import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * author: yolanda-zhao
 * description:自我管理的设置界面
 * date: 2019/9/10
 */

public class CustomSettingActivity extends AppCompatActivity implements RadioGroup.OnCheckedChangeListener, View.OnClickListener {

    @BindView(R.id.user_shape_spinner)
    Spinner shapeSpn;
    @BindView(R.id.user_goal_spinner)
    Spinner goalSpn;

    @BindView(R.id.user_id_edt)
    EditText mUserIdEdt;
    @BindView(R.id.user_male_rb)
    RadioButton mUserMaleRb;
    @BindView(R.id.user_female_rb)
    RadioButton mUserFemaleRb;
    @BindView(R.id.user_height_tv)
    TextView mUserHeightTv;
    @BindView(R.id.user_birthday_tv)
    TextView mUserBirthdayTv;
    @BindView(R.id.user_unit_kg)
    RadioButton mUserUnitKg;
    @BindView(R.id.user_unit_lb)
    RadioButton mUserUnitLb;
    @BindView(R.id.user_unit_jin)
    RadioButton mUserUnitJin;
    @BindView(R.id.user_unit_st)
    RadioButton mUserUnitSt;
    @BindView(R.id.user_gender_grp)
    RadioGroup mUserGenderGrp;
    @BindView(R.id.user_calc_grp)
    RadioGroup mUserCalcGrp;
    @BindView(R.id.user_unit_grp)
    RadioGroup mUserUnitGrp;
    @BindView(R.id.height_unit_grp)
    RadioGroup mHeightUnitGrp;
    @BindView(R.id.btn_sure)
    Button mSure;

    @BindView(R.id.user_clothes_edt)
    EditText user_clothes_edt;
    @BindView(R.id.btn_double_sure)
    Button btnDoubleSure;

    private Config mBleConfig; //蓝牙配置对象
    private String mGender = "male";//用户性别
    private int mHeight = 172; //用户身高
    private Date mBirthday = null; //用户生日

    private User mUser;

    public static Intent getCallIntent(Context context) {
        return new Intent(context, CustomSettingActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_setting);
        ButterKnife.bind(this);
        initView();
        initData();
        initListener();
    }

    private void initData() {
        mBleConfig = new Config();
        mUser = new User();
    }


    private void initView() {
        mBirthday = DateUtils.getDate(1990, 1, 1);
        mUserBirthdayTv.setText(DateUtils.getBirthdayString(mBirthday, CustomSettingActivity.this));
    }


    private void initListener() {
        mUserGenderGrp.setOnCheckedChangeListener(this);
        mUserCalcGrp.setOnCheckedChangeListener(this);
        mUserUnitGrp.setOnCheckedChangeListener(this);
        mHeightUnitGrp.setOnCheckedChangeListener(this);
        mUserHeightTv.setOnClickListener(this);
        mUserBirthdayTv.setOnClickListener(this);
        btnDoubleSure.setOnClickListener(this);
        mSure.setOnClickListener(this);


        shapeSpn.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mUser.setChoseShape(position);

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mUser.setChoseShape(UserShape.SHAPE_NONE.getCode());

            }
        });

        goalSpn.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mUser.setChoseGoal(position);

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mUser.setChoseGoal(UserGoal.GOAL_NONE.getCode());

            }
        });

    }

    @Override
    public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
        switch (checkedId) {
            case R.id.user_male_rb:
                mGender = QNInfoConst.GENDER_MAN;
                break;
            case R.id.user_female_rb:
                mGender = QNInfoConst.GENDER_WOMAN;
                break;
            case R.id.normal_calc_rb:
                mUser.setAthleteType(QNInfoConst.CALC_NORMAL);
                break;
            case R.id.athlete_calc_rb:
                mUser.setAthleteType(QNInfoConst.CALC_ATHLETE);
                break;
            case R.id.user_unit_kg:
                mBleConfig.setUnit(QNUnit.WEIGHT_UNIT_KG);
                break;
            case R.id.user_unit_lb:
                mBleConfig.setUnit(QNUnit.WEIGHT_UNIT_LB);
                break;
            case R.id.user_unit_jin:
                mBleConfig.setUnit(QNUnit.WEIGHT_UNIT_JIN);
                break;
            case R.id.user_unit_st:
                mBleConfig.setUnit(QNUnit.WEIGHT_UNIT_ST);
                break;
            case R.id.user_unit_st_only:
                mBleConfig.setUnit(QNUnit.WEIGHT_UNIT_ST_ONLY);
                break;
            case R.id.height_unit_cm:
                mBleConfig.setHeightUnit(QNHeightUnit.CM);
                break;
            case R.id.height_unit_ft_in:
                mBleConfig.setHeightUnit(QNHeightUnit.FT_IN);
                break;
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.user_height_tv:
                HeightPickerDialog.Builder heightBuilder = new HeightPickerDialog.Builder().heightChooseListener(new HeightPickerDialog.HeightChooseListener() {
                    @Override
                    public void onChoose(int height) {
                        mHeight = height;
                        mUserHeightTv.setText(height + "cm");
                    }
                });
                heightBuilder.defaultHeight(172);
                heightBuilder.maxHeight(240);
                heightBuilder.minHeight(40);
                heightBuilder.themeColor(getResources().getColor(R.color.themeColor_0fbfef)).context(CustomSettingActivity.this).build().show();
                break;
            case R.id.user_birthday_tv:
                DatePickerDialog.Builder dateBuilder = new DatePickerDialog.Builder()
                        .dateChooseListener(new DatePickerDialog.DateChooseListener() {

                            @Override
                            public void onChoose(Date date) {
                                if (DateUtils.getAge(date) < 10) {
                                    ToastMaker.show(CustomSettingActivity.this, getResources().getString(R.string.RegisterViewController_lowAge10));
                                }

                                mBirthday = date;
                                mUserBirthdayTv.setText(DateUtils.getBirthdayString(date, CustomSettingActivity.this));
                            }
                        });

                dateBuilder.defaultYear(1990);
                dateBuilder.defaultMonth(1);
                dateBuilder.defaultDay(1);
                dateBuilder.context(CustomSettingActivity.this)
                        .themeColor(getResources().getColor(R.color.themeColor_0fbfef))
                        .build().show();
                break;
            case R.id.btn_sure:
                if (checkInfo()) return;
                startActivity(SelfManagementActivity.getCallIntent(this, mUser, mBleConfig));
                finish();
                break;

            case R.id.btn_double_sure:
                if (checkInfo()) return;
                startActivity(SelfMultiDeviceConnectActivity.getCallIntent(this,mUser,mBleConfig));
                finish();
                break;
        }
    }

    private boolean checkInfo() {
        String userId = mUserIdEdt.getText().toString().trim();

        double clothesWeight = 0;
        try {
            clothesWeight = Double.parseDouble(user_clothes_edt.getText().toString().trim());
        } catch (Exception e) {
            e.printStackTrace();
            ToastMaker.show(this, getResources().getString(R.string.input_cloth));
            return true;
        }
        if (clothesWeight < 0) {
            ToastMaker.show(this, getResources().getString(R.string.input_cloth));
            return true;
        }

        if (userId.isEmpty()) {
            ToastMaker.show(this, getResources().getString(R.string.user_id_empty));
            return true;
        } else if (mUserGenderGrp.getCheckedRadioButtonId() == -1) {
            ToastMaker.show(this, getResources().getString(R.string.select_grander));
            return true;
        } else if (mHeight == 0) {
            ToastMaker.show(this, getResources().getString(R.string.input_height));
            return true;
        } else if (mBirthday == null) {
            ToastMaker.show(this, getString(R.string.input_birthday));
            return true;
        } else if (mUserUnitGrp.getCheckedRadioButtonId() == -1) {
            ToastMaker.show(this, getResources().getString(R.string.select_weight));
            return true;
        }

        mUser.setUserId(userId);
        mUser.setHeight(mHeight);
        mUser.setGender(mGender);
        mUser.setBirthDay(mBirthday);
        mUser.setClothesWeight(clothesWeight);

        return false;
    }

}

