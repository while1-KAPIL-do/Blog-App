package com.frazycrazy.kappu.apptech2;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class UploadVideoActivity extends AppCompatActivity {


    EditText et_link,et_title,et_desc;
    String user_email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_video);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        user_email = Objects.requireNonNull(mAuth.getCurrentUser()).getEmail();


        et_link = findViewById(R.id.et_acc_name);
        et_title = findViewById(R.id.et_acc_mobile);
        et_desc = findViewById(R.id.et_acc_address);


    }

    String isInserted ;
    private String registerToPhpServer(final String link,final String title, final String desc) {
        String api_register =  getResources().getString(R.string.api_save_vedio);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, api_register,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jObj_p = new JSONObject(response);
                            jObj_p.getBoolean(getResources().getString(R.string.api_res_error));

                            if (!jObj_p.getBoolean(getResources().getString(R.string.api_res_error))) {
                                isInserted = "inserted";
                                finish();
                            } else {
                                String errorMsg = jObj_p.getString(getResources().getString(R.string.api_res_message));
                                Toast.makeText(getApplicationContext(), "server : "+errorMsg, Toast.LENGTH_LONG).show();
                                isInserted = errorMsg;
                            }
                        } catch (JSONException e) {
                            // JSON error
                            isInserted = e.getMessage();
                            e.printStackTrace();
                            //Toast.makeText(getApplicationContext(), "Error !\n" + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                isInserted = error.getMessage();
                //Toast.makeText(RegisterActivity.this, "Connection error !", Toast.LENGTH_SHORT).show();
                error.printStackTrace();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("link", link);
                params.put("title", title);
                params.put("des", desc);
                params.put("user_email", user_email);
                return params;
            }
        };
        Mysingleton.getInstance(UploadVideoActivity.this).addToRequestque(stringRequest);
        //-----------

        Log.d("------>>>>>>", "registerToPhpServer: "+isInserted);
        return isInserted;
    }


    public void do_btn_upload(View view) {

        if (!TextUtils.isEmpty(et_link.getText().toString()) && !TextUtils.isEmpty(et_title.getText().toString()) && !TextUtils.isEmpty(et_desc.getText().toString()) ) {
            String result = registerToPhpServer(et_link.getText().toString(), et_title.getText().toString(), et_desc.getText().toString());
            Toast.makeText(this, "" + result, Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(this, "Fill all the fields first...", Toast.LENGTH_SHORT).show();
        }
    }
}
