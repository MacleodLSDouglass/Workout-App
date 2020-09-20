package com.example.stopwatch.models;

import java.util.ArrayList;

public class ExerciseModel {
    public String exercise;
    public ArrayList<SetModel> sets;

    public ExerciseModel() {}
    public ExerciseModel(String exercise, ArrayList<SetModel> sets) {
        this.exercise = exercise;
        this.sets = sets;
    }
}
