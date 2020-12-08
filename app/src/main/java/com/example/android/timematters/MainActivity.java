package com.example.android.timematters;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;

import com.example.android.timematters.database.ActivityEntry;
import com.example.android.timematters.database.AppDatabase;
import com.example.android.timematters.database.DbExecutor;
import com.example.android.timematters.utils.AppResources;
import com.example.android.timematters.utils.AppUtils;
import com.example.android.timematters.utils.TmActivityInfo;
import com.example.android.timematters.viewmodel.MainViewModel;
import com.facebook.stetho.Stetho;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.view.View;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity implements ActivityAdapter.ActivityAdapterOnClickHandler {

    private static final String TAG = "MainActivity";

    private static final double DISTANT_MARGIN = 100;
    private static final int CLEAN_UP_DAYS = 30;

    private AppDatabase mDb;
    private FloatingActionButton mFab;
    private MainViewModel mViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick back arrow icon: ");
                setActiveActivities(true);
                replaceActivitiesFragment();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        Stetho.initializeWithDefaults(this);

        checkDataBase();

        setupViewModel();

        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment activityListFragment = fragmentManager.findFragmentById(R.id.activity_list_container);
        if (activityListFragment == null) {
            activityListFragment = ActivitiesFragment.newInstance(mViewModel.isActiveActivities());
            fragmentManager.beginTransaction()
                    .add(R.id.activity_list_container, activityListFragment)
                    .commit();
        }

        mFab = findViewById(R.id.fab);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setActiveActivities(false);
                replaceActivitiesFragment();
            }
        });

        if (!mViewModel.isActiveActivities()) {
            setupSelectionScreen();
        }

        // Register notification chanel
        AppUtils.createNotificationChannel(this);

    }

    private void replaceActivitiesFragment() {
        if (mViewModel.isActiveActivities()) {
            mFab.show();
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setTitle(R.string.app_name);
        } else {
            setupSelectionScreen();
        }
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment activityListFragment = ActivitiesFragment.newInstance(mViewModel.isActiveActivities());
        fragmentManager.beginTransaction()
                .replace(R.id.activity_list_container, activityListFragment)
                .commit();
    }

    private void setupSelectionScreen() {
        mFab.hide();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.select_activity);
        getSupportActionBar().setBackgroundDrawable(ContextCompat.getDrawable(getApplicationContext(), R.color.colorAccent));
    }

    private void checkDataBase() {

        mDb = AppDatabase.getInstance(getApplicationContext());

        LiveData<List<ActivityEntry>> activities = mDb.activityDao().loadAllActivities();
        activities.observe(this, new Observer<List<ActivityEntry>>() {
            @Override
            public void onChanged(List<ActivityEntry> activityEntries) {
                if ( (activityEntries == null) || (activityEntries.size() == 0) ) {
                    DbExecutor.getInstance().diskIO().execute(new Runnable() {
                        @Override
                        public void run() {
                            ActivityEntry activityEntry = new ActivityEntry(ActivityType.FITNESS.ordinal(), getString(R.string.activity_1_name), false, false, false);
                            activityEntry.setColor(AppResources.getInstance().getActivityColor(activityEntry.getId()));
                            mDb.activityDao().insertActivity(activityEntry);
                            activityEntry = new ActivityEntry(ActivityType.STUDY.ordinal(), getString(R.string.activity_2_name), false, false, false);
                            activityEntry.setColor(AppResources.getInstance().getActivityColor(activityEntry.getId()));
                            mDb.activityDao().insertActivity(activityEntry);
                            activityEntry = new ActivityEntry(ActivityType.HOBBIES.ordinal(), getString(R.string.activity_3_name), false, false, false);
                            activityEntry.setColor(AppResources.getInstance().getActivityColor(activityEntry.getId()));
                            mDb.activityDao().insertActivity(activityEntry);
                            activityEntry = new ActivityEntry(ActivityType.WORK.ordinal(), getString(R.string.activity_4_name), false, false, false);
                            activityEntry.setColor(AppResources.getInstance().getActivityColor(activityEntry.getId()));
                            mDb.activityDao().insertActivity(activityEntry);
                        }
                    });
                }
            }
        });

        // Clean expired task log entries
        Log.d(TAG, "checkDataBase: last clean date: " + AppUtils.getProperty(this, AppUtils.PROPERTY_LAST_CLEAN_DATE));
        if (AppUtils.isCleanDatabase(this)) {

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            calendar.add(Calendar.DATE, (CLEAN_UP_DAYS -1) * -1);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            final Date date = calendar.getTime();
            Log.d(TAG, "checkDataBase: Clean expired task log entries with date < " + date + " - " + date.getTime());

            DbExecutor.getInstance().diskIO().execute(new Runnable() {
                @Override
                public void run() {
                    mDb.taskLogDao().deleteTaskLogByDate(date);
                    AppUtils.setLastCleanDate(MainActivity.this, new Date());
                    Log.d(TAG, "checkDataBase: new last clean date: " + AppUtils.getProperty(MainActivity.this, AppUtils.PROPERTY_LAST_CLEAN_DATE));
                }
            });

        }

    }

    private void setupViewModel() {

        mViewModel = new ViewModelProvider(this).get(MainViewModel.class);
        mViewModel.getActivities().observe(this, new Observer<List<ActivityEntry>>() {
            @Override
            public void onChanged(List<ActivityEntry> activityEntries) {
                Log.d("ViewModelTest", "onChanged: Non active activities");
                if ( !mViewModel.isActiveActivities() || (activityEntries.size() == 0) ) {
                    mFab.hide();
                } else {
                    mFab.show();
                }
            }
        });
        mViewModel.getLinkedActivities().observe(this, new Observer<List<ActivityEntry>>() {
            @Override
            public void onChanged(List<ActivityEntry> linkedActivities) {
                Log.d("ViewModelTest", "onChanged: Activities with links");
                mViewModel.getLinkedActivities().removeObserver(this);
                checkLinkedActivities(linkedActivities);
           }
        });
        Log.d("ViewModelTest", "setupViewModel(): mActiveActivities=" + mViewModel.isActiveActivities());
    }

    @Override
    public void onClick(final ActivityEntry activityEntry) {
        if (mViewModel.isActiveActivities()) {
            Log.d(TAG, "onClick: start working with " + activityEntry.getName());
            startTaskLogActivity(activityEntry);
        } else {
            Log.d(TAG, "onClick: activating " + activityEntry.getName());
            setActiveActivities(true);
            activityEntry.setActive(true);
            DbExecutor.getInstance().diskIO().execute(new Runnable() {
                @Override
                public void run() {
                    mDb.activityDao().updateActivity(activityEntry);
                }
            });
            replaceActivitiesFragment();
        }
    }

    private void startTaskLogActivity(ActivityEntry activityEntry) {
        TmActivityInfo activityInfo = TmActivityInfo.newActivityInfo(activityEntry);
        Intent intent = new Intent(this, TaskLogActivity.class);
        Bundle args = new Bundle();
        args.putParcelable(TaskLogActivity.ARG_ACTIVITY_INFO, activityInfo);
        intent.putExtras(args);
        startActivity(intent);
    }

    public void setActiveActivities(boolean active) {
        mViewModel.setActiveActivities(active);
        getSupportActionBar().setBackgroundDrawable(ContextCompat.getDrawable(getApplicationContext(), R.color.colorPrimary));
    }

    private void checkLinkedActivities(List<ActivityEntry> linkedActivities) {
        List<ActivityEntry> timeLinkedActivities = new ArrayList<>();
        boolean checkLocation = false;
        for (ActivityEntry linkedActivity: linkedActivities) {
            Log.d(TAG, "checkLinkedActivities: name=" + linkedActivity.getName() + " - start=" + linkedActivity.getStartTime() + " - finish=" + linkedActivity.getFinishTime() + " - latitude=" + linkedActivity.getLatitude() + " - longitude=" + linkedActivity.getLongitude());
            if ( linkedActivity.getStartTime() != null ) {
                if (matchCurrentTime(linkedActivity)) {
                    Log.d(TAG, "checkLinkedActivities: Match time");
                    timeLinkedActivities.add(linkedActivity);
                    if (linkedActivity.isLinkToLocation()) {
                        checkLocation = true;
                    }
                }
            } else {
                if (linkedActivity.isLinkToLocation()) {
                    timeLinkedActivities.add(linkedActivity);
                    checkLocation = true;
                }
            }
        }
        if (timeLinkedActivities.size() == 1) {
            if (checkLocation) {
                Log.d(TAG, "checkLinkedActivities: Check location for " + timeLinkedActivities.get(0).getName());
                checkLocation(timeLinkedActivities);
            } else {
                Log.d(TAG, "checkLinkedActivities: Start Activity Tasks log for " + timeLinkedActivities.get(0).getName());
                startTaskLogActivity(timeLinkedActivities.get(0));
            }
        } else {
            if ( (timeLinkedActivities.size() > 1) && checkLocation ) {
                Log.d(TAG, "checkLinkedActivities: Check location for " + timeLinkedActivities.size() +  " activities");
                checkLocation(timeLinkedActivities);
            } else {
                Log.d(TAG, "checkLinkedActivities: No activity matches for auto start");
            }
        }
    }

    private boolean matchCurrentTime(ActivityEntry activityEntry) {
        try {
            Date startTime = new SimpleDateFormat("HH:mm").parse(activityEntry.getStartTime());
            Calendar calendarStart = Calendar.getInstance();
            calendarStart.setTime(startTime);
            calendarStart.add(Calendar.DATE, 1);

            Date finishTime = new SimpleDateFormat("HH:mm").parse(activityEntry.getFinishTime());
            Calendar calendarFinish = Calendar.getInstance();
            calendarFinish.setTime(finishTime);
            calendarFinish.add(Calendar.DATE, 1);

            SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
            Date date = new Date();
            String time = formatter.format(date);
            Date CurrentTime = new SimpleDateFormat("HH:mm:ss").parse(time);
            Calendar calendarCurrent = Calendar.getInstance();
            calendarCurrent.setTime(CurrentTime);
            calendarCurrent.add(Calendar.DATE, 1);

            if (calendarCurrent.after(calendarStart) && calendarCurrent.before(calendarFinish)) {
                return true;
            }
        } catch ( ParseException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void checkLocation(final List<ActivityEntry> linkedActivities) {
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                Log.d(TAG, "checkLinkedActivities: Current location=" + location.getLatitude() + " - " + location.getLongitude());
                                Location currentLocation = new Location("");
                                currentLocation.setLatitude(location.getLatitude());
                                currentLocation.setLongitude(location.getLongitude());

                                ActivityEntry startActivity = null;
                                for (ActivityEntry linkedActivity: linkedActivities) {
                                    float dist = 0;
                                    if (linkedActivity.isLinkToLocation()) {
                                        Log.d(TAG, "checkLinkedActivities: " + linkedActivity.getName() + " location=" + linkedActivity.getLatitude() + " - " + linkedActivity.getLongitude());
                                        Location activityLocation   = new Location("");
                                        activityLocation.setLatitude(linkedActivity.getLatitudeDouble());
                                        activityLocation.setLongitude(linkedActivity.getLongitudeDouble());
                                        dist = activityLocation.distanceTo(currentLocation);
                                        Log.d(TAG, "checkLinkedActivities: dist=" + dist);
                                    } else {
                                        Log.d(TAG, "checkLinkedActivities: " + linkedActivity.getName() + " no location linked");
                                    }
                                    if (dist < DISTANT_MARGIN) {
                                        if (startActivity == null) {
                                            startActivity = linkedActivity;
                                        } else {
                                            startActivity = null;
                                            Log.d(TAG, "checkLinkedActivities: More than 1 activity matches location");
                                            break;
                                        }
                                    }
                                }
                                if (startActivity != null) {
                                    Log.d(TAG, "checkLinkedActivities: Start Activity Tasks log for " + startActivity.getName());
                                    startTaskLogActivity(startActivity);
                                }

                            } else {
                                Log.d(TAG, "checkLinkedActivities: Current location not retrieved");
                            }
                        }
                    });
        }
    }

}