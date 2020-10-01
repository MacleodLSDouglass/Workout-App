package com.example.stopwatch.activities.Workout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.stopwatch.Application;
import com.example.stopwatch.R;
import com.example.stopwatch.activities.Exercise.Exercise;
import com.example.stopwatch.models.ExerciseModel;
import com.example.stopwatch.models.SetModel;

import java.util.ArrayList;
import java.util.List;

public class Workout extends AppCompatActivity{
    private RecyclerView recView;
    private List<ExerciseModel> workout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout);

        recView = findViewById(R.id.workoutRecycler);
    }

    protected void onResume() {
        super.onResume();

        try {
            Application app = (Application)getApplication();
            workout = app.workout.exercises;
        } catch (Exception e) {
            finish();
        }

        recView.setLayoutManager(new LinearLayoutManager(this));
        WorkoutAdapter adapter = new WorkoutAdapter(this, workout);
        adapter.setOnClickListener(new WorkoutAdapter.ClickListener() {
            @Override
            public void onClick(int position, View view) {
                Intent intent = new Intent(getApplicationContext(), Exercise.class);
                intent.putExtra("index", position);
                startActivity(intent);
            }
        });
        recView.setAdapter(adapter);
    }

    // activates the "Create" button is clicked. Starts "Workout" activity with a null index
    // indicating a new exercise
    public void onClickCreate(View view)  {
        workout.add(new ExerciseModel("", new ArrayList<SetModel>()));
        int lastIndex = workout.size() - 1;

        Intent intent = new Intent(this, Exercise.class);
        intent.putExtra("index", lastIndex);
        startActivity(intent);
    }

    // activates when clicking "Timer" button
    public void onClickTimer(View view)  {
        finish();
    }
}