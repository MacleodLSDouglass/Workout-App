package com.example.stopwatch;

import com.example.stopwatch.models.WorkoutModel;

public class Application extends android.app.Application {
    public WorkoutModel workout;

    @Override
    public void onCreate() {
        super.onCreate();
    }
}
