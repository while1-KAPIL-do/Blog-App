package com.frazycrazy.kappu.apptech2;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.FrameLayout;
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

import java.util.ArrayList;
import java.util.List;

public class WebActivity extends AppCompatActivity {

    WebView webView;

    // Initializing of view
    LayoutInflater layoutInflater2;
    // fb native ad
    LinearLayout linearLayout2 ;
    View mview2;
    TextView myAd_title2,myAd_sponsored_label2,myAd_social_context2,myAd_body2;
    Button myAd_call_to_action2;
    @SuppressWarnings("deprecation")
    AdIconView myAd_ad_icon2;
    MediaView myAd_native_ad_media2;
    private final String TAG = WebActivity.class.getSimpleName();
    private NativeAd nativeAd2;

    // admobe interstial ad
    private InterstitialAd mInterstitialAd2;


    ProgressBar progressBar;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);

        // native ad call
        linearLayout2 = findViewById(R.id.liner_layout_activity_web);
        AudienceNetworkAds.initialize(this);
        loadNativeAd();
        inflateMyLayout();


        Intent i = getIntent();
        String url = i.getStringExtra("url");

        //********* webview ***************


        progressBar = findViewById(R.id.progressBar);

        webView = findViewById(R.id.webView);
        webView.setWebChromeClient(new MyChrome());
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                progressBar.setVisibility(View.VISIBLE);
                setTitle("Loading...");
            }
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                progressBar.setVisibility(View.INVISIBLE);
                setTitle(view.getTitle());
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                progressBar.setProgress(progress);
            }
        });

        webView.loadUrl(url);

        //********* / webview ***************
    }

    @Override
    public void onBackPressed() {
        if(webView.canGoBack()){ webView.goBack();}
        else{super.onBackPressed(); }
    }

    // for Full Screen

    private class MyChrome extends WebChromeClient {

        private View mCustomView;
        private CustomViewCallback mCustomViewCallback;
        private int mOriginalOrientation;
        private int mOriginalSystemUiVisibility;

        MyChrome(){ }

        public  Bitmap getDefaultVideoPoster(){
            if(mCustomView == null){
                return  null;
            }
            return BitmapFactory.decodeResource(getApplicationContext().getResources(),2130837573);
        }

        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);
        }

        public void onHideCustomView(){
            ((FrameLayout)getWindow().getDecorView()).removeView(this.mCustomView);
            this.mCustomView = null;
            getWindow().getDecorView().setSystemUiVisibility(this.mOriginalSystemUiVisibility);
            setRequestedOrientation(this.mOriginalOrientation);
            this.mCustomViewCallback.onCustomViewHidden();
            this.mCustomViewCallback = null;
        }

        public void onShowCustomView(View parentView,CustomViewCallback paramCustomViewCallback){
            if (this.mCustomView != null){
                onHideCustomView();
                return;
            }
            this.mCustomView= parentView;
            this.mOriginalSystemUiVisibility = getWindow().getDecorView().getSystemUiVisibility();
            this.mOriginalOrientation =  getRequestedOrientation();
            this.mCustomViewCallback = paramCustomViewCallback;
            ((FrameLayout)getWindow().getDecorView()).addView(this.mCustomView, new FrameLayout.LayoutParams(-1,-1));
            getWindow().getDecorView().setSystemUiVisibility(3846);

        }
    }



    // ads-------------
    // adding native ad layout
    @SuppressLint("InflateParams")
    public void inflateMyLayout(){
        // fb ad inflate
        layoutInflater2 = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        assert layoutInflater2 != null;
        mview2 = layoutInflater2.inflate(R.layout.my_adview,null,false);


        // crashed !
//                // Add the AdOptionsView
//                LinearLayout adChoicesContainer = findViewById(R.id.ad_choices_container);
//                AdOptionsView adOptionsView = new AdOptionsView(BlogActivity.this, nativeAd2, null);
//                adChoicesContainer.removeAllViews();
//                adChoicesContainer.addView(adOptionsView, 0);

        myAd_title2 = mview2.findViewById(R.id.native_ad_title);
        myAd_sponsored_label2 = mview2.findViewById(R.id.native_ad_sponsored_label);
        myAd_social_context2 = mview2.findViewById(R.id.native_ad_social_context);
        myAd_body2 = mview2.findViewById(R.id.native_ad_body);

        myAd_call_to_action2 = mview2.findViewById(R.id.native_ad_call_to_action);

        myAd_ad_icon2 = mview2.findViewById(R.id.native_ad_icon);
        myAd_native_ad_media2 = mview2.findViewById(R.id.native_ad_media);
    }

    // FB native ad
    private void loadNativeAd() {
        nativeAd2 = new NativeAd(this, getResources().getString(R.string.fb_native_ad_1));
        nativeAd2.setAdListener(new NativeAdListener() {
            @Override
            public void onMediaDownloaded(Ad ad) {
                // Native ad finished downloading all assets
                Log.e(TAG, "Native ad finished downloading all assets.");
            }

            @Override
            public void onError(Ad ad, AdError adError) {
                // Native ad failed to load
                Log.e(TAG, "Native ad failed to load: " + adError.getErrorMessage());
                //Toast.makeText(WebActivity.this, "Native ad failed to load ", Toast.LENGTH_SHORT).show();
                // cancel native ad
                mview2.setVisibility(View.GONE);
                webView.setVisibility(View.VISIBLE);
                linearLayout2.removeView(mview2);
                // call admobe interstial ad
                callInterstialAd();
            }

            @Override
            public void onAdLoaded(Ad ad) {
                // Native ad is loaded and ready to be displayed
                Log.d(TAG, "Native ad is loaded and ready to be displayed!");
                // call native ad
                callNativeAd();

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
        nativeAd2.loadAd();
    }
    public void callNativeAd(){


        myAd_title2.setText(nativeAd2.getAdHeadline());
        myAd_sponsored_label2.setText(nativeAd2.getSponsoredTranslation());
        myAd_social_context2.setText(nativeAd2.getAdSocialContext());
        myAd_body2.setText(nativeAd2.getAdBodyText());
        myAd_call_to_action2.setText(nativeAd2.getAdCallToAction());
        myAd_call_to_action2.setVisibility(nativeAd2.hasCallToAction() ? View.VISIBLE : View.INVISIBLE);


        // Create a list of clickable views
        List<View> clickableViews = new ArrayList<>();
        clickableViews.add(myAd_title2);
        clickableViews.add(myAd_call_to_action2);

        // close ad
        ImageView myAd_ad_close = mview2.findViewById(R.id.native_ad_close);
        myAd_ad_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                linearLayout2.removeView(mview2);
                webView.setVisibility(View.VISIBLE);
            }
        });
        nativeAd2.registerViewForInteraction(
                mview2,
                myAd_native_ad_media2,
                myAd_ad_icon2,
                clickableViews);
        linearLayout2.addView(mview2);

    }

    // admob Interstial ad
    public void callInterstialAd() {

        //********* InterstitialAd
        AdRequest mAdRequest2 = new AdRequest.Builder().build();
        mInterstitialAd2 = new InterstitialAd(WebActivity.this);
        mInterstitialAd2.setAdUnitId(getResources().getString(R.string.interstitial_ad_1));
        mInterstitialAd2.loadAd(mAdRequest2);
        mInterstitialAd2.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                if (mInterstitialAd2.isLoaded()) {
                    mInterstitialAd2.show();
                }
            }
        });
    }

}
