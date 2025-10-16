package com.qingniu.qnble.demo.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author: yxb
 * @description:
 * @date: 2025/10/16
 */
public class WifiInfoModel implements Parcelable {
    public String ssid;
    public int level; // 信号强度 (dBm)

    public WifiInfoModel() {
    }

    public WifiInfoModel(String ssid, int level) {
        this.ssid = ssid;
        this.level = level;
    }

    protected WifiInfoModel(Parcel in) {
        ssid = in.readString();
        level = in.readInt();
    }

    public String getSsid() {
        return ssid;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public static final Creator<WifiInfoModel> CREATOR = new Creator<WifiInfoModel>() {
        @Override
        public WifiInfoModel createFromParcel(Parcel in) {
            return new WifiInfoModel(in);
        }

        @Override
        public WifiInfoModel[] newArray(int size) {
            return new WifiInfoModel[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(ssid);
        dest.writeInt(level);
    }
}

