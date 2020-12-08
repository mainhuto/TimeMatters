package com.example.android.timematters;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.example.android.timematters.database.ActivityEntry;
import com.example.android.timematters.database.AppDatabase;
import com.example.android.timematters.database.DbExecutor;
import com.example.android.timematters.utils.ValidationDialog;
import com.example.android.timematters.viewmodel.ActivityEntryViewModel;
import com.example.android.timematters.viewmodel.ActivityEntryViewModelFactory;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class ActivityEntrySettingsActivity extends AppCompatActivity implements TimePickerFragment.NoticeDialogListener, OnMapReadyCallback {

    private static final String TAG = "ActivityEntrySettingsAc";

    public static final String ARG_ACTIVITY_ID = "activity_id";
    public static final String START_TIME_PICKER_TAG = "start_time";
    public static final String FINISH_TIME_PICKER_TAG = "finish_time";
    private static final int PERMISSION_REQUEST_CODE = 1;
    private static final int DEFAULT_ZOOM = 15;


    private AppDatabase mDb;
    private ActivityEntry mActivityEntry;

    private SwitchCompat mRateSwitch;
    private SwitchCompat mNotificationSwitch;
    private SwitchCompat mTimetableSwitch;
    private SwitchCompat mLocationSwitch;
    private ConstraintLayout mTimetableConstraintLayout;
    private ConstraintLayout mLocationConstraintLayout;

    private ConstraintSet mConstraintSet1;
    private ConstraintSet mConstraintSet2;

    private Button mStartTimeButton;
    private Button mFinishTimeButton;

    private GoogleMap mMap;
    private Marker mMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_activity);

        initViews();

        mDb = AppDatabase.getInstance(this);

        Intent intent = getIntent();
        if ( (intent != null) && intent.hasExtra(ARG_ACTIVITY_ID) ) {
            int activityId = getIntent().getIntExtra(ARG_ACTIVITY_ID, 0);
            ActivityEntryViewModelFactory factory = new ActivityEntryViewModelFactory(mDb, activityId);
            final ActivityEntryViewModel viewModel = new ViewModelProvider(this, factory).get(ActivityEntryViewModel.class);
            viewModel.getActivityEntry().observe(this, new Observer<ActivityEntry>() {
                @Override
                public void onChanged(@Nullable ActivityEntry activityEntry) {
                    viewModel.getActivityEntry().removeObserver(this);
                    mActivityEntry = activityEntry;
                    populateUI();
                }
            });

        }

    }

    @Override
    public boolean onSupportNavigateUp() {
        if (validate()) {
            finish();
        }
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        updateActivityEntry();
    }

    private void initViews() {

        mTimetableConstraintLayout = findViewById(R.id.timetable_cl);
        mConstraintSet1 = new ConstraintSet();
        mConstraintSet1.clone(mTimetableConstraintLayout);

        mLocationConstraintLayout = findViewById(R.id.location_cl);
        mConstraintSet2 = new ConstraintSet();
        mConstraintSet2.clone(mLocationConstraintLayout);

        mRateSwitch = findViewById(R.id.rate_switch);
        mNotificationSwitch = findViewById(R.id.notification_switch);

        mTimetableSwitch = findViewById(R.id.timetable_switch);
        mTimetableSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                Log.d(TAG, "onCheckedChanged: " + checked);
                if (checked) {
                    mTimetableConstraintLayout.setVisibility(View.VISIBLE);
                } else {
                    mTimetableConstraintLayout.setVisibility(View.GONE);
                }
            }
        });

        mLocationSwitch = findViewById(R.id.location_switch);
        mLocationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                Log.d(TAG, "onCheckedChanged: " + checked);
                if ( (checked) && checkLocationPermission() ) {
                    requestMap();
                    mLocationConstraintLayout.setVisibility(View.VISIBLE);
                    if (mTimetableConstraintLayout.getVisibility() == View.VISIBLE) {
                        Log.d(TAG, "onCheckedChanged: timetable is visible" );
                        mConstraintSet2.applyTo(mLocationConstraintLayout);
                    } else {
                        Log.d(TAG, "onCheckedChanged: timetable is not visible" );
                        mConstraintSet1.applyTo(mLocationConstraintLayout);
                    }
                } else {
                    mLocationConstraintLayout.setVisibility(View.GONE);
                }
            }
        });

        mStartTimeButton = findViewById(R.id.start_time_button);
        mStartTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTimePickerDialog(START_TIME_PICKER_TAG);
            }
        });

        mFinishTimeButton = findViewById(R.id.finish_time_button);
        mFinishTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTimePickerDialog(FINISH_TIME_PICKER_TAG);
            }
        });

    }

    private void populateUI() {
        mRateSwitch.setChecked(mActivityEntry.isRateTask());
        mNotificationSwitch.setChecked(mActivityEntry.isNotification());
        if (mActivityEntry.isLinkToTimetable()) {
            mTimetableSwitch.setChecked(true);
            mStartTimeButton.setText(mActivityEntry.getStartTime());
            mFinishTimeButton.setText(mActivityEntry.getFinishTime());
        } else {
            mTimetableSwitch.setChecked(false);
        }
        if (mActivityEntry.isLinkToLocation()) {
            mLocationSwitch.setChecked(true);
        } else {
            mLocationSwitch.setChecked(false);
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(mActivityEntry.getName() + " settings");

    }


    private boolean validate() {
        if ( mTimetableSwitch.isChecked() && mStartTimeButton.getText().toString().equals("00:00") && mFinishTimeButton.getText().toString().equals("00:00") ) {
            ValidationDialog validationDialog = ValidationDialog.newInstance(getString(R.string.timetable_mandatory));
            validationDialog.show(getSupportFragmentManager(), null);
            return false;
        } else {
            if ( mLocationSwitch.isChecked() && (mMarker == null) ) {
                ValidationDialog validationDialog = ValidationDialog.newInstance(getString(R.string.marker_mandatory));
                validationDialog.show(getSupportFragmentManager(), null);
                return false;
            }
        }
        return true;
    }

    private void updateActivityEntry() {
        mActivityEntry.setRateTask(mRateSwitch.isChecked());
        mActivityEntry.setNotification(mNotificationSwitch.isChecked());
        if (mTimetableSwitch.isChecked()) {
            mActivityEntry.setStartTime(mStartTimeButton.getText().toString());
            mActivityEntry.setFinishTime(mFinishTimeButton.getText().toString());
        } else {
            mActivityEntry.setStartTime(null);
            mActivityEntry.setFinishTime(null);
        }
        if (mLocationSwitch.isChecked() && (mMarker != null)) {
            mActivityEntry.setLatitude(mMarker.getPosition().latitude);
            mActivityEntry.setLongitude(mMarker.getPosition().longitude);
        } else {
            mActivityEntry.setLatitude(null);
            mActivityEntry.setLongitude(null);
        }
        DbExecutor.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                mDb.activityDao().updateActivity(mActivityEntry);
            }
        });
    }

    public void showTimePickerDialog(String tag) {
        DialogFragment newFragment = new TimePickerFragment();
        newFragment.show(getSupportFragmentManager(), tag);
    }

    @Override
    public void onTimePicked(String tag, String time) {
        switch (tag) {
            case START_TIME_PICKER_TAG:
                mStartTimeButton.setText(time);
                break;
            case FINISH_TIME_PICKER_TAG:
                mFinishTimeButton.setText(time);
                break;
        }
    }

    private boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "checkLocationPermission: Location permission granted" );
            return true;
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[] { Manifest.permission.ACCESS_FINE_LOCATION }, PERMISSION_REQUEST_CODE);
                mLocationSwitch.setChecked(false);
            }
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                // If request is cancelled, the result arrays are empty.
                if ( (grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED) ) {
                    mLocationSwitch.setChecked(true);
                }  else {
                    Toast.makeText(this, getString(R.string.need_location_permission_message), Toast.LENGTH_LONG).show();
                    mLocationSwitch.setChecked(false);
                }
        }
    }

    private void requestMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;

        // Add a marker to the location linked to the activity
        LatLng location;
        if (mActivityEntry.isLinkToLocation()) {
            location = new LatLng(mActivityEntry.getLatitudeDouble(),mActivityEntry.getLongitudeDouble());
            mMarker =  mMap.addMarker(new MarkerOptions().position(location).title(getString(R.string.activity_location_tag)));
        } else {
            location = new LatLng(-34, 151);
        }
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, DEFAULT_ZOOM));

        // Enable the zoom controls and MyLocation for the map
        mMap.getUiSettings().setZoomControlsEnabled(true);

        // If location permission granted, enable MyLocation for the map
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }
//        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
//            @Override
//            public boolean onMarkerClick(Marker marker) {
//                return false;
//            }
//        });

        // Set on map click listener
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                Log.d(TAG, "onMapClick: "+ latLng);
                if (mMarker != null) {
                    mMarker.remove();
                }
                mMarker = mMap.addMarker(new MarkerOptions().position(latLng).title(getString(R.string.activity_location_tag)));
                Log.d(TAG, "onMapClick: " + mMarker.getId() + " - " + mMarker.getSnippet());

            }
        });

    }
}

