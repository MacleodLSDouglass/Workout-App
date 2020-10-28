package com.example.stopwatch.activities.Main;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.example.stopwatch.Application;
import com.example.stopwatch.R;
import com.example.stopwatch.activities.Workout.WorkoutActivity;
import com.example.stopwatch.models.ExerciseModel;
import com.example.stopwatch.models.SetModel;
import com.example.stopwatch.models.WorkoutModel;

import org.jetbrains.annotations.NotNull;

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
    // RecyclerViews and adapter
    private RecyclerView workoutRecView;
    private RecyclerView currentExerciseRecView;
    private MainAdapter workoutAdapter;
    // spinner for selecting workout
    private Spinner workoutSelectionSpinner;
    private ArrayAdapter<String> spinnerAdapter;
    // start/stop button
    private Button startStopBtn;
    // the selected workout
    private WorkoutModel workout;
    // number of seconds displayed on timer.
    private int seconds = 0;
    // whether the timer keeps going after finishing a set or and exercise
    private boolean continuous;
    // whether the timer is running
    private static boolean running;
    // current exercise and set counters
    private int workoutExercises = 0;
    private int currentExercise = 0;
    private int exerciseSets = 0;
    private int currentSet = 0;
    // timer thread
    private Handler timerHandler;
    private Runnable timerRunnable;
    private TextView timerView;
    // notification objects
    private NotificationManager notificationManager;
    private Handler notificationHandler;
    private Runnable notificationRunnable;
    private NotificationCompat.Builder builder;
    private PendingIntent pendingToggleIntent;
    private BroadcastReceiver broadcastReceiver;
    // sound player
    private MediaPlayer mediaPlayer;
