package com.example.stopwatch.activities.Workout;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stopwatch.R;
import com.example.stopwatch.activities.Exercise.Exercise;
import com.example.stopwatch.models.ExerciseModel;

import java.util.List;

public class WorkoutAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private LayoutInflater lInf;
    private List<ExerciseModel> workout;
    private ClickListener clickListener;

    // data is passed into the constructor
    public WorkoutAdapter(Context context, List<ExerciseModel> workout) {
        this.lInf = LayoutInflater.from(context);
        this.workout = workout;
    }

    // inflates the row layout from xml when needed
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = lInf.inflate(R.layout.exercise_layout, parent, false);
        return new WorkoutAdapter.ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        WorkoutAdapter.ViewHolder nHolder = (WorkoutAdapter.ViewHolder) holder;
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
            clickListener.onClick(getAdapterPosition(), view);
        }
    }

    public void setOnClickListener(ClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public interface ClickListener {
        void onClick(int position, View view);
    }
}
