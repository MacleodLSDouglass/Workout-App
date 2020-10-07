package com.example.stopwatch.activities.Exercise;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.stopwatch.Application;
import com.example.stopwatch.R;
import com.example.stopwatch.models.ExerciseModel;
import com.example.stopwatch.models.SetModel;
import com.example.stopwatch.models.WorkoutModel;

import java.io.FileOutputStream;
import java.util.ArrayList;

public class ExerciseActivity extends AppCompatActivity {
    private TextView weightTxt;
    private EditText exerciseEdit;
    private EditText setsEdit;
    private TextView currentIndexDisplay;

    private ExerciseAdapter adapter;
    private RecyclerView recView;

    private int currentIndex;
    private int totalExercises;
    private WorkoutModel workout;
//---Activity Methods-------------------------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise);

        // find required View objects from XML layout
        weightTxt = findViewById(R.id.exerciseWeightTxt);
        exerciseEdit = findViewById(R.id.exerciseExerciseEdit);
        setsEdit = findViewById(R.id.exerciseSetsEdit);
        currentIndexDisplay = findViewById(R.id.workoutIndex);
        recView = findViewById(R.id.mainRec);

        // get the current selected workout from Application.workout
        try {
            Application app = (Application)getApplication();
            workout = app.workout;
        } catch (Exception e) {
            finish();
        }

        // recover index of selected exercise passed on by previous activity "WorkoutActivity" and
        // find the total number of exercises of the workout
        currentIndex = getIntent().getIntExtra("index", -1);
        totalExercises = workout.exercises.size();

        // set title of Application
        setTitle(workout.workout);

        // convert weight (from Kg to Lbs and vise versa currently)
//        weightTxt.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                weightConversion();
//            }
//        });

        initialiseRecycler();
    }
//---Buttons-------------------------------------------------------------------------------
    // when the save button is clicked. finish activity to go back to "WorkoutActivity"
    // activity
    public void onClickSave(View view)  {
        if(!save()) {
            return;
        }
        finish();
    }

    public void onClickDelete(View view) {
        if(!delete()) {
            return;
        }
        finish();
    }

    // when left button is clicked. Populates recycler with values from next exercise to
    // the left
    public void onClickLeft(View view) {
        if(!save()) {
            return;
        }

        currentIndex -= 1;
        // finish and go back to previous activity if trying to go below index 0
        if(currentIndex == -1 ) {
            finish();
            return;
        }

        initialiseRecycler();
    }

    // when right button is clicked. Populates recycler with values from next exercise to
    // the right
    public void onClickRight(View view) {
        if(!save()) {
            return;
        }

        currentIndex += 1;
        // finish and go back to previous activity if trying to go above the number of items in
        // the file collection
        if(currentIndex == workout.exercises.size() ) {
            finish();
            return;
        }

        initialiseRecycler();
    }
