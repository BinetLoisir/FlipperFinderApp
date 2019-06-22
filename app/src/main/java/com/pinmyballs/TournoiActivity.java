package com.pinmyballs;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.pinmyballs.fragment.FragmentTournoiListe;
import com.pinmyballs.fragment.FragmentTournoiMap;
import com.pinmyballs.fragment.FragmentTournoiNew;
import com.pinmyballs.utils.BottomNavigationViewHelper;
import com.pinmyballs.utils.SectionsPagerAdapter;

public class TournoiActivity extends AppCompatActivity {
    private static final String TAG = "TournoiActivity";
    private static final int ACTIVITY_NUM = 0;
    private Context mContext = TournoiActivity.this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tournoi);
        setupBottomNavigationView();
        setupViewPager();
    }

    /**
     * Responsible for adding the tabs
     */
    private void setupViewPager(){
        SectionsPagerAdapter adapter = new SectionsPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new FragmentTournoiListe());
        adapter.addFragment(new FragmentTournoiMap());
        adapter.addFragment(new FragmentTournoiNew());
        ViewPager viewPager = (ViewPager) findViewById(R.id.container2);
        viewPager.setAdapter(adapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        tabLayout.getTabAt(0).setIcon(R.drawable.ic_view_list_black_24dp);
        tabLayout.getTabAt(1).setIcon(R.drawable.ic_map_black_24dp);
        tabLayout.getTabAt(2).setIcon(R.drawable.ic_add_black_24dp);
    }


    /**
     * Bottom Navigation View Setup
     */
    private void setupBottomNavigationView(){
        Log.d(TAG, "setupBottomNavigationView: setting up");
        BottomNavigationViewEx bottomNavigationViewEx = (BottomNavigationViewEx) findViewById(R.id.bottomNavViewBar);
        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationViewEx);
        BottomNavigationViewHelper.enableNavigation(mContext,bottomNavigationViewEx);
        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);
    }

}
