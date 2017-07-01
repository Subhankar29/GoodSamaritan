package com.goodsamaritan;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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
import com.goodsamaritan.drawer.contacts.Contacts;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterCore;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import io.fabric.sdk.android.Fabric;


public class MainActivity extends AppCompatActivity implements Serializable {

    // Note: Your consumer key and secret should be obfuscated in your source code before shipping.
    private static final String TWITTER_KEY = "C9sUZZm8FFI96HVnS2EWxGvJM";
    private static final String TWITTER_SECRET = "1aOQGiVFvGd7qJrCo60E5FObF4Tr4FLYWvObzmacQZQtvEnS5T";
    private static final String TAG ="TAG:";
    private static final int APP_PERMS = 1097;

    //Strong Reference to authCallback
    AuthCallback authCallback;

    //Facebook Variables
    CallbackManager callbackManager;
    List<String> permissionNeeds;
    AccessToken accessToken=null;

    //Firebase Variables
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase firebaseDatabase;

    //Progress Dialog
    ProgressDialog pd;

    //Verification Status Flag
    boolean isComplete=false;

    //Is Sign Up clicked
    boolean isSignUpClicked=false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        pd=new ProgressDialog(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("Sign Up");

        //Request Permissions (for Marshmallow onwards)
        requestPermissions();


        //Start with Phone Number verification, then Facebook and then Firebase
        //In future, will check Firebase database to verify if new or old account.
        final EditText phone = (EditText) findViewById(R.id.phoneid);
        Button otp = (Button) findViewById(R.id.send_otp);
        System.out.println("BEFORE PHONE");


        startPhoneNumberVerification(phone.getText());

        Button sign_up_btn= (Button) findViewById(R.id.sign_up_btn);
        sign_up_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText nameText = (EditText) findViewById(R.id.nameid);
                EditText emailText = (EditText) findViewById(R.id.email);
                RadioButton radioFemale = (RadioButton) findViewById(R.id.radioFemale);
                EditText phoneText = (EditText) findViewById(R.id.phoneid);
                EditText passwordText = (EditText) findViewById(R.id.passwordid);
                RadioGroup radioGroup = (RadioGroup) findViewById(R.id.radioSex);

                if(nameText.getText().length()==0)nameText.setError("Required!");
                else if(emailText.getText().length()==0)emailText.setError("Required!");
                else if(phoneText.getText().length()==0)phoneText.setError("Required!");
                else if(passwordText.getText().length()==0)passwordText.setError("Required!");
                else if(radioGroup.getCheckedRadioButtonId()==-1)radioFemale.setError("Required!");
                else {
                    isSignUpClicked=true;
                    //Start with Phone Number verification, then Facebook and then Firebase

                    EditText phone = (EditText) findViewById(R.id.phoneid);
                    startPhoneNumberVerification(phone.getText());

                }



            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    /*
    Note that this is a weak phone authentication method. Regardless of whether the authentication
    succeeds or fails, users have full right to write any phone number to the database once
    authenticated with Facebook ID. This is client side authentication and should be replaced with
    server side authentication in future to mitigate this problem.
    Update: Migration from Digits to Firebase Phone Authentication required before September 30 2017.
    This will also mitigate the above problem.
     */
    void startPhoneNumberVerification(CharSequence phoneNumber){

        pd.setTitle("Authenticating");
        pd.setMessage("Verifying Phone Number");
        pd.setCancelable(false);
        pd.show();

        //Initialize Digits Kit
        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
        Fabric.with(this, new TwitterCore(authConfig), new Digits.Builder().build());

        //Start Authentication Flow
        authCallback = new AuthCallback() {
            @Override
            public void success(DigitsSession session, String phoneNumber) {
                // TODO: associate the session userID with your user model
                Toast.makeText(getApplicationContext(), "Authentication successful for "
                        + phoneNumber, Toast.LENGTH_LONG).show();

                //Now Facebook Login
                System.out.println("BEFORE FACEBOOK");
                facebookLogin();
            }

            @Override
            public void failure(DigitsException exception) {
                Log.d("Digits", "Sign in with Digits failure", exception);
            }
        };
        AuthConfig.Builder authConfigBuilder = new AuthConfig.Builder()
                .withAuthCallBack(authCallback)
                .withPhoneNumber("+91"+phoneNumber);

        System.out.print("HELLO_WORLD\nHELLO_WORLD\nHELLO_WORLD\nHELLO_WORLD\nHELLO_WORLD\nHELLO_WORLD\n");
        System.out.println("Valid User?:"+(Digits.getActiveSession()==null));
        if((Digits.getActiveSession()==null)&&isSignUpClicked){
            Digits.authenticate(authConfigBuilder.build());
        }
        else if((Digits.getActiveSession()!=null))facebookLogin();
        else pd.dismiss();


    }

    private void handleFacebookAccessToken(AccessToken token) {

        if(accessToken==null)return;
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
                        authenticateFirebase();
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithCredential", task.getException());
                            Toast.makeText(MainActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }

                        // ...
                    }
                });

    }

    void authenticateFirebase(){

        pd.dismiss();
        pd.setMessage("Going Online! :)");
        pd.show();

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                    Toast.makeText(getApplicationContext(),user.getUid(),Toast.LENGTH_SHORT).show();

                    isComplete=true;

                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                    handleFacebookAccessToken(accessToken);
                }
                // ...
            }
        };

        mAuth.addAuthStateListener(mAuthListener);
    }

    void facebookLogin(){

        pd.dismiss();
        pd.setMessage("Verifying Facebook Credentials");
        pd.show();

        permissionNeeds= Arrays.asList("email","user_friends");
        FacebookSdk.setApplicationId("193849254386118");
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
                        //System.out.println("1");
                        //Connect to Firebase
                        accessToken=loginResult.getAccessToken();

                        //Now Firebase Login
                        System.out.println("BEFORE FIREBASE");
                        authenticateFirebase();

                    }

                    @Override
                    public void onCancel() {
                        // App code
                        Toast.makeText(getApplicationContext(),"You cancelled some permissions required.",Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        // App code
                        LoginManager.getInstance().logInWithReadPermissions(MainActivity.this,MainActivity.this.permissionNeeds);
                    }
                }
        );
        LoginManager.getInstance().logInWithReadPermissions(MainActivity.this,permissionNeeds);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                while (!isComplete) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }



                Intent i = new Intent(MainActivity.this, MainScreenActivity.class);
                EditText name = (EditText) findViewById(R.id.nameid);
                EditText email = (EditText) findViewById(R.id.email);
                EditText phone = (EditText) findViewById(R.id.phoneid);
                RadioGroup radioGroup= (RadioGroup) findViewById(R.id.radioSex);
                String gender;
                switch (radioGroup.getCheckedRadioButtonId()){
                    case R.id.radioMale:
                        gender="male";
                        break;
                    case R.id.radioFemale:
                        gender="female";
                        break;
                    default:
                        gender="null";
                        break;
                }

                pd.dismiss();

                if(/*isSignUpClicked*/true){
                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    EditText passwordText = (EditText) findViewById(R.id.passwordid);
                    User user =new User(mAuth.getCurrentUser().getUid(),name.getText().toString(),gender,Digits.getActiveSession().getPhoneNumber(),Contacts.ITEMS,"0",passwordText.getText().toString());
                    //database.getReference().getRoot().child("Users").push().setValue(user.uid);
                    System.out.println("FIREBASE SET_VALUE\n\n\n"+user.uid);
                    //database.getReference().getRoot().child("Users").setValue(user.uid);
                    database.getReference().getRoot().child("Users/"+user.uid+"/").setValue(user);
                }

                i.putExtra("com.goodsamaritan.myphone",Digits.getActiveSession().getPhoneNumber());
                startActivity(i);
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();

    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        EditText phone = (EditText) findViewById(R.id.phoneid);
        System.out.println("BEFORE PHONE RESUME");
        //startPhoneNumberVerification(phone.getText());

    }

    public void requestPermissions(){
        if ((ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED)||
                (ContextCompat.checkSelfPermission(this,Manifest.permission.READ_PHONE_STATE)!= PackageManager.PERMISSION_GRANTED)|| (ContextCompat.checkSelfPermission(this,Manifest.permission.READ_CONTACTS)!= PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.READ_PHONE_STATE,Manifest.permission.READ_CONTACTS},APP_PERMS);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case APP_PERMS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // task you need to do.

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
}
