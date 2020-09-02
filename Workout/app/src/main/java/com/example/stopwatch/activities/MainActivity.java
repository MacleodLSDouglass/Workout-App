package com.example.stopwatch.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.stopwatch.R;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    // Use seconds, running and wasRunning respectively
    // to record the number of seconds passed,
    // whether the stopwatch is running and
    // whether the stopwatch was running
    // before the activity was paused.

    // Number of seconds displayed
    // on stopwatch.
    private int seconds = 0;

    // Is the stopwatch running?
    private boolean running;
    private boolean wasRunning;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState != null) {
            // Get the previous state of the stopwatch
            // if the activity has been
            // destroyed and recreated
            seconds = savedInstanceState.getInt("seconds");
            running = savedInstanceState.getBoolean("running");
            wasRunning = savedInstanceState.getBoolean("wasRunning");
        }

        runTimer();
    }

    // Save the state of the stopwatch
    // if it's about to be destroyed
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putInt("seconds", seconds);
        savedInstanceState.putBoolean("running", running);
        savedInstanceState.putBoolean("wasRunning", wasRunning);
    }

    // If the activity is paused,
    // stop the stopwatch
    @Override
    protected void onPause() {
        super.onPause();
        wasRunning = running;
        running = false;
    }

    // If the activity is resumed,
    // start the stopwatch
    // again if it was running previously
    @Override
    protected void onResume() {
        super.onResume();
        if (wasRunning) {
            running = true;
        }
    }

    // Start the stopwatch running
    // when the Start button is clicked.
    // Below method gets called
    // when the Start button  is clicked
    public void onClickStart(View view) {
        final Button startStop = (Button)findViewById(R.id.start_stop);
//        startStop.setText("Stop");
//        running = true;

        if (running == true) {
            running = false;
            startStop.setText("Start");
        } else {
            running = true;
            startStop.setText("Stop");
        }
    }

    // Stop the stopwatch running
    // when the Stop button is clicked.
    // Below method gets called
    // when the Stop button is clicked
    public void onClickStop(View view) {
        running = false;
    }

    // Reset the stopwatch when
    // the Reset button is clicked.
    // Below method is called
    // when Reset button is clicked
    public void onClickReset(View view) {
        running = false;
        seconds = 0;
    }

    // Sets the Number of seconds on the timer.
    // The runTimer() method uses a Handler
    // to increment the seconds and
    // update the text view
    private void runTimer() {
        // Get the text view
        final TextView timeView = (TextView)findViewById(R.id.time_view);

        //Creates a new Handler
        final Handler handler = new Handler();

        // Call the post() method,
        // passing in the new Runnable.
        // The post() method processes
        // code without a delay,
        // so the code in the Runnable
        // will run almost immediately.
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

    // Begin and switch to the Exercise activity when 'Workout' button is clicked.
    public void onClickWorkout(View view)  {
        Intent intent = new Intent(this, Workout.class);
        startActivity(intent);
    }

    // Read the exercises file when 'Read' button is clicked.
    public void onClickRead(View view) {
        TextView exerciseView = (TextView)findViewById(R.id.exercises_view);
        String fileName = "exercises";

        // Try to open file.
        FileInputStream fis = null;
        try {
            fis = this.openFileInput(fileName);
        }
        catch(Exception e) {
            e.printStackTrace();
        }

        // Try to read file in UTF_8 charset. Append lines to StringBuilder object,
        // convert StringBuilder to String and set text of exerciseView to String
        // so contents of file can be displayed on the screen.
        InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(isr)) {
            String line = reader.readLine();
            while (line != null) {
                sb.append(line).append('\n');
                line = reader.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            exerciseView.setText(sb.toString());
        }

        // Try to close file.
        try {
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
