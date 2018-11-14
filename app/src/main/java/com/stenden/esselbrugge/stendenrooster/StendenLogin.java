package com.stenden.esselbrugge.stendenrooster;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;

import org.json.JSONArray;
import org.json.JSONObject;

public class StendenLogin extends AppCompatActivity {

    public EditText Email;
    public EditText Password;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stenden_login);
        getSupportActionBar().setTitle("Stenden Login");

        LinearLayout loadme = findViewById(R.id.loadme);
        loadme.setVisibility(View.GONE);

        Email = findViewById(R.id.input_email);
        Password = findViewById(R.id.input_password);

        SharedPreferences SP = getApplicationContext().getSharedPreferences("Cookies", MODE_PRIVATE);
        Email.setText( SP.getString("Email","") );
        Password.setText( SP.getString("Password","") );

        if(!Email.getText().equals("")){
            CheckBox keepLoggedIn = findViewById(R.id.keepLoggedIn);
            keepLoggedIn.setChecked(true);
        }


    }

    public void Login(View v){

        LinearLayout logincontent = findViewById(R.id.logincontent);
        LinearLayout loadme = findViewById(R.id.loadme);

        if(Email.getText().toString().isEmpty()){
            Email.setError("Email is empty");
            return;
        }
            if(!Email.getText().toString().endsWith("@student.stenden.com") && !Email.getText().toString().endsWith("@student.nhlstenden.com")
                    && !Email.getText().toString().endsWith("@stenden.com")
                    && !Email.getText().toString().endsWith("@nhlstenden.com")){
                Email.setError("Email is not a valid (NHL) Stenden account");
                return;
            }



        if(Password.getText().toString().isEmpty()){
            Password.setError("Please enter a password");
            return;
        }

        final String email = Email.getText().toString();
        final String password = Password.getText().toString();
        v.setEnabled(false);

        logincontent.setVisibility(View.GONE);
        loadme.setVisibility(View.VISIBLE);

        new Thread() {
            public void run() {
                final ServerConnection con = new ServerConnection();
                final String koekjes = con.Login(email,password,getApplicationContext());

                StendenLogin.this.runOnUiThread(new Runnable() {
                    public void run() {
                        try {
                            CheckLogin(koekjes);

                        }catch (Exception e){
                            Log.e("d",e.toString());
                        }
                    }
                });

            }
        }.start();

    }

    public void CheckLogin(String koekjes){
        Button loginbtn = findViewById(R.id.btn_login);

        LinearLayout loadme = findViewById(R.id.loadme);
        CheckBox keepLoggedIn = findViewById(R.id.keepLoggedIn);
        LinearLayout logincontent = findViewById(R.id.logincontent);
        try {
            if(!koekjes.equals("Error") && !koekjes.equals("")){
                SharedPreferences SP = getApplicationContext().getSharedPreferences("Cookies", MODE_PRIVATE);
                SP.edit().putString("Cookie",koekjes).commit();

                if(keepLoggedIn.isChecked()){
                    SP.edit().putString("Email",Email.getText() + "").apply();
                    SP.edit().putString("Password",Password.getText() + "").apply();
                }

                Intent returnIntent = new Intent();
                setResult(StendenLogin.RESULT_OK, returnIntent);
                this.finish();
            }else{
                if(koekjes.equals("Error")) {
                    Password.setError("Email or password incorrect");
                }else{
                    Password.setError("Something went wrong");
                }
                loginbtn.setEnabled(true);
                logincontent.setVisibility(View.VISIBLE);
                loadme.setVisibility(View.GONE);
            }
        }catch (Exception e){
            Log.d("Login",e.toString());
        }
    }


}
