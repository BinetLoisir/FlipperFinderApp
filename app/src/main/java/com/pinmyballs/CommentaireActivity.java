package com.pinmyballs;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.pinmyballs.fragment.FragmentCommentairesDeleted;
import com.pinmyballs.fragment.FragmentCommentairesNew;
import com.pinmyballs.fragment.FragmentCommentairesPost;
import com.pinmyballs.utils.BottomNavigationViewHelper;
import com.pinmyballs.utils.SectionsPagerAdapter;

public class CommentaireActivity extends AppCompatActivity {

	private static final String TAG = "CommentaireActivity";
	private static final int ACTIVITY_NUM = 1;
	private final Context mContext = CommentaireActivity.this;
    private FirebaseAnalytics firebaseAnalytics;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_actus);
		setupBottomNavigationView();
        setupViewPager();
    }

    /**
     * Responsible for adding the tabs
     */
    private void setupViewPager() {
        SectionsPagerAdapter adapter = new SectionsPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new FragmentCommentairesPost());
        adapter.addFragment(new FragmentCommentairesNew());
        adapter.addFragment(new FragmentCommentairesDeleted());
        ViewPager viewPager = findViewById(R.id.container2);
        viewPager.setAdapter(adapter);

        TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        tabLayout.getTabAt(0).setText(R.string.tab_commentaires);
        tabLayout.getTabAt(1).setText(R.string.tab_ajouts);
        tabLayout.getTabAt(2).setText(R.string.tab_retraits);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                String tabName = "init";
                switch (position) {
                    case 0:
                        tabName = getString(R.string.tab_commentaires);
                        break;
                    case 1:
                        tabName = getString(R.string.tab_ajouts);
                        break;
                    case 2:
                        tabName = getString(R.string.tab_retraits);
                        break;
                }

                firebaseAnalytics = FirebaseAnalytics.getInstance(mContext);
                Bundle bundle = new Bundle();
                String screenName = "Commentaires";
                bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName);
                bundle.putString(FirebaseAnalytics.Param.SCREEN_CLASS, TAG);
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "TAB "+ tabName + " selected");
                firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM,bundle);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });






    }


    /**
     * Bottom Navigation View Setup
     */
    /*private void setupBottomNavigationView(){
        Log.d(TAG, "setupBottomNavigationView: setting up");
        BottomNavigationViewEx bottomNavigationViewEx = findViewById(R.id.bottomNavViewBar);
        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationViewEx);
        BottomNavigationViewHelper.enableNavigation(mContext,bottomNavigationViewEx);
        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);
    }*/

    private void setupBottomNavigationView() {
        Log.d(TAG, "setBottomNavigationView: setting up");
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationView);
        BottomNavigationViewHelper.enableNavigation(mContext, bottomNavigationView);
        Menu menu = bottomNavigationView.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);
    }

}
