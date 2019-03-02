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
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {


    static  final int RC_SIGN_IN = 1;
    private GoogleApiClient mGoogleApiClient;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthlistener;
    ImageView iv_alert_email,iv_alert_pass ;

    //-----login
    private  FirebaseAuth mAuth_signin;
    private ProgressDialog mProgerss_signin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        //-----login
        mAuth_signin = FirebaseAuth.getInstance();
        mProgerss_signin = new ProgressDialog(this);
        //-----login/

        SignInButton mGoogleBtn = findViewById(R.id.sign_in);
        // db-------

        mAuth = FirebaseAuth.getInstance();
        mAuthlistener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                if(firebaseAuth.getCurrentUser() != null){
                    Intent mainIntent = new Intent(SignUpActivity.this,NavigationActivity.class);
                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(mainIntent);
                    finish();
                }
            }
        };

        //db---------
        // google signup-----
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(getApplicationContext()).enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
            @Override
            public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                Toast.makeText(SignUpActivity.this, "ConnectionFailed...!", Toast.LENGTH_SHORT).show();
            }
        }).addApi(Auth.GOOGLE_SIGN_IN_API,gso).build();

        mGoogleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });


        // Login--------

        iv_alert_email = findViewById(R.id.iv_alert_email);
        iv_alert_pass = findViewById(R.id.iv_alert_pass);

        et_email = findViewById(R.id.editText_signup_email);

        et_password = findViewById(R.id.editText_signup_password);

        et_email.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                iv_alert_email.setVisibility(View.VISIBLE);

                if (et_email.getText().toString().trim().indexOf('@')<1 ||
                        et_email.getText().toString().trim().lastIndexOf('.')>= et_email.getText().toString().trim().length()-2 ||
                        et_email.getText().toString().trim().lastIndexOf('.') - et_email.getText().toString().trim().indexOf('@')<3 )
                {
                    iv_alert_email.setImageResource(R.drawable.ic_error_outline_black_24dp);
                }
                else
                {
                    iv_alert_email.setImageResource(R.drawable.ic_done_black_24dp);
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
        et_password.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable editable) {
                iv_alert_pass.setVisibility(View.VISIBLE);

                if (et_password.getText().toString().trim().length() <= 5 ) {
                    iv_alert_pass.setImageResource(R.drawable.ic_error_outline_black_24dp);
                } else {
                    iv_alert_pass.setImageResource(R.drawable.ic_done_black_24dp);
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

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthlistener);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult task = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            Toast.makeText(this, "connecting...", Toast.LENGTH_SHORT).show();
            if(task.isSuccess()){
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getSignInAccount();
                if (account != null) {
                    firebaseAuthWithGoogle(account);
                }else {
                    Toast.makeText(this, "failed Nothing select...", Toast.LENGTH_SHORT).show();
                }
            }else{
                // Google Sign In failed, update UI appropriately
                Toast.makeText(this, "Google Sign In failed", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            Toast.makeText(SignUpActivity.this, "sign in fails", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
    private void updateUI(FirebaseUser user){
        if(user != null){
            String email = user.getEmail();
            String name = user.getDisplayName();
            String mobile = "null";
            String image = "null";
            String address = "null";
            String des = "null";

            if (user.getPhoneNumber()!=null){
                mobile =user.getPhoneNumber();
            }
            if (user.getPhotoUrl()!=null){
                image = user.getPhotoUrl().toString();
            }

            String uid = user.getUid();

            DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
            DatabaseReference newPost = mDatabase.child(uid);
            newPost.child(getResources().getString(R.string.user_name)).setValue(name);
            newPost.child(getResources().getString(R.string.user_email)).setValue(email);
            newPost.child("image").setValue(image); // upload only in firebase
            newPost.child(getResources().getString(R.string.user_mobile)).setValue(mobile);
            newPost.child(getResources().getString(R.string.user_address)).setValue(address);
            newPost.child(getResources().getString(R.string.user_profile_desc)).setValue(des);

            String res = ""+registerToPhpServer(email,name,mobile);


            if (res.equals("Already Exist")){
                Toast.makeText(this, "Welcome back "+name, Toast.LENGTH_SHORT).show();
            } else{
                Toast.makeText(this, "Welcome "+name, Toast.LENGTH_SHORT).show();
            }

        }
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);

    }


    String errorMsg;
    private String registerToPhpServer(final String email, final String name, final String mobile){
        String api_register = getResources().getString(R.string.api_register);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, api_register,
                new Response.Listener<String>() {
                    @SuppressLint("ApplySharedPref")
                    @Override
                    public void onResponse(String response) {
                        try {

                            Log.d("------=-=-=-=------", "onResponse: "+response);
                            JSONObject jObj_p = new JSONObject(response);
                            jObj_p.getBoolean(getResources().getString(R.string.api_res_error));
                            errorMsg = jObj_p.getString(getResources().getString(R.string.api_res_message));

                           switch (errorMsg) {
                                case "Already Exist": {

                                    isInserted = "logged in";
                                    JSONObject myJsonObj = jObj_p.getJSONObject(getResources().getString(R.string.api_user_show_user));
                                    String user_email = myJsonObj.getString(getResources().getString(R.string.user_email));
                                    String user_name = myJsonObj.getString(getResources().getString(R.string.user_name));
                                    String user_mobile = myJsonObj.getString(getResources().getString(R.string.user_mobile));
                                    String user_address = myJsonObj.getString(getResources().getString(R.string.user_address));
                                    String user_profile_desc = myJsonObj.getString(getResources().getString(R.string.user_profile_desc));

                                    SharedPreferences sharedPreferences = getSharedPreferences(getResources().getString(R.string.shared_pref), MODE_PRIVATE);
                                    SharedPreferences.Editor editor = sharedPreferences.edit();

                                    editor.putString(getResources().getString(R.string.sp_email), user_email);
                                    editor.putString(getResources().getString(R.string.sp_name), user_name);
                                    editor.putString(getResources().getString(R.string.sp_mobile), user_mobile);
                                    editor.putString(getResources().getString(R.string.sp_address), user_address);
                                    editor.putString(getResources().getString(R.string.sp_profile_desc), user_profile_desc);

                                    editor.commit();

                                    break;
                                }
                                case "Register Sucessfull ": {

                                    SharedPreferences sharedPreferences = getSharedPreferences(getResources().getString(R.string.shared_pref), MODE_PRIVATE);
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.putString(getResources().getString(R.string.sp_email), email);
                                    editor.putString(getResources().getString(R.string.sp_name), name);
                                    editor.putString(getResources().getString(R.string.sp_mobile), mobile);
                                    editor.putString(getResources().getString(R.string.sp_address), "");
                                    editor.putString(getResources().getString(R.string.sp_profile_desc), "");
                                    editor.commit();

                                    break;
                                }
                                default:
                                    //Toast.makeText(getApplicationContext(), errorMsg, Toast.LENGTH_LONG).show();
                                    break;
                            }
                        } catch (JSONException e) {
                            // JSON error
                            e.printStackTrace();
                            errorMsg = e.getMessage();
                            Toast.makeText(getApplicationContext(), "Error !\n" + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                   }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(SignUpActivity.this, "Connection error !", Toast.LENGTH_SHORT).show();
                errorMsg = error.getMessage();
                error.printStackTrace();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put(getResources().getString(R.string.user_email), email);
                params.put(getResources().getString(R.string.user_name),name);
                params.put(getResources().getString(R.string.user_mobile), mobile);
                return params;
            }
        };
        Mysingleton.getInstance(SignUpActivity.this).addToRequestque(stringRequest);
        //-----------
        return errorMsg;
    }

    EditText et_email,et_password;
    public void do_register(View view) {
        startActivity(new Intent(SignUpActivity.this,RegisterActivity.class));
    }


    String isInserted ;
    private String logInToPhpServer(final String my_email,final String my_pass) {

        mProgerss_signin.setMessage("Logging In ...");
        mProgerss_signin.show();

        String api_login =  getResources().getString(R.string.api_login);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, api_login,
                new Response.Listener<String>() {
                    @SuppressLint("ApplySharedPref")
                    @Override
                    public void onResponse(String response) {

                        Log.d("----->>>>>LogIn", "onResponse: "+response);
                        try {
                            JSONObject jObj_p = new JSONObject(response);
                            boolean b = jObj_p.getBoolean(getResources().getString(R.string.api_res_error));

                            if (!b) {
                                isInserted = "logged in";
                                JSONObject myJsonObj = jObj_p.getJSONObject(getResources().getString(R.string.api_user_show_user));
                                String user_email = myJsonObj.getString(getResources().getString(R.string.user_email));
                                String user_name = myJsonObj.getString(getResources().getString(R.string.user_name));
                                String user_mobile = myJsonObj.getString(getResources().getString(R.string.user_mobile));
                                String user_address = myJsonObj.getString(getResources().getString(R.string.user_address));
                                String user_profile_desc = myJsonObj.getString(getResources().getString(R.string.user_profile_desc));


                                SharedPreferences sharedPreferences = getSharedPreferences(getResources().getString(R.string.shared_pref), MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPreferences.edit();

                                editor.putString(getResources().getString(R.string.sp_email),user_email);
                                editor.putString(getResources().getString(R.string.sp_name),user_name);
                                editor.putString(getResources().getString(R.string.sp_mobile),user_mobile);
                                editor.putString(getResources().getString(R.string.sp_address),user_address);
                                editor.putString(getResources().getString(R.string.sp_profile_desc),user_profile_desc);

                                mAuth_signin.signInWithEmailAndPassword(my_email,my_pass )
                                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                            @Override
                                            public void onComplete(@NonNull Task<AuthResult> task) {
                                                if (task.isSuccessful()) {
                                                    Intent mainIntent = new Intent(SignUpActivity.this, NavigationActivity.class);
                                                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                    startActivity(mainIntent);
                                                    finish();
                                                    mProgerss_signin.dismiss();

                                                } else {
                                                    Toast.makeText(SignUpActivity.this, "Login error ! \n    try again", Toast.LENGTH_SHORT).show();
                                                    mProgerss_signin.dismiss();
                                                }
                                            }
                                        });

                                editor.commit();
                                mProgerss_signin.dismiss();
                                Log.d("----->>>>>", "onResponse: if");
                            } else {
                                //Toast.makeText(getApplicationContext(), "server : "+errorMsg, Toast.LENGTH_LONG).show();
                                isInserted = jObj_p.getString(getResources().getString(R.string.api_res_message));
                                Log.d("----->>>>>", "onResponse: esle");
                                mProgerss_signin.dismiss();
                            }
                        } catch (JSONException e) {
                            // JSON error
                            isInserted = e.getMessage();
                            mProgerss_signin.dismiss();
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(), "Error !\n" + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                isInserted = error.getMessage();
                mProgerss_signin.dismiss();
                Toast.makeText(SignUpActivity.this, "Connection error !", Toast.LENGTH_SHORT).show();
                error.printStackTrace();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put(getResources().getString(R.string.user_email), my_email);
                params.put(getResources().getString(R.string.user_password), my_pass);
                return params;
            }
        };
        Mysingleton.getInstance(SignUpActivity.this).addToRequestque(stringRequest);
        //-----------

        return isInserted;
    }

    public void do_login(View view) {

        String em = ""+et_email.getText().toString();
        String ps = ""+et_password.getText().toString();

        if (!TextUtils.isEmpty(em) && !TextUtils.isEmpty(ps)) {

            String responseFromServer = ""+logInToPhpServer(em,ps);
            if (responseFromServer.equals("null") || responseFromServer == null){
                Toast.makeText(this, "Loading...", Toast.LENGTH_SHORT).show();
            }
        }else {
            Toast.makeText(this, "Fill the remaining fields ...!", Toast.LENGTH_SHORT).show();

        }

    }
}