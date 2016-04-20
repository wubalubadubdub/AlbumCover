package com.teamtreehouse.albumcover.transition;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.transition.Transition;
import android.transition.TransitionValues;
import android.transition.Visibility;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by bearg on 4/19/2016.
 */
public class Scale extends Visibility {
    @Override
    public Animator onAppear(ViewGroup sceneRoot, View view, TransitionValues startValues, TransitionValues endValues) {
        return createScaleAnimator(view, 0, 1); // fab is appearing, so it should scale from 0 to 1

    }

    @Override
    public Animator onDisappear(ViewGroup sceneRoot, View view, TransitionValues startValues, TransitionValues endValues) {
        return createScaleAnimator(view, 1, 0); // fab is disappearing, so scale from 1 to 0
    }

    // helper method to create the Animator needed above
    private Animator createScaleAnimator(View view, float fromScale, float toScale) {
        AnimatorSet set = new AnimatorSet();
        ObjectAnimator x = ObjectAnimator.ofFloat(view, View.SCALE_X, fromScale, toScale);
        ObjectAnimator y = ObjectAnimator.ofFloat(view, View.SCALE_Y, fromScale, toScale);
        set.playTogether(x, y);
        return set;

    }
}
