package com.pinmyballs.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.pinmyballs.PreferencesActivity;
import com.pinmyballs.R;
import com.pinmyballs.metier.Flipper;
import com.pinmyballs.metier.Score;
import com.pinmyballs.service.ScoreService;
import com.pinmyballs.utils.ListeScoresAdapter;

import java.util.ArrayList;

public class FragmentHiScoreFlipper extends Fragment {
    private static final String TAG = "FragmentHiScoreFlipper";
    ScoreService scoreService;
    ArrayList<Score> listeScores = new ArrayList<Score>();
    ListView listeScoreView = null;
    FloatingActionButton fab;
    TextView TVnoscore;
    private SharedPreferences settings;
    private Flipper flipper;
    private String pseudo;


    public FragmentHiScoreFlipper(){
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_score_flipper, container, false);
        super.onCreate(savedInstanceState);

        flipper = (Flipper) getArguments().getSerializable("flip");
        pseudo = (String) getArguments().getString("pseudo");


        //Widgets
        settings = requireActivity().getSharedPreferences(PreferencesActivity.PREFERENCES_FILENAME, 0);
        listeScoreView = (ListView) rootView.findViewById(R.id.listScoreViewFrag);
        fab = (FloatingActionButton) rootView.findViewById(R.id.fabscorefrag);
        TVnoscore = (TextView) rootView.findViewById(R.id.noscore);

        fab.setOnClickListener(view -> {
            FragmentDialogScore fragmentDialogScore = new FragmentDialogScore();
            Bundle bundle =new Bundle();
            bundle.putString("pseudo", pseudo);
            fragmentDialogScore.setArguments(bundle);
            fragmentDialogScore.show(requireFragmentManager(), "score input");
        });

        loadScores();

        return rootView;

    }

    private void loadScores() {
        long flipperId = flipper.getId();
        //GetScores
        if (flipperId != 0) {
            scoreService = new ScoreService(new FragmentHiScoreFlipper.FragmentCallback() {
                @Override
                public void onTaskDone() {
                    Log.d(TAG, "onTaskDone: creation of ScoreService in FragmentScore");
                }
            });
            listeScores = scoreService.getScoresByFlipperId(getActivity(), flipperId);
            TVnoscore.setVisibility(listeScores.size() == 0 ? View.VISIBLE : View.INVISIBLE);
            ListeScoresAdapter listeScoresAdapter = new ListeScoresAdapter(requireActivity(), R.layout.simple_list_item_score, listeScores);
            //SetAdapter
            listeScoreView.setAdapter(listeScoresAdapter);
            Log.d(TAG, "loadScores: loaded " +listeScores.size() + " scores");
        }
    }


    //AJOUT INTERFACE TEST
    public void receivedScore(Score score){
        Toast toast = Toast.makeText(getContext(), "Score reçu : " + String.valueOf(score.getScore()), Toast.LENGTH_SHORT);
        toast.show();
    }


    public interface FragmentCallback {
        void onTaskDone();
    }

}
