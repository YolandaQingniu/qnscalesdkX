package com.qingniu.qnble.demo.bean;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

/**
 * Created by ch on 2019/10/23.
 */

public class IndicateBean implements Parcelable {

    private List<String> indicateDescribe;

    private String currentIndicate;

    public List<String> getIndicateDescribe() {
        return indicateDescribe;
    }

    public void setIndicateDescribe(List<String> indicateDescribe) {
        this.indicateDescribe = indicateDescribe;
    }

    public String getCurrentIndicate() {
        return currentIndicate;
    }

    public void setCurrentIndicate(String currentIndicate) {
        this.currentIndicate = currentIndicate;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringList(this.indicateDescribe);
        dest.writeString(this.currentIndicate);
    }

    public IndicateBean() {
    }

    protected IndicateBean(Parcel in) {
        this.indicateDescribe = in.createStringArrayList();
        this.currentIndicate = in.readString();
    }

    public static final Creator<IndicateBean> CREATOR = new Creator<IndicateBean>() {
        @Override
        public IndicateBean createFromParcel(Parcel source) {
            return new IndicateBean(source);
        }

        @Override
        public IndicateBean[] newArray(int size) {
            return new IndicateBean[size];
        }
    };
}
