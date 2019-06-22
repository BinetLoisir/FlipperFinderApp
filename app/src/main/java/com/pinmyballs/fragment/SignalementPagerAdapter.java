package com.pinmyballs.fragment;

import android.util.SparseArray;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class SignalementPagerAdapter extends FragmentPagerAdapter{

    public static final int PAGE_COUNT = 3;
    SparseArray<SignalementWizardFragment> registeredFragments = new SparseArray<SignalementWizardFragment>();

    public SignalementPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int arg0) {
        switch(arg0){
            case 0:
                return new FragmentSignalementModele();
            case 1:
                return new FragmentSignalementAdresse();
            case 2:
                return new FragmentSignalementMap();
        }
        return null;
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        SignalementWizardFragment fragment = (SignalementWizardFragment) super.instantiateItem(container, position);
        registeredFragments.put(position, fragment);
        return fragment;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        registeredFragments.remove(position);
        super.destroyItem(container, position, object);
    }

    public SignalementWizardFragment getRegisteredFragment(int position) {
        return registeredFragments.get(position);
    }
}
