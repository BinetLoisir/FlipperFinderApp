package com.pinmyballs.fragment;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.pinmyballs.PageInfoFlipperPager;
import com.pinmyballs.PreferencesActivity;
import com.pinmyballs.R;
import com.pinmyballs.metier.Flipper;
import com.pinmyballs.metier.Score;
import com.pinmyballs.service.ScoreService;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class FragmentDialogScore extends DialogFragment {

    private static final String TAG = "FragmentDialogScore";
    SharedPreferences settings;
    TextView PseudoTV, ScoreTV;
    String pseudo;
    //Score newScore;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog
        View view = inflater.inflate(R.layout.dialog_enterscore, null);
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(view);


        PseudoTV = (TextView) view.findViewById(R.id.PseudoNewScore);
        //Get the Pseudo
        settings = getActivity().getSharedPreferences(PreferencesActivity.PREFERENCES_FILENAME, 0);
        pseudo = settings.getString(PreferencesActivity.KEY_PSEUDO_FULL, "");
        Log.d(TAG, "onViewCreated: pseudo " + pseudo);
        PseudoTV.setText(pseudo);
        ScoreTV = (TextView) view.findViewById(R.id.ScoreNewScore);


        builder.setTitle(R.string.boutonSoumettreScore);
        builder.setPositiveButton(R.string.boutonValideScore, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //check fields
                if (PseudoTV.getText().length() == 0) {
                    PseudoTV.setText("AAA");
                }
                if (ScoreTV.getText().length() == 0) {
                    return;
                }
                Intent intent = getActivity().getIntent();
                Flipper flipper = (Flipper) intent.getSerializableExtra(PageInfoFlipperPager.INTENT_FLIPPER_POUR_INFO);

                //make score
                Score newScore = new Score(1, "", 1, "", "", 1, flipper);
                newScore.setId(new Date().getTime());
                String newpseudo = PseudoTV.getText().toString();
                newScore.setPseudo(newpseudo);
                Long score = Long.parseLong(ScoreTV.getText().toString().replaceAll(",", ""));
                newScore.setScore(score);
                newScore.setDate(new SimpleDateFormat("yyyy/MM/dd", Locale.FRANCE).format(new Date()));

                newScore.setFlipperId(flipper.getId());
                newScore.setFlipper(flipper);
                // Envoyer le score
                envoyerScore(newScore);
                //Sauvegarder
                // On sauvegarde le pseudo
                SharedPreferences.Editor editor = settings.edit();
                editor.putString(PreferencesActivity.KEY_PSEUDO_FULL, newpseudo);
                editor.apply();

            }
        })
                .setNegativeButton(R.string.boutonCancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }


    public void envoyerScore(Score score) {
        ScoreService scoreService = new ScoreService(new FragmentScoreFlipper.FragmentCallback() {
            @Override
            public void onTaskDone() {
            }
        });
        scoreService.ajouteScore(getActivity(), score);

    }
/*


    @Override
    public void onDismiss(DialogInterface dialog) {
        Log.d(TAG, "onDismiss: ");
        Intent intent = getActivity().getIntent();
        getActivity().finish();
        startActivity(intent);
        super.onResume();
        super.onDismiss(dialog);
    }
    */
}