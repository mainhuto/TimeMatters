package com.example.android.timematters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class TaskLogCollectionAdapter extends FragmentStateAdapter {

    private final int mActivityId;

    public TaskLogCollectionAdapter(@NonNull Fragment fragment, int activityId) {
        super(fragment);
        mActivityId = activityId;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        int days = 0;
        switch (position) {
            case 0:
                days = 1;
                break;
            case 1:
                days = 7;
                break;
            case 2:
                days = 30;
                break;
        }
        return TaskLogsFragment.newInstance(mActivityId, days);
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}
