package com.pinmyballs.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.pinmyballs.R;

import java.nio.charset.Charset;

public class Fragment1 extends Fragment {

    private EditText editText;
    private Button button;

    public interface Fragment1Listener{
        void sendTextToFragment2(CharSequence charSequence);
    }

    private Fragment1Listener fragment1Listener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment1,container,false);
        editText = v.findViewById(R.id.editText);
        button = v.findViewById(R.id.button);
        button.setText("Send to F2");

        button.setOnClickListener(view -> {
            CharSequence input = editText.getText();
            //send data
            fragment1Listener.sendTextToFragment2(input);
        });



        return v;
    }

    public void UpdateEditText(CharSequence text){
        editText.setText(text);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof Fragment1Listener){
            fragment1Listener = (Fragment1Listener) context;
        }else  {
            throw new RuntimeException(context.toString()+ "must implement Fragment1Listener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        fragment1Listener = null;
    }
}
