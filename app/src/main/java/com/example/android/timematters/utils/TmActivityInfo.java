package com.example.android.timematters.utils;


import android.os.Parcel;
import android.os.Parcelable;

import com.example.android.timematters.database.ActivityEntry;

public class TmActivityInfo implements Parcelable {

    private int id;
    private String name;
    private boolean active;
    private boolean rateTask;
    private boolean notification;
    private String startTime;
    private String finishTime;
    private String latitude;
    private String longitude;
    private int color;

    public TmActivityInfo() {}

    protected TmActivityInfo(Parcel in) {
        id = in.readInt();
        name = in.readString();
        active = in.readByte() != 0;
        rateTask = in.readByte() != 0;
        notification = in.readByte() != 0;
        startTime = in.readString();
        finishTime = in.readString();
        latitude = in.readString();
        longitude = in.readString();
        color = in.readInt();
    }

    public static TmActivityInfo newActivityInfo(ActivityEntry activityEntry) {
        TmActivityInfo activityInfo = new TmActivityInfo();
        activityInfo.setId(activityEntry.getId());
        activityInfo.setName(activityEntry.getName());
        activityInfo.setActive(activityEntry.isActive());
        activityInfo.setRateTask(activityEntry.isRateTask());
        activityInfo.setNotification(activityEntry.isNotification());
        activityInfo.setStartTime(activityEntry.getStartTime());
        activityInfo.setFinishTime(activityEntry.getFinishTime());
        activityInfo.setLatitude(activityEntry.getLatitude());
        activityInfo.setLongitude(activityEntry.getLongitude());
        activityInfo.setColor(activityEntry.getColor());
        return activityInfo;
    }


    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeByte((byte) (active ? 1 : 0));
        dest.writeByte((byte) (rateTask ? 1 : 0));
        dest.writeByte((byte) (notification ? 1 : 0));
        dest.writeString(startTime);
        dest.writeString(finishTime);
        dest.writeString(latitude);
        dest.writeString(longitude);
        dest.writeInt(color);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<TmActivityInfo> CREATOR = new Creator<TmActivityInfo>() {
        @Override
        public TmActivityInfo createFromParcel(Parcel in) {
            return new TmActivityInfo(in);
        }

        @Override
        public TmActivityInfo[] newArray(int size) {
            return new TmActivityInfo[size];
        }
    };

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isRateTask() {
        return rateTask;
    }

    public void setRateTask(boolean rateTask) {
        this.rateTask = rateTask;
    }

    public boolean isNotification() {
        return notification;
    }

    public void setNotification(boolean notification) {
        this.notification = notification;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public void setFinishTime(String finishTime) {
        this.finishTime = finishTime;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }
}
