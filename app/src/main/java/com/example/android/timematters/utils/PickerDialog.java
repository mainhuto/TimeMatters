package com.example.android.timematters.utils;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import com.example.android.timematters.R;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class PickerDialog extends DialogFragment {

    private static final String TAG = "PickerDialog";

    private static final String ARG_PICKER_TYPE = "picker_type";
    private static final String ARG_ACTIVITY_ID = "activity_id";

    public enum PickerType {COLOR, ICON}

    public interface PickerDialogListener {
        void onPickerDialogPositiveClick(int position, String tag);
        void onPickerDialogNegativeClick(String tag);
    }

    private PickerType mType;
    private int mActivityId;
    private int mPosition;
    private PickerDialogListener mListener;

    public static PickerDialog newInstance(PickerType pickerType, int activityId) {
        PickerDialog dialogFragment = new PickerDialog();
        Bundle args = new Bundle();
        args.putInt(ARG_PICKER_TYPE, pickerType.ordinal());
        args.putInt(ARG_ACTIVITY_ID, activityId);
        dialogFragment.setArguments(args);
        return dialogFragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        // Retrieve parameters
        if ( getArguments() != null ) {
            if ( getArguments().containsKey(ARG_PICKER_TYPE) ) {
                int intType = getArguments().getInt(ARG_PICKER_TYPE);
                if (PickerType.values().length > intType) {
                    mType = PickerType.values()[intType];
                }
            }
            if ( getArguments().containsKey(ARG_ACTIVITY_ID) ) {
                mActivityId = getArguments().getInt(ARG_ACTIVITY_ID);
            }
        }

        // Create dialog builder
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Set negative button
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onPickerDialogNegativeClick(getTag());
                    }
                });

        // Inflate layout
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_picker, null);
        builder.setView(view);

        // Retrieve item list for the activity and picker type
        List<Integer> items;
        if (PickerType.COLOR.equals(mType)) {
            items = AppResources.getInstance().getColors();
        } else {
            items = AppResources.getInstance().getIconPalette(mActivityId);
        }

        // Create an setup gridView
        GridView gridview = view.findViewById(R.id.color_gw);
        gridview.setAdapter(new PickerAdapter(getActivity(), items));

        // Set On click listener for the gridView items
        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            public void onItemClick(AdapterView parent, View v,int position, long id) {
                mPosition = position;
                Log.d(TAG, "onItemClick: " +  mPosition);
                mListener.onPickerDialogPositiveClick(mPosition, getTag());
                dismiss();
            }
        });

        return builder.create();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (PickerDialogListener) context;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(context.toString() + " must implement " + PickerDialogListener.class);
        }
    }

}
