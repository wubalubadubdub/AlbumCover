package com.teamtreehouse.albumcover;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.graphics.Palette;
import android.transition.ChangeBounds;
import android.transition.Fade;
import android.transition.Scene;
import android.transition.Slide;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.transition.TransitionSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.teamtreehouse.albumcover.transition.Fold;
import com.teamtreehouse.albumcover.transition.Scale;

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

    private TransitionManager mTransitionManager;
    private Scene mExpandedScene;
    private Scene mCollapsedScene;
    private Scene mCurrentScene; // tracks which scene we're in

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_detail);
        ButterKnife.bind(this);
        populate();
        setupTransitions();
    }

    // we replaced the animate() method with a custom transition
    // we'll create a set of transitions and use this to run it
    private Transition createTransition() {
        TransitionSet set = new TransitionSet();
        set.setOrdering(TransitionSet.ORDERING_SEQUENTIAL);

        // create transition for the fab
        Transition tFab = new Scale();
        tFab.setDuration(150);
        tFab.addTarget(fab);

        // ..and for title group
        Transition tTitle = new Fold();
        tTitle.setDuration(150);
        tTitle.addTarget(titlePanel);

        //..and for track group
        Transition tTrack = new Fold();
        tTrack.setDuration(150);
        tTrack.addTarget(trackPanel);

        // add transitions to set in order you want them to run
        set.addTransition(tTrack);
        set.addTransition(tTitle);
        set.addTransition(tFab);

        return set;
    }



    @OnClick(R.id.album_art)
    public void onAlbumArtClick(View view) {
        Transition transition = createTransition();
        // after method below, changes to detailContainer are being tracked
        TransitionManager.beginDelayedTransition(detailContainer, transition);

        // my own code, to set all three views to invisible
        View[] detailContainerViews = new View[] {fab, titlePanel, trackPanel};
        for(View v : detailContainerViews) {
            v.setVisibility(View.INVISIBLE);
        }
    }

    @OnClick(R.id.track_panel)
    public void onTrackPanelClicked(View view) {
        if (mCurrentScene == mExpandedScene) {
            mCurrentScene = mCollapsedScene;
        } else {
            mCurrentScene = mExpandedScene;
        }
        mTransitionManager.transitionTo(mCurrentScene);
    }

    private void setupTransitions() {
        // use a transition to enter this activity from parent activity
        /*Slide slide = new Slide(Gravity.BOTTOM);
        // exclude the status bar from the animation
        slide.excludeTarget(android.R.id.statusBarBackground, true);
        getWindow().setEnterTransition(slide);*/

        // define root of View hierarchy where we want transition to happen
        // contains everything that needs to be animated
        ViewGroup transitionRoot = detailContainer;
        mTransitionManager = new TransitionManager();

        // expanded scene
        mExpandedScene = Scene.getSceneForLayout(transitionRoot,
                R.layout.activity_album_detail_expanded, this);
        // we want to reset the album image and other fields to the specific ones
        // for each item in the grid instead of using the default "Mean Something" one
        // as defined in the XML
        mExpandedScene.setEnterAction(new Runnable() {
            @Override
            public void run() {
                // since some Views are recreated, corresponding fields bound by
                // ButterKnife no longer refer to Views on screen. we must call bind again.
                ButterKnife.bind(AlbumDetailActivity.this);
                populate();
                mCurrentScene = mExpandedScene;
            }
        });
        TransitionSet expandTransitionSet = new TransitionSet();

        // set the ordering so that lyrics fade in comes after ChangeBounds transition.
        // orders them based on what order they were added to the TransitionSet. in our
        // case, that's ChangeBounds followed by Fade
        expandTransitionSet.setOrdering(TransitionSet.ORDERING_SEQUENTIAL);

        // create 1st transition that happens with the ViewGroups other than the TextView
        // that contains the lyrics
        ChangeBounds changeBounds = new ChangeBounds();
        changeBounds.setDuration(200);

        expandTransitionSet.addTransition(changeBounds);

        // Fade is a type of transition that we'll use to fade the lyrics in
        Fade fadeLyrics = new Fade();
        fadeLyrics.addTarget(R.id.lyrics);
        fadeLyrics.setDuration(150);

        expandTransitionSet.addTransition(fadeLyrics);

        // collapsed scene
        mCollapsedScene = Scene.getSceneForLayout(transitionRoot,
                R.layout.activity_album_detail, this);

        mCollapsedScene.setEnterAction(new Runnable() {
            @Override
            public void run() {
                // since some Views are recreated, corresponding fields bound by
                // ButterKnife no longer refer to Views on screen. we must call bind again.
                ButterKnife.bind(AlbumDetailActivity.this);
                populate();
                mCurrentScene = mCollapsedScene;
            }
        });
        TransitionSet collapseTransitionSet = new TransitionSet();
        collapseTransitionSet.setOrdering(TransitionSet.ORDERING_SEQUENTIAL);

        // Fade is a type of transition that we'll use to fade the lyrics in
        Fade fadeOutLyrics = new Fade();
        fadeOutLyrics.addTarget(R.id.lyrics);
        fadeOutLyrics.setDuration(150);
        collapseTransitionSet.addTransition(fadeOutLyrics);

        ChangeBounds resetBounds = new ChangeBounds();
        resetBounds.setDuration(200);
        collapseTransitionSet.addTransition(resetBounds);

        mTransitionManager.setTransition(mExpandedScene, mCollapsedScene,
                collapseTransitionSet);
        mTransitionManager.setTransition(mCollapsedScene, mExpandedScene,
                expandTransitionSet);
        // must enter starting scene before 1st transition
        mCollapsedScene.enter();
        //postponeEnterTransition();
    }

    private void populate() {
       /* following code can simulate a delay in the animation in response to clicking
          due to, e.g. network latency
       new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                int albumArtResId = getIntent().getIntExtra(EXTRA_ALBUM_ART_RESID,
                        R.drawable.mean_something_kinder_than_wolves);
                albumArtView.setImageResource(albumArtResId);

                Bitmap albumBitmap = getReducedBitmap(albumArtResId);
                colorizeFromImage(albumBitmap);
                startPostponedEnterTransition();
            }
        } , 1000);*/

        int albumArtResId = getIntent().getIntExtra(EXTRA_ALBUM_ART_RESID,
                R.drawable.mean_something_kinder_than_wolves);
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
        //fab.setBackgroundTintList(new ColorStateList(states, colors));
    }
}
