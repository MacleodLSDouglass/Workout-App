package com.example.stopwatch.activities;

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
            workout = app.workout;
        } catch (Exception e) {
            finish();
        }

        recView.setLayoutManager(new LinearLayoutManager(this));
        recView.setAdapter(new Adapter(this, workout));
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

    // --------------------------------------------------------------------------------------------
    // adapter for RecyclerView
    private class Adapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private LayoutInflater lInf;
        private List<ExerciseModel> workout;

        // data is passed into the constructor
        public Adapter(Context context, List<ExerciseModel> workout) {
            this.lInf = LayoutInflater.from(context);
            this.workout = workout;
        }

        // inflates the row layout from xml when needed
        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = lInf.inflate(R.layout.workout_layout, parent, false);
            return new ViewHolder(view);
        }

        // binds the data to the TextView in each row
        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            ViewHolder nHolder = (ViewHolder) holder;
            ExerciseModel exercise = workout.get(position);

            nHolder.exercise.setText(exercise.exercise);
            nHolder.sets.setText(Integer.toString(exercise.sets.size()));
        }

        // total number of rows
        @Override
        public int getItemCount() {
            return workout.size();
        }

        // stores and recycles views as they are scrolled off screen
        private class ViewHolder extends RecyclerView.ViewHolder
                implements View.OnClickListener {
            TextView exercise;
            TextView sets;

            ViewHolder(View itemView) {
                super(itemView);
                exercise = itemView.findViewById(R.id.workoutExercise);
                sets = itemView.findViewById(R.id.workoutSets);
                itemView.setOnClickListener(this);
            }

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(lInf.getContext(), Exercise.class);
                intent.putExtra("index", getAdapterPosition());
                startActivity(intent);
            }
        }
    }
}