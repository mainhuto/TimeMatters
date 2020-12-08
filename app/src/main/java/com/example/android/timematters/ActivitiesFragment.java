package com.example.android.timematters;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.timematters.database.ActivityEntry;
import com.example.android.timematters.database.AppDatabase;
import com.example.android.timematters.database.DbExecutor;
import com.example.android.timematters.viewmodel.ActivityListViewModel;
import com.example.android.timematters.viewmodel.ActivityListViewModelFactory;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

public class ActivitiesFragment extends Fragment {

    public static final String ARG_ACTIVE_ACTIVITIES = "active_activities";

    private AppDatabase mDb;
    private ActivityAdapter mActivityAdapter;
    private boolean mActiveActivities;
    private TextView mNoActivitiesTextView;

    private ActivityAdapter.ActivityAdapterOnClickHandler mClickHandler;

    public static ActivitiesFragment newInstance(boolean activeActivities) {
        ActivitiesFragment fragment = new ActivitiesFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_ACTIVE_ACTIVITIES, activeActivities);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mActiveActivities = getArguments().getBoolean(ARG_ACTIVE_ACTIVITIES);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView =  inflater.inflate(R.layout.fragment_activities, container, false);

        mNoActivitiesTextView = rootView.findViewById(R.id.no_activities_tv);
//        RecyclerView recyclerView = rootView.findViewById(R.id.activity_list_rv);

        RecyclerView activitiesRecyclerView = rootView.findViewById(R.id.activity_list_rv);
        Activity activity = getActivity();
        if (activity != null) {

            mDb = AppDatabase.getInstance(activity.getApplicationContext());

            GridLayoutManager layoutManager = new GridLayoutManager(activity.getApplicationContext(), 1);
            activitiesRecyclerView.setLayoutManager(layoutManager);
            mActivityAdapter = new ActivityAdapter(mClickHandler);
            activitiesRecyclerView.setAdapter(mActivityAdapter);

            // If the fragment is showing the active activities attach a touch helper to deactivate when a user swipes
            if (mActiveActivities) {
                new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
                    @Override
                    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                        return false;
                    }

                    @Override
                    public void onSwiped(@NonNull final RecyclerView.ViewHolder viewHolder, int swipeDir) {
                        int position = viewHolder.getAdapterPosition();
                        List<ActivityEntry> activities = mActivityAdapter.getActivities();
                        final ActivityEntry activityEntry = activities.get(position);
                        activityEntry.setActive(false);
                        DbExecutor.getInstance().diskIO().execute(new Runnable() {
                            @Override
                            public void run() {
                                mDb.activityDao().updateActivity(activityEntry);
                            }
                        });
                    }
                }).attachToRecyclerView(activitiesRecyclerView);
            }

            setupActivityListViewModel(mActiveActivities);

        }

        return rootView;
    }

    private void showAddActivityMessage() {
        mNoActivitiesTextView.setVisibility(View.VISIBLE);
    }

    private void hideAddActivityMessage() {
        mNoActivitiesTextView.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof Activity){
            this.mClickHandler = (ActivityAdapter.ActivityAdapterOnClickHandler) context;
        }
    }

    private void setupActivityListViewModel(final Boolean byActive) {
        ActivityListViewModelFactory factory = new ActivityListViewModelFactory(mDb, byActive);
        ActivityListViewModel viewModel = new ViewModelProvider(this, factory).get(ActivityListViewModel.class);
        viewModel.getActivities().observe(getViewLifecycleOwner(), new Observer<List<ActivityEntry>>() {
            @Override
            public void onChanged(List<ActivityEntry> activityEntries) {
                Log.d(ActivityListViewModel.TAG, "onChanged: list activities");
                mActivityAdapter.setActivities(activityEntries);
                if (activityEntries.size() == 0) {
                    showAddActivityMessage();
                } else {
                    hideAddActivityMessage();
                }
            }
        });
    }

}