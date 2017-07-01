package com.goodsamaritan;

import android.app.FragmentManager;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
//import android.provider.ContactsContract.Contacts; Only for reference. Cannot be imported as there's another Contacts already!
import android.provider.ContactsContract.CommonDataKinds.Email;


import com.goodsamaritan.drawer.contacts.ContactsFragment;
import com.goodsamaritan.drawer.contacts.Contacts;
import com.goodsamaritan.drawer.home.HomeFragment;
import com.goodsamaritan.drawer.help_and_feedback.HelpAndFeedbackFragment;
import com.goodsamaritan.drawer.map.MapFragment;
import com.goodsamaritan.drawer.settings.SettingsFragment;
import com.goodsamaritan.drawer.profile.ProfileFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Iterator;
import java.util.Set;

public class MainScreenActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,ContactsFragment.OnListFragmentInteractionListener,HomeFragment.OnHomeInteractionListener,HelpAndFeedbackFragment.OnHelpAndFeedbackInteractionListener,SettingsFragment.OnSettingsInteractionListener,ProfileFragment.OnProfileInteractionListener,MapFragment.OnFragmentInteractionListener {

    FragmentManager manager;
    FirebaseAuth auth;
    FirebaseDatabase database;

    private static final int CONTACT_PICKER_RESULT = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        View nav=navigationView.getHeaderView(0);

        //Initialize Fragment Manager
        manager = getFragmentManager();

        //Set Home as default
        navigationView.getMenu().getItem(0).setChecked(true);
        HomeFragment homeFragment = new HomeFragment();
        manager.beginTransaction().replace(R.id.content_main_screen_layout,homeFragment).commit();


        //Set Title
        setTitle(R.string.app_name);

        //Initialize Firebase Database
        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();



        //Set Profile Name
        final TextView main_screen_name= (TextView) nav.findViewById(R.id.main_screen_name);
        database.getReference().getRoot().child("Users").child(auth.getCurrentUser().getUid()).child("name").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                main_screen_name.setText(dataSnapshot.getValue(String.class));

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        //Set Email Id
        final TextView main_screen_email=(TextView) nav.findViewById(R.id.main_screen_email);
        database.getReference().getRoot().child("Users").child(auth.getCurrentUser().getUid()).child("phone").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                main_screen_email.setText(dataSnapshot.getValue(String.class));

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onStart(){
        super.onStart();
        database.getReference().getRoot().child("Users").child(auth.getCurrentUser().getUid()).child("isAvailable").setValue("true");
        if(!LocationService.isRunning){
            Log.d("LOCATIONSERVICE","Started");
            Thread t1 = new Thread(new Runnable() {
                @Override
                public void run() {
                    startService(new Intent(MainScreenActivity.this,LocationService.class).putExtra("com.goodsamaritan.myphone",getIntent().getStringExtra("com.goodsamaritan.myphone")));
                }
            });
            t1.start();
        } else Log.d("LOCATIONSERVICE","Running");

        if(getIntent().getExtras()==null)
        Log.d("GETEXTRAS:","It's null");
        /*if(getIntent().getExtras()!=null&&getIntent().getExtras().getBoolean("startMaps")){
            Location location =getIntent().getExtras().getParcelable("location");
            Intent mapIntent = new Intent(MainScreenActivity.this,RouteFragmentActivity.class);
            mapIntent.putExtra("location",location);
            getIntent().getExtras().clear();
            startActivity(mapIntent);

        }*/
        if(getIntent().getBooleanExtra("com.goodsamaritan.startMaps",false)){


            Location location =getIntent().getParcelableExtra("com.goodsamaritan.location");

            Log.d("LOCATION","Name:"+getIntent().getStringExtra("com.goodsamaritan.name")+" lat:"+location.getLatitude()+" long:"+location.getLongitude());

            Uri gmmIntentUri = Uri.parse("google.navigation:q="+location.getLatitude()+","+location.getLongitude());
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            getIntent().removeExtra("com.goodsamaritan.startMaps");  //Required, else this block will launch the Maps intent everytime.

            //Inform the victim you're ready to help.
           Log.d("HELPERSERVICE",getIntent().getStringExtra("com.goodsamaritan.uid"));
            database.getReference().child("Users").child(getIntent().getStringExtra("com.goodsamaritan.uid")).child("helpers").child(auth.getCurrentUser().getUid()).setValue("");

            startActivity(mapIntent);

        }

    }

