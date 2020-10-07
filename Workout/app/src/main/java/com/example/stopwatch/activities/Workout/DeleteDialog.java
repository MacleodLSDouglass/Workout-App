package com.example.stopwatch.activities.Workout;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;

public class DeleteDialog extends DialogFragment{
    // functionality implemented in WorkoutActivity
    public interface FragmentModelListener {
        public void onDialogPositiveClick(DialogFragment dialog);
        public void onDialogNegativeClick(DialogFragment dialog);
    }

    DeleteDialog.FragmentModelListener listener;

    // initialises multiple function listener for button clicks etc
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (DeleteDialog.FragmentModelListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException();
        }
    }

    // creates(inflates) custom layout when dialog is created
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle("Are you sure you want to delete this workout?")
                .setPositiveButton("yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        listener.onDialogPositiveClick(DeleteDialog.this);
                    }
                })
                .setNegativeButton("no", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        listener.onDialogNegativeClick(DeleteDialog.this);
                    }
                });
        return builder.create();
    }
}
