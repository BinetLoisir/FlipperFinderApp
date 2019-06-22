package com.pinmyballs.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.pinmyballs.R;
import com.pinmyballs.metier.Score;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class ListeScoresAdapter extends ArrayAdapter<Score>{

    private static final String TAG = ListeScoresAdapter.class.getSimpleName();
    private int mResource;
    private Context mContext;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());
    private DateFormat df = DateFormat.getDateInstance(DateFormat.LONG, Locale.FRANCE);

    public ListeScoresAdapter(@NonNull Context mContext, int mResource, @NonNull ArrayList<Score> objects) {
        super(mContext, mResource, objects);
        this.mContext = mContext;
        this.mResource = mResource;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        //Get Score information
        String pseudo = getItem(position).getPseudo();
        Long score = getItem(position).getScore();
        String date = getItem(position).getDate();

        LayoutInflater inflater = LayoutInflater.from(mContext);
        convertView = inflater.inflate(mResource, parent,false);

        DecimalFormat formatter = new DecimalFormat("###,###,###,###");

        TextView Tpseudo = (TextView) convertView.findViewById(R.id.scorePseudo);
        TextView Tscore = (TextView) convertView.findViewById(R.id.scoreScore);
        TextView Tdate = (TextView) convertView.findViewById(R.id.scoreDate);

        try {
            Tpseudo.setText(pseudo);
            Tscore.setText(formatter.format(score));
            Tdate.setText(df.format(dateFormat.parse(date)));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return convertView;

    }
}