    @Override
    public void onPause(){
        database.getReference().getRoot().child("Users").child(auth.getCurrentUser().getUid()).child("isAvailable").setValue("false");
        super.onPause();
    }

    @Override
    public void onResume(){
        super.onResume();
        database.getReference().getRoot().child("Users").child(auth.getCurrentUser().getUid()).child("isAvailable").setValue("true");

        /*if(getIntent().getExtras()!=null&&getIntent().getExtras().getBoolean("startMaps")) {
            Location location = getIntent().getExtras().getParcelable("location");
            Intent mapIntent = new Intent(MainScreenActivity.this, RouteFragmentActivity.class);
            mapIntent.putExtra("location", location);
            getIntent().getExtras().clear();
            startActivity(mapIntent);
        }*/
    }

    @Override
    public void onStop(){
        database.getReference().getRoot().child("Users").child(auth.getCurrentUser().getUid()).child("isAvailable").setValue("false");
        super.onStop();
    }

    @Override
    public void onDestroy(){
        database.getReference().getRoot().child("Users").child(auth.getCurrentUser().getUid()).child("isAvailable").setValue("false");
        super.onDestroy();

    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState){
        database.getReference().getRoot().child("Users").child(auth.getCurrentUser().getUid()).child("isAvailable").setValue("false");
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState){
        super.onSaveInstanceState(savedInstanceState);
        database.getReference().getRoot().child("Users").child(auth.getCurrentUser().getUid()).child("isAvailable").setValue("true");
    }




    @Override
    public void onBackPressed() {
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if(!navigationView.getMenu().getItem(0).isChecked()){
            HomeFragment homeFragment = new HomeFragment();
            manager.beginTransaction().replace(R.id.content_main_screen_layout,homeFragment).commit();
            navigationView.getMenu().getItem(0).setChecked(true);
        } /*else{
            super.onBackPressed();
        }*/
    }

    public void doLaunchContactPicker() {
        /*Intent contactPickerIntent = new Intent(Intent.ACTION_PICK,
                android.provider.ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(contactPickerIntent, CONTACT_PICKER_RESULT);*/

        //Beta Multiple Contacts
        Intent contactPickerInt = new Intent(MainScreenActivity.this,ContactsPickerActivity.class);
        startActivity(contactPickerInt);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main2, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        } else if(id == R.id.action_person_add){
            doLaunchContactPicker();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("RESULT","Reached");

        if(resultCode == RESULT_OK){
            /*Bundle extras = data.getExtras();
            Set keys = extras.keySet();
            Iterator iterate = keys.iterator();
            while (iterate.hasNext()) {
                String key = (String) iterate.next();
                Log.v("PERSON_ADD", key + "[" + extras.get(key) + "]");
            }*/
            Uri result = data.getData();
            Log.v("PERSON_ADD", "Got a result: "
                    + result.toString());
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();


        if (id == R.id.nav_home) {
            manager = getFragmentManager();
            HomeFragment homeFragment= new HomeFragment();
            manager.beginTransaction().replace(R.id.content_main_screen_layout,homeFragment).commit();

        } else if (id == R.id.nav_profile) {
            ProfileFragment profileFragment = new ProfileFragment();
            manager.beginTransaction().replace(R.id.content_main_screen_layout,profileFragment).commit();

        } else if (id == R.id.nav_settings) {
            SettingsFragment settingsFragment = new SettingsFragment();
            manager.beginTransaction().replace(R.id.content_main_screen_layout,settingsFragment).commit();

        } else if (id == R.id.add_contacts) {
            ContactsFragment contactsFragment = new ContactsFragment();
            manager.beginTransaction().replace(R.id.content_main_screen_layout,contactsFragment).commit();
        } else if (id == R.id.help_and_fb) {
            HelpAndFeedbackFragment helpAndFeedbackFragment = new HelpAndFeedbackFragment();
            manager.beginTransaction().replace(R.id.content_main_screen_layout,helpAndFeedbackFragment).commit();

        } else if(id == R.id.dr_map){
            MapFragment mapFragment = new MapFragment();
            manager.beginTransaction().replace(R.id.content_main_screen_layout,mapFragment).commit();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onListFragmentInteraction(Contacts.ContactItem item) {
    }

    @Override
    public void onHomeInteraction(Uri uri) {

    }

    @Override
    public void onHelpAndFeedbackInteraction(Uri uri) {

    }

    @Override
    public void onSettingsInteraction(Uri uri) {

    }

    @Override
    public void onProfileInteraction(Uri uri) {

    }

    @Override
    public void onMapInteraction(Uri uri) {

    }
}
