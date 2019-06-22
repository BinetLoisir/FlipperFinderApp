package com.pinmyballs.fragment;

import android.content.Context;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.pinmyballs.R;
import com.pinmyballs.metier.Commentaire;
import com.pinmyballs.service.CommentaireService;

public class EditCommentDialog extends DialogFragment {

    private static final String TAG = "EditCommentDialog";
    public OnInputSelected mOnInputSelected;
    private String text = "Texte du commentaire";
    //widgets
    private EditText mInput;
    private TextView mActionOK, mActionCancel;

    private static CharSequence trim(CharSequence s, int start, int end) {
        while (start < end && Character.isWhitespace(s.charAt(start))) {
            start++;
        }

        while (end > start && Character.isWhitespace(s.charAt(end - 1))) {
            end--;
        }

        return s.subSequence(start, end);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_editcomment, container, false);
        mInput = view.findViewById(R.id.input_ET);
        mActionOK = view.findViewById(R.id.action_ok);
        mActionCancel = view.findViewById(R.id.action_cancel);

        final Commentaire commentaire = (Commentaire) getArguments().getSerializable("comment");
        Spanned html = Html.fromHtml(commentaire.getTexte());
        CharSequence trimmed = trim(html, 0, html.length());
        mInput.setText(trimmed);

        mActionCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: closing dialog");
                getDialog().dismiss();
            }
        });

        mActionOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: send new comment");
                String input = mInput.getText().toString();
                String htmlString = Html.toHtml(mInput.getText());
                htmlString = htmlString.replaceAll("[\n]", "");

                if (!input.equals("")) {
                    //Pour transfer vers paretn fragment (codingwithmitch) not used
                    //mOnInputSelected.sendInput(input);

                    //Update database
                    CommentaireService commentaireService = new CommentaireService(new FragmentCommentaireFlipper.FragmentCallback() {
                        @Override
                        public void onTaskDone() {
                            //TODO do something, genre mise à jour de la database
                        }
                    });

                    Commentaire oldCommentaire = commentaire;
                    Commentaire newCommentaire = oldCommentaire;
                    newCommentaire.setTexte(htmlString);
                    commentaireService.updateCommentaire(getActivity(), oldCommentaire, newCommentaire);

                }
                getDialog().dismiss();
            }
        });
        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            mOnInputSelected = (OnInputSelected) getTargetFragment();
        } catch (ClassCastException e) {
            Log.e(TAG, "onAttach: ClassCastException : " + e.getMessage());
        }
    }

    public interface OnInputSelected {
        void sendInput(String input);
    }

}
