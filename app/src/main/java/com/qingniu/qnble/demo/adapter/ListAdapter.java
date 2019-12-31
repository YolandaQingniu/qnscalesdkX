package com.qingniu.qnble.demo.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.qingniu.qnble.demo.R;
import com.qingniu.qnble.demo.bean.IndicateBean;
import com.qingniu.qnble.demo.util.IndicateUtils;
import com.qingniu.qnble.utils.QNLogUtils;
import com.yolanda.health.qnblesdk.constant.QNIndicator;
import com.yolanda.health.qnblesdk.out.QNBleApi;
import com.yolanda.health.qnblesdk.out.QNScaleItemData;
import com.yolanda.health.qnblesdk.out.QNUser;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by ch on 2019/10/23.
 */

public class ListAdapter extends BaseAdapter {

    private List<QNScaleItemData> mDatas;
    private QNBleApi mQNBleApi;
    private QNUser qnUser;

    public ListAdapter(List<QNScaleItemData> mDatas, QNBleApi mQNBleApi, QNUser qnUser) {
        this.mDatas = mDatas;
        this.mQNBleApi = mQNBleApi;
        this.qnUser = qnUser;
    }

    @Override
    public int getCount() {
        return mDatas.size();
    }

    @Override
    public Object getItem(int position) {
        return mDatas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return mDatas.get(position).hashCode();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_data, null);
        }
        TextView indicateNameTv = (TextView) convertView.findViewById(R.id.indicate_nameTv);
        TextView indicateValueTv = (TextView) convertView.findViewById(R.id.indicate_valueTv);
        TextView indicateLevelTv = (TextView) convertView.findViewById(R.id.indicate_levelTv);
        TextView standardJudgeTv = (TextView) convertView.findViewById(R.id.standardJudgeTv);
        TextView currentStandardTv = (TextView) convertView.findViewById(R.id.currentStandardTv);
        QNScaleItemData itemData = mDatas.get(position);

        indicateNameTv.setText(itemData.getName());
        //sdk返回的数据单位一直不变，用户需要自己去转化为自己需要的单位数据
        //和重量有关的指标
        if (itemData.getType() == QNIndicator.TYPE_WEIGHT || itemData.getType() == QNIndicator.TYPE_BONE
                || itemData.getType() == QNIndicator.TYPE_MUSCLE_MASS) {
            indicateValueTv.setText(initWeight(itemData.getValue()));
        } else {
            indicateValueTv.setText(String.valueOf(itemData.getValue()));
        }
        IndicateBean indicateBean = IndicateUtils.getIndicate(parent.getContext(), itemData.getType(),
                qnUser.getGender(), qnUser.getHeight(), calcAge(qnUser.getBirthDay()), itemData.getValue());
        standardJudgeTv.setText(indicateBean.getIndicateDescribe().toString());
        currentStandardTv.setText(indicateBean.getCurrentIndicate());
        return convertView;
    }

    private String initWeight(double weight) {
        int unit = mQNBleApi.getConfig().getUnit();
        return mQNBleApi.convertWeightWithTargetUnit(weight, unit);
    }

    private int calcAge(Date birthday) {
        if (birthday == null) {
            return 0;
        }
        int age = getAgeToDay(birthday);

        //2019年01月04日16:07:58 修改年龄计算为年月日方式
        //修改年龄的界限
        if (age < 3) {
            return 3;
        } else if (age > 80) {
            return 80;
        } else return age;
    }

    private int getAgeToDay(Date birthday) {
        if (birthday == null) {
            return 0;
        }

        Calendar cal = Calendar.getInstance();

        //设置当前日期
        cal.setTime(new Date());
        int curYear = cal.get(Calendar.YEAR);
        int curMonth = cal.get(Calendar.MONTH) + 1;
        int curDay = cal.get(Calendar.DAY_OF_MONTH);

        //设置生日日期
        cal.setTime(birthday);
        int userYear = cal.get(Calendar.YEAR);
        int userMonth = cal.get(Calendar.MONTH) + 1;
        int userDay = cal.get(Calendar.DAY_OF_MONTH);

        int age = curYear - userYear - 1;
        if ((curMonth > userMonth) || (curMonth == userMonth && curDay >= userDay)) {
            age++;
        }
        QNLogUtils.log("BleUser", "计算的年龄为:" + age + ";当前时间为:" + System.currentTimeMillis() +
                ";生日为:" + birthday.getTime());
        return age;
    }
}
