package com.example.stopwatch.activities.MainActivity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.stopwatch.Application;
import com.example.stopwatch.R;
import com.example.stopwatch.activities.Workout.Workout;
import com.example.stopwatch.models.ExerciseModel;
import com.example.stopwatch.models.SetModel;
import com.example.stopwatch.models.WorkoutModel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements CreateDialog.FragmentModelListener {
    private RecyclerView workoutRecView;
    private RecyclerView exerciseRecView;
    private MainAdapter workoutAdapter;
    private Spinner workoutSelectionSpinner;

    private WorkoutModel workout;

    private Button startStopBtn;

    // use seconds, running and wasRunning respectively to record the number of seconds passed,
    // whether the watch is running and whether the watch was running
    // before the activity was paused.

    // number of seconds displayed on watch.
    private int seconds = 0;

    // whether the watch is running
    private boolean running;
    private boolean wasRunning;

    // current exercise and set counters
    private int workoutExercises = 0;
    private int currentExercise = 0;
    private int exerciseSets = 0;
    private int currentSet = 0;
//---Activity Methods-------------------------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // find XML views
        workoutRecView = findViewById(R.id.exerciseRec);
        startStopBtn = findViewById(R.id.MainStartStopBtn);
        workoutSelectionSpinner = findViewById(R.id.workoutSpinner);

        // initialise spinner/dropdown with file names from application directory
        String[] files = this.fileList();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, files);
        workoutSelectionSpinner.setAdapter(adapter);
        workoutSelectionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                readFile();
                initialiseRecycler();
                seconds = 0;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        if (savedInstanceState != null) {
            // Get the previous state of the stopwatch if the activity has been
            // destroyed and recreated
            seconds = savedInstanceState.getInt("seconds");
            running = savedInstanceState.getBoolean("running");
            wasRunning = savedInstanceState.getBoolean("wasRunning");
        }

        readFile();
        initialiseRecycler();
        runTimer();
    }

    // Save the state of the stopwatch if it's about to be destroyed
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putInt("seconds", seconds);
        savedInstanceState.putBoolean("running", running);
        savedInstanceState.putBoolean("wasRunning", wasRunning);
    }

    // If the activity is paused, stop the stopwatch
    @Override
    protected void onPause() {
        super.onPause();
        wasRunning = running;
        running = false;
    }

    // If the activity is resumed, start the stopwatch
    // again if it was running previously
    @Override
    protected void onResume() {
        super.onResume();
        if (wasRunning) {
            running = true;
        }

        // set the workout in the application class to it can be accessed from anywhere in the
        // application
        try {
            Application app = (Application)getApplication();
            workout = app.workout;
        } catch (Exception a) {
            readFile();
            try {
                Application app = (Application)getApplication();
                workout = app.workout;
            } catch (Exception b) {
                finish();
            }
        }
    }
//---Buttons----------------------------------------------------------------------------------------
    // create a new workout when the 'Create' button is clicked
    public void onClickCreate(View view) {
        DialogFragment fragment = new CreateDialog();
        fragment.show(getSupportFragmentManager(), "Create_Workout");
    }

    // Start the stopwatch running when the Start button is clicked.
    // Below method gets called when the Start button is clicked
    public void onClickStart(View view) {
        if (running == true) {
            running = false;
            startStopBtn.setText("Start");
        } else {
            running = true;
            startStopBtn.setText("Stop");
        }
    }

    // Reset the stopwatch when the Reset button is clicked.
    // Below method is called when Reset button is clicked
    public void onClickReset(View view) {
        running = false;

        currentExercise = 0;
        currentSet = 0;

        seconds = 0;
    }

    // Begin and switch to the Exercise activity when 'Workout' button is clicked.
    public void onClickWorkout(View view)  {
        startActivity(
                new Intent(this, Workout.class));
    }
