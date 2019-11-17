package com.pinmyballs.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.pinmyballs.PageInfoFlipperPager;
import com.pinmyballs.R;
import com.pinmyballs.metier.Flipper;

import java.util.ArrayList;

public class OtherFlipperAdapter extends ArrayAdapter<Flipper> {

    private static final String TAG = OtherFlipperAdapter.class.getSimpleName();
    int mResource;
    private Context mContext;


    public OtherFlipperAdapter(@NonNull Context context, int resource, @NonNull ArrayList<Flipper> objects) {
        super(context, resource, objects);
        this.mResource = resource;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        View view = convertView;

        if (view == null) {
            LayoutInflater layoutInflater = LayoutInflater.from(getContext());
            view = layoutInflater.inflate(mResource, parent, false);
        }

        view.setTag(position);
        view.setOnClickListener(OtherModelClickListener);

        Flipper flipper = getItem(position);

        if (flipper != null) {
            String modele = flipper.getModele().getNom();
            TextView modeleTv = view.findViewById(R.id.itemModeleFlipper);
            modeleTv.setText(modele);
        }

        return view;
        //return super.getView(position, convertView, parent);
    }

    private View.OnClickListener OtherModelClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Flipper flipper = getItem((Integer) v.getTag());
            if (flipper != null){
                Intent intent = new Intent(getContext(), PageInfoFlipperPager.class);
                intent.putExtra(PageInfoFlipperPager.INTENT_FLIPPER_POUR_INFO, flipper);
                intent.putExtra(PageInfoFlipperPager.INTENT_FLIPPER_ONGLET_DEFAUT, 0);
                getContext().startActivity(intent);
                ((Activity) getContext()).finish();
            }
        }
    };
}
