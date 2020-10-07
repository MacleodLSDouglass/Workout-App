package com.example.stopwatch;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import com.example.stopwatch.models.WorkoutModel;

public class Application extends android.app.Application {
    // first set in MainActivity when file is read.
    // collection is used and updated throughout entire application
    public WorkoutModel workout;
    // weight conversion to be done
    public int currentWeightMetric;

    @Override
    public void onCreate() {
        super.onCreate();
    }
}
