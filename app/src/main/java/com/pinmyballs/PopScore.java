package com.pinmyballs;

import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.pinmyballs.fragment.FragmentDialogScore;
import com.pinmyballs.fragment.FragmentHiScoreFlipper;
import com.pinmyballs.metier.Flipper;
import com.pinmyballs.metier.Score;
import com.pinmyballs.service.ScoreService;
import com.pinmyballs.utils.ListeScoresAdapter;

import java.util.ArrayList;

public class PopScore extends AppCompatActivity {

    private static final String TAG = "PopScore";
    
    Flipper flipper;
    ScoreService scoreService;
    ArrayList<Score> listeScores = new ArrayList<>();
    ListView listeScoreView = null;
    FloatingActionButton fab;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: ");
        setContentView(R.layout.pop_score);
        listeScoreView = findViewById(R.id.listScoreView);

        fab = findViewById(R.id.fabscore);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                //        .setAction("Action", null).show();
                FragmentDialogScore newFragment = new FragmentDialogScore();
                newFragment.show(getSupportFragmentManager(), "score input");
                //refreshScore();

            }
        });

        //Adjust Popup size
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        getWindow().setLayout((int) (width * 0.9), (int) (height * 0.6));

        //Get flipper
        Intent intent = getIntent();
        flipper = (Flipper) intent.getSerializableExtra(PageInfoFlipperPager.INTENT_FLIPPER_POUR_INFO);

        //GetScores
        scoreService = new ScoreService(new FragmentHiScoreFlipper.FragmentCallback() {
            @Override
            public void onTaskDone() {
                refreshScore();
            }
        });

        refreshScore();
    }

    private void refreshScore() {
        long flipperId = flipper.getId();
        //GetScores
        if (flipperId != 0) {
            listeScores = scoreService.getScoresByFlipperId(getApplicationContext(), flipperId);
            ListeScoresAdapter listeScoresAdapter = new ListeScoresAdapter(this, R.layout.simple_list_item_score, listeScores);
            //SetAdapter
            listeScoreView.setAdapter(listeScoresAdapter);
            Log.d(TAG, "refreshScore: UpdatedAdapter---------->" +listeScores.size());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: ");
        refreshScore();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: ");
        refreshScore();

    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop: ");
        refreshScore();

    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: ");
        refreshScore();

    }
}





