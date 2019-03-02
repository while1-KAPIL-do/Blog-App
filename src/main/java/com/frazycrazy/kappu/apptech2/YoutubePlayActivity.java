package com.frazycrazy.kappu.apptech2;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdIconView;
import com.facebook.ads.AudienceNetworkAds;
import com.facebook.ads.MediaView;
import com.facebook.ads.NativeAd;
import com.facebook.ads.NativeAdListener;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;

import java.util.ArrayList;
import java.util.List;

public class YoutubePlayActivity extends YouTubeBaseActivity {

    ProgressBar progressBar_for_yt;

    // Initializing of view
    LayoutInflater layoutInflater3;
    LinearLayout linearLayout3 ;

    // fb native ad
    View mview3;
    TextView myAd_title3,myAd_sponsored_label3,myAd_social_context3,myAd_body3;
    Button myAd_call_to_action3;
    @SuppressWarnings("deprecation")
    AdIconView myAd_ad_icon3;
    MediaView myAd_native_ad_media3;

    // YT
    private final String TAG = BlogActivity.class.getSimpleName();
    private NativeAd nativeAd3;

    // admobe interstial ad
    private InterstitialAd mInterstitialAd3;

    YouTubePlayerView youTubePlayerView;
    YouTubePlayer.OnInitializedListener onInitializedListener;

    FloatingActionButton fab;
    TextView tv_title,tv_desc;

