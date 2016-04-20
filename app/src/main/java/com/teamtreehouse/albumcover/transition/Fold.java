package com.teamtreehouse.albumcover.transition;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.transition.TransitionValues;
import android.transition.Visibility;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by bearg on 4/19/2016.
 */
public class Fold extends Visibility {
    @Override
    public Animator onAppear(ViewGroup sceneRoot, View view, TransitionValues startValues,
                             TransitionValues endValues) {

        // if we're making the view appear, folding is false
        return createFoldAnimator(view, false);

    }

    @Override
    public Animator onDisappear(ViewGroup sceneRoot, View view, TransitionValues startValues,
                                TransitionValues endValues) {

        return createFoldAnimator(view, false);

    }

    public Animator createFoldAnimator(View view, boolean folding) {
        int start = view.getTop();
        // trick to get the end value
        int end = view.getTop() + view.getMeasuredHeight() - 1;

        if(folding) {
            // if we're folding up, reverse the animation
            // need to introduce 3rd variable to switch the values of two variables
            int temp = start;
            start = end;
            end = temp;
        }
        view.setBottom(start);

        ObjectAnimator animator = ObjectAnimator.ofInt(view, "bottom", start, end);
        return animator;
    }
}



