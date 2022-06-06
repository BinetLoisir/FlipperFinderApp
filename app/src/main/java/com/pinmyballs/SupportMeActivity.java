package com.pinmyballs;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.pinmyballs.metier.Flipper;
import com.pinmyballs.service.base.BaseFlipperService;
import com.pinmyballs.utils.UpdatesAdapter;

import java.util.ArrayList;

public class SupportMeActivity extends AppCompatActivity {

    private static final String TAG = SupportMeActivity.class.getSimpleName();

    ActionBar mActionbar;
    SharedPreferences settings;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_support_me);
        initActionBar();
        initView();
    }

    private void initActionBar() {
        mActionbar = getSupportActionBar();
        mActionbar.setTitle(R.string.title_support_me);
        mActionbar.setHomeButtonEnabled(true);
        mActionbar.setDisplayHomeAsUpEnabled(true);
    }

    private void initView() {


    }





}
