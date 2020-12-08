package com.example.android.timematters;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

public class CollectionTaskLogFragment extends Fragment {

    private static final String ARG_ACTIVITY_ID = "activity_id";

    // When requested, this adapter returns a DemoObjectFragment,
    // representing an object in the collection.
    TaskLogCollectionAdapter demoCollectionAdapter;
    ViewPager2 mViewPager;

    private int mActivityId;

    private TabLayout.OnTabSelectedListener mOnTabSelectedListener;

    public static CollectionTaskLogFragment newInstance(int activityId) {
        CollectionTaskLogFragment fragment = new CollectionTaskLogFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_ACTIVITY_ID, activityId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mActivityId = getArguments().getInt(ARG_ACTIVITY_ID);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.collection_task_log, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        demoCollectionAdapter = new TaskLogCollectionAdapter(this, mActivityId);
        mViewPager = view.findViewById(R.id.pager);
        mViewPager.setAdapter(demoCollectionAdapter);

        TabLayout tabLayout = view.findViewById(R.id.tab_layout);
        tabLayout.addOnTabSelectedListener(mOnTabSelectedListener);

        TabLayoutMediator tabLayoutMediator = new TabLayoutMediator(tabLayout, mViewPager, true, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {

                switch (position) {
                    case 0:
                        tab.setTag(1);
                        tab.setText(R.string.tab_today_text);
                        break;
                    case 1:
                        tab.setTag(7);
                        tab.setText(R.string.tab_week_text);
                        break;
                    case 2:
                        tab.setTag(30);
                        tab.setText(R.string.tab_month_text);
                        break;
                }
            }
        });
        tabLayoutMediator.attach();

    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof Activity) {
            mOnTabSelectedListener = (TabLayout.OnTabSelectedListener) context;
        }

    }
}


