package com.frazycrazy.kappu.apptech2;

import android.annotation.SuppressLint;
import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Created by saurav on 28-Aug-18.
 */

class Mysingleton {

    @SuppressLint("StaticFieldLeak")
    private static Mysingleton myInstance;
    private RequestQueue requestQueue;
    @SuppressLint("StaticFieldLeak")
    private static Context mCtx;

    private  Mysingleton(Context context){

        mCtx = context;
        requestQueue =getRequestQueue();
    }

    private RequestQueue getRequestQueue(){

        if(requestQueue == null){
            requestQueue = Volley.newRequestQueue(mCtx.getApplicationContext());
        }
        return requestQueue;
    }

    static  synchronized  Mysingleton getInstance(Context context){

        if(myInstance == null){
            myInstance = new Mysingleton(context);
        }
        return  myInstance;
    }


    <T> void addToRequestque(Request<T> request){
        requestQueue.add(request);
    }
}
