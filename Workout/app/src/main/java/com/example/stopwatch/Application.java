package com.example.stopwatch;

import com.example.stopwatch.models.ExerciseModel;

import java.util.ArrayList;

public class Application extends android.app.Application {
    public ArrayList<ExerciseModel> workout;

    @Override
    public void onCreate() {
        super.onCreate();
    }
}