//---Activity Methods-------------------------------------------------------------------------------
    // when activity if created
    @RequiresApi(api = Build.VERSION_CODES.KITKAT_WATCH)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // find XML views
        workoutRecView = findViewById(R.id.mainRec);
        timerView = findViewById(R.id.mainTimeTxt);
        workoutSelectionSpinner = findViewById(R.id.mainSpinner);
        startStopBtn = findViewById(R.id.mainStartStopBtn);
        Button resetBtn = findViewById(R.id.mainResetBtn);
        // reset current exercise on long click
        resetBtn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                stopTimer();
                if(currentExercise != 0) {
                    if(currentSet > 1) {
                        // reset current exercise if currently within exercise
                        RecyclerView.ViewHolder prevHolder =
                                currentExerciseRecView.findViewHolderForAdapterPosition(
                                        currentSet - 1);
                        if (prevHolder != null) {
                            prevHolder.itemView.setBackgroundColor(Color.LTGRAY);
                        }
                        currentExercise --;
                    } else {
                        // go back an exercise if at the beginning of exercise
                        RecyclerView.ViewHolder prevHolder =
                                workoutRecView.
                                        findViewHolderForAdapterPosition(
                                                currentExercise - 1);
                        if (prevHolder != null) {
                            unFocusExercise(prevHolder);
                        }
                        currentExercise -= 2;
                    }
                }
                currentSet = 0;
                running = false;
                seconds = 0;
                setTimeExercise();
                return true;
            }
        });
        Button skipBtn = findViewById(R.id.mainSkipBtn);
        // skip current exercise on long click
        skipBtn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                stopTimer();
                currentSet = 0;
                running = false;
                seconds = 0;
                setTimeExercise();
                return true;
            }
        });
        ToggleButton continuousToggle = (ToggleButton) findViewById(R.id.mainContinuousBtn);
        continuousToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    continuous = true;
                    Toast.makeText(buttonView.getContext(),
                            "Timer will continue after finishing a set or an exercise.",
                            Toast.LENGTH_SHORT).show();
                }
                else {
                    continuous = false;
                    Toast.makeText(buttonView.getContext(),
                            "Timer will stop after finishing a set or and exercise.",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        // setup for running the timer. read file, initialise the main RecyclerView,
        // create the timer Handler and Runnable.
        timerHandler = new Handler();
        createTimerRunnable();

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
                stopTimer();
                seconds = 0;
                workoutExercises = 0;
                currentExercise = 0;
                exerciseSets = 0;
                currentSet = 0;
                readFile();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        // setup for notification
        createNotificationManager();
        notificationHandler = new Handler();
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(running) {
                    stopTimer();
                    createNotificationBuilder("Start");
                    Toast.makeText(context, "Timer Started",
                            Toast.LENGTH_LONG).show();
                } else {
                    runTimer();
                    createNotificationBuilder("Stop");
                    Toast.makeText(context, "Timer Stopped",
                            Toast.LENGTH_LONG).show();
                }
            }
        };
        IntentFilter filter = new IntentFilter("android.intent.action.CUSTOM");
        registerReceiver(broadcastReceiver, filter);
    }
    // if the activity is returned to
    @Override
    protected void onResume() {
        super.onResume();

        removeNotification();

        // compare the number of files to the number of spinner items; file(workout) may have been
        // deleted. If there are not remaining files call readFile to create a new Example.csv file
        String[] files = this.fileList();
        if(spinnerAdapter.getCount() != files.length) {
            spinnerAdapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_item, files);
            workoutSelectionSpinner.setAdapter(spinnerAdapter);
        }
        if(files.length == 0) {
            readFile();
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
    // if activity is closed
    @Override
    protected void onDestroy() {
        super.onDestroy();

        removeNotification();
    }
//---Buttons----------------------------------------------------------------------------------------
    // create a new workout when the "Create" button is clicked
    public void onClickCreate(View view) {
        DialogFragment fragment = new CreateDialog();
        fragment.show(getSupportFragmentManager(), "Create_Workout");
    }
    // Start the stopwatch running when the "Start" button is clicked.
    public void onClickStart(View view) {
        if (running == false) {
            running = true;
            runTimer();
        } else {
            running = false;
            stopTimer();
        }
    }
    // Reset the stopwatch when the "Reset" button is clicked.
    public void onClickReset(View view) {
        stopTimer();
        if(currentSet != 0) {
            currentSet --;
        }
        running = false;
        seconds = 0;
        setTimeSet();
    }
    // skip current set
    public void onClickSkip(View view) {
        stopTimer();
        running = false;
        seconds = 0;
        if(currentSet == exerciseSets) {
            setTimeExercise();
        } else{
            setTimeSet();
        }
    }
    // Begin and switch to the "ExerciseActivity" when "Workout" button is clicked.
    public void onClickWorkout(View view)  {
        startActivity(
                new Intent(this, WorkoutActivity.class));
    }
//---Private Methods--------------------------------------------------------------------------------
    // Sets the Number of seconds on the timer. The runTimer() method uses a Handler
    // to increment the seconds and update the text view
    private void createTimerRunnable() {
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                if (seconds == 0 && !workoutAdapter.workout.isEmpty()) {
                    // when all sets in exercise have been completed
                    if (currentSet == exerciseSets) {
                        // find current set and set its background back to light grey
                        RecyclerView.ViewHolder vHolder =
                                currentExerciseRecView.findViewHolderForAdapterPosition(
                                        currentSet - 1);
                        if (vHolder != null) {
                            vHolder.itemView.setBackgroundColor(Color.LTGRAY);
                        }
                        setTimeExercise();
                        // when all exercises have been completed
                        if (currentExercise - 1 == workoutExercises) {
                            // reset everything. stop timer and return
                            workoutExercises = 0;
                            currentExercise = 0;
                            exerciseSets = 0;
                            currentSet = 0;
                            setTimeExercise();
                            Toast.makeText(MainActivity.this,
                                    "Well done! You have completed the workout.",
                                    Toast.LENGTH_SHORT).show();
                            startStopBtn.setText("Start");
                            stopTimer();
                            return;
                         }
                     }
                    else {
                        setTimeSet();
                    }

                    // play the set done sound
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

                    // stop the timer
                    if (continuous == false) {
                        startStopBtn.setText("Start");
                        stopTimer();
                        return;
                    }
                }
                setTimerText();
                // decrement the seconds variable.
                seconds --;
                // post the code again with a delay of 1 second
                timerHandler.postDelayed(this, 1000);
            }
        };
    }
    // sets the text of the timer view to the time of the current set
    private void setTimeExercise() {
        // find previous exercise and un-focus it
        RecyclerView.ViewHolder prevHolder =
                workoutRecView.
                        findViewHolderForAdapterPosition(
                                currentExercise - 1);
        if (prevHolder != null) {
            unFocusExercise(prevHolder);
        }
        // find the current exercise and highlight it
        RecyclerView.ViewHolder vHolder =
                workoutRecView.
                        findViewHolderForAdapterPosition(currentExercise);
        if (vHolder != null) {
            focusExercise(vHolder);
            currentExerciseRecView =
                    (RecyclerView) vHolder.itemView.
                            findViewById(R.id.mainRec);
            currentExerciseRecView.setBackgroundColor(Color.LTGRAY);
        }

        // set number of exercise sets remaining
        exerciseSets = currentExerciseRecView.getAdapter().getItemCount();
        // increment counter of total workout exercises
        currentExercise++;
        // reset sets counter for the new exercise
        currentSet = 0;

        setTimeSet();
    }
    // sets the text of the timer view to the time of the current set
    private void setTimeSet() {
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
                RecyclerView.ViewHolder vHolder =
                        currentExerciseRecView.findViewHolderForAdapterPosition(
                                currentSet);
                if (vHolder != null) {
                    EditText time = (EditText) vHolder.itemView.
                            findViewById(R.id.time);
                    seconds = Integer.parseInt(time.getText().toString());
                    vHolder.itemView.setBackgroundColor(Color.GRAY);
                }
                // increment counter for current set
                currentSet++;
                // set text of timer to current set
                setTimerText();
            }
        }, 10);
    }
    // formats the 'seconds' variable to hours:minutes:seconds and sets the text of the timer view
    private void setTimerText() {
        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        int secs = seconds % 60;

        // format the set the view text
        String time = String.format(Locale.getDefault(),
                "%d:%02d:%02d", hours, minutes, secs);
        timerView.setText(time);
    }
    // post the runnable
    private void runTimer() {
        // delay so RecyclerView does not return null values
        if(workoutAdapter.workout.isEmpty()) {
            Toast.makeText(this, "There is no workout selected.",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        timerRunnable.run();
        running = true;
        startStopBtn.setText("Stop");

        // keeps notification open if timer is running
        if(builder != null)
        {
            builder.setOngoing(true);
        }
    }
    // stop the runnable
    private void stopTimer() {
        timerHandler.removeCallbacks(timerRunnable);
        running = false;
        startStopBtn.setText("Start");
        // allow user to close notification when timer is not running
        if(builder != null)
        {
            builder.setOngoing(false);
        }
    }
    // focuses(expands and highlights) a ViewHolder
    private void focusExercise(@NotNull RecyclerView.ViewHolder holder) {
        holder.itemView.findViewById(R.id.mainRec)
                .setVisibility(View.VISIBLE);
        holder.itemView.findViewById(R.id.name)
                .setBackgroundColor(Color.GRAY);
    }
    // un-focuses(collapses and un-highlights) a ViewHolder
    private void unFocusExercise(@NotNull RecyclerView.ViewHolder holder) {
        holder.itemView.findViewById(R.id.name)
                .setBackgroundColor(Color.WHITE);
        holder.itemView.findViewById(R.id.mainRec)
                .setVisibility(View.GONE);
    }
    // reads file. Called in onCreate() and when user selects workout with workoutSpinner.
    private void readFile() {
        // gets the selected file from the spinner and set the title of the Application
        String fileName = "Example.csv";
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
            // if file doesn't exist, create a example workout
            File file = new File(getApplicationContext().getFilesDir(), fileName);
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

        initialiseRecycler();
    }
    // sets the recycler with the values from Application.workout. Called in readFile()
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
                setTimeExercise();
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
    // sent the activity to the background
    // creates the Runnable and posts uses the Handler to post that Runnable. Called in onStop()
    private void setNotification() {
        createNotificationRunnable();
        notificationHandler.post(notificationRunnable);
    }
    // destroy the Runnable of the Handler and cancel the notification
    private void removeNotification() {
        notificationHandler.removeCallbacks(notificationRunnable);
        notificationManager.cancel(1);
    }
    // Create the NotificationManager with a new NotificationChannel, but only on API 26+ because
    // the NotificationChannel class is new and not in the support library
    // called in onCreate()
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
    // create the runnable that sets up the notification called in setNotification()
    // and when the user leaves the main page
    private void createNotificationRunnable() {
        if(running == true) {
            createNotificationBuilder("Stop");
        } else {
            createNotificationBuilder("Start");
        }
        notificationRunnable = new Runnable () {
            @Override
            public void run() {
                // format the seconds into hours, minutes, and seconds
                int hours = seconds / 3600;
                int minutes = (seconds % 3600) / 60;
                int secs = seconds % 60;
                String time = String.format(Locale.getDefault(),
                        "%d:%02d:%02d", hours, minutes, secs);
                    // update the progress bar and timer of the notification
                    builder.setProgress(workoutExercises, currentExercise - 1 ,
                            false)
                            .setContentText(time);
                    notificationManager.notify(1, builder.build());

                    if(currentExercise == workoutExercises) {
                        builder.setContentText("You have completed your workout.")
                                .setProgress(0,0,false);
                        notificationManager.notify(1, builder.build());
                        return;
                    }

                    notificationHandler.postDelayed(this, 100);

            }
        };
    }

    private void createNotificationBuilder(String actionTitle) {
        Intent intent = new Intent(MainActivity.this, MainActivity.class);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setAction(Intent.ACTION_MAIN);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(MainActivity.this,
                0, intent, 0);

        // buttons to allow stopping and starting the timer in the notification
        Intent toggleIntent = new Intent("android.intent.action.CUSTOM");
        pendingToggleIntent = PendingIntent.getBroadcast(getApplicationContext(),
                1, toggleIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // create the notification
        builder = new NotificationCompat.Builder(getApplicationContext(), "1");
        builder.setContentTitle(workout.workout)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(pendingIntent)
                .setPriority(Notification.PRIORITY_MAX)
                .setWhen(0)
                .addAction(R.drawable.ic_launcher_foreground, actionTitle,
                        pendingToggleIntent)
                .setChannelId("1");
        if(running == true) {
            builder.setOngoing(true);
        }
    }
}
