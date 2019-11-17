package com.pinmyballs;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.pinmyballs.fragment.Fragment1;
import com.pinmyballs.fragment.Fragment2;
import com.pinmyballs.utils.ProgressBarHandler;

public class TestActivity extends AppCompatActivity implements Fragment1.Fragment1Listener, Fragment2.Fragment2Listener {

    private Fragment1 fragment1;
    private Fragment2 fragment2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        fragment1 = new Fragment1();
        fragment2 = new Fragment2();

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.frame1, fragment1)
                .replace(R.id.frame2, fragment2)
        .commit();
    }

    @Override
    public void sendTextToFragment2(CharSequence charSequence) {
        fragment2.UpdateEditText(charSequence);
    }

    @Override
    public void sendTextToFragment1(CharSequence charSequence) {
        fragment1.UpdateEditText(charSequence);
    }
}
