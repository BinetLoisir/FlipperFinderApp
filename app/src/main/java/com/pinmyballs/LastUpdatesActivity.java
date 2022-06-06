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

public class LastUpdatesActivity extends AppCompatActivity {

    private static final String TAG = LastUpdatesActivity.class.getSimpleName();
    private UpdatesAdapter updatesAdapter;
    private ArrayList<Flipper> listFlippers;
    public static final int MAX_NUMBER = 50;

    ActionBar mActionbar;
    SharedPreferences settings;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lastupdates);
        initActionBar();
        initView();
    }

    private void initActionBar() {
        mActionbar = getSupportActionBar();
        mActionbar.setTitle("Last updates");
        mActionbar.setHomeButtonEnabled(true);
        mActionbar.setDisplayHomeAsUpEnabled(true);
    }

    private void initView() {
        RecyclerView recyclerView = findViewById(R.id.recycl_updates);
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        //recyclerView.setHasFixedSize(true);
        // use a linear layout manager
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        listFlippers = new ArrayList<>();
        listFlippers.addAll(new BaseFlipperService().getLastUpdated(getApplicationContext(), MAX_NUMBER));


        updatesAdapter = new UpdatesAdapter(this, listFlippers);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        recyclerView.setAdapter(updatesAdapter);

    }





}
