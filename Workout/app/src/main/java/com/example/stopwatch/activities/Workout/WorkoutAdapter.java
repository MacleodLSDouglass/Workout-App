package com.example.stopwatch.activities.Workout;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stopwatch.R;
import com.example.stopwatch.models.ExerciseModel;

import java.util.List;

public class WorkoutAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    // collection of values to be set into Views
    public List<ExerciseModel> workout;

    private LayoutInflater lInf;
    private ClickListener clickListener;

    public WorkoutAdapter(Context context, List<ExerciseModel> workout) {
        this.workout = workout;
        this.lInf = LayoutInflater.from(context);
    }

    // inflates the row layout from xml based on getItemCount()
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = lInf.inflate(R.layout.exercise_layout, parent, false);
        return new WorkoutAdapter.ViewHolder(view);
    }

    // binds the data to the Views in each row
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
    private class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView exercise;
        TextView sets;

        ViewHolder(View itemView) {
            super(itemView);
            exercise = itemView.findViewById(R.id.name);
            sets = itemView.findViewById(R.id.sets);
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
