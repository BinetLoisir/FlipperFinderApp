package com.pinmyballs.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.pinmyballs.R;
import com.pinmyballs.metier.Tournoi;
import com.pinmyballs.service.base.BaseTournoiService;
import com.pinmyballs.utils.TournoiAdapter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class FragmentTournoiListe extends Fragment{
    private static final String TAG = "FragmentTournoiListe";
    private Switch mSwitchTournois;



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tournoi_liste,container,false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final ListView listTournois = (ListView) view.findViewById(R.id.tournoisliste);

        mSwitchTournois =  (Switch) view.findViewById(R.id.switchTournoiListe);
        mSwitchTournois.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                populateListTournois(listTournois);
                if (isChecked) {

                    // The toggle is enabled

                } else {
                    // The toggle is disabled

                }

            }
        });
        populateListTournois(listTournois);

    }

    public void populateListTournois(ListView listeView){
        ArrayList<Tournoi> Tournois;
        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");

        // Récupère la liste des tournois
        BaseTournoiService baseTournoiService = new BaseTournoiService();
        if(!mSwitchTournois.isChecked()) {
            Tournois = baseTournoiService.getAllFutureTournoi(getActivity().getBaseContext()); //IMPORTANT
        }
        else{
            Tournois = baseTournoiService.getAllTournoi(getActivity().getBaseContext()); //IMPORTANT
        }

        // Tri les tournois du plus ancien au plus récent.
        Collections.sort(Tournois, new Comparator<Tournoi>() {
                    @Override
                    public int compare(Tournoi t2, Tournoi t1) {
                        String t1date = t1.getDate();
                        String t2date = t2.getDate();
                        int result = 0;
                        try {
                            result = dateFormat.parse(t2date).compareTo(dateFormat.parse(t1date));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        return result;
                    }
                }
        );

        TournoiAdapter adapter = new TournoiAdapter(getActivity(), R.layout.simple_liste_item_tournoi, Tournois);
        listeView.setAdapter(adapter);

        final ArrayList<Tournoi> finalTournois = Tournois;
        listeView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Uri uri = Uri.parse(finalTournois.get(position).getUrl()); // missing 'http://' will cause crashed
                if (!TextUtils.isEmpty(uri.toString())) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                }
            }
        });
    }
}
