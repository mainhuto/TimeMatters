package com.example.android.timematters;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.NumberPicker;

import com.example.android.timematters.database.AppDatabase;
import com.example.android.timematters.database.CategoryEntry;
import com.example.android.timematters.database.DbExecutor;
import com.example.android.timematters.utils.AppResources;
import com.example.android.timematters.utils.PickerDialog;
import com.example.android.timematters.utils.TmActivityInfo;
import com.example.android.timematters.utils.ValidationDialog;
import com.example.android.timematters.viewmodel.CategoryViewModel;
import com.example.android.timematters.viewmodel.CategoryViewModelFactory;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

public class CategoryDetailActivity extends AppCompatActivity implements PickerDialog.PickerDialogListener {

    private static final String TAG = "CategoryDetailActivity";

    public static final String ARG_ACTION = "action";
    public static final String ARG_CATEGORY_ID = "category_id";
    public static final String ARG_ACTIVITY_INFO = "activity_info";

    public enum Action {INSERT, UPDATE, DELETE}

    private AppDatabase mDb;
    private Action mAction;
    private Integer mCategoryId;
    private TmActivityInfo mActivityInfo;
    private CategoryEntry mCategoryEntry;
    private int mCategoryColorId;
    private int mCategoryIconId;
    private boolean mSaveEntry;

    private EditText mCategoryNameEditText;
    private SwitchCompat mNotificationSwitch;
    private NumberPicker mHourPiker;
    private NumberPicker mMinutePiker;
    private ImageView mColorImageView;
    private ImageView mIconImageView;

