package com.teamtreehouse.albumcover;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.graphics.Palette;
import android.transition.ChangeBounds;
import android.transition.Scene;
import android.transition.TransitionManager;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class AlbumDetailActivity extends Activity {

    public static final String EXTRA_ALBUM_ART_RESID = "EXTRA_ALBUM_ART_RESID";

    @Bind(R.id.album_art) ImageView albumArtView;
    @Bind(R.id.fab) ImageButton fab;
    @Bind(R.id.title_panel) ViewGroup titlePanel;
    @Bind(R.id.track_panel) ViewGroup trackPanel;
    @Bind(R.id.detail_container) ViewGroup detailContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_detail);
        ButterKnife.bind(this);
        populate();
    }

    private void animate() {

//        ObjectAnimator animatorFabX = ObjectAnimator.ofFloat(fab, "scaleX", 0, 1);
//        ObjectAnimator animatorFabY = ObjectAnimator.ofFloat(fab, "scaleY", 0, 1);

//        AnimatorSet scaleFab = new AnimatorSet();
        // no start method called on below, since it'll get played sequentially after
        // the first two animations in the set.playSequentially(..) method below.
//        scaleFab.playTogether(animatorFabX, animatorFabY);

        Animator scaleFab = AnimatorInflater.loadAnimator(this, R.animator.scale);
        scaleFab.setTarget(fab);


        // two below methods return the pixel value for the top and bottom of titlePanel
        int titleStartValue = titlePanel.getTop();
        int titleEndValue = titlePanel.getBottom();

        int trackStartValue = trackPanel.getTop();
        int trackEndValue = trackPanel.getBottom();

        // hide both panels and fab initially so we don't get flickering during animations
        titlePanel.setBottom(titleStartValue);
        trackPanel.setBottom(titleStartValue);
        fab.setScaleX(0);
        fab.setScaleY(0);

        // not all functionality is available through animate call. to animate TextViews
        // so that the bottoms expand, must use ObjectAnimator class
        ObjectAnimator animatorTitle = ObjectAnimator.ofInt(titlePanel, "bottom",
                titleStartValue, titleEndValue);
        // the interpolator controls whether the animation happens at a constant speed or
        // accelerates/decelerates. we will make the top one accelerate moving toward the bottom
        // one and make the bottom one decelerate, so we accelerate in and decelerate out
        animatorTitle.setInterpolator(new AccelerateInterpolator());

        ObjectAnimator animatorTrack = ObjectAnimator.ofInt(trackPanel, "bottom",
                trackStartValue, trackEndValue);
        animatorTrack.setInterpolator(new DecelerateInterpolator());

        /*animatorTitle.setDuration(1000);
        animatorTrack.setDuration(1000);
        // animation will start 1s after album cover is clicked
        animatorTitle.setStartDelay(1000);*/

        AnimatorSet set = new AnimatorSet();
        // plays the animations for the ViewGroup titlePanel, then ViewGroup trackPanel, then fab
        set.playSequentially(animatorTitle, animatorTrack, scaleFab);
        set.start();
    }

    @OnClick(R.id.album_art)
    public void onAlbumArtClick(View view) {
        animate();

    }

    @OnClick(R.id.track_panel)
    public void onTrackPanelClicked(View view) {
        // define root of View hierarchy where we want transition to happen
        // contains everything that needs to be animated
        ViewGroup transitionRoot = detailContainer;
        Scene expandedScene = Scene.getSceneForLayout(transitionRoot,
                R.layout.activity_album_detail_expanded, view.getContext());
        TransitionManager.go(expandedScene, new ChangeBounds());

    }

    private void populate() {
        int albumArtResId = getIntent().getIntExtra(EXTRA_ALBUM_ART_RESID, R.drawable.mean_something_kinder_than_wolves);
        albumArtView.setImageResource(albumArtResId);

        Bitmap albumBitmap = getReducedBitmap(albumArtResId);
        colorizeFromImage(albumBitmap);
    }

    private Bitmap getReducedBitmap(int albumArtResId) {
        // reduce image size in memory to avoid memory errors
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = false;
        options.inSampleSize = 8;
        return BitmapFactory.decodeResource(getResources(), albumArtResId, options);
    }

    private void colorizeFromImage(Bitmap image) {
        Palette palette = Palette.from(image).generate();

        // set panel colors
        int defaultPanelColor = 0xFF808080;
        int defaultFabColor = 0xFFEEEEEE;
        titlePanel.setBackgroundColor(palette.getDarkVibrantColor(defaultPanelColor));
        trackPanel.setBackgroundColor(palette.getLightMutedColor(defaultPanelColor));

        // set fab colors
        int[][] states = new int[][]{
                new int[]{android.R.attr.state_enabled},
                new int[]{android.R.attr.state_pressed}
        };

        int[] colors = new int[]{
                palette.getVibrantColor(defaultFabColor),
                palette.getLightVibrantColor(defaultFabColor)
        };
        fab.setBackgroundTintList(new ColorStateList(states, colors));
    }
}
