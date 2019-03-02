package com.frazycrazy.kappu.apptech2;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.Gravity;
import android.view.SubMenu;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.frazycrazy.kappu.apptech2.fragments.RegisterFragment;
import com.frazycrazy.kappu.apptech2.model.MenuModel;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;

public class NavigationActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {


    // Firebase
    FirebaseAuth logout_mAuth;
    FirebaseAuth.AuthStateListener mAuthlistener;

    private FirebaseAnalytics mFirebaseAnalytics;
    //____________
    private static final String TAG ="Navigation --- >>>>";
    private static final int NUM_COLUMNS = 2;
    SwipeRefreshLayout mSwipeRefreshLayout;
    JSONArray cast;

    boolean isUserActive=false;

    boolean is_btn_shw ;
    StaggeredRecyclerViewAdapter staggeredRecyclerViewAdapter;

    // Error
    ImageView error_img;
    TextView error_txt;

    ProgressBar progressBar;

    AutoCompleteTextView autoCompleteTextView ;
    String[] data_list_title;
    HashSet<String> data_list_cat;

    private ArrayList<MyObject> mObj = new ArrayList<>();
    String sEmail;
    String sName;

    // Scheduled Notification Time in MILISECOUNDS
    // ( 1000 = 1 sec | 1000*60*60 = 1 hr | 1000*60*60*24 = 1 day )
    int SECHDULED_NOTI_TIME = 1000*60*60;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Get data frome Shared Pref
        SharedPreferences sharedPreferences = getSharedPreferences(getResources().getString(R.string.shared_pref), MODE_PRIVATE);
        sEmail = sharedPreferences.getString(getResources().getString(R.string.sp_email),"");
        sName = sharedPreferences.getString(getResources().getString(R.string.sp_name),"");

        // Firebase

