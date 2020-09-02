package com.example.stopwatch;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.stopwatch.models.SetsModel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class Exercise extends AppCompatActivity {
    private static final String TAG = "ExerciseActivity";
    private MyRecyclerViewAdapter adapter;
    private final String FILENAME = "workout.txt";
    private EditText exerciseEdit;
    private EditText setsEdit;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise);

        // find EditText objects from XML layout
        exerciseEdit = findViewById(R.id.exerciseEdit);
        setsEdit = findViewById(R.id.setsEdit);
        recyclerView = findViewById(R.id.recyclerView);

        // read from file
        ReadFile();

        // focus the first input field on creation
        exerciseEdit.requestFocus();

        // listener to clear EditView focus when clicking done on the keyboard when focusing
        // the sets input; but not remove the keyboard. This then leads into the onFocusChangeListener
        setsEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE){
                    setsEdit.clearFocus();
                }
                return false;
            }
        });

        // listener to take the number of sets from the sets TextView (id = setNumberExercises)
        // and format the RecyclerView to display number of LinearLayout ViewHolders (components)
        // equal to number of sets. This is triggered when the user un-focuses the "sets" TextView
        setsEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus) {
                    if (setsEdit.getText() != null) {
                        try {
                            final int setCount = Integer.parseInt(setsEdit.getText().toString());
                            int recyclerCount = adapter.getItemCount();

                            // if number of sets in "sets" EditText is less than the number of
                            // recycler items, add recycler items to equal sets
                            while(setCount > recyclerCount) {
                                adapter.insertAt(0);
                                recyclerCount = adapter.getItemCount();
                            }

                            // if number of sets in "sets" EditText is more than the number of
                            // recycler items, remove recycler items to equal sets
                            while(setCount < recyclerCount) {
                                adapter.removeAt(recyclerCount - 1);
                                recyclerCount = adapter.getItemCount();
                            }

                            // delayed focus to first item in recycler view otherwise will cause
                            // null pointer exception
                            recyclerView.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    RecyclerView.ViewHolder vHolder =
                                            recyclerView.findViewHolderForAdapterPosition(0);
                                    if (vHolder != null) {
                                        View firstView = vHolder.itemView.findViewById(R.id.setReps);
                                        firstView.requestFocus();
                                        InputMethodManager imm = (InputMethodManager) getSystemService
                                                (Context.INPUT_METHOD_SERVICE);
                                        imm.showSoftInput(firstView, InputMethodManager.SHOW_IMPLICIT);
                                    }

                                    // set the 'final' button of the last EditView to 'done' instead
                                    // of 'next'
                                    vHolder =
                                            recyclerView.findViewHolderForAdapterPosition(setCount - 1);
                                    if (vHolder != null) {
                                        final EditText lastView = vHolder.itemView.findViewById(R.id.setTime);
                                        lastView.setImeOptions(6);
                                    }
                                }
                            }, 50);
                        } catch(Exception e) {
                            Log.e(TAG, e.getMessage());
                            finish();
                        }
                    }
                }
            }
        });
    }

    // method for generating the RecyclerView, number of ViewsHolders is based on the number of sets
    // specified in the sets input
    private void CreateRecycler(int numberOfSets) {
        // data to populate the RecyclerView with
        ArrayList<SetsModel> sets = new ArrayList<>();

        for (int a = 0; a < numberOfSets; a++) {
            sets.add(new SetsModel (a, a +1, a + 2));
        }

        // set up the RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MyRecyclerViewAdapter(this, sets);
        recyclerView.setAdapter(adapter);
    }

    public void onClickSave(View view)  {
        // get the text from the "exercise" and "sets" EditText objects, initialise comma for
        // splitting the values in a CSV file and combine the values and comma
        String exercise = exerciseEdit.getText().toString();
        String sets = setsEdit.getText().toString();
        char comma = ',';
        String toWrite = exercise + comma + sets;

        // get text from all EditText from all items in the RecyclerView and add them as
        // values separated by a comma to the final string "toWrite"
        for (int a = 0; a < recyclerView.getChildCount(); a++) {
            RecyclerView.ViewHolder vHolder = recyclerView.findViewHolderForAdapterPosition(a);

            if (vHolder != null) {
                EditText reps = vHolder.itemView.findViewById(R.id.setReps);
                EditText weight = vHolder.itemView.findViewById(R.id.setWeight);
                EditText time = vHolder.itemView.findViewById(R.id.setTime);
                toWrite += comma + reps.getText().toString() +
                    comma + weight.getText().toString() +
                    comma + time.getText().toString();
            }
        }

        // try to open output to file, then write to file.
        try (FileOutputStream fos = this.openFileOutput(FILENAME, this.MODE_PRIVATE)) {
            fos.write(toWrite.getBytes());
        } catch (Exception e)  {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        finish();
    }

    public void ReadFile() {
        // try to open input to file, then read from file
        FileInputStream fis;
        try {
            fis = this.openFileInput(FILENAME);
        }
        catch(Exception e) {
            return;
        }
        InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(isr)) {
            String line = reader.readLine();
            while (line != null) {
                sb.append(line).append('\n');
                line = reader.readLine();
            }
        } catch (IOException e) {

        } finally {
            // convert final StringBuilder to String and remove the newline characters from the end
            // of the line, then split the string by commas
            String contents = sb.toString().replace("\n","");
            final String[] separatedValues = contents.split(",");

            // set the first two values to their respective EditText objects
            exerciseEdit.setText(separatedValues[0]);
            setsEdit.setText(separatedValues[1]);

            // create collection of sets from SetsModel class then add respective values from string
            // to each SetsModel object
            ArrayList<SetsModel> sets = new ArrayList<>();

            for (int a = 2; a < separatedValues.length - 2; a += 3) {
                sets.add(new SetsModel (Integer.parseInt(separatedValues[a]),
                        Integer.parseInt(separatedValues[a+1]),
                        Integer.parseInt(separatedValues[a+2])));
            }

            // initialise the RecyclerView with required LinearLayoutManager and Adapter, along with
            // the collection of SetsModel objects
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            adapter = new MyRecyclerViewAdapter(this, sets);
            recyclerView.setAdapter(adapter);
        }

        // close the FileInputStream now we are done with it
        try {
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}