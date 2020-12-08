package com.example.android.timematters.viewmodel;

import com.example.android.timematters.database.AppDatabase;
import com.example.android.timematters.database.TaskEntry;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

public class TaskViewModel extends ViewModel {

    private LiveData<TaskEntry> mTaskEntry;
    private Integer mCategoryId;
    private Integer mColor;
    private Integer mIcon;
    private Integer mHoursDuration;
    private Integer mMinutesDuration;
    private boolean mNotificationActive;


    public TaskViewModel(AppDatabase database, @Nullable Integer taskId) {
        if (taskId != null) {
            mTaskEntry = database.taskDao().loadTaskById(taskId);
            if ( (mTaskEntry != null) &&  (mTaskEntry.getValue() != null ) ) {
                mCategoryId = mTaskEntry.getValue().getCategoryId();
                mColor = mTaskEntry.getValue().getColor();
                mIcon = mTaskEntry.getValue().getIcon();
                mHoursDuration = mTaskEntry.getValue().getHoursDuration();
                mMinutesDuration = mTaskEntry.getValue().getMinutesDuration();
                mNotificationActive = mTaskEntry.getValue().isNotification();
            }
        }
    }

    public LiveData<TaskEntry> getTaskEntry() {
        return mTaskEntry;
    }

    public boolean isCategorySet() {
        return mCategoryId != null;
    }

    public Integer getCategoryId() {
        return mCategoryId;
    }

    public void setCategoryId(Integer categoryId) {
        mCategoryId = categoryId;
    }

    public Integer getColor() {
        return mColor;
    }

    public void setColor(Integer color) {
        mColor = color;
    }

    public Integer getIcon() {
        return mIcon;
    }

    public void setIcon(Integer icon) {
        mIcon = icon;
    }

    public Integer getHoursDuration() {
        return mHoursDuration;
    }

    public void setHoursDuration(Integer hoursDuration) {
        mHoursDuration = hoursDuration;
    }

    public Integer getMinutesDuration() {
        return mMinutesDuration;
    }

    public void setMinutesDuration(Integer minutesDuration) {
        mMinutesDuration = minutesDuration;
    }

    public boolean isNotificationActive() {
        return mNotificationActive;
    }

    public void setNotificationActive(boolean notificationActive) {
        mNotificationActive = notificationActive;
    }
}