        // Analytics
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        // Auth
        logout_mAuth = FirebaseAuth.getInstance();
        mAuthlistener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if(firebaseAuth.getCurrentUser() == null){
                   // user not sign in
                    isUserActive = false;
                    is_btn_shw = false;

                }else{
                    // user signed in
                    isUserActive = true;
                }
            }
        };

        //______________ Gone _____________________
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        //______________ Gone _____________________/

        // Set Drawer
        final DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        ConstraintLayout drawer_icon;
        drawer_icon = findViewById(R.id.open_nev);
        drawer_icon.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("RtlHardcoded")
            @Override
            public void onClick(View view) {
                // set drawer on left side
                drawer.openDrawer(Gravity.LEFT);

            }
        });



        // Notification after 1 hr after opening app
        // ( 1000 = 1 sec | 1000*60*60 = 1 hr | 1000*60*60*24 = 1 day )
        scheduleNotification(getNotification(), SECHDULED_NOTI_TIME);

        // initialization error / progress bar
        error_img =  findViewById(R.id.no_internet_img);
        error_txt =   findViewById(R.id.no_internet_txt);
        progressBar = findViewById(R.id.progress);

        // checking internet connection
        if (isConn()) {
            // swipe_up layout gone
            findViewById(R.id.swipe_refresh).setVisibility(View.GONE);

            // taking all data from PHP server
            callData();
            //progressBar.setVisibility(View.INVISIBLE);
        }else{
            error_txt.setText("No Internet Connection ! \n swipe-up for Refresh ");
            error_img.setImageResource(R.drawable.no_internet);
            progressBar.setVisibility(View.INVISIBLE);
            findViewById(R.id.swipe_refresh).setVisibility(View.VISIBLE);
        }

        // Swipe Up for Refresh network
        mSwipeRefreshLayout = findViewById(R.id.swipe_refresh);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (isConn()) {
                    findViewById(R.id.swipe_refresh).setVisibility(View.GONE);
                    // taking all data from PHP server
                    callData();
                }else{
                    error_txt.setText("No Internet Connection ! \n swipe-up for Refresh ");
                    error_img.setImageResource(R.drawable.no_internet);
                    Toast.makeText(NavigationActivity.this, "No Internet !", Toast.LENGTH_SHORT).show();
                    findViewById(R.id.swipe_refresh).setVisibility(View.VISIBLE);
                }
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });


    }
    DrawerLayout drawerLayout;

    protected void onStart() {
        super.onStart();
        logout_mAuth.addAuthStateListener(mAuthlistener);
    }
    private void displayMenu(boolean isUserAuth) {

        // initialize nav drawer
        NavigationView navigationView = findViewById(R.id.nav_view);
        drawerLayout = findViewById(R.id.drawer_layout);

        // set drawer items
        Menu menu = navigationView.getMenu();


        if(isUserActive){

            // Menu - 1 - Account
            // set drawer items -> when user is logged in
            menu.add("Account").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {

                    //menuItem.setIcon(R.drawable.ic_email_black_24dp);
                    startActivity(new Intent(NavigationActivity.this, AccountActivity.class));
                    finish();
                    drawerLayout.closeDrawers();
                    return false;
                }
            });

            // set drawer item -> when user is Authorized by php server --> isUserAuth
            if (isUserAuth) {
                menu.add("Send Your Own Video").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {

                        //menuItem.setIcon(R.drawable.ic_touch_app_black_24dp);
                        startActivity(new Intent(NavigationActivity.this, UploadVideoActivity.class));
                        drawerLayout.closeDrawers();
                        return false;
                    }
                });
            }
        }else{
            // set drawer items -> when user is not logged in
            menu.add("Sign Up and Win ").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {

                   // menuItem.setIcon(R.drawable.ic_email_black_24dp);

                    Fragment fragment;
                    fragment = new RegisterFragment();
                    findViewById(R.id.content_frame).setVisibility(View.VISIBLE);
                    findViewById(R.id.recycler_view).setVisibility(View.GONE);
                    findViewById(R.id.swipe_refresh).setVisibility(View.GONE);
                    FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                    ft.replace(R.id.content_frame, fragment);
                    ft.commit();
                    drawerLayout.closeDrawers();
                    return false;
                }
            });
        }


        // Menu - 2 - Home
        menu.add("Home").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {

                //menuItem.setIcon(R.drawable.ic_home_black_24dp);

                initRecyclerView(mObj);
                findViewById(R.id.content_frame).setVisibility(View.GONE);
                findViewById(R.id.swipe_refresh).setVisibility(View.GONE);
                findViewById(R.id.no_internet_txt).setVisibility(View.GONE);
                findViewById(R.id.no_internet_img).setVisibility(View.GONE);
                findViewById(R.id.recycler_view).setVisibility(View.VISIBLE);
                drawerLayout.closeDrawers();
                return false;
            }
        });

        // Menu - 3 - Category
        // taking all category by PHP server using --> data_list_cat --> addData()
        MenuModel menuModel = new MenuModel(); 
        SubMenu menu1 = menu.addSubMenu("Category"); //.setHeaderIcon(R.drawable.ic_filter_vintage_black_24dp);

        if(!data_list_cat.isEmpty()) {
            for (String aData_list_cat : data_list_cat) {
                menuModel.add(aData_list_cat);
            }
        }
        for(String m : menuModel.findAll()){
            menu1.add(m).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    Log.d(TAG, "onMenuItemClick: "+menuItem.toString());

                    // on Menu Item clicked --> Category
                    do_onMenuItemClick(menuItem.toString());
                    findViewById(R.id.content_frame).setVisibility(View.GONE);
                    findViewById(R.id.swipe_refresh).setVisibility(View.GONE);
                    findViewById(R.id.no_internet_txt).setVisibility(View.GONE);
                    findViewById(R.id.no_internet_img).setVisibility(View.GONE);
                    findViewById(R.id.recycler_view).setVisibility(View.VISIBLE);
                    drawerLayout.closeDrawers();
                    //Toast.makeText(NavigationActivity.this, ""+menuItem.toString(), Toast.LENGTH_SHORT).show();
                    return false;
                }
            });
        }

        // Menu - 4 - Logout
        if(isUserActive) {
            menu.add("Logout").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {

                    //menuItem.setIcon(R.drawable.ic_backspace_black_24dp);

                    AlertDialog.Builder builder = new AlertDialog.Builder(NavigationActivity.this);
                    builder.setTitle("Are you sure want to Log-out ?").setPositiveButton("log-out", new DialogInterface.OnClickListener() {
                        @SuppressLint("ApplySharedPref")
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            // removing shared pref
                            SharedPreferences sharedPreferences = getSharedPreferences(getResources().getString(R.string.shared_pref), MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString(getResources().getString(R.string.sp_email),"");
                            editor.putString(getResources().getString(R.string.sp_name),"");
                            editor.putString(getResources().getString(R.string.sp_mobile),"");
                            editor.putString(getResources().getString(R.string.sp_address),"");
                            editor.putString(getResources().getString(R.string.sp_profile_desc),"");
                            editor.commit();

                            logout_mAuth.signOut();
                            drawerLayout.closeDrawers();
                            Toast.makeText(NavigationActivity.this, "Logging Out...", Toast.LENGTH_SHORT).show();
                            recreate();
                        }
                    }).setNegativeButton("NO",null).setCancelable(false);
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                    return false;
                }
            });
        }
        drawerLayout.closeDrawers();
    }
    public void do_onMenuItemClick(String item) {

        //--------->  displayMenu();

        try {
            ArrayList<MyObject> mObj_cat = new ArrayList<>();
            for (int i = cast.length() - 1; i >= 0; i--) {
                JSONObject obj_item = cast.getJSONObject(i);
                if (item.equals(obj_item.getString("cat"))){

                    mObj_cat.add(new MyObject(getResources().getString(R.string.image_url_prefix)+obj_item.getString("img_url"),
                            obj_item.getString("title"), obj_item.getString("des"),
                            obj_item.getString("url"), obj_item.getString("type")));
                }
            }
            initRecyclerView(mObj_cat);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    
    // Notification
    private Notification getNotification() {

        // scheduled Noti Initializing
        Notification.Builder builder = new Notification.Builder(this);
        builder.setContentTitle(getResources().getString(R.string.scheduled_notification_title));
        builder.setContentText(getResources().getString(R.string.scheduled_notification_text));
        // set Large icon
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_notifications_active_black_24dp));
        // set Small icon
        builder.setSmallIcon(R.mipmap.ic_launcher);
        // On click on Notifi...
        Intent noti_intent = new Intent(this, SplashActivity.class);
        PendingIntent content_intent = PendingIntent.getActivity(this, 0, noti_intent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.addAction(android.R.drawable.ic_menu_view,"View",content_intent);
        Uri path = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        builder.setSound(path);
        return builder.build();
    }
    private void scheduleNotification(Notification notification, int delay) {

        // set Alarm for notification
        Intent notificationIntent = new Intent(this, NotificationPublisher.class);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION_ID, 1);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION, notification);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        long futureInMillis = SystemClock.elapsedRealtime() + delay;
        AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        assert alarmManager != null;
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, futureInMillis, pendingIntent);
    }

    // checking internet conn
    private boolean isConn(){
        ConnectivityManager connectivityManager = (ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = null;
        if (connectivityManager != null) {
            networkInfo = connectivityManager.getActiveNetworkInfo();
        }
        return (networkInfo!=null && networkInfo.isConnected());
    }
    
    // alert dialog on back press
    private class ScrollListener extends RecyclerView.OnScrollListener {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            // staggeredRecyclerViewAdapter.invalidateSpanAssignments();
        }
    }
    // taking data from php Server
    void callData() {

        progressBar.setVisibility(View.VISIBLE);
        //String server_url = "http://www.json-generator.com/api/json/get/bUYHvhSbqW?indent=2";

        String server_url = getResources().getString(R.string.api_alldata);

        // check OUTPUT ->  Example,json

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, server_url, (String) null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("------->>>>>>>", "onResponse: " + response);
                        try {
                            cast = response.getJSONArray(getResources().getString(R.string.api_res_obj1));
                            JSONObject myData ;
                            if (cast.length() == 0){
                                Toast.makeText(NavigationActivity.this, "Database is empty !", Toast.LENGTH_SHORT).show();
                                progressBar.setVisibility(View.INVISIBLE);
                            }else {
                                progressBar.setVisibility(View.INVISIBLE);

                                // Taking all data From the server

                                data_list_title = new String[cast.length()];
                                data_list_cat = new HashSet<>(cast.length());

                                // for (int i = 0; i < cast.length()  ; i++) {  // for reverse order
                                for (int i = cast.length() - 1, j = 0; i >= 0; i--, j++) {
                                    myData = cast.getJSONObject(i);
                                    Log.d(TAG, "onResponse: " + i + " \n" + myData);

                                    //String data_id = ""+ myData.getString("id");
                                    String data_imageTitle = "" + myData.getString( getResources().getString(R.string.api_res_obj1_array_title));
                                    String data_imageDest = "" + myData.getString(getResources().getString(R.string.api_res_obj1_array_des));
                                    String data_imageUrl = getResources().getString(R.string.image_url_prefix)
                                            + myData.getString(getResources().getString(R.string.api_res_obj1_array_imag_url));
                                    String data_mainUrl = "" + myData.getString(getResources().getString(R.string.api_res_obj1_array_url));
                                    String data_type = "" + myData.getString(getResources().getString(R.string.api_res_obj1_array_type));
                                    String data_cat = "" + myData.getString(getResources().getString(R.string.api_res_obj1_array_cat));

                                    data_list_title[j] = "" + data_imageTitle;
                                    Log.d(TAG, "onResponse: \n\n" + j + " : " + data_imageTitle + "\n");

                                    data_list_cat.add(data_cat);


                                    if (data_imageUrl.isEmpty()) {
                                        data_imageUrl = getResources().getString(R.string.no_img_url);
                                    }
                                    if (data_imageTitle.isEmpty()) {
                                        data_imageTitle = "No Title Available";
                                    }
                                    if (data_imageDest.isEmpty()) {
                                        data_imageDest = "No Description Available";
                                    }
                                    mObj.add(new MyObject(data_imageUrl, data_imageTitle, data_imageDest, data_mainUrl, data_type));
                                }

                                    Log.d(TAG, "onResponse: List -> " + data_list_cat.toString());

                                if (isUserActive){
                                    String user_email_id = Objects.requireNonNull(logout_mAuth.getCurrentUser()).getEmail();
                                    isUserAuthorize(user_email_id);
                                }else{
                                    displayMenu(false);
                                }

                                initRecyclerView(mObj);

                                // Apply all data on Staggered Recycler Vie
                            }
                        } catch (JSONException e) {
                            error_txt.setText("Server Error ! \n swipe-up for Refresh ");
                            error_img.setImageResource(R.drawable.server_error);
                            findViewById(R.id.swipe_refresh).setVisibility(View.VISIBLE);

                            progressBar.setVisibility(View.INVISIBLE);
                            Toast.makeText(NavigationActivity.this, "Server Error ...!", Toast.LENGTH_SHORT).show();
                            Toast.makeText(NavigationActivity.this, "Contact on Admin", Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {


                progressBar.setVisibility(View.INVISIBLE);
                error_txt.setText("Your Internet Connection is to slow... \n swipe-up for Refresh ");
                error_img.setImageResource(R.drawable.no_internet);
                findViewById(R.id.swipe_refresh).setVisibility(View.VISIBLE);
                Toast.makeText(NavigationActivity.this, "Check your connection !", Toast.LENGTH_SHORT).show();
                error.printStackTrace();
            }
        });
        Mysingleton.getInstance(NavigationActivity.this).addToRequestque(jsonObjectRequest);
    }

    // Staggered Recycler View
    private void initRecyclerView(ArrayList<MyObject>  myObject){
        Log.d(TAG, "initRecyclerView: initializing staggered recyclerview.");

        // set Recycler View
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        staggeredRecyclerViewAdapter =
                new StaggeredRecyclerViewAdapter(this, myObject);
        StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(NUM_COLUMNS, LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(staggeredGridLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addOnScrollListener(new NavigationActivity.ScrollListener());
        recyclerView.setAdapter(staggeredRecyclerViewAdapter);
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
       
        return true;
    }


    public void do_search_btn(View view) {

        // Search Icon & AutoComplete ----> Working

        if (data_list_title != null){
            // data is not empty
            autoCompleteTextView = findViewById(R.id.auto_comp_tv);
            Log.d(TAG, "do_search_btn: " + autoCompleteTextView.getVisibility());
            if (autoCompleteTextView.getVisibility() != View.VISIBLE) {

                // search bar activate
                autoCompleteTextView.setVisibility(View.VISIBLE);
                autoCompleteTextView.setBackgroundColor(Color.WHITE);
                autoCompleteTextView.setHintTextColor(Color.GRAY);
                //autoCompleteTextView.setDropDownBackgroundDrawable(R.drawable.server_error);
                autoCompleteTextView.requestFocus();
                // auto Complete Search Bar
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.autocomplete_custom, data_list_title);

                autoCompleteTextView.setAdapter(adapter);
                autoCompleteTextView.setThreshold(1);
                autoCompleteTextView.setAdapter(adapter);
            } else {
                Toast.makeText(this, "" + autoCompleteTextView.getText().toString(), Toast.LENGTH_SHORT).show();
                callData_for_search_bar(autoCompleteTextView.getText().toString());
            }
            autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    Log.d(TAG, "onItemClick: " + i + " : " + autoCompleteTextView.getText().toString() + "\n" + view + "\n" + adapterView);
                    callData_for_search_bar(autoCompleteTextView.getText().toString());
                }
            });
            // autoCompleteTextView.showDropDown();

            // auto Complete Search Bar----

        }else{
            // data is empty
            Toast.makeText(this, "Load the page first !", Toast.LENGTH_SHORT).show();
        }
    }
    public void callData_for_search_bar(String title){

        // On click on --> Search bar drop down
        try {
            JSONObject myData_for_search;
            for (int i = 0; i < cast.length(); i++) {
                myData_for_search = cast.getJSONObject(i);
                if(myData_for_search.getString("title").equals(title)){
                    switch (myData_for_search.getString("type")) {
                        case "youtube": {
                            Intent intent = new Intent(NavigationActivity.this, YoutubePlayActivity.class);
                            intent.putExtra("url", myData_for_search.getString("url"));
                            intent.putExtra("image_title", myData_for_search.getString("title"));
                            intent.putExtra("image_desc", myData_for_search.getString("des"));
                            NavigationActivity.this.startActivity(intent);
                            break;
                        }
                        case "blog": {

                            Intent intent = new Intent(NavigationActivity.this, BlogActivity.class);
                            intent.putExtra("image_url", myData_for_search.getString("img_url"));
                            intent.putExtra("image_title", myData_for_search.getString("title"));
                            intent.putExtra("image_desc", myData_for_search.getString("des"));
                            NavigationActivity.this.startActivity(intent);
                            break;
                        }
                        case "web": {

                            Intent intent = new Intent(NavigationActivity.this, WebActivity.class);
                            intent.putExtra("url", myData_for_search.getString("url"));
                            NavigationActivity.this.startActivity(intent);
                            break;
                        }
                        default:
                            Toast.makeText(NavigationActivity.this, "Data is not Available !", Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
    
    @Override
    public void onBackPressed() {

        // check drawer is open ?
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {

            // if drawer open then close
            drawer.closeDrawer(GravityCompat.START);
        } else {
            // otherwise exit pop up
            AlertDialog.Builder builder = new AlertDialog.Builder(NavigationActivity.this);
            builder.setTitle("Are you sure want to exit ?").setPositiveButton("YES", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    NavigationActivity.super.onBackPressed();
                    finish();
                }
            }).setNegativeButton("NO",null).setCancelable(false);
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }
    }


    // Checking is user Is Authorized user or not ?
    int isAuth ;
    public void isUserAuthorize(final String check_email){

        // send the user id to php server if id is authorized then it ill return 1 in shw
        String api_user_show =  getResources().getString(R.string.api_user_show);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, api_user_show,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            Log.d(TAG, "onResponse: ----->"+response);

                            JSONObject jObj_p = new JSONObject(response);
                            jObj_p.getBoolean(getResources().getString(R.string.api_res_error));

                            JSONObject checkObj = jObj_p.getJSONObject(getResources().getString(R.string.api_user_show_user));
                            isAuth = checkObj.getInt("shw");

                            if (isAuth == 1) {
                                // authorize user
                                displayMenu(true);
                            }else{
                                // non authorize user
                                displayMenu(false);
                            }

                        } catch (JSONException e) {
                            // JSON error
                            e.printStackTrace();
                            //Toast.makeText(getApplicationContext(), "Error !\n" + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("email", check_email);
                return params;
            }
        };
        Mysingleton.getInstance(NavigationActivity.this).addToRequestque(stringRequest);
        //-----------

    }

}
