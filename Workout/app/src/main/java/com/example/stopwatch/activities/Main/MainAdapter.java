package com.example.stopwatch.activities.Main;

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

// a specific custom Adapter for each RecyclerView in the Application as they require different
// functionality and have different item types
public class MainAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    // collection of values to be set into ViewHolder Views
    public List<ExerciseModel> workout;

    private LayoutInflater lInf;
    private MainAdapter.ClickListener clickListener;

    MainAdapter(Context con, List<ExerciseModel> workout) {
        this.workout = workout;
        this.lInf = LayoutInflater.from(con);
    }

    // called when ViewHolder is created; called number of times equal to getItemCount()
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = lInf.inflate(
                R.layout.exercise_with_recview_layout, parent, false);
        return new MainAdapter.ViewHolder(view);
    }

    // called after onCreateViewHolder() to bind values to the items
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        MainAdapter.ViewHolder nHolder = (MainAdapter.ViewHolder)holder;

        ExerciseModel exercise = workout.get(position);

        nHolder.exercise.setText(exercise.exercise);

        LinearLayoutManager lMan = new LinearLayoutManager(
                nHolder.sets.getContext(), RecyclerView.VERTICAL, false);
        lMan.setInitialPrefetchItemCount(4);

        nHolder.sets.setLayoutManager(lMan);
        MainAdapter.ExerciseAdapter exerciseAdapter = new MainAdapter.ExerciseAdapter(
                nHolder.sets.getContext(), exercise.sets);
        nHolder.sets.setAdapter(exerciseAdapter);
    }

    // number of ViewHolders created and size of collection given to RecyclerView
    @Override
    public int getItemCount() {
        return workout.size();
    }

    // custom ViewHolder for unique View
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView exercise;
        RecyclerView sets;

        ViewHolder(View itemView) {
            super(itemView);
            exercise = itemView.findViewById(R.id.name);
            sets = itemView.findViewById(R.id.mainRec);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) { clickListener.onClick(getAdapterPosition(), view);}

    }

    public void setOnClickListener(MainAdapter.ClickListener clickListener) {
        this.clickListener = clickListener;
    }

    // functionality implemented in MainActivity class
    public interface ClickListener {
        void onClick(int position, View view);
    }

    // custom child adapter for the list of sets for each exercise in the list of exercises
    // of the workout
    public  class ExerciseAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        public List<SetModel> sets;

        private LayoutInflater lInf;

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
            nHolder.weight.setText(Double.toString(set.weight));
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
                reps = itemView.findViewById(R.id.reps);
                weight = itemView.findViewById(R.id.weight);
                time = itemView.findViewById(R.id.time);
            }
        }
    }
}

