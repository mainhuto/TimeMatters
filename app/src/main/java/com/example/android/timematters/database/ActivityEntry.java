package com.example.android.timematters.database;

import android.text.TextUtils;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "activity")
public class ActivityEntry {

    @PrimaryKey
    private int id;
    private String name;
    private boolean active;
    @ColumnInfo(name = "rate_task")
    private boolean rateTask;
    private boolean notification;
    @ColumnInfo(name = "start_time")
    private String startTime;
    @ColumnInfo(name = "finish_time")
    private String finishTime;
    private String latitude;
    private String longitude;
    private int color;

    @Ignore
    public ActivityEntry(int id, String name, boolean active, boolean rateTask, boolean notification) {
        this.id = id;
        this.name = name;
        this.active = active;
        this.rateTask = rateTask;
        this.notification = notification;
        this.startTime = null;
        this.finishTime = null;
        this.latitude = null;
        this.longitude = null;
        this.color = 0;
    }

    public ActivityEntry(int id, String name, boolean active, boolean rateTask, boolean notification, String startTime, String finishTime, String latitude, String longitude, int color) {
        this.id = id;
        this.name = name;
        this.active = active;
        this.rateTask = rateTask;
        this.notification = notification;
        this.startTime = startTime;
        this.finishTime = finishTime;
        this.latitude = latitude;
        this.longitude = longitude;
        this.color = color;
    }

    public boolean isLinkToTimetable() {
        return !TextUtils.isEmpty(startTime) && !TextUtils.isEmpty(finishTime);
    }

    public boolean isLinkToLocation() {
        return !TextUtils.isEmpty(latitude) && !TextUtils.isEmpty(longitude);
    }

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

    public boolean isActive() {
        return active;
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

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getFinishTime() {
        return finishTime;
    }

    public void setFinishTime(String finishTime) {
        this.finishTime = finishTime;
    }

    public String getLatitude() {
        return latitude;
    }

    public double getLatitudeDouble() {
        if (TextUtils.isEmpty(latitude)) {
            return 0;
        }
        return Double.parseDouble(latitude);
    }

    public void setLatitude(double latitude) {
        setLatitude(String.valueOf(latitude));
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public double getLongitudeDouble() {
        if (TextUtils.isEmpty(longitude)) {
            return 0;
        }
        return Double.parseDouble(longitude);
    }

    public void setLongitude(double longitude) {
        setLongitude(String.valueOf(longitude));
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