    private CategoryViewModel mViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_detail);

        mDb = AppDatabase.getInstance(this);

        mSaveEntry = false;

        Intent intent = getIntent();
        if (intent != null) {
            if ( intent.hasExtra(ARG_ACTION) ) {
                int intAction = getIntent().getIntExtra(ARG_ACTION, 0);
                if (Action.values().length > intAction) {
                    mAction = Action.values()[intAction];
                }
            }
            if ( intent.hasExtra(ARG_ACTIVITY_INFO) ) {
                mActivityInfo = getIntent().getParcelableExtra(ARG_ACTIVITY_INFO);
                setTitle();
            }
            if ( intent.hasExtra(ARG_CATEGORY_ID) ) {
                mCategoryId = getIntent().getIntExtra(ARG_CATEGORY_ID, 0);
            }

            CategoryViewModelFactory factory = new CategoryViewModelFactory(mDb, mCategoryId);
            mViewModel = new ViewModelProvider(this, factory).get(CategoryViewModel.class);

            initViews();

            if (mViewModel.getColor() != null) {
                restoreUI();
            } else {
                if (Action.INSERT.equals(mAction)) {
                    Log.d(TAG, "onCreate: set color to 0");
                    changeColor(0);
                    mViewModel.setNotificationActive(mActivityInfo.isNotification());
                }
            }

            if ( Action.UPDATE.equals(mAction) ) {
                mViewModel.getCategoryEntry().observe(this, new Observer<CategoryEntry>() {
                    @Override
                    public void onChanged(@Nullable CategoryEntry categoryEntry) {
                        mViewModel.getCategoryEntry().removeObserver(this);
                        if (categoryEntry != null) {
                            mCategoryEntry = categoryEntry;
                            if (mViewModel.getColor() == null) {
                                populateUI();
                            }
                        }
                    }
                });
            }
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_category_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        Log.d(TAG, "onOptionsItemSelected: " + id + " - " + item.getTitle());
        switch (id) {
            case R.id.action_accept:
                if (validate()) {
                    mSaveEntry = true;
                    finish();
                }
                break;
            case R.id.action_cancel:
                mSaveEntry = false;
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if ( mSaveEntry ) {
            switch (mAction) {
                case INSERT:
                    insertCategoryEntry();
                    break;
                case UPDATE:
                    updateCategoryEntry();
                    break;
            }
        }
    }

    @Override
    public void onPickerDialogPositiveClick(int position, String tag) {
        if (PickerDialog.PickerType.COLOR.name().equals(tag)) {
            changeColor(position);
        } else {
            if (PickerDialog.PickerType.ICON.name().equals(tag)) {
                changeIcon(position);
            }
        }
    }

    @Override
    public void onPickerDialogNegativeClick(String tag) {}

    private void initViews() {
        Log.d(TAG, "initViews: starts");
        mCategoryNameEditText = findViewById(R.id.category_name_pt);
        mNotificationSwitch = findViewById(R.id.notification_switch);
        mNotificationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean active) {
                Log.d(TAG, "onCheckedChanged: " + active);
                mViewModel.setNotificationActive(active);
            }
        });
        mHourPiker = findViewById(R.id.hour_picker_np);
        mHourPiker.setMaxValue(23);
        mHourPiker.setMinValue(0);
        mHourPiker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int oldValue, int newValue) {
                Log.d(TAG, "onValueChange: " + oldValue + " - " + newValue);
                mViewModel.setHoursDuration(newValue);
                checkNotificationSwitch();
            }
        });
        mMinutePiker = findViewById(R.id.minute_picker_np);
        mMinutePiker.setMaxValue(59);
        mMinutePiker.setMinValue(0);
        mMinutePiker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int oldValue, int newValue) {
                Log.d(TAG, "onValueChange: " + oldValue + " - " + newValue);
                mViewModel.setMinutesDuration(newValue);
                checkNotificationSwitch();
            }
        });
        mColorImageView = findViewById(R.id.category_color_iv);
        mColorImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment newFragment = PickerDialog.newInstance(PickerDialog.PickerType.COLOR, mActivityInfo.getId());
                newFragment.show(getSupportFragmentManager(), PickerDialog.PickerType.COLOR.name());
            }
        });
        mIconImageView = findViewById(R.id.category_icon_iv);
        mIconImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment newFragment = PickerDialog.newInstance(PickerDialog.PickerType.ICON, mActivityInfo.getId());
                newFragment.show(getSupportFragmentManager(), PickerDialog.PickerType.ICON.name());
            }
        });

        checkNotificationSwitch();

        String subtitle;
        if (Action.INSERT.equals(mAction)) {
            subtitle = getString(R.string.new_category_title);
        } else {
            subtitle = getString(R.string.category_detail_title);
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setSubtitle(subtitle);
        }


    }

    private void populateUI() {
        Log.d(TAG, "populateUI: starts");
        mCategoryNameEditText.setText(mCategoryEntry.getName());
        mNotificationSwitch.setChecked(mCategoryEntry.isNotification());
        mHourPiker.setValue(mCategoryEntry.getHoursDuration());
        mViewModel.setHoursDuration(mCategoryEntry.getHoursDuration());
        mMinutePiker.setValue(mCategoryEntry.getMinutesDuration());
        mViewModel.setMinutesDuration(mCategoryEntry.getMinutesDuration());
        checkNotificationSwitch();
        changeColor(mCategoryEntry.getColor());
        changeIcon(mCategoryEntry.getIcon());
    }

    private void  restoreUI() {
        Log.d(TAG, "restoreUI: starts");
        if (mViewModel.getColor() != null) {
            changeColor(mViewModel.getColor());
        }
        if (mViewModel.getIcon() != null) {
            changeIcon(mViewModel.getIcon());
        }
        if (mViewModel.getHoursDuration() != null) {
            mHourPiker.setValue(mViewModel.getHoursDuration());
        }
        if (mViewModel.getMinutesDuration() != null) {
            mMinutePiker.setValue(mViewModel.getMinutesDuration());
        }
        checkNotificationSwitch();
    }

    private void setTitle() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(mActivityInfo.getName());
        }
    }

    private boolean validate() {
        if (mCategoryNameEditText.getText().toString().isEmpty()) {
            ValidationDialog validationDialog = ValidationDialog.newInstance(getString(R.string.category_name_mandatory));
            validationDialog.show(getSupportFragmentManager(), null);
            return false;
        }
        return true;
    }

    private void updateCategoryEntry() {
        mCategoryEntry.setName(mCategoryNameEditText.getText().toString());
        mCategoryEntry.setNotification(mNotificationSwitch.isChecked());
        mCategoryEntry.setHoursDuration(mHourPiker.getValue());
        mCategoryEntry.setMinutesDuration(mMinutePiker.getValue());
        mCategoryEntry.setColor(mCategoryColorId);
        mCategoryEntry.setIcon(mCategoryIconId);
        DbExecutor.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                mDb.categoryDao().updateCategory(mCategoryEntry);
            }
        });
    }

    private void insertCategoryEntry() {
        String name = mCategoryNameEditText.getText().toString();
        int hours = mHourPiker.getValue();
        int minutes = mMinutePiker.getValue();
        boolean notification = mNotificationSwitch.isChecked();
        mCategoryEntry = new CategoryEntry(mActivityInfo.getId(), name, hours, minutes, notification, mCategoryColorId, mCategoryIconId);
        DbExecutor.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                mDb.categoryDao().insertCategory(mCategoryEntry);
            }
        });
    }

    private void checkNotificationSwitch(){
        if ( (mHourPiker.getValue() > 0 ) || (mMinutePiker.getValue() > 0) ) {
            mNotificationSwitch.setEnabled(true);
            mNotificationSwitch.setChecked(mViewModel.isNotificationActive());
        } else {
            mNotificationSwitch.setEnabled(false);
            boolean notificationActive = mViewModel.isNotificationActive();
            mNotificationSwitch.setChecked(false);
            mViewModel.setNotificationActive(notificationActive);
        }
    }

    private void changeColor(int colorId) {
        mCategoryColorId = colorId;
        if (colorId == 0) {
            mColorImageView.setColorFilter(null);
        } else {
            mColorImageView.setColorFilter(getResources().getColor(AppResources.getInstance().getColor(colorId)));
        }
        mViewModel.setColor(colorId);
    }

    private void changeIcon(int iconId) {
        mCategoryIconId = iconId;
        mIconImageView.setImageResource(AppResources.getInstance().getIcon(mActivityInfo.getId(), iconId));
        mViewModel.setIcon(iconId);
    }

}

