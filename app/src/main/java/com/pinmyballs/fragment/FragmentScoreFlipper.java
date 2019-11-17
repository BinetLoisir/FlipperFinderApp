package com.pinmyballs.fragment;

import com.pinmyballs.metier.Flipper;

public class FragmentScoreFlipper {

    Flipper flipper;

    public FragmentScoreFlipper(Flipper flipper) {
        this.flipper = flipper;
    }

    public interface FragmentCallback {
        void onTaskDone();
    }

}
