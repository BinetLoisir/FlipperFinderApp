package com.pinmyballs.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.pinmyballs.PageInfoFlipperPager;
import com.pinmyballs.R;
import com.pinmyballs.metier.Flipper;
import com.pinmyballs.service.base.BaseFlipperService;

import java.util.ArrayList;
import java.util.List;

/**
 * Classe pour un item de la liste de flipper de l'activité
 * PageListeResultat
 * @author Fafouche
 *
 */
public class ListeFlipperAdapter extends ArrayAdapter<Flipper> {

    private List<Flipper> listeFlippers;
    private double latitude = 0;
    private double longitude = 0;
    private OnClickListener InfoFlipperClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            //EasyTracker.getTracker().sendEvent("ui_action", "button_press", "item_info_flipper", 0L);

            Flipper p = listeFlippers.get((Integer) v.getTag());
            Intent infoActivite = new Intent(getContext(), PageInfoFlipperPager.class);
            infoActivite.putExtra(PageInfoFlipperPager.INTENT_FLIPPER_POUR_INFO, p);
            // On va sur l'onglet de la carte
            infoActivite.putExtra(PageInfoFlipperPager.INTENT_FLIPPER_ONGLET_DEFAUT, 0);
            getContext().startActivity(infoActivite);

        }
    };

    public ListeFlipperAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
    }

    public ListeFlipperAdapter(Context context, int resource, List<Flipper> items, double latitude, double longitude) {

        super(context, resource, items);

        this.listeFlippers = items;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;

        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.simple_list_item_flipper, null);
        }

        // On set les tags pour pouvoir retrouver sur quelle ligne on a cliqué.
        v.setTag(position);
        v.setOnClickListener(InfoFlipperClickListener);

        Flipper p = listeFlippers.get(position);

        BaseFlipperService baseFlipperService = new BaseFlipperService();
        ArrayList<Flipper> otherFlippers = baseFlipperService.rechercheOtherFlipper(getContext(), p);
        boolean morethanone = otherFlippers.size() > 0;

        if (p != null) {

            ImageView markerIcone = v.findViewById(R.id.markericon);
            TextView modeleTV = v.findViewById(R.id.textModeleFlipper);
            TextView adresseTV = v.findViewById(R.id.textAdresseFlipper);
            TextView distanceTV = v.findViewById(R.id.distance);
            TextView dateMajTV = v.findViewById(R.id.dateMaj);
            TextView nomBar = v.findViewById(R.id.nomBar);
            ImageView warningImage = v.findViewById(R.id.warningicon);

            if (modeleTV != null) {
                modeleTV.setText(p.getModele().getNomComplet());
            }
            if (adresseTV != null) {
                adresseTV.setText(p.getEnseigne().getAdresse() + " " + p.getEnseigne().getVille());
            }
            if (distanceTV != null) {
                float[] resultDistance = new float[5];
                Location.distanceBetween(latitude, longitude, Double.valueOf(p.getEnseigne().getLatitude()),
                        Double.valueOf(p.getEnseigne().getLongitude()), resultDistance);
                float distanceFloat = resultDistance[0];
                distanceTV.setText(LocationUtil.formatDist(distanceFloat));
            }
            if (nomBar != null) {
                nomBar.setText(p.getEnseigne().getNom());
            }

            // Affichage de la date de mise à jour
            int nbJours = LocationUtil.getDaysSinceMajFlip(p);
            dateMajTV.setTextColor(Color.parseColor("#04B404"));
            warningImage.setVisibility(View.GONE);
            markerIcone.setImageResource((MarkerChoice(p, morethanone)));


            if (nbJours == -1){
                // Date nulle ou mal formattée : Rouge!
                dateMajTV.setTextColor(Color.parseColor("#FE2E2E"));
                dateMajTV.setText(getContext().getResources().getString(R.string.dateMajDefault));
                warningImage.setVisibility(View.VISIBLE);
            }else if (nbJours > 365){
                // Mis à jour il y a plus de 365 jours, on met en Rouge
                dateMajTV.setTextColor(Color.parseColor("#FE2E2E"));
                dateMajTV.setText("Vu il y a " + nbJours + " jours.");
                warningImage.setVisibility(View.VISIBLE);
            } else if (nbJours > 60) {
                // Mis à jour il y a plus de 60 jours, on met en Orange
                dateMajTV.setTextColor(Color.parseColor("#E68A00"));
                dateMajTV.setText("Vu il y a " + nbJours + " jours.");
            } else if (nbJours == 0) {
                // Mis à jour aujourd'hui
                //dateMajTV.setTextColor(Color.parseColor("#04B404"));
                dateMajTV.setText("Vu aujourd'hui.");
            } else if (nbJours == 1) {
                // Confirmé hier
                //dateMajTV.setTextColor(Color.parseColor("#04B404"));
                dateMajTV.setText("Vu hier.");
            } else {
                // Mis à jour récemment, on met en vert
                //dateMajTV.setTextColor(Color.parseColor("#04B404"));
                dateMajTV.setText("Vu il y a " + nbJours + " jours.");
            }
        }

        return v;
    }

    private int MarkerChoice(Flipper flipper, Boolean morethanone) {
        int nbJours = LocationUtil.getDaysSinceMajFlip(flipper);
        if (nbJours < 8) {
            return morethanone ? R.mipmap.ic_flipsmarker_new : R.mipmap.ic_flipmarker_new;
        }
        if (nbJours < 60) {
            return morethanone ? R.mipmap.ic_flipsmarker_blue : R.mipmap.ic_flipmarker_blue;
        }
        if (nbJours < 365) {
            return morethanone ? R.mipmap.ic_flipsmarker_lightblue : R.mipmap.ic_flipmarker_lightblue;
        }
        if (nbJours > 365) {
            return morethanone ? R.mipmap.ic_flipsmarker_grey : R.mipmap.ic_flipmarker_grey;
        }
        return morethanone ? R.mipmap.ic_flipsmarker_grey : R.mipmap.ic_flipmarker_grey;
    }

}
