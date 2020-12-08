package com.example.android.timematters.utils;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.example.android.timematters.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class ValidationDialog extends DialogFragment {

    private static final String ARG_VALIDATION_MESSAGE = "validation_message";

    private String mValidationMessage;

    public static ValidationDialog newInstance(String validationMessaged) {
        ValidationDialog dialogFragment = new ValidationDialog();
        Bundle args = new Bundle();
        args.putString(ARG_VALIDATION_MESSAGE, validationMessaged);
        dialogFragment.setArguments(args);
        return dialogFragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        // Retrieve parameters
        if (getArguments() != null) {
            if (getArguments().containsKey(ARG_VALIDATION_MESSAGE)) {
                mValidationMessage = getArguments().getString(ARG_VALIDATION_MESSAGE);
            }
        }

        // Inflate layout
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_validation, null);
        TextView message = view.findViewById(R.id.message_tv);
        message.setText(mValidationMessage);

        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view)
                .setPositiveButton(R.string.accept, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dismiss();
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();

    }
}
