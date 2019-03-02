package com.frazycrazy.kappu.apptech2;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class BlogActivity extends AppCompatActivity {

    // Initializing of view
    ImageView iv_image;
    TextView tv_title,tv_desc;
    LayoutInflater layoutInflater;
    LinearLayout linearLayout ;

    // fb native ad
    View mview;
    TextView myAd_title,myAd_sponsored_label,myAd_social_context,myAd_body;
    Button myAd_call_to_action;
    @SuppressWarnings("deprecation")
    AdIconView myAd_ad_icon;
    MediaView myAd_native_ad_media;
    private final String TAG = BlogActivity.class.getSimpleName();
    private NativeAd nativeAd;

    // admobe interstial ad
    private  InterstitialAd mInterstitialAd;

    // custom progress
    ProgressBar progressBar;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blog);


        iv_image = findViewById(R.id.imageView_blog);
        tv_desc = findViewById(R.id.textView_blogDesc);
        tv_title = findViewById(R.id.textView_blogTitle);
        progressBar = findViewById(R.id.progress_for_blog);



        // get data from Home Activity
        Intent i = getIntent();
        String imgUrl = ""+i.getStringExtra("image_url");
        String imgTitle = ""+i.getStringExtra("image_title");
        String imgDesc = ""+i.getStringExtra("image_desc");

        // set data
        tv_title.setText(imgTitle);
        tv_desc.setText(imgDesc);
        Picasso.get().load(imgUrl).into(iv_image);


        // native ad call
        linearLayout = findViewById(R.id.liner_layout_activity_blog);
        AudienceNetworkAds.initialize(this);
        loadNativeAd();
        inflateMyLayout();
    }


    // adding native ad layout
    @SuppressLint("InflateParams")
    public void inflateMyLayout(){
        // fb ad inflate
        layoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        assert layoutInflater != null;
        mview = layoutInflater.inflate(R.layout.my_adview,null,false);


        // crashed !
//                // Add the AdOptionsView
//                LinearLayout adChoicesContainer = findViewById(R.id.ad_choices_container);
//                AdOptionsView adOptionsView = new AdOptionsView(BlogActivity.this, nativeAd, null);
//                adChoicesContainer.removeAllViews();
//                adChoicesContainer.addView(adOptionsView, 0);

        myAd_title = mview.findViewById(R.id.native_ad_title);
        myAd_sponsored_label = mview.findViewById(R.id.native_ad_sponsored_label);
        myAd_social_context = mview.findViewById(R.id.native_ad_social_context);
        myAd_body = mview.findViewById(R.id.native_ad_body);

        myAd_call_to_action = mview.findViewById(R.id.native_ad_call_to_action);

        myAd_ad_icon = mview.findViewById(R.id.native_ad_icon);
        myAd_native_ad_media = mview.findViewById(R.id.native_ad_media);
    }

    // FB native ad
    private void loadNativeAd() {

       progressBar.setVisibility(View.VISIBLE);
        nativeAd = new NativeAd(this, getResources().getString(R.string.fb_native_ad_1));
        nativeAd.setAdListener(new NativeAdListener() {
            @Override
            public void onMediaDownloaded(Ad ad) {
                // Native ad finished downloading all assets
                Log.e(TAG, "Native ad finished downloading all assets.");
            }

            @Override
            public void onError(Ad ad, AdError adError) {
                // Native ad failed to load
                Log.e(TAG, "Native ad failed to load: " + adError.getErrorMessage());

                progressBar.setVisibility(View.INVISIBLE);

                //Toast.makeText(BlogActivity.this, "Native ad failed to load ", Toast.LENGTH_SHORT).show();
                // cancel native ad
                mview.setVisibility(View.GONE);
                linearLayout.removeView(mview);

                // add data to screen
                iv_image.setVisibility(View.VISIBLE);
                tv_title.setVisibility(View.VISIBLE);
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
                progressBar.setVisibility(View.INVISIBLE);
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
        nativeAd.loadAd();
    }
    public void callNativeAd(){


        myAd_title.setText(nativeAd.getAdHeadline());
        myAd_sponsored_label.setText(nativeAd.getSponsoredTranslation());
        myAd_social_context.setText(nativeAd.getAdSocialContext());
        myAd_body.setText(nativeAd.getAdBodyText());
        myAd_call_to_action.setText(nativeAd.getAdCallToAction());
        myAd_call_to_action.setVisibility(nativeAd.hasCallToAction() ? View.VISIBLE : View.INVISIBLE);


        // Create a list of clickable views
        List<View> clickableViews = new ArrayList<>();
        clickableViews.add(myAd_title);
        clickableViews.add(myAd_call_to_action);

        // close ad
        ImageView myAd_ad_close = mview.findViewById(R.id.native_ad_close);
        myAd_ad_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                linearLayout.removeView(mview);
                iv_image.setVisibility(View.VISIBLE);
                tv_title.setVisibility(View.VISIBLE);
                tv_desc.setVisibility(View.VISIBLE);
            }
        });
        nativeAd.registerViewForInteraction(
                mview,
                myAd_native_ad_media,
                myAd_ad_icon,
                clickableViews);
        linearLayout.addView(mview);

    }

    // admob Interstial ad
    public void callInterstialAd() {

        //********* InterstitialAd
        AdRequest mAdRequest2 = new AdRequest.Builder().build();
        mInterstitialAd = new InterstitialAd(BlogActivity.this);
        mInterstitialAd.setAdUnitId(getResources().getString(R.string.interstitial_ad_1));
        mInterstitialAd.loadAd(mAdRequest2);
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                if (mInterstitialAd.isLoaded()) {
                    mInterstitialAd.show();
                }
            }
        });
    }


}
