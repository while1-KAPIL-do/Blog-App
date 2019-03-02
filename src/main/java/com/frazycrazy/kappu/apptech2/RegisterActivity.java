package com.frazycrazy.kappu.apptech2;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class RegisterActivity extends AppCompatActivity {


    private EditText et_username,et_useremail,et_userpassword,et_userpassword2;
    private FirebaseAuth mAuth_submit;
    private DatabaseReference mDatabase;

    boolean isdataInserted=false;

    ImageView iv_alert,iv_alert_p,iv_alert_p2,iv_alert_name;
    private ProgressDialog progressDialog ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth_submit = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        progressDialog = new ProgressDialog(this);
        et_username = findViewById(R.id.editText_reg_name);
        et_useremail = findViewById(R.id.editText_reg_email);
        et_userpassword = findViewById(R.id.editText_reg_password);
        et_userpassword2 = findViewById(R.id.editText_reg_password2);

        iv_alert = findViewById(R.id.alert_img_email);
        iv_alert_p = findViewById(R.id.alert_img_pass);
        iv_alert_p2 = findViewById(R.id.alert_img_pass2);
        iv_alert_name = findViewById(R.id.alert_img_name);

        // name credential
        et_username.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                iv_alert_name.setVisibility(View.VISIBLE);

                if (et_username.getText().toString().length() <= 2 )
                {
                    iv_alert_name.setImageResource(R.drawable.ic_error_outline_black_24dp);
                }
                else
                {
                    iv_alert_name.setImageResource(R.drawable.ic_done_black_24dp);
                }
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // other stuffs
            }
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // other stuffs
            }
        });

        // email credential
        et_useremail.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                iv_alert.setVisibility(View.VISIBLE);

                if (et_useremail.getText().toString().trim().indexOf('@')<1 ||
                        et_useremail.getText().toString().trim().lastIndexOf('.')>= et_useremail.getText().toString().trim().length()-2 ||
                        et_useremail.getText().toString().trim().lastIndexOf('.') - et_useremail.getText().toString().trim().indexOf('@')<3 )
                {
                    iv_alert.setImageResource(R.drawable.ic_error_outline_black_24dp);
                }
                else
                {
                    iv_alert.setImageResource(R.drawable.ic_done_black_24dp);
                }
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // other stuffs
            }
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // other stuffs
            }
        });

        // password credential
        et_userpassword.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable editable) {
                iv_alert_p.setVisibility(View.VISIBLE);

                if (et_userpassword.getText().toString().trim().length() <= 5 ) {
                    iv_alert_p.setImageResource(R.drawable.ic_error_outline_black_24dp);
                } else {
                    iv_alert_p.setImageResource(R.drawable.ic_done_black_24dp);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

        });

        // password 2 credential
        et_userpassword2.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable editable) {
                iv_alert_p2.setVisibility(View.VISIBLE);

                if (et_userpassword2.getText().toString().trim().length() <= 5 ) {
                        iv_alert_p2.setImageResource(R.drawable.ic_error_outline_black_24dp);
                } else {
                    iv_alert_p2.setImageResource(R.drawable.ic_done_black_24dp);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

        });
    }

    public void m4_do_submit(View view) {

        final String name = et_username.getText().toString().trim();
        final String email = et_useremail.getText().toString().trim();
        final String pass = et_userpassword.getText().toString().trim();
        String pass2 = et_userpassword2.getText().toString().trim();

        if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(email) && !TextUtils.isEmpty(pass) && !TextUtils.isEmpty(pass2) ){

            if (pass.compareTo(pass2)==0){

                // upload to php server
                String responseToFromServer = ""+registerToPhpServer(email,pass,name);

               if (responseToFromServer.equals("Already Exist")){

                    Toast.makeText(this, "This User is already exist ! \n Try Login", Toast.LENGTH_SHORT).show();
                }else if(isdataInserted) {
                   Toast.makeText(this, "Welcome", Toast.LENGTH_SHORT).show();
                }else{
                   Toast.makeText(RegisterActivity.this, "Loading...", Toast.LENGTH_SHORT).show();
                }

            }else {
                Toast.makeText(this, "Password did not matched...! ", Toast.LENGTH_SHORT).show();
            }
        }else {
            Toast.makeText(this, "Fill all the Fields...", Toast.LENGTH_SHORT).show();
        }
    }

    String isInserted ;
    private String registerToPhpServer(final String email,final String pass, final String name) {

        progressDialog.setMessage("Signing up...");
        progressDialog.show();

        String api_register = "http://apptechinteractive.com/blog/index.php/app/register";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, api_register,
                new Response.Listener<String>() {
                    @SuppressLint("ApplySharedPref")
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jObj_p = new JSONObject(response);
                            jObj_p.getBoolean("error");

                            if (!jObj_p.getBoolean("error")) {
                                isInserted = "user detail inserted";
                                isdataInserted = true;

                                mAuth_submit.createUserWithEmailAndPassword(email, pass)
                                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                            @Override
                                            public void onComplete(@NonNull Task<AuthResult> task) {

                                                if (task.isSuccessful()) {

                                                    @SuppressLint({"NewApi", "LocalSuppress"}) String uid = Objects.requireNonNull(mAuth_submit.getCurrentUser()).getUid();

                                                    DatabaseReference current_db = mDatabase.child(uid);
                                                    current_db.child(getResources().getString(R.string.user_name)).setValue(name);
                                                    current_db.child(getResources().getString(R.string.user_email)).setValue(email);
                                                    current_db.child(getResources().getString(R.string.user_password)).setValue(pass);
                                                    progressDialog.dismiss();
                                                    Toast.makeText(RegisterActivity.this, "Register Successful. ", Toast.LENGTH_SHORT).show();
                                                    Intent mainIntent = new Intent(RegisterActivity.this, NavigationActivity.class);
                                                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                    startActivity(mainIntent);
                                                    finish();
                                                } else {
                                                    //Toast.makeText(RegisterActivity.this, "try again..", Toast.LENGTH_SHORT).show();
                                                    progressDialog.dismiss();
                                                }
                                            }
                                        });

                                SharedPreferences sharedPreferences = getSharedPreferences(getResources().getString(R.string.shared_pref), MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString(getResources().getString(R.string.sp_email),email);
                                editor.putString(getResources().getString(R.string.sp_name),name);
                                editor.putString(getResources().getString(R.string.sp_mobile),"");
                                editor.putString(getResources().getString(R.string.sp_address),"");
                                editor.putString(getResources().getString(R.string.sp_profile_desc),"");
                                editor.commit();

                                progressDialog.dismiss();
                            } else {
                                // Toast.makeText(getApplicationContext(), "server : "+errorMsg, Toast.LENGTH_LONG).show();
                                isInserted = jObj_p.getString("message");
                                progressDialog.dismiss();
                            }
                        } catch (JSONException e) {
                            // JSON error
                            progressDialog.dismiss();
                            isInserted = e.getMessage();
                            e.printStackTrace();
                            //Toast.makeText(getApplicationContext(), "Error !\n" + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();
                isInserted = error.getMessage();
                Toast.makeText(RegisterActivity.this, "Connection error !", Toast.LENGTH_SHORT).show();
                error.printStackTrace();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put(getResources().getString(R.string.user_email), email);
                params.put(getResources().getString(R.string.user_password), pass);
                params.put(getResources().getString(R.string.user_name), name);
                return params;
            }
        };
        Mysingleton.getInstance(RegisterActivity.this).addToRequestque(stringRequest);
        //-----------

        Log.d("------>>>>>>", "registerToPhpServer: "+isInserted);
        return isInserted;
    }


    public void do_jump_to_signu(View view) {
        startActivity(new Intent(RegisterActivity.this,SignUpActivity.class));
        finish();
    }
}
