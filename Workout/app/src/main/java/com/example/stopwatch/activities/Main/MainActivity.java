package com.example.stopwatch.activities.Main;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.os.Build;
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
import com.example.stopwatch.activities.Workout.WorkoutActivity;
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
    // RecyclerViews and related
    private RecyclerView workoutRecView;
    private RecyclerView currentExerciseRecView;
    private MainAdapter workoutAdapter;
    // spinner for selecting workout
    private Spinner workoutSelectionSpinner;
    private ArrayAdapter<String> spinnerAdapter;
    // buttons
    private Button startStopBtn;
    private Button resetBtn;
    private Button skipBtn;
    // the selected workout
    private WorkoutModel workout;
    // number of seconds displayed on watch.
    private int seconds = 0;
    // whether the watch is running
    private static boolean running;
    // current exercise and set counters
    private int workoutExercises = 0;
    private int currentExercise = 0;
    private int exerciseSets = 0;
    private int currentSet = 0;
    // notification for when user leaves app
    NotificationManager notificationManager;
    boolean notificationDisplayed;
    BroadcastReceiver broadcastReceiver;
    // sound player
    MediaPlayer mediaPlayer;
//---Activity Methods-------------------------------------------------------------------------------
    // when activity if created
    @RequiresApi(api = Build.VERSION_CODES.KITKAT_WATCH)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // find XML views
        workoutRecView = findViewById(R.id.mainRec);
        workoutSelectionSpinner = findViewById(R.id.mainSpinner);
        startStopBtn = findViewById(R.id.mainStartStopBtn);
        resetBtn = findViewById(R.id.mainResetBtn);
        // reset current exercise on long click
        resetBtn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if(currentExercise != 0) {
                    currentExercise --;
                }
                currentSet = 0;
                running = false;
                seconds = 0;
                return false;
            }
        });
        skipBtn = findViewById(R.id.mainSkipBtn);
        // skip current exercise on long click
        skipBtn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                currentExercise ++;
                currentSet = 0;
                running = false;
                seconds = 0;
                return false;
            }
        });

        if (savedInstanceState != null) {
            // Get the previous state of the stopwatch if the activity has been
            // destroyed and recreated
            seconds = savedInstanceState.getInt("seconds");
            running = savedInstanceState.getBoolean("running");
        }

        // initialise spinner/dropdown with file names from application directory
        String[] files = this.fileList();
        spinnerAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, files);
        workoutSelectionSpinner.setAdapter(spinnerAdapter);
        workoutSelectionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            // set current workout to the workout selected by user from the spinner/dropdown
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // reset all values as we have changed workout
                readFile();
                initialiseRecycler();
                seconds = 0;
                workoutExercises = 0;
                currentExercise = 0;
                exerciseSets = 0;
                currentSet = 0;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        // setup for notification
        createNotificationManager();
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Toast.makeText(context, "Intent Detected from inside",
                    Toast.LENGTH_LONG).show();
                if(running) {
                    running = false;
                } else {
                    running = true;
                }
            }
        };
        IntentFilter filter = new IntentFilter("android.intent.action.CUSTOM");
        registerReceiver(broadcastReceiver, filter);

        runTimer();
    }

    // save the state of the stopwatch if it's about to be destroyed
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putInt("seconds", seconds);
        savedInstanceState.putBoolean("running", running);
    }

    // if the activity is sent to background
    @RequiresApi(api = Build.VERSION_CODES.KITKAT_WATCH)
    @Override
    protected void onStop() {
        super.onStop();

        setNotification();
    }

    // if the activity is returned to
    @Override
    protected void onResume() {
        super.onResume();

        removeNotification();

        // compare the number of files to the number of spinner items; file(workout) may have been
        // deleted
        String[] files = this.fileList();
        if(spinnerAdapter.getCount() != files.length) {
            spinnerAdapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_item, files);
            workoutSelectionSpinner.setAdapter(spinnerAdapter);
        }

        // set the workout in the application class so it can be accessed from anywhere in the
        // application
        try {
            Application app = (Application)getApplication();
            workout = app.workout;
        } catch (Exception a) {
            // read selected file again if Application.workout is somehow null
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
    // create a new workout when the "Create" button is clicked
    public void onClickCreate(View view) {
        DialogFragment fragment = new CreateDialog();
        fragment.show(getSupportFragmentManager(), "Create_Workout");
    }

    // Start the stopwatch running when the "Start" button is clicked.
    public void onClickStart(View view) {
        if (running == true) {
            running = false;
            startStopBtn.setText("Start");
        } else {
            running = true;
            startStopBtn.setText("Stop");
        }
    }

    // Reset the stopwatch when the "Reset" button is clicked.
    public void onClickReset(View view) {
        if(currentSet != 0) {
            currentSet --;
        }
        running = false;
        seconds = 0;
    }

    // skip current set
    public void onClickSkip(View view) {
        running = false;
        seconds = 0;
    }

    // Begin and switch to the "ExerciseActivity" when "Workout" button is clicked.
    public void onClickWorkout(View view)  {
        startActivity(
                new Intent(this, WorkoutActivity.class));
    }
//---Private Methods--------------------------------------------------------------------------------
    // Sets the Number of seconds on the timer. The runTimer() method uses a Handler
    // to increment the seconds and update the text view
    private void runTimer() {
        // Get the text view and create new Handler
        final TextView timeView = (TextView)findViewById(R.id.mainTimeTxt);
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
                                    vHolder.itemView.findViewById(R.id.name)
                                            .setBackgroundColor(Color.WHITE);
                                    currentExerciseRecView.setBackgroundColor(Color.WHITE);
                                    currentExerciseRecView.setVisibility(View.GONE);

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
                                    prevHolder.itemView.findViewById(R.id.name)
                                            .setBackgroundColor(Color.WHITE);
                                    currentExerciseRecView.setBackgroundColor(Color.WHITE);
                                    currentExerciseRecView.setVisibility(View.GONE);
                                }
                                // find the current exercise and highlight it
                                RecyclerView.ViewHolder evHolder =
                                        workoutRecView.
                                                findViewHolderForAdapterPosition(currentExercise);
                                if(evHolder != null) {
                                    evHolder.itemView.findViewById(R.id.name)
                                            .setBackgroundColor(Color.GRAY);
                                    evHolder.itemView.findViewById(R.id.mainRec)
                                            .setVisibility(View.VISIBLE);
                                    currentExerciseRecView =
                                            (RecyclerView)evHolder.itemView.
                                                    findViewById(R.id.mainRec);
                                    currentExerciseRecView.setBackgroundColor(Color.LTGRAY);
                                    // set number of exercise sets remaining
                                    exerciseSets = currentExerciseRecView.getAdapter().getItemCount();
                                    // increment counter of total workout exercises
                                    currentExercise ++;
                                    // reset sets counter for the new exercise
                                    currentSet = 0;
                                }
                            }

                            // find the current set and extract the time value to put into the
                            // timer. Delayed as the view was just set to visible
                            // (RecyclerView changed)
                            currentExerciseRecView.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    // find the previous set and set its background back
                                    // to light grey
                                    RecyclerView.ViewHolder prevHolder =
                                            currentExerciseRecView.findViewHolderForAdapterPosition(
                                                    currentSet - 1);
                                    if (prevHolder != null) {
                                        prevHolder.itemView.setBackgroundColor(Color.LTGRAY);
                                    }
                                    // find current set and set its background to grey.
                                    // also set timer to time of set
                                    RecyclerView.ViewHolder svHolder =
                                            currentExerciseRecView.findViewHolderForAdapterPosition(
                                                    currentSet);
                                    if (svHolder != null) {
                                        EditText time = (EditText)svHolder.itemView.
                                                findViewById(R.id.time);
                                        seconds = Integer.parseInt(time.getText().toString());
                                        svHolder.itemView.setBackgroundColor(Color.GRAY);
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
                            mediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.ding);
                            mediaPlayer.setOnCompletionListener(
                                    new MediaPlayer.OnCompletionListener() {
                                @Override
                                public void onCompletion(MediaPlayer mp) {
                                    mediaPlayer.release();
                                    mediaPlayer = null;
                                }
                            });
                            mediaPlayer.start();
                        }

                        int hours = seconds / 3600;
                        int minutes = (seconds % 3600) / 60;
                        int secs = seconds % 60;

                        // format the seconds into hours, minutes, and seconds and set the timeView
                        // with the formatted seconds
                        String time = String.format(Locale.getDefault(),
                                "%d:%02d:%02d", hours, minutes, secs);
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

    // reads the file selected by the user
    private void readFile() {
        // gets the selected file from the spinner and set the title of the Application
        String fileName = "";
        try {
            fileName = workoutSelectionSpinner.getSelectedItem().toString();
        }
        catch(Exception a) {
            try {
                String[] files = this.fileList();
                fileName = files[0];
            } catch(Exception e) {}
        }

        // remove the .csv file extension from the file name
        String workoutName = fileName.replace(".csv", "");
        setTitle(workoutName);

        // create an array to deposit values into, then attempt to read the file
        ArrayList<String> readFile = new ArrayList<>();
        FileInputStream fis;
        try {
            File file = new File(getApplicationContext().getFilesDir(),fileName);
            if(!file.exists()) {
                final String example = "Deadlift,8,100,90,7,110,120,6,100,150\n" +
                        "Squat,8,100,90,7,110,120,6,100,150\n" +
                        "Benchpress,8,100,90,7,110,120,6,100,150";
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
            // create collection of ExerciseModel then add respective values from read file
            // to each ExerciseModel object
            workout = new WorkoutModel();
            workout.workout = workoutName;
            workout.exercises = new ArrayList<>();

            for (String line: readFile) {
                String[] separatedLine = line.split(",");

                ExerciseModel exercise = new ExerciseModel();
                exercise.exercise = separatedLine[0];
                exercise.sets = new ArrayList<>();
                for (int a = 1; a < separatedLine.length; a += 3) {
                    exercise.sets.add(new SetModel(Integer.parseInt(separatedLine[a]),
                            Double.parseDouble(separatedLine[a+1]),
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

    // sets the recycler with the values from Application.workout
    private void initialiseRecycler() {
        // number of exercises in workout
        workoutExercises = workout.exercises.size();
        // initialise workout RecyclerView
        workoutRecView.setLayoutManager(new LinearLayoutManager(this));
        workoutAdapter = new MainAdapter(this, workout.exercises);
        // set a listener for when the user clicks a RecyclerView item
        workoutAdapter.setOnClickListener(new MainAdapter.ClickListener() {
            @Override
            public void onClick(int position, View view) {
                // expands and collapses the RecyclerView of the item clicked (default is collapsed)
                RecyclerView recView = (RecyclerView)view.findViewById(R.id.mainRec);
                if(recView.getVisibility() == View.GONE) {
                    view.findViewById(R.id.mainRec).setVisibility(View.VISIBLE);
                } else {
                    view.findViewById(R.id.mainRec).setVisibility(View.GONE);
                }
            }
        });
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
                    evHolder.itemView.findViewById(R.id.name)
                            .setBackgroundColor(Color.GRAY);
                    evHolder.itemView.findViewById(R.id.mainRec)
                            .setVisibility(View.VISIBLE);
                    currentExerciseRecView =
                            (RecyclerView)evHolder.itemView.
                                    findViewById(R.id.mainRec);
                    currentExerciseRecView.setBackgroundColor(Color.LTGRAY);
                }
            }
        }, 10);
    }
//---Dialog Functionality---------------------------------------------------------------------------
    // when user clicks the affirmative (currently "yes") button of the dialog
    // also closes the dialog
    public void onDialogPositiveClick(DialogFragment dialog) {
        // cast the DialogFragment to custom dialog then get the text of the user input
        CreateDialog nDialog = (CreateDialog)dialog;
        String name = nDialog.title.getText().toString();
        if(name == null) {
            Toast.makeText(this, "Workout name cannot be empty.",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        // create a new file with the specified file name inputted by the user
        try (FileOutputStream fos = this.openFileOutput(name + ".csv",
                this.MODE_PRIVATE)) {
            fos.write("".getBytes());
        } catch (Exception e)  { }
        // adds the new file name to the selection spinner list
        String[] files = this.fileList();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, files);
        workoutSelectionSpinner.setAdapter(adapter);
    }
    // when user clicks the negative (currently "No") button of the dialog
    // does nothing currently; just closes the dialog
    public void onDialogNegativeClick(DialogFragment dialog) { }
//---Notification Functionality---------------------------------------------------------------------
    // notification for continuation of timer and view of timer in notification tab, after user has
    // left the application
    @RequiresApi(api = Build.VERSION_CODES.KITKAT_WATCH)
    private void setNotification() {
        notificationDisplayed = true;
        // go to the timer activity of the application when clicking the notification
        Intent intent = new Intent(MainActivity.this, MainActivity.class);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setAction(Intent.ACTION_MAIN);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(MainActivity.this,
                0, intent, 0);

        // buttons to allow stopping and starting the timer in the notification
        Intent toggleIntent = new Intent("android.intent.action.CUSTOM");
        PendingIntent pendingToggleIntent = PendingIntent.getBroadcast(getApplicationContext(),
                1, toggleIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // create the notification
        final NotificationCompat.Builder builder =
                new NotificationCompat.Builder(getApplicationContext(), "1");
        builder.setContentTitle(workout.workout)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(pendingIntent)
                .addAction(R.drawable.ic_launcher_foreground, "Start/Stop",
                        pendingToggleIntent)
                .setAutoCancel(true)
                .setOngoing(true)
                .setChannelId("1");

        // create new thread to handle the notification functionality
        final Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                // format the seconds into hours, minutes, and seconds
                int hours = seconds / 3600;
                int minutes = (seconds % 3600) / 60;
                int secs = seconds % 60;
                String time = String.format(Locale.getDefault(),
                        "%d:%02d:%02d", hours, minutes, secs);
                // update the progress bar and timer of the notification
                builder.setProgress(workoutExercises, currentExercise, false)
                        .setContentText(time);
                notificationManager.notify(1, builder.build());

                if(currentExercise == workoutExercises) {
                    builder.setContentText("You have completed your workout.")
                            .setProgress(0,0,false);
                    notificationManager.notify(1, builder.build());
                    return;
                }

                if(notificationDisplayed) {
                    handler.postDelayed(this, 100);
                }
            }
        });
    }

    // clear and destroy the notification
    private void removeNotification() {
        notificationDisplayed = false;
        notificationManager.cancel(1);
    }

    // Create the NotificationManager with a new NotificationChannel,
    // but only on API 26+ because
    // the NotificationChannel class is new and not in the support library
    private void createNotificationManager() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel notificationChannel = new NotificationChannel(
                    "1", name, importance);
            notificationChannel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }
}
