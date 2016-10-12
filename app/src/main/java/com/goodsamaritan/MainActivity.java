package com.goodsamaritan;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
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
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.LoggingBehavior;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterCore;

import java.util.Arrays;
import java.util.List;

import io.fabric.sdk.android.Fabric;


public class MainActivity extends AppCompatActivity {

    // Note: Your consumer key and secret should be obfuscated in your source code before shipping.
    private static final String TWITTER_KEY = "C9sUZZm8FFI96HVnS2EWxGvJM";
    private static final String TWITTER_SECRET = "1aOQGiVFvGd7qJrCo60E5FObF4Tr4FLYWvObzmacQZQtvEnS5T";
    private static final String TAG ="TAG:";

    //Facebook Variables
    CallbackManager callbackManager;

    //Firebase Variables
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("Sign Up");

        //Initialize Firebase
        initFirebase();

        Button sign_up_btn= (Button) findViewById(R.id.sign_up_btn);
        sign_up_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, MainScreenActivity.class);
                EditText name = (EditText) findViewById(R.id.nameid);
                EditText email = (EditText) findViewById(R.id.email);
                EditText phone = (EditText) findViewById(R.id.phoneid);
                i.putExtra("name", name.getText().toString());
                i.putExtra("email", email.getText().toString());

                startPhoneNumberVerification(phone.getText(), i);

                //Facebook Login
                List<String> permissionNeeds= Arrays.asList("email");
                FacebookSdk.sdkInitialize(MainActivity.this.getApplicationContext());

                FacebookSdk.addLoggingBehavior(LoggingBehavior.GRAPH_API_DEBUG_INFO);
                FacebookSdk.addLoggingBehavior(LoggingBehavior.DEVELOPER_ERRORS);
                FacebookSdk.addLoggingBehavior(LoggingBehavior.INCLUDE_ACCESS_TOKENS);
                FacebookSdk.addLoggingBehavior(LoggingBehavior.INCLUDE_RAW_RESPONSES);
                FacebookSdk.setApplicationId("193849254386118");
                FacebookSdk.setIsDebugEnabled(true);

                callbackManager = CallbackManager.Factory.create();
                LoginManager.getInstance().registerCallback(callbackManager,
                        new FacebookCallback<LoginResult>() {
                            @Override
                            public void onSuccess(LoginResult loginResult) {
                                // App code
                                //Toast.makeText(getApplicationContext(),"Success",Toast.LENGTH_LONG).show();
                                System.out.println("1");
                                //Connect to Firebase
                                handleFacebookAccessToken(loginResult.getAccessToken());

                            }

                            @Override
                            public void onCancel() {
                                // App code
                                //Toast.makeText(getApplicationContext(),"Cancel",Toast.LENGTH_LONG).show();
                                System.out.println("2");
                            }

                            @Override
                            public void onError(FacebookException exception) {
                                // App code
                                //Toast.makeText(getApplicationContext(),"Fail",Toast.LENGTH_LONG).show();
                                System.out.println("3");
                            }
                        }
                );
                LoginManager.getInstance().logInWithReadPermissions(MainActivity.this,permissionNeeds);



            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
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
                        //startActivity(intent);
                    }

                    @Override
                    public void failure(DigitsException exception) {
                        Log.d("Digits", "Sign in with Digits failure", exception);
                    }
                })
                .withPhoneNumber("+91"+phoneNumber);

        Digits.authenticate(authConfigBuilder.build());
    }

    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithCredential", task.getException());
                            Toast.makeText(MainActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }

                        // ...
                    }
                });
    }

    void initFirebase(){
        //FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
                // ...
            }
        };
    }

    void connectToFirebase(){

    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
}
