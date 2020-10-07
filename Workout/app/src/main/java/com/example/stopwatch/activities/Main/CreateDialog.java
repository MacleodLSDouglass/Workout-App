package com.example.stopwatch.activities.Main;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.fragment.app.DialogFragment;

import com.example.stopwatch.R;

// custom dialog for unique functionality. Dialog is overlay when user clicks "Create" button in
// MainActivity. Used for creating new file(workout)
public class CreateDialog extends DialogFragment{
    // field for inputting name of workout
    public EditText title;

    // functionality implemented in MainActivity
    public interface FragmentModelListener {
        public void onDialogPositiveClick(DialogFragment dialog);
        public void onDialogNegativeClick(DialogFragment dialog);
    }

    FragmentModelListener listener;

    // initialises multiple function listener for button clicks etc
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (FragmentModelListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException();
        }
    }

    // creates(inflates) custom layout when dialog is created
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        View inflatedView = inflater.inflate(R.layout.create_dialog_layout, null);
        title = (EditText) inflatedView.findViewById(R.id.dialogName);

        builder.setTitle("Create new workout")
                .setView(inflatedView)
                .setPositiveButton("yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        listener.onDialogPositiveClick(CreateDialog.this);
                    }
                })
                .setNegativeButton("no", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        listener.onDialogNegativeClick(CreateDialog.this);
                    }
                });
        return builder.create();
    }
}

