package com.pinmyballs.utils;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.pinmyballs.R;
import com.pinmyballs.TrashItem;
import com.pinmyballs.database.FlipperDatabaseHandler;
import com.pinmyballs.metier.Flipper;
import com.pinmyballs.service.FlipperService;
import com.pinmyballs.service.base.BaseFlipperService;

import java.util.ArrayList;

public class TrashAdapter extends RecyclerView.Adapter<TrashAdapter.MyViewHolder> {
    Context context;
    private final ArrayList<TrashItem> mDataset;

    // Provide a reference to the views for each data item
    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView tv_flipId;
        public TextView tv_pseudo;
        public TextView tv_date;
        public TextView tv_model;
        public TextView tv_enseigne;
        public TextView tv_address;
        public ImageView iv_delete;
        public SwitchCompat sw_actif;

        public ConstraintLayout constraintLayout;

        public MyViewHolder(View itemView) {
            super(itemView);
            this.tv_flipId = (TextView) itemView.findViewById(R.id.item_trash_flipid);
            this.tv_pseudo = (TextView) itemView.findViewById(R.id.item_trash_pseudo);
            this.tv_date = (TextView) itemView.findViewById(R.id.item_trash_date);
            this.tv_model = (TextView) itemView.findViewById(R.id.item_trash_model);
            this.tv_enseigne = (TextView) itemView.findViewById(R.id.item_trash_enseigne);
            this.tv_address = (TextView) itemView.findViewById(R.id.item_trash_address);
            this.iv_delete = (ImageView) itemView.findViewById(R.id.item_trash_delete);
            this.sw_actif = (SwitchCompat) itemView.findViewById(R.id.item_trash_switch);
            constraintLayout = (ConstraintLayout) itemView.findViewById(R.id.CSLayout);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public TrashAdapter(ArrayList<TrashItem> myDataset) {
        mDataset = myDataset;
    }

    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public TrashAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                        int viewType) {
        // create a new view
        View listItem = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.simple_list_item_trash, parent, false);

        return new MyViewHolder(listItem);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        final TrashItem trashItem = mDataset.get(position);
        //final TrashItem trashItem = mDataset.get(holder.getBindingAdapterPosition());

        Flipper flipper = new BaseFlipperService().getFlipperById(holder.tv_flipId.getContext(), trashItem.getFlipId());

        holder.tv_flipId.setText(trashItem.getFlipIdAsString());
        holder.tv_pseudo.setText(trashItem.getPseudo());
        holder.tv_date.setText(trashItem.getDateFormatted());
        if (flipper != null) {
            holder.tv_model.setText(flipper.getModele().getNomComplet());
            holder.tv_enseigne.setText(flipper.getEnseigne().getNom());
            holder.tv_address.setText(flipper.getEnseigne().getAdresseCompleteSansPays());
            holder.sw_actif.setChecked(flipper.isActif());
        }
        holder.iv_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int currentPosition = holder.getBindingAdapterPosition();
                if (currentPosition != RecyclerView.NO_POSITION) {
                    FlipperService flipperService = new FlipperService(null);
                    if (flipper != null && flipper.isActif()) {
                        flipperService.modifieEtatFlip(view.getContext(), flipper, trashItem.getPseudo());
                    }

                    // Mise à jour de la Trash List dans Back4App avec Processed = True
                    ParseQuery<ParseObject> query = ParseQuery.getQuery(FlipperDatabaseHandler.FLIPTRASH_TABLE_NAME);
                    query.whereEqualTo(FlipperDatabaseHandler.FLIPTRASH_PROCESSED, false);

                    query.getFirstInBackground(new GetCallback<ParseObject>() {
                        @Override
                        public void done(ParseObject object, ParseException e) {
                            if (e == null) {
                                Log.d("trashitems", "TrashFlipper found :  " + object.getObjectId());
                                object.put(FlipperDatabaseHandler.FLIPTRASH_PROCESSED, true);
                                object.saveInBackground(e1 -> {
                                    if (e1 == null) {
                                        Log.d("trashitems", "Processed = true");
                                        mDataset.remove(currentPosition);
                                        notifyItemRemoved(currentPosition);
                                    }
                                });
                            } else {
                                Log.d("trashFlipper : ", "Error: " + e.getMessage());
                            }
                        }
                    });
                }
            }
        });
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}