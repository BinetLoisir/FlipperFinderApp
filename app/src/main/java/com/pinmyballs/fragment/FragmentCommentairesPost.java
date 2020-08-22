package com.pinmyballs.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.pinmyballs.R;
import com.pinmyballs.metier.Commentaire;
import com.pinmyballs.service.GlobalService;
import com.pinmyballs.utils.ListeCommentaireAdapter;

import java.util.ArrayList;

public class FragmentCommentairesPost extends Fragment {
    private static final String TAG = "FragmentCommentairesPos";
    private int NB_MAX_COMMENTAIRE = 50;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_commentaires_post, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final ListView listView = view.findViewById(R.id.listCommPost);
        populateList(listView, Commentaire.TYPE_POST, true);
    }

    public void populateList(ListView listView, String type, boolean includeNull) {
        GlobalService globalService = new GlobalService();
        ArrayList<Commentaire> listeCommentaire = globalService.getLastCommentaireType(getContext(), NB_MAX_COMMENTAIRE, type, includeNull);
        ListeCommentaireAdapter listeCommentaireAdapter = new ListeCommentaireAdapter(getContext(), R.layout.simple_list_item_commentaire, listeCommentaire);
        listView.setAdapter(listeCommentaireAdapter);
    }


}