//---Private Methods--------------------------------------------------------------------------------
    // Sets the Number of seconds on the timer. The runTimer() method uses a Handler
    // to increment the seconds and update the text view
    private void runTimer() {
        // Get the text view
        final TextView timeView = (TextView)findViewById(R.id.time_view);

        //Creates a new Handler
        final Handler handler = new Handler();

        // delay so RecyclerView does not return null values
        workoutRecView.postDelayed(new Runnable() {
            @Override
            public void run() {
                // call the post() method, passing in the new Runnable. the post() method processes
                // code without a delay, so the code in the Runnable will run almost immediately
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if(seconds == 0 && !workoutAdapter.workout.isEmpty()) {
                            // when all exercises have been completed
                            if(currentExercise == workoutExercises) {
                                // find last exercise and un-highlight it
                                RecyclerView.ViewHolder vHolder =
                                        workoutRecView.
                                                findViewHolderForAdapterPosition(
                                                        currentExercise - 1);
                                if(vHolder != null) {
                                    vHolder.itemView.findViewById(R.id.exerciseTxt)
                                            .setBackgroundColor(Color.WHITE);
                                    exerciseRecView.setBackgroundColor(Color.WHITE);
                                    exerciseRecView.setVisibility(View.GONE);

                                    // reset workout
                                    workoutExercises = 0;
                                    currentExercise = 0;
                                    exerciseSets = 0;
                                    currentSet = 0;
                                    Toast.makeText(MainActivity.this,
                                            "Well done! You have completed the workout.",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }

                            // when all sets in exercise have been completed
                            if(exerciseSets == 0) {
                                // find previous exercise and un-highlight it
                                RecyclerView.ViewHolder prevHolder =
                                        workoutRecView.
                                                findViewHolderForAdapterPosition(
                                                        currentExercise - 1);
                                if(prevHolder != null) {
                                    prevHolder.itemView.findViewById(R.id.exerciseTxt)
                                            .setBackgroundColor(Color.WHITE);
                                    exerciseRecView.setBackgroundColor(Color.WHITE);
                                    exerciseRecView.setVisibility(View.GONE);
                                }
                                // find the current exercise and highlight it
                                RecyclerView.ViewHolder evHolder =
                                        workoutRecView.
                                                findViewHolderForAdapterPosition(currentExercise);
                                if(evHolder != null) {
                                    evHolder.itemView.findViewById(R.id.exerciseTxt)
                                            .setBackgroundColor(Color.GRAY);
                                    evHolder.itemView.findViewById(R.id.exerciseRec)
                                            .setVisibility(View.VISIBLE);
                                    exerciseRecView =
                                            (RecyclerView)evHolder.itemView.
                                                    findViewById(R.id.exerciseRec);
                                    exerciseRecView.setBackgroundColor(Color.LTGRAY);
                                    // set number of exercise sets remaining
                                    exerciseSets = exerciseRecView.getAdapter().getItemCount();
                                    // increment counter of total workout exercises
                                    currentExercise ++;
                                    // reset sets counter for the new exercise
                                    currentSet = 0;
                                }
                            }

                            // find the current set and extract the time value to put into the
                            // timer. Delayed as the view was just set to visible
                            // (RecyclerView changed)
                            exerciseRecView.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    RecyclerView.ViewHolder svHolder =
                                            exerciseRecView.findViewHolderForAdapterPosition(
                                                    currentSet);
                                    if (svHolder != null) {
                                        EditText time = (EditText)svHolder.itemView.
                                                findViewById(R.id.setTime);
                                        seconds = Integer.parseInt(time.getText().toString());
                                    }
                                    // increment counter for current set
                                    currentSet ++;
                                }
                            }, 10);

                            // reduce the number of exercise sets still remaining
                            exerciseSets --;

                            // stop the timer
                            running = false;
                            startStopBtn.setText("Start");
                        }

                        int hours = seconds / 3600;
                        int minutes = (seconds % 3600) / 60;
                        int secs = seconds % 60;

                        // format the seconds into hours, minutes, and seconds
                        String time = String.format(Locale.getDefault(),
                                "%d:%02d:%02d", hours, minutes, secs);

                        // set the text view text.
                        timeView.setText(time);

                        // if running is true, decrement the seconds variable.
                        if (running) {
                            seconds--;
                        }

                        // post the code again with a delay of 1 second
                        handler.postDelayed(this, 1000);
                    }
                });
            }
        }, 50);

    }

    // called in onResume method, reads the file 'workout.csv'
    private void readFile() {
        String fileName;
        try {
            fileName = workoutSelectionSpinner.getSelectedItem().toString();
        } catch(Exception a) {
            fileName = "Example.csv";
        }
        String name = fileName.replace(".csv", "");
        setTitle(name);

        ArrayList<String> readFile = new ArrayList<>();

        // try to open input to file, then read from file
        FileInputStream fis;
        try {
            File file = new File(getApplicationContext().getFilesDir(),fileName);
            if(!file.exists()) {
                String example = "deadlift,1,2,3\nsquat,2,3,4\nbenchpress,3,4,5";
                FileOutputStream fos = this.openFileOutput(fileName, this.MODE_PRIVATE);
                fos.write(example.getBytes());

                ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                        android.R.layout.simple_spinner_item, Collections.singletonList(fileName));
                workoutSelectionSpinner.setAdapter(adapter);
            }

            fis = this.openFileInput(fileName);
        }
        catch(Exception e) {
            return;
        }
        InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
        try (BufferedReader reader = new BufferedReader(isr)) {
            String line = reader.readLine();
            while (line != null) {
                readFile.add(line);
                line = reader.readLine();
            }
        } catch (IOException e) { } finally {
            // create collection of ExerciseModel then add respective values from "file"
            // to each ExerciseModel object
            workout = new WorkoutModel();
            workout.workout = name;
            workout.exercises = new ArrayList<>();

            for (String line: readFile) {
                String[] separatedLine = line.split(",");

                ExerciseModel exercise = new ExerciseModel();
                exercise.exercise = separatedLine[0];
                exercise.sets = new ArrayList<>();
                for (int a = 1; a < separatedLine.length; a += 3) {
                    exercise.sets.add(new SetModel(Integer.parseInt(separatedLine[a]),
                            Integer.parseInt(separatedLine[a+1]),
                            Integer.parseInt(separatedLine[a+2])));
                }
                workout.exercises.add(exercise);
            }

            // save the values obtained from the file to the Application.workout object that
            // can be accessed across the application
            Application app = (Application) getApplication();
            app.workout = workout;
        }

        try {
            fis.close();
        } catch (IOException e) {
            return;
        }
    }

    private void initialiseRecycler() {
        // number of exercises in workout
        workoutExercises = workout.exercises.size();
        // initialise workout RecyclerView
        workoutRecView.setLayoutManager(new LinearLayoutManager(this));
        workoutAdapter = new MainAdapter(this, workout.exercises);
        workoutRecView.setAdapter(workoutAdapter);

        // delayed due to RecyclerView recently initialised
        workoutRecView.postDelayed(new Runnable() {
            @Override
            public void run() {
                // find the current exercise from where we left off and highlight it
                RecyclerView.ViewHolder evHolder =
                        workoutRecView.
                                findViewHolderForAdapterPosition(currentExercise - 1);
                if(evHolder != null) {
                    evHolder.itemView.findViewById(R.id.exerciseTxt)
                            .setBackgroundColor(Color.GRAY);
                    evHolder.itemView.findViewById(R.id.exerciseRec)
                            .setVisibility(View.VISIBLE);
                    exerciseRecView =
                            (RecyclerView)evHolder.itemView.
                                    findViewById(R.id.exerciseRec);
                    exerciseRecView.setBackgroundColor(Color.LTGRAY);
                }
            }
        }, 10);
    }
//---Dialog Functionality---------------------------------------------------------------------------
    public void onDialogPositiveClick(DialogFragment dialog) {
        CreateDialog nDialog = (CreateDialog)dialog;

        try (FileOutputStream fos = this.openFileOutput(nDialog.title.getText().toString()
                        + ".csv",
                this.MODE_PRIVATE)) {
            fos.write("".getBytes());
        } catch (Exception e)  { }

        String[] files = this.fileList();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, files);
        workoutSelectionSpinner.setAdapter(adapter);
    }

    public void onDialogNegativeClick(DialogFragment dialog) { }
}
