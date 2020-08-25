package com.example.stopwatch.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.stopwatch.Exercise;
import com.example.stopwatch.R;

public class Workout extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout);
    }

    public void onClickExercise(View view)  {
        Intent intent = new Intent(this, Exercise.class);
        startActivity(intent);
    }

    public void onClickTimer(View view)  {
        finish();
    }
}