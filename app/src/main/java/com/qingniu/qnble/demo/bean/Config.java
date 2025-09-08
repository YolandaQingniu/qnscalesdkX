package com.qingniu.qnble.demo.bean;

import android.os.Parcel;
import android.os.Parcelable;

public class Config implements Parcelable {
    private boolean onlyScreenOn;
    private boolean allowDuplicates;
    private int duration;

    private int language;

    private boolean enhanceBleBroadcast;

    public boolean isEnhanceBleBroadcast() {
        return enhanceBleBroadcast;
    }

    public Config setEnhanceBleBroadcast(boolean enhanceBleBroadcast) {
        this.enhanceBleBroadcast = enhanceBleBroadcast;
        return this;
    }

    public boolean isOnlyScreenOn() {
        return onlyScreenOn;
    }

    public void setOnlyScreenOn(boolean onlyScreenOn) {
        this.onlyScreenOn = onlyScreenOn;
    }

    public boolean isAllowDuplicates() {
        return allowDuplicates;
    }

    public void setAllowDuplicates(boolean allowDuplicates) {
        this.allowDuplicates = allowDuplicates;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getUnit() {
        return unit;
    }

    public void setUnit(int unit) {
        this.unit = unit;
    }

    public int getHeightUnit() {
        return heightUnit;
    }

    public void setHeightUnit(int heightUnit) {
        this.heightUnit = heightUnit;
    }

    private int unit;

    private int heightUnit;

    public long getScanOutTime() {
        return scanOutTime;
    }

    public void setScanOutTime(long scanOutTime) {
        this.scanOutTime = scanOutTime;
    }

    private long scanOutTime=6000;

    public long getConnectOutTime() {
        return connectOutTime;
    }

    public void setConnectOutTime(long connectOutTime) {
        this.connectOutTime = connectOutTime;
    }

    public int getLanguage() {
        return language;
    }

    public void setLanguage(int language) {
        this.language = language;
    }

    private long connectOutTime=6000;

    public Config() {
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte(this.onlyScreenOn ? (byte) 1 : (byte) 0);
        dest.writeByte(this.allowDuplicates ? (byte) 1 : (byte) 0);
        dest.writeInt(this.duration);
        dest.writeByte(this.enhanceBleBroadcast ? (byte) 1 : (byte) 0);
        dest.writeInt(this.unit);
        dest.writeInt(this.heightUnit);
        dest.writeInt(this.language);
        dest.writeLong(this.scanOutTime);
        dest.writeLong(this.connectOutTime);
    }

    protected Config(Parcel in) {
        this.onlyScreenOn = in.readByte() != 0;
        this.allowDuplicates = in.readByte() != 0;
        this.duration = in.readInt();
        this.enhanceBleBroadcast = in.readByte() != 0;
        this.unit = in.readInt();
        this.heightUnit = in.readInt();
        this.language = in.readInt();
        this.scanOutTime = in.readLong();
        this.connectOutTime = in.readLong();
    }

    public static final Creator<Config> CREATOR = new Creator<Config>() {
        @Override
        public Config createFromParcel(Parcel source) {
            return new Config(source);
        }

        @Override
        public Config[] newArray(int size) {
            return new Config[size];
        }
    };
}
