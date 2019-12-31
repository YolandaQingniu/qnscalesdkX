package com.qingniu.qnble.demo.bean;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

/**
 * author: yolanda-XY
 * description: ${用户本地信息}
 * date: 2018/4/28
 */

public class User implements Parcelable {
    /**
     * 用户唯一标识，不能为空，每个用户唯一，与存储数据相关
     */
    private String userId;
    /**
     * 身高，单位 cm，最低40，最高240
     */
    private int height;

    /**
     * 性别，男: male，女: female
     */
    private String gender;
    /**
     * 生日，用以计算年龄，日期精度精确到天，计算的年龄范围是 3~80，
     * 超过这个范围的，等于上限或下限
     */
    private Date birthDay;
    /**
     * 设置使用算法的类型；
     * 1表示运动员算法，0是普通算法
     * 当用户年龄小于18岁时，即使设置为运动员模式，也是使用普通模式
     */
    private int athleteType;

    /**
     * 选择的体型
     */
    private int choseShape;

    /**
     * 选择的目标
     */
    private int choseGoal;


    private double clothesWeight;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Date getBirthDay() {
        return birthDay;
    }

    public void setBirthDay(Date birthDay) {
        this.birthDay = birthDay;
    }

    public User() {
    }

    public int getAthleteType() {
        return athleteType;
    }

    public void setAthleteType(int athleteType) {
        this.athleteType = athleteType;
    }

    public int getChoseShape() {
        return choseShape;
    }

    public void setChoseShape(int choseShape) {
        this.choseShape = choseShape;
    }

    public int getChoseGoal() {
        return choseGoal;
    }

    public void setChoseGoal(int choseGoal) {
        this.choseGoal = choseGoal;
    }

    public double getClothesWeight() {
        return clothesWeight;
    }

    public void setClothesWeight(double clothesWeight) {
        this.clothesWeight = clothesWeight;
    }

    @Override
    public String toString() {
        return "User{" +
                "userId='" + userId + '\'' +
                ", height=" + height +
                ", gender='" + gender + '\'' +
                ", birthDay=" + birthDay +
                ", athleteType=" + athleteType +
                ", choseShape=" + choseShape +
                ", choseGoal=" + choseGoal +
                ", clothesWeight=" + clothesWeight +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.userId);
        dest.writeInt(this.height);
        dest.writeString(this.gender);
        dest.writeLong(this.birthDay != null ? this.birthDay.getTime() : -1);
        dest.writeInt(this.athleteType);
        dest.writeInt(this.choseShape);
        dest.writeInt(this.choseGoal);
        dest.writeDouble(this.clothesWeight);
    }

    protected User(Parcel in) {
        this.userId = in.readString();
        this.height = in.readInt();
        this.gender = in.readString();
        long tmpBirthDay = in.readLong();
        this.birthDay = tmpBirthDay == -1 ? null : new Date(tmpBirthDay);
        this.athleteType = in.readInt();
        this.choseShape = in.readInt();
        this.choseGoal = in.readInt();
        this.clothesWeight = in.readDouble();
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel source) {
            return new User(source);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };
}
