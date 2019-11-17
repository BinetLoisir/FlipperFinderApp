package com.pinmyballs;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.pinmyballs.fragment.FragmentCommentairesDeleted;
import com.pinmyballs.fragment.FragmentCommentairesNew;
import com.pinmyballs.fragment.FragmentCommentairesPost;
import com.pinmyballs.metier.Commentaire;
import com.pinmyballs.utils.BottomNavigationViewHelper;
import com.pinmyballs.utils.SectionsPagerAdapter;

import java.util.ArrayList;

public class CommentaireActivity extends AppCompatActivity {

	private static final String TAG = "CommentaireActivity";
	private static final int ACTIVITY_NUM = 1;
	private Context mContext = CommentaireActivity.this;

	ArrayList<Commentaire> listeCommentaires = new ArrayList<Commentaire>();

	ListView listeCommentaireView = null;
    private int NB_MAX_COMMENTAIRE = 50;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_actus);
		setupBottomNavigationView();
        setupViewPager();

        //A transfter dans le PagerAdapter
        /*
		listeCommentaireView = (ListView) findViewById(R.id.listeCommentairesRecents);
		GlobalService globalService = new GlobalService();
		listeCommentaires = globalService.getLastCommentaireType(getApplicationContext(), NB_MAX_COMMENTAIRE,Commentaire.TYPE_POST, true);
		ListeCommentaireAdapter customAdapter = new ListeCommentaireAdapter(this, R.layout.simple_list_item_commentaire, listeCommentaires);
		listeCommentaireView.setAdapter(customAdapter);
		*/
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

        tabLayout.getTabAt(0).setText("COMMENTAIRES");
        tabLayout.getTabAt(1).setText("AJOUTS");
        tabLayout.getTabAt(2).setText("RETRAITS");
    }


    /**
     * Bottom Navigation View Setup
     */
    private void setupBottomNavigationView(){
        Log.d(TAG, "setupBottomNavigationView: setting up");
        BottomNavigationViewEx bottomNavigationViewEx = findViewById(R.id.bottomNavViewBar);
        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationViewEx);
        BottomNavigationViewHelper.enableNavigation(mContext,bottomNavigationViewEx);
        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);
    }

}
