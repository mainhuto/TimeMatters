package com.example.android.timematters.viewmodel;

import com.example.android.timematters.database.AppDatabase;
import com.example.android.timematters.database.CategoryEntry;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

public class CategoryViewModel extends ViewModel {

    private LiveData<CategoryEntry> mCategoryEntry;
    private Integer mColor;
    private Integer mIcon;
    private Integer mHoursDuration;
    private Integer mMinutesDuration;
    private boolean mNotificationActive;

    public CategoryViewModel(AppDatabase database, @Nullable Integer categoryId) {
        if (categoryId != null) {
            mCategoryEntry = database.categoryDao().loadCategoryById(categoryId);
        }
        if ( (mCategoryEntry != null) &&  (mCategoryEntry.getValue() != null ) ) {
            mColor = mCategoryEntry.getValue().getColor();
            mIcon = mCategoryEntry.getValue().getIcon();
            mHoursDuration = mCategoryEntry.getValue().getHoursDuration();
            mMinutesDuration = mCategoryEntry.getValue().getMinutesDuration();
            mNotificationActive = mCategoryEntry.getValue().isNotification();
        }
    }

    public LiveData<CategoryEntry> getCategoryEntry() {
        return mCategoryEntry;
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
