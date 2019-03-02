package com.frazycrazy.kappu.apptech2;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class AccountActivity extends AppCompatActivity {

    String sEmail,sName,sMobile,sAddress,sProfileDescription;
    private EditText et_acc_name,et_acc_mobile,et_acc_address,et_acc_des;
    private ProgressDialog mProgerss_signin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        // initializing
        mProgerss_signin = new ProgressDialog(this);
        TextView tv_acc_email = findViewById(R.id.tv_acc_email);
        et_acc_name = findViewById(R.id.et_acc_name);
        et_acc_mobile = findViewById(R.id.et_acc_mobile);
        et_acc_address = findViewById(R.id.et_acc_address);
        et_acc_des = findViewById(R.id.et_acc_des);

        // Get data frome Shared Pref
        SharedPreferences sharedPreferences = getSharedPreferences(getResources().getString(R.string.shared_pref), MODE_PRIVATE);
        sEmail = sharedPreferences.getString(getResources().getString(R.string.sp_email),"");
        sName = sharedPreferences.getString(getResources().getString(R.string.sp_name),"");
        sMobile = sharedPreferences.getString(getResources().getString(R.string.sp_mobile),"");
        sAddress = sharedPreferences.getString(getResources().getString(R.string.sp_address),"");
        sProfileDescription = sharedPreferences.getString(getResources().getString(R.string.sp_profile_desc),"");

        // Set data on Layout view
        tv_acc_email.setText(sEmail);
        et_acc_name.setText(sName);
        et_acc_mobile.setText(sMobile);
        et_acc_address.setText(sAddress);
        et_acc_des.setText(sProfileDescription);




    }

    // On click -- Update
    public void do_btn_acc_update(View view) {

        // get data from input fields
        String my_name = ""+et_acc_name.getText().toString() ;
        String my_mobile = ""+et_acc_mobile.getText().toString() ;
        String my_address = ""+et_acc_address.getText().toString();
        String my_des = ""+et_acc_des.getText().toString();

        // check fields is empty or not
        if (!TextUtils.isEmpty(my_name) && !TextUtils.isEmpty(my_mobile)
                && !TextUtils.isEmpty(my_address) && !TextUtils.isEmpty(my_des)){

            //  send to php server for updating
            UpdateUserToPhpServer(sEmail,my_name,my_mobile,my_address,my_des);
        }else{
            Toast.makeText(this, "Fill the remaining fields first !", Toast.LENGTH_SHORT).show();
        }

    }


    private void UpdateUserToPhpServer(final String email,final String name,
                                       final String mobile, final String address,final String des){

        mProgerss_signin.setMessage("Updating...");
        mProgerss_signin.show();

        // request for API by Volley
        String api_profile_update = getResources().getString(R.string.api_profile_update);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, api_profile_update,
                new Response.Listener<String>() {
                    @SuppressLint("ApplySharedPref")
                    @Override
                    public void onResponse(String response) {

                        try {
                            Log.d("------------>", "onResponse: "+response);

                            // string convert into Json object
                            JSONObject jObj_p = new JSONObject(response);
                            jObj_p.getBoolean("error");

                            // checking -> PHP server side error
                            if (!jObj_p.getBoolean("error")) {

                                // checking -> PHP server side error (true)
                                // when data is updated then Shared pref value ill updated
                                SharedPreferences sharedPreferences = getSharedPreferences(
                                        getResources().getString(R.string.shared_pref), MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString(getResources().getString(R.string.sp_name),name);
                                editor.putString(getResources().getString(R.string.sp_mobile),mobile);
                                editor.putString(getResources().getString(R.string.sp_address),address);
                                editor.putString(getResources().getString(R.string.sp_profile_desc),des);
                                editor.commit();

                                // Jump to Home page (Navigation Activity )
                                Toast.makeText(getApplicationContext(), jObj_p.getString("message"), Toast.LENGTH_LONG).show();
                                startActivity(new Intent(AccountActivity.this,NavigationActivity.class));
                                finish();
                                  } else {

                                // checking -> PHP server side error (false)
                                Toast.makeText(getApplicationContext(), jObj_p.getString("message"), Toast.LENGTH_LONG).show();
                            }
                            mProgerss_signin.dismiss();
                        } catch (JSONException e) {
                            // JSON error ---> from server side (it must be value or result is chenged or removed ! )
                            mProgerss_signin.dismiss();
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(), "Error !\n" + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // Network error
                Toast.makeText(AccountActivity.this, "Connection error !", Toast.LENGTH_SHORT).show();
                error.printStackTrace();
                mProgerss_signin.dismiss();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                // passing all values in php server
                Map<String, String> params = new HashMap<>();
                params.put(getResources().getString(R.string.user_email), email);
                params.put(getResources().getString(R.string.user_name), name);
                params.put(getResources().getString(R.string.user_mobile),mobile);
                params.put(getResources().getString(R.string.user_address), address);
                params.put(getResources().getString(R.string.user_profile_desc), des);
                return params;
            }
        };
        // function which required to execute Volley
        Mysingleton.getInstance(AccountActivity.this).addToRequestque(stringRequest);
        //-----------
    }


    @Override
    public void onBackPressed() {
        // for reliability
        startActivity(new Intent(AccountActivity.this,NavigationActivity.class));
        finish();
        super.onBackPressed();
    }
}
