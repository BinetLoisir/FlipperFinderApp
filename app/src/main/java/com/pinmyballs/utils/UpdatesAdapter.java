package com.pinmyballs.utils;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pinmyballs.PageInfoFlipperPager;
import com.pinmyballs.R;
import com.pinmyballs.metier.Flipper;

import java.util.ArrayList;

public class UpdatesAdapter extends RecyclerView.Adapter<UpdatesAdapter.MyViewHolder> {
    private static final String TAG = "ModelAdapter";

    private final Context context;
    private final ArrayList<Flipper> listFlips;


    public UpdatesAdapter(Context context, ArrayList<Flipper> listModels ) {
        this.context = context;
        this.listFlips = listModels;
    }

    // Create new views (invoked by the layout manager)
    // Inflate item layout in onCreateViewHolder() method and inflate item_row for recycler view
    @NonNull
    @Override
    public UpdatesAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.simple_list_item_flipper2, parent, false);
        return new MyViewHolder(v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        // - get element from your data set at this position
        // - replace the contents of the view with that element
        Flipper flipper = listFlips.get(position);
        holder.setDetails(flipper);
        holder.itemView.setOnClickListener(view -> {
            Intent intent = new Intent(view.getContext(), PageInfoFlipperPager.class);
            intent.putExtra(PageInfoFlipperPager.INTENT_FLIPPER_POUR_INFO, flipper);
            intent.putExtra(PageInfoFlipperPager.INTENT_FLIPPER_ONGLET_DEFAUT, 0);
            view.getContext().startActivity(intent);
        });
    }

    // Return the size of your data set (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return listFlips.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        private final TextView TVdays;
        private final TextView TVmodel;
        private final TextView TVenseigne;
        private final TextView TVadresse;

        MyViewHolder(View v) {
            super(v);
            TVdays = v.findViewById(R.id.dateMaj);
            TVmodel = v.findViewById(R.id.textModeleFlipper);
            TVenseigne = v.findViewById(R.id.nomBar);
            TVadresse = v.findViewById(R.id.textAdresseFlipper);
        }

        void setDetails(Flipper flipper) {
            TVdays.setText(LocationUtil.getDaysSinceMajFlip(flipper) + "j");
            TVmodel.setText(flipper.getModele().getNomComplet());
            TVenseigne.setText(flipper.getEnseigne().getNom());
            TVadresse.setText(flipper.getEnseigne().getVille());

        }
    }
}
