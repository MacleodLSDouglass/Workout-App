package com.example.stopwatch.activities.MainActivity;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stopwatch.R;
import com.example.stopwatch.models.ExerciseModel;
import com.example.stopwatch.models.SetModel;

import java.util.List;

public class MainAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    LayoutInflater lInf;
    List<ExerciseModel> workout;

    MainAdapter(Context con, List<ExerciseModel> workout) {
        this.lInf = LayoutInflater.from(con);
        this.workout = workout;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = lInf.inflate(
                R.layout.exercise_with_recview_layout, parent, false);
        return new MainAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        MainAdapter.ViewHolder nHolder = (MainAdapter.ViewHolder)holder;

        ExerciseModel exercise = workout.get(position);

        nHolder.exercise.setText(exercise.exercise);

        LinearLayoutManager lMan = new LinearLayoutManager(
                nHolder.sets.getContext(), RecyclerView.VERTICAL, false);
        lMan.setInitialPrefetchItemCount(4);

        nHolder.sets.setLayoutManager(lMan);
        nHolder.sets.setAdapter(new MainAdapter.ExerciseAdapter(nHolder.sets.getContext(), exercise.sets));
    }

    @Override
    public int getItemCount() {
        return workout.size();
    }

    private class ViewHolder extends RecyclerView.ViewHolder {
        TextView exercise;
        RecyclerView sets;

        ViewHolder(View itemView) {
            super(itemView);
            exercise = itemView.findViewById(R.id.exerciseTxt);
            sets = itemView.findViewById(R.id.exerciseRec);
        }
    }

    // create the child adapter for the list of sets for each exercise in the list of exercises
    // of the workout
    private class ExerciseAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        LayoutInflater lInf;
        List<SetModel> sets;

        ExerciseAdapter(Context con, List<SetModel> sets) {
            this.lInf = LayoutInflater.from(con);
            this.sets = sets;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                                          int viewType) {
            View view = lInf.inflate(R.layout.set_layout, parent, false);
            return new MainAdapter.ExerciseAdapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            MainAdapter.ExerciseAdapter.ViewHolder nHolder = (MainAdapter.ExerciseAdapter.ViewHolder)holder;

            SetModel set = sets.get(position);

            nHolder.reps.setText(Integer.toString(set.reps));
            nHolder.weight.setText(Integer.toString(set.weight));
            nHolder.time.setText(Integer.toString(set.time));
        }

        @Override
        public int getItemCount() {
            return sets.size();
        }

        private class ViewHolder extends RecyclerView.ViewHolder {
            public EditText reps;
            public EditText weight;
            public EditText time;

            ViewHolder(View itemView) {
                super(itemView);
                reps = itemView.findViewById(R.id.setReps);
                weight = itemView.findViewById(R.id.setWeight);
                time = itemView.findViewById(R.id.setTime);
            }
        }
    }
}

