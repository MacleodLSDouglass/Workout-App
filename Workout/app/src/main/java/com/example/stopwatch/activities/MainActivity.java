package com.example.stopwatch.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.stopwatch.Application;
import com.example.stopwatch.R;
import com.example.stopwatch.models.ExerciseModel;
import com.example.stopwatch.models.SetModel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private RecyclerView recView;

    // Use seconds, running and wasRunning respectively to record the number of seconds passed,
    // whether the stopwatch is running and whether the stopwatch was running
    // before the activity was paused.

    // Number of seconds displayed on stopwatch.
    private int seconds = 0;

    // Is the stopwatch running?
    private boolean running;
    private boolean wasRunning;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // initialise values
        recView = findViewById(R.id.workoutRec);

        if (savedInstanceState != null) {
            // Get the previous state of the stopwatch if the activity has been
            // destroyed and recreated
            seconds = savedInstanceState.getInt("seconds");
            running = savedInstanceState.getBoolean("running");
            wasRunning = savedInstanceState.getBoolean("wasRunning");
        }

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

        readFile();
    }

    // Start the stopwatch running when the Start button is clicked.
    // Below method gets called when the Start button  is clicked
    public void onClickStart(View view) {
        final Button startStop = (Button)findViewById(R.id.MainStartStopBtn);

        if (running == true) {
            running = false;
            startStop.setText("Start");
        } else {
            running = true;
            startStop.setText("Stop");
        }
    }

    // Reset the stopwatch when the Reset button is clicked.
    // Below method is called when Reset button is clicked
    public void onClickReset(View view) {
        running = false;
        seconds = 0;
    }

    // Begin and switch to the Exercise activity when 'Workout' button is clicked.
    public void onClickWorkout(View view)  {
        startActivity(new Intent(this, Workout.class));
    }

    // Sets the Number of seconds on the timer. The runTimer() method uses a Handler
    // to increment the seconds and update the text view
    private void runTimer() {
        // Get the text view
        final TextView timeView = (TextView)findViewById(R.id.time_view);

        //Creates a new Handler
        final Handler handler = new Handler();

        // call the post() method, passing in the new Runnable.
        // The post() method processes code without a delay,
        // so the code in the Runnable will run almost immediately
        handler.post(new Runnable() {
            @Override
            public void run() {
                int hours = seconds / 3600;
                int minutes = (seconds % 3600) / 60;
                int secs = seconds % 60;

                // Format the seconds into hours, minutes,
                // and seconds
                String time = String.format(Locale.getDefault(),
                        "%d:%02d:%02d", hours, minutes, secs);

                // Set the text view text.
                timeView.setText(time);

                // If running is true, increment the
                // seconds variable.
                if (running) {
                    seconds++;
                }

                // Post the code again
                // with a delay of 1 second
                handler.postDelayed(this, 1000);
            }
        });
    }

    // called in onResume method, reads the file 'workout.csv'
    private void readFile() {
        final String FILENAME = "workout.csv";
        ArrayList<String> readFile = new ArrayList<>();

        // try to open input to file, then read from file
        FileInputStream fis;
        try {
            File file = new File(getApplicationContext().getFilesDir(),FILENAME);
            if(!file.exists()) {
                String example = "deadlift,1,2,3\nsquat,2,3,4\nbenchpress,3,4,5";
                FileOutputStream fos = this.openFileOutput(FILENAME, this.MODE_PRIVATE);
                fos.write(example.getBytes());
            }

            fis = this.openFileInput(FILENAME);
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
            ArrayList<ExerciseModel> workout = new ArrayList<>();

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
                workout.add(exercise);
            }

            // initialise the RecyclerView with required LinearLayoutManager and Adapter, along with
            // the collection of models
            recView.setLayoutManager(new LinearLayoutManager(this));
            recView.setAdapter(new WorkoutAdapter(this, workout));

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

    // ----------------------------------------------------------------------------------------
    // create the parent adapter for the list of exercises in the workout
    private class WorkoutAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        LayoutInflater lInf;
        List<ExerciseModel> workout;

        WorkoutAdapter(Context con, List<ExerciseModel> workout) {
            this.lInf = LayoutInflater.from(con);
            this.workout = workout;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = lInf.inflate(R.layout.workout_set_layout, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            ViewHolder nHolder = (ViewHolder)holder;

            ExerciseModel exercise = workout.get(position);

            nHolder.exercise.setText(exercise.exercise);

            LinearLayoutManager lMan = new LinearLayoutManager(
                    nHolder.sets.getContext(), RecyclerView.VERTICAL, false);
            lMan.setInitialPrefetchItemCount(4);

            nHolder.sets.setLayoutManager(lMan);
            nHolder.sets.setAdapter(new ExerciseAdapter(nHolder.sets.getContext(), exercise.sets));
        }

        @Override
        public int getItemCount() {
            return workout.size();
        }

        private class ViewHolder extends RecyclerView.ViewHolder {
            TextView exercise;
            RecyclerView sets;

            ViewHolder(View itemView) {
                super(itemView);
                exercise = itemView.findViewById(R.id.exerciseTxt);
                sets = itemView.findViewById(R.id.workoutRec);
            }
        }

        // create the child adapter for the list of sets for each exercise in the list of exercises
        // of the workout
        private class ExerciseAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
            LayoutInflater lInf;
            List<SetModel> sets;

            ExerciseAdapter(Context con, List<SetModel> sets) {
                this.lInf = LayoutInflater.from(con);
                this.sets = sets;
            }

            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                                              int viewType) {
                View view = lInf.inflate(R.layout.set_layout, parent, false);
                return new ViewHolder(view);
            }

            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
                ViewHolder nHolder = (ViewHolder)holder;

                SetModel set = sets.get(position);

                nHolder.reps.setText(Integer.toString(set.reps));
                nHolder.weight.setText(Integer.toString(set.weight));
                nHolder.time.setText(Integer.toString(set.time));
            }

            @Override
            public int getItemCount() {
                return sets.size();
            }

            private class ViewHolder extends RecyclerView.ViewHolder {
                public EditText reps;
                public EditText weight;
                public EditText time;

                ViewHolder(View itemView) {
                    super(itemView);
                    reps = itemView.findViewById(R.id.setReps);
                    weight = itemView.findViewById(R.id.setWeight);
                    time = itemView.findViewById(R.id.setTime);
                }
            }
        }
    }
}
