package com.pinmyballs.utils;

import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.util.Log;
import android.util.SparseLongArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pinmyballs.PopMap;
import com.pinmyballs.PopMapLarge;
import com.pinmyballs.R;
import com.pinmyballs.metier.Flipper;
import com.pinmyballs.metier.ModeleFlipper;
import com.pinmyballs.service.base.BaseFlipperService;
import com.pinmyballs.service.base.BaseModeleService;
import com.pinmyballs.service.base.BaseScoreService;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModelAdapter extends RecyclerView.Adapter<ModelAdapter.MyViewHolder> implements Filterable {
    private static final String TAG = "ModelAdapter";


    private Context context;
    private ArrayList<ModeleFlipper> listModels;
    private ArrayList<ModeleFlipper> listModelsFull;

    private Filter exampleFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<ModeleFlipper> filteredList = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(listModelsFull);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();

                for (ModeleFlipper modeleFlipper : listModelsFull) {
                    if (modeleFlipper.getNomComplet().toLowerCase().contains(filterPattern) ) {
                        filteredList.add(modeleFlipper);
                    }
                }
            }
            Log.d(TAG, "performFiltering: " + listModels.size() + " / " + filteredList.size() + " / " + listModelsFull.size());
            FilterResults results = new FilterResults();
            results.values = filteredList;

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            listModels.clear();
            listModels.addAll((List<ModeleFlipper>) results.values);
            notifyDataSetChanged();
        }
    };

    public ModelAdapter(Context context, ArrayList<ModeleFlipper> listModels) {
        this.context = context;
        this.listModels = listModels;
        listModelsFull = new ArrayList<>(listModels);
    }

    // Create new views (invoked by the layout manager)
    // Inflate item layout in onCreateViewHolder() method and inflate item_row for recycler view
    @NonNull
    @Override
    public ModelAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_model, parent, false);
        return new MyViewHolder(v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        // - get element from your data set at this position
        // - replace the contents of the view with that element
        ModeleFlipper model = listModels.get(position);
        holder.setDetails(model);
        holder.itemView.setOnClickListener(view -> {
            BaseFlipperService baseFlipperService = new BaseFlipperService();
            ArrayList<Flipper> list = baseFlipperService.getFlipperByModel(view.getContext(), model.getId());
            ArrayList<Parcelable> listflip = new ArrayList<>();
            for(Flipper flipper : list){
                listflip.add(Parcels.wrap(flipper));
            }
            Intent intent = new Intent(view.getContext(), PopMapLarge.class);
            intent.putExtra(PopMapLarge.KEY_MODELNAME, model.getNomComplet());
            intent.putParcelableArrayListExtra(PopMapLarge.KEY_LISTFLIP, listflip);
            view.getContext().startActivity(intent);
        });
    }

    // Return the size of your data set (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return listModels.size();
    }

    @Override
    public Filter getFilter() {
        return exampleFilter;
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        private HashMap<Long, Integer> ModelCountHM;
        private TextView TVModelName;
        private TextView TVModelBrand;
        private TextView TVModelQty;

        MyViewHolder(View v) {
            super(v);
            TVModelName = v.findViewById(R.id.modelName);
            TVModelBrand = v.findViewById(R.id.modelBrand);
            TVModelQty = v.findViewById(R.id.modelQty);
            ModelCountHM = new HashMap<Long, Integer>();

            v.setOnClickListener(view -> {

                Toast.makeText(v.getContext(),"black",Toast.LENGTH_SHORT).show();;
            });
        }

        void setDetails(ModeleFlipper modeleFlipper) {
            populateHashMap(context);
            TVModelName.setText(modeleFlipper.getNom());
            TVModelBrand.setText(modeleFlipper.getMarque()+", "+ modeleFlipper.getAnneeLancement());
            TVModelQty.setText(String.valueOf(ModelCountHM.get(modeleFlipper.getId())));
            if (ModelCountHM.get(modeleFlipper.getId()) < 1){
                TVModelQty.setBackgroundColor( context.getResources().getColor(R.color.grey));
            } else {
                TVModelQty.setBackground( context.getResources().getDrawable(R.drawable.round_textview_full));
            }
        }

        void setOnClick(ModeleFlipper modeleFlipper) {

        }

        void populateHashMap(Context context) {
            ArrayList<Long> listModelIDs = new BaseModeleService().getAllIdModeleFlipper(context);
            ArrayList<Flipper> listFlippersActifs = new BaseFlipperService().getAllActiveFlippers(context);

            for (Long modelID : listModelIDs
            ) {
                int modelcount = 0;
                for (Flipper flip : listFlippersActifs
                ) {
                    if (flip.getIdModele() == modelID) {
                        modelcount++;
                    }
                }
                ModelCountHM.put(modelID, modelcount);
            }
        }

    }
}