    int counter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_youtube_play);

        progressBar_for_yt = findViewById(R.id.progress_for_yt);

        fab = findViewById(R.id.floatingActionButton);
        tv_title = findViewById(R.id.textView_videoTitle);
        tv_desc = findViewById(R.id.textView_videoDesc);

        final Intent i = getIntent();

        String imgTitle = i.getStringExtra("image_title");
        String imgDesc = i.getStringExtra("image_desc");
        tv_title.setText(imgTitle);
        tv_desc.setText(imgDesc);
        Log.d(TAG, "onCreate: "+i.getStringExtra("url"));

        //final String s = i.getStringExtra("url").trim().split("v=")[1];

        //Log.d("Check------>>>>>>", "onCreate: "+s);
        youTubePlayerView = findViewById(R.id.youtube_view);
        onInitializedListener = new YouTubePlayer.OnInitializedListener() {
            @Override
            public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean b) {
                youTubePlayer.loadVideo(i.getStringExtra("url").trim().split("v=")[1]);
                //youTubePlayer.setFullscreen(true);
                youTubePlayer.pause();

            }

            @Override
            public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {

            }
        };

        // native ad call
        linearLayout3 = findViewById(R.id.liner_layout_activity_youtube);
        AudienceNetworkAds.initialize(this);
        loadNativeAd();
        inflateMyLayout();


    }



    @Override
    public void onBackPressed() {
        Intent returnIntent = new Intent();
        returnIntent.putExtra("hasBackPressed",counter);
        setResult(Activity.RESULT_OK,returnIntent);
        finish();
    }

    public void play(View view) {
        youTubePlayerView.initialize("AIzaSyBfBLslee5KbU5o0w6nm_waMzA8q6evAx4", onInitializedListener);
        fab.setVisibility(View.GONE);
    }


    // adding native ad layout
    @SuppressLint("InflateParams")
    public void inflateMyLayout(){
        // fb ad inflate
        layoutInflater3 = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        assert layoutInflater3 != null;
        mview3 = layoutInflater3.inflate(R.layout.my_adview,null,false);


        // crashed !
//                // Add the AdOptionsView
//                LinearLayout adChoicesContainer = findViewById(R.id.ad_choices_container);
//                AdOptionsView adOptionsView = new AdOptionsView(BlogActivity.this, nativeAd3, null);
//                adChoicesContainer.removeAllViews();
//                adChoicesContainer.addView(adOptionsView, 0);

        myAd_title3 = mview3.findViewById(R.id.native_ad_title);
        myAd_sponsored_label3 = mview3.findViewById(R.id.native_ad_sponsored_label);
        myAd_social_context3 = mview3.findViewById(R.id.native_ad_social_context);
        myAd_body3 = mview3.findViewById(R.id.native_ad_body);

        myAd_call_to_action3 = mview3.findViewById(R.id.native_ad_call_to_action);

        myAd_ad_icon3 = mview3.findViewById(R.id.native_ad_icon);
        myAd_native_ad_media3 = mview3.findViewById(R.id.native_ad_media);
    }

    // FB native ad
    private void loadNativeAd() {

        progressBar_for_yt.setVisibility(View.VISIBLE);

        nativeAd3 = new NativeAd(this, getResources().getString(R.string.fb_native_ad_1));
        nativeAd3.setAdListener(new NativeAdListener() {
            @Override
            public void onMediaDownloaded(Ad ad) {
                // Native ad finished downloading all assets
                Log.e(TAG, "Native ad finished downloading all assets.");
            }

            @Override
            public void onError(Ad ad, AdError adError) {
                // Native ad failed to load
                Log.e(TAG, "Native ad failed to load: " + adError.getErrorMessage());


                progressBar_for_yt.setVisibility(View.INVISIBLE);

                //Toast.makeText(YoutubePlayActivity.this, "Native ad failed to load ", Toast.LENGTH_SHORT).show();
                // cancel native ad
                mview3.setVisibility(View.GONE);
                linearLayout3.removeView(mview3);

                // add data to screen
                youTubePlayerView.setVisibility(View.VISIBLE);
                tv_title.setVisibility(View.VISIBLE);

                fab.setVisibility(View.VISIBLE);
                tv_desc.setVisibility(View.VISIBLE);

                // call admobe interstial ad
                callInterstialAd();
            }

            @Override
            public void onAdLoaded(Ad ad) {
                // Native ad is loaded and ready to be displayed
                Log.d(TAG, "Native ad is loaded and ready to be displayed!");
                // call native ad
                callNativeAd();
                progressBar_for_yt.setVisibility(View.INVISIBLE);

            }

            @Override
            public void onAdClicked(Ad ad) {
                // Native ad clicked
                Log.d(TAG, "Native ad clicked!");
            }

            @Override
            public void onLoggingImpression(Ad ad) {
                // Native ad impression
                Log.d(TAG, "Native ad impression logged!");
            }
        });

        // Request for ad
        nativeAd3.loadAd();
    }
    public void callNativeAd(){


        myAd_title3.setText(nativeAd3.getAdHeadline());
        myAd_sponsored_label3.setText(nativeAd3.getSponsoredTranslation());
        myAd_social_context3.setText(nativeAd3.getAdSocialContext());
        myAd_body3.setText(nativeAd3.getAdBodyText());
        myAd_call_to_action3.setText(nativeAd3.getAdCallToAction());
        myAd_call_to_action3.setVisibility(nativeAd3.hasCallToAction() ? View.VISIBLE : View.INVISIBLE);


        // Create a list of clickable views
        List<View> clickableViews = new ArrayList<>();
        clickableViews.add(myAd_title3);
        clickableViews.add(myAd_call_to_action3);


        // close ad
        ImageView myAd_ad_close = mview3.findViewById(R.id.native_ad_close);
        myAd_ad_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                linearLayout3.removeView(mview3);
                youTubePlayerView.setVisibility(View.VISIBLE);
                tv_title.setVisibility(View.VISIBLE);
                fab.setVisibility(View.VISIBLE);
                tv_desc.setVisibility(View.VISIBLE);
            }
        });
        nativeAd3.registerViewForInteraction(
                mview3,
                myAd_native_ad_media3,
                myAd_ad_icon3,
                clickableViews);
        linearLayout3.addView(mview3);

    }

    // admob Interstial ad
    public void callInterstialAd() {

        //********* InterstitialAd
        AdRequest mAdRequest2 = new AdRequest.Builder().build();
        mInterstitialAd3 = new InterstitialAd(YoutubePlayActivity.this);
        mInterstitialAd3.setAdUnitId(getResources().getString(R.string.interstitial_ad_1));
        mInterstitialAd3.loadAd(mAdRequest2);
        mInterstitialAd3.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                if (mInterstitialAd3.isLoaded()) {
                    mInterstitialAd3.show();
                }
            }
        });
    }


}
