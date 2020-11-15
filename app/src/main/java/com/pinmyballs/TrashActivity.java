package com.pinmyballs;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.pinmyballs.database.FlipperDatabaseHandler;
import com.pinmyballs.databinding.ActivityTrashBinding;
import com.pinmyballs.utils.MyAdapter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class TrashActivity extends AppCompatActivity {
    private ActivityTrashBinding binding;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<TrashItem> trashItemList = new ArrayList<>();
    private Button refreshButton;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTrashBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        //setup Refresh Button
        refreshButton = binding.trashButtonRefresh;
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                refreshList();
            }
        });

        //trashItemList.add(new TrashItem(Long.valueOf("1793747638486"), "JUL", new Date(2020-1900,3,23),false));
        //trashItemList.add(new TrashItem(Long.valueOf("1604779130254"), "MAX", new Date(2020-1900,5,7),false));
        //trashItemList.add(new TrashItem(Long.valueOf("2"), "BOB", new Date(2020-1900,8,2),false));

        //setup recyclerView
        recyclerView = binding.recyclerView;
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        // specify an adapter
        mAdapter = new MyAdapter(trashItemList);
        recyclerView.setAdapter(mAdapter);

        refreshList();

    }

    private void refreshList(){
        trashItemList.clear();
        ParseQuery<ParseObject> query = ParseQuery.getQuery(FlipperDatabaseHandler.FLIPTRASH_TABLE_NAME);
        query.whereEqualTo(FlipperDatabaseHandler.FLIPTRASH_PROCESSED, false);
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> trashListPO, ParseException e) {
                if (e == null) {
                    Log.d("trashitems", "Retrieved " + trashListPO.size() + " trashitems");
                    for(ParseObject item : trashListPO){
                        trashItemList.add(convert(item));
                    }
                    mAdapter.notifyDataSetChanged();
                } else {
                    Log.d("trashitems", "Error: " + e.getMessage());
                }
            }
        });

    }
    private TrashItem convert(ParseObject PO){
        TrashItem trashItem = new TrashItem();
        trashItem.flipId = PO.getLong(FlipperDatabaseHandler.FLIPTRASH_FLIP_ID);
        trashItem.pseudo = PO.getString(FlipperDatabaseHandler.FLIPTRASH_PSEUDO);
        trashItem.date = PO.getCreatedAt();
        trashItem.processed = PO.getBoolean(FlipperDatabaseHandler.FLIPTRASH_PROCESSED);
     return trashItem;
    }


}

