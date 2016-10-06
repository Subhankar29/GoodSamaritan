package com.goodsamaritan;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.digits.sdk.android.*;
import com.digits.sdk.android.Digits;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterCore;
import io.fabric.sdk.android.Fabric;


public class MainActivity extends AppCompatActivity {

    // Note: Your consumer key and secret should be obfuscated in your source code before shipping.
    private static final String TWITTER_KEY = "C9sUZZm8FFI96HVnS2EWxGvJM";
    private static final String TWITTER_SECRET = "1aOQGiVFvGd7qJrCo60E5FObF4Tr4FLYWvObzmacQZQtvEnS5T";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("Sign Up");

        Button sign_up_btn= (Button) findViewById(R.id.sign_up_btn);
        sign_up_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this,MainScreenActivity.class);
                EditText name = (EditText) findViewById(R.id.nameid);
                EditText email = (EditText) findViewById(R.id.email);
                EditText phone = (EditText) findViewById(R.id.phoneid);
                i.putExtra("name",name.getText().toString());
                i.putExtra("email",email.getText().toString());
                System.out.println("Put values:#####\n\n\n "+name.getText()+" "+email.getText());

                startPhoneNumberVerification(phone.getText(),i);


            }
        });

        //Initialize Digits Kit
        /*TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
        Fabric.with(this, new TwitterCore(authConfig), new Digits.Builder().build());

        DigitsAuthButton digitsButton = (DigitsAuthButton) findViewById(R.id.auth_button);
        digitsButton.setText("Hello");
        digitsButton.setBackgroundColor(getResources().getColor(R.color.colorAccent));
        digitsButton.setCallback(new AuthCallback() {
            @Override
            public void success(DigitsSession session, String phoneNumber) {
                // TODO: associate the session userID with your user model
                Toast.makeText(getApplicationContext(), "Authentication successful for "
                        + phoneNumber, Toast.LENGTH_LONG).show();
            }

            @Override
            public void failure(DigitsException exception) {
                Log.d("Digits", "Sign in with Digits failure", exception);
            }
        });*/


    }

    void startPhoneNumberVerification(CharSequence phoneNumber, final Intent intent){
        //Initialize Digits Kit
        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
        Fabric.with(this, new TwitterCore(authConfig), new Digits.Builder().build());
        //Start Authentication Flow
        AuthConfig.Builder authConfigBuilder = new AuthConfig.Builder()
                .withAuthCallBack(new AuthCallback() {
                    @Override
                    public void success(DigitsSession session, String phoneNumber) {
                        // TODO: associate the session userID with your user model
                        Toast.makeText(getApplicationContext(), "Authentication successful for "
                                + phoneNumber, Toast.LENGTH_LONG).show();
                        startActivity(intent);
                    }

                    @Override
                    public void failure(DigitsException exception) {
                        Log.d("Digits", "Sign in with Digits failure", exception);
                    }
                })
                .withPhoneNumber("+91"+phoneNumber);

        Digits.authenticate(authConfigBuilder.build());
    }
}
