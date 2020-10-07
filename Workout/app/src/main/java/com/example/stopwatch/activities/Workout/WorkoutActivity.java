package com.example.stopwatch.activities.Workout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.stopwatch.Application;
import com.example.stopwatch.R;
import com.example.stopwatch.activities.Exercise.ExerciseActivity;
import com.example.stopwatch.activities.Main.CreateDialog;
import com.example.stopwatch.activities.Main.MainActivity;
import com.example.stopwatch.models.ExerciseModel;
import com.example.stopwatch.models.SetModel;
import com.example.stopwatch.models.WorkoutModel;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class WorkoutActivity extends AppCompatActivity implements DeleteDialog.FragmentModelListener{
    private TextView totalExercises;
    private RecyclerView recView;

    private WorkoutModel workout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout);

        // find required XML views
        totalExercises = findViewById(R.id.workoutIndex);
        recView = findViewById(R.id.workoutRec);
    }

    protected void onResume() {
        super.onResume();

        // get the current selected workout from the Application
        try {
            Application app = (Application)getApplication();
            workout = app.workout;
        } catch (Exception e) {
            finish();
        }

        // set title and total number of exercises in Application.workout
        setTitle(workout.workout);
        totalExercises.setText(Integer.toString(workout.exercises.size()));

        // initialise the RecyclerView
        recView.setLayoutManager(new LinearLayoutManager(this));
        WorkoutAdapter adapter = new WorkoutAdapter(this, workout.exercises);
        // set listener for when user clicks item in RecyclerView.
        adapter.setOnClickListener(new WorkoutAdapter.ClickListener() {
            // starts "WorkoutActivity" with the index of the clicked item
            @Override
            public void onClick(int position, View view) {
                Intent intent = new Intent(getApplicationContext(), ExerciseActivity.class);
                intent.putExtra("index", position);
                startActivity(intent);
            }
        });
        recView.setAdapter(adapter);
    }

    public void onClickDelete(View view) {
        DialogFragment fragment = new DeleteDialog();
        fragment.show(getSupportFragmentManager(), "Delete_Workout");
    }

    // when the "Create" button is clicked. Starts "WorkoutActivity" with a null index
    // indicating a new exercise
    public void onClickCreate(View view)  {
        workout.exercises.add(new ExerciseModel("", new ArrayList<SetModel>()));
        int lastIndex = workout.exercises.size() - 1;

        Intent intent = new Intent(this, ExerciseActivity.class);
        intent.putExtra("index", lastIndex);
        startActivity(intent);
    }

    // activates when clicking "Timer" button
    public void onClickTimer(View view)  {
        finish();
    }
//---Dialog Functionality---------------------------------------------------------------------------
    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        if(!this.deleteFile(workout.workout + ".csv")) {
            Toast.makeText(WorkoutActivity.this,
                    "Something went wrong with deleting the file.",
                    Toast.LENGTH_SHORT).show();
        }
        workout = null;
        finish();
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) { }
}