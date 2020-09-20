package com.example.stopwatch.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.stopwatch.Application;
import com.example.stopwatch.R;
import com.example.stopwatch.models.ExerciseModel;
import com.example.stopwatch.models.SetModel;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class Exercise extends AppCompatActivity {
    private static final String TAG = "ExerciseActivity";
    private EditText exerciseEdit;
    private EditText setsEdit;
    private TextView currentIndexDisplay;

    private Adapter adapter;
    private RecyclerView recView;

    private int currentIndex;
    private ArrayList<ExerciseModel> workout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise);

        // find EditText and TextView objects from XML layout
        exerciseEdit = findViewById(R.id.exerciseEdit);
        setsEdit = findViewById(R.id.setsEdit);
        currentIndexDisplay = findViewById(R.id.exerciseIndex);

        recView = findViewById(R.id.workoutRec);

        // recover information passed on by previous activity "Workout"
        currentIndex = getIntent().getIntExtra("index", -1);

        try {
            Application app = (Application)getApplication();
            workout = app.workout;
        } catch (Exception e) {
            finish();
        }

        InitialiseRecycler();
    }

    // activates when the save button is clicked. finished activity to go back to "Workout"
    // activity
    public void onClickFinish(View view)  {
        if(!save()) {
            return;
        }
        finish();
    }

    // activates when left button is clicked. Populates recycler with values from next exercise to
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

        InitialiseRecycler();
    }

    // activates when right button is clicked. Populates recycler with values from next exercise to
    // the right
    public void onClickRight(View view) {
        if(!save()) {
            return;
        }

        currentIndex += 1;
        // finish and go back to previous activity if trying to go above the number of items in
        // the file collection
        if(currentIndex == workout.size() ) {
            finish();
            return;
        }

        InitialiseRecycler();
    }

    private void InitialiseRecycler() {
        ExerciseModel exercise = workout.get(currentIndex);

        try {
            exerciseEdit.setText(exercise.exercise);
            setsEdit.setText(Integer.toString(exercise.sets.size()));
        } catch(Exception e) {}

        // set current index display box
        currentIndexDisplay.setText(Integer.toString(currentIndex + 1));

        // initialise the RecyclerView with required LinearLayoutManager and Adapter, along with
        // the collection of SetModel objects
        recView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new Adapter(this, exercise.sets);
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
                                                vHolder.itemView.findViewById(R.id.setReps);
                                        firstView.requestFocus();
                                        InputMethodManager imm =
                                                (InputMethodManager) getSystemService
                                                (Context.INPUT_METHOD_SERVICE);
                                        imm.showSoftInput(firstView,
                                                InputMethodManager.SHOW_IMPLICIT);
                                    }

                                    // set the 'final' button of the last EditView to 'done' instead
                                    // of 'next'
                                    vHolder =
                                            recView.
                                                    findViewHolderForAdapterPosition(setCount - 1);
                                    if (vHolder != null) {
                                        final EditText lastView =
                                                vHolder.itemView.findViewById(R.id.setTime);
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

        // focus on the first input field
        exerciseEdit.requestFocus();
    }

    private boolean save() {
        final String FILENAME = "workout.csv";

        // get and check exercise name
        String exerciseName = exerciseEdit.getText().toString();
        if(exerciseName.isEmpty()) {
            Toast.makeText(this, "Exercise name cannot be empty",
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        workout.get(currentIndex).exercise = exerciseName;
        workout.get(currentIndex).sets = new ArrayList<>();

        // get text from all EditText from all items in the RecyclerView and update the
        // appropriate objects in the existing application.workout object that can be accessed
        // from anywhere in the application
        for (int a = 0; a < recView.getChildCount(); a++) {
            RecyclerView.ViewHolder vHolder = recView.findViewHolderForAdapterPosition(a);

            if (vHolder != null) {
                EditText reps = vHolder.itemView.findViewById(R.id.setReps);
                EditText weight = vHolder.itemView.findViewById(R.id.setWeight);
                EditText time = vHolder.itemView.findViewById(R.id.setTime);

                workout.get(currentIndex).sets.add(a, new SetModel(
                        Integer.parseInt(reps.getText().toString()),
                        Integer.parseInt(weight.getText().toString()),
                        Integer.parseInt(time.getText().toString())));
            }
        }

        // create string that will be written to file
        String toWrite = "";
        // take each ExerciseModel in the Application.workout object
        for(ExerciseModel exercise: workout) {
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
        } catch (Exception e)  {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        return true;
    }

    // ----------------------------------------------------------------------------------------
    // adapter class for recycler
    private class Adapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private List<SetModel> sets;
        private LayoutInflater mInflater;

        // data is passed into the constructor
        Adapter(Context context, List<SetModel> data) {
            this.mInflater = LayoutInflater.from(context);
            this.sets = data;
        }

        // inflates the row layout from xml when needed
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = mInflater.inflate(R.layout.set_layout, parent, false);
            return new Adapter.ViewHolder(view);
        }

        // binds the data to the TextView in each row
        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            Adapter.ViewHolder nHolder = (Adapter.ViewHolder) holder;

            SetModel set = sets.get(position);

            nHolder.reps.setText(String.valueOf(set.reps));
            nHolder.weight.setText(String.valueOf(set.weight));
            nHolder.time.setText(String.valueOf(set.time));
        }

        // total number of rows
        @Override
        public int getItemCount() {
            return sets.size();
        }

        // stores and recycles views as they are scrolled off screen
        private class ViewHolder extends RecyclerView.ViewHolder {
            EditText reps;
            EditText weight;
            EditText time;

            ViewHolder(View itemView) {
                super(itemView);
                reps = itemView.findViewById(R.id.setReps);
                weight = itemView.findViewById(R.id.setWeight);
                time = itemView.findViewById(R.id.setTime);
            }
        }

        public void insertAt(int position) {
            sets.add(new SetModel());
            notifyItemInserted(position);
            notifyItemRangeChanged(position, sets.size());
        }

        public void removeAt(int position) {
            sets.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, sets.size());
        }
    }
}

// RecyclerView object adds tasks to a list that runs simultaneously to the program.
// If adding or removing items; then trying to locate an item at specific index,
// you must delay the index search until the RecyclerView finishes it's list of tasks.
// eg - recyclerView.postDelayed(new Runnable(){}, 50);