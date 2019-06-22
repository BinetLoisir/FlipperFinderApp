package com.pinmyballs;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.pinmyballs.utils.ProgressBarHandler;

public class TestActivity extends AppCompatActivity {

    FloatingActionButton fab;
    ProgressBarHandler mProgressBarHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        mProgressBarHandler = new ProgressBarHandler(this); // In onCreate

        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mProgressBarHandler.getVisibility() == View.INVISIBLE) {
                    mProgressBarHandler.show(); // To show the progress bar
                } else {
                    mProgressBarHandler.hide(); // To hide the progress bar
                }
                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).show();
            }
        });
    }

}