//---Private Methods-------------------------------------------------------------------------------

    private void initialiseRecycler() {
        // create new ExerciseModel for putting new values into
        ExerciseModel exercise = new ExerciseModel();
        try {
            exercise = workout.exercises.get(currentIndex);
        } catch(Exception a) {}

        try {
            exerciseEdit.setText(exercise.exercise);
            setsEdit.setText(Integer.toString(exercise.sets.size()));
        } catch(Exception e) {}

        // set "currentIndexDisplay" to "currentIndex" out of total amount of exercises
        currentIndexDisplay.setText(Integer.toString(currentIndex + 1)
                + "/" + totalExercises);

        // initialise the RecyclerView with SetModel objects
        recView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ExerciseAdapter(this, exercise.sets);
        recView.setAdapter(adapter);

        // listener to clear EditView focus when clicking done on the keyboard when focusing
        // the sets input; but not remove the keyboard.
        // This then leads into the onFocusChangeListener
        setsEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE){
                    setsEdit.clearFocus();
                }
                return false;
            }
        });

        // listener to take the number of sets from the "setsEdit" EditView and format the
        // RecyclerView to display number of items equal to number of sets. This is triggered when
        // the user un-focuses the "setsEdit" EditView
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

                            // delayed until the recycler finishes adding or removing items
                            recView.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    // focus on the first value of the first
                                    // item of the recyclerView
                                    RecyclerView.ViewHolder vHolder =
                                            recView.findViewHolderForAdapterPosition(0);
                                    if (vHolder != null) {
                                        View firstView =
                                                vHolder.itemView.findViewById(R.id.reps);
                                        firstView.requestFocus();
                                        InputMethodManager imm =
                                                (InputMethodManager) getSystemService
                                                (Context.INPUT_METHOD_SERVICE);
                                        imm.showSoftInput(firstView,
                                                InputMethodManager.SHOW_IMPLICIT);
                                    }

                                    // set the 'final' button of the last EditView to 'done' instead
                                    // of 'next'
                                    vHolder = recView
                                                    .findViewHolderForAdapterPosition(setCount - 1);
                                    if (vHolder != null) {
                                        final EditText lastView =
                                                vHolder.itemView.findViewById(R.id.time);
                                        lastView.setImeOptions(6);
                                    }
                                }
                            }, 50);
                        } catch(Exception e) {
                            finish();
                        }
                    }
                }
            }
        });

        // focus on the first input field
        exerciseEdit.requestFocus();
    }

    // saves exercise to Application.workout
    private boolean save() {
        // get and check exercise name
        String exerciseName = exerciseEdit.getText().toString();
        if(exerciseName.isEmpty()) {
            Toast.makeText(this, "Exercise name cannot be empty.",
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        // get and check that there is at least one set
        if(adapter.sets.isEmpty()) {
            Toast.makeText(this, "Must have at least one set.",
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        // create new ExerciseModel to then populate with the user inputs
        ExerciseModel exercise = new ExerciseModel();
        exercise.exercise = exerciseName;
        exercise.sets = new ArrayList<>();
        for (int a = 0; a < adapter.getItemCount(); a++) {
            RecyclerView.ViewHolder vHolder = recView.findViewHolderForAdapterPosition(a);

            if (vHolder != null) {
                EditText repsEdit = vHolder.itemView.findViewById(R.id.reps);
                EditText weightEdit = vHolder.itemView.findViewById(R.id.weight);
                EditText timeEdit = vHolder.itemView.findViewById(R.id.time);

                int time = Integer.parseInt(timeEdit.getText().toString());
                if(time == 0) {
                    Toast.makeText(this, "All sets must have a time.",
                            Toast.LENGTH_SHORT).show();
                    return false;
                }

                exercise.sets.add(a, new SetModel(
                        Integer.parseInt(repsEdit.getText().toString()),
                        Double.parseDouble(weightEdit.getText().toString()),
                        time));
            }
        }

        // attempt to save new ExerciseModel to Application.workout
        try {
            workout.exercises.set(currentIndex, exercise);
        } catch (Exception e) {
            Toast.makeText(this, "Something went wrong.",
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        toFile();
        return true;
    }

    // deletes exercise from Application.workout
    private boolean delete() {
        workout.exercises.remove(currentIndex);
        toFile();
        return true;
    }

    // saves Application.workout to file
    private void toFile() {
        final String FILENAME = workout.workout + ".csv";

        // create string that will be written to file
        String toWrite = "";
        // take each ExerciseModel in the Application.workout object
        for(ExerciseModel exercise: workout.exercises) {
            // add the exercise name
            toWrite += exercise.exercise;
            // add each set and respective values
            for(SetModel set: exercise.sets) {
                toWrite += "," + set.reps + "," + set.weight + "," + set.time;
            }
            // add newline
            toWrite += '\n';
        }

        // try to open output to file, then overwrite current file contents with the updated
        // file
        try (FileOutputStream fos = this.openFileOutput(FILENAME, this.MODE_PRIVATE)) {
            fos.write(toWrite.getBytes());
        } catch (Exception e)  { }
    }

    // converts weight metric units
    private void weightConversion() {
        String currentMetric = weightTxt.getText().toString();
        switch (currentMetric) {
            case "Weight(Lbs)":
                for(int a = 0; a < adapter.getItemCount(); a++) {
                    ExerciseAdapter.ViewHolder holder = (ExerciseAdapter.ViewHolder)
                            recView.findViewHolderForAdapterPosition(a);
                    if(holder == null) {return;}
                    Double weight = Double.parseDouble(holder.weight.getText().toString()) / 2.2;
                    holder.weight.setText(weight.toString());
                    weightTxt.setText(R.string.weightKgHint);
                }
                break;
            default:
                for(int a = 0; a < adapter.getItemCount(); a++) {
                    ExerciseAdapter.ViewHolder holder = (ExerciseAdapter.ViewHolder)
                            recView.findViewHolderForAdapterPosition(a);
                    if(holder == null) {return;}
                    Double weight = Double.parseDouble(holder.weight.getText().toString()) * 2.2;
                    holder.weight.setText(weight.toString());
                    weightTxt.setText(R.string.weightLbsHint);
                }
                break;
        }
    }
}

// RecyclerView object adds tasks to a list that runs simultaneously to the program.
// If adding or removing items; then trying to locate an item at specific index,
// you must delay the index search until the RecyclerView finishes it's list of tasks.
// eg - recyclerView.postDelayed(new Runnable(){}, 50);