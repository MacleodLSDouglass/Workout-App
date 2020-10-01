package com.example.stopwatch.models;

import java.util.ArrayList;

public class WorkoutModel {
    public String workout;
    public ArrayList<ExerciseModel> exercises;

    public WorkoutModel () { }
    public WorkoutModel(String workout, ArrayList<ExerciseModel> exercises) {
        this.workout = workout;
        this.exercises = exercises;
    }
}
