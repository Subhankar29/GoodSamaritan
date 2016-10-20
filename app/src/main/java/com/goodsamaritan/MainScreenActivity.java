package com.goodsamaritan;

import android.app.FragmentManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
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

import com.goodsamaritan.drawer.ContactsFragment;
import com.goodsamaritan.drawer.contacts.Contacts;
import com.goodsamaritan.drawer.contacts.HomeFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainScreenActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,ContactsFragment.OnListFragmentInteractionListener,HomeFragment.OnFragmentInteractionListener {

    FragmentManager manager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        View nav=navigationView.getHeaderView(0);

        //Fragment Manager
        manager = getFragmentManager();
        //Set Home as default
        navigationView.getMenu().getItem(0).setChecked(true);
        HomeFragment homeFragment = new HomeFragment();
        manager.beginTransaction().replace(R.id.content_main_screen_layout,homeFragment).commit();


        //Set Title
        setTitle(R.string.app_name);

        //Initialize Firebase Database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();



        //Set Profile Name
        final TextView main_screen_name= (TextView) nav.findViewById(R.id.main_screen_name);
        main_screen_name.setText(getIntent().getExtras().getString("name","not"));
        //System.out.println(getIntent().getStringExtra("name")+"\nEmail:"+getIntent().getStringExtra("email"));

        //Set Email Id
        final TextView main_screen_email=(TextView) nav.findViewById(R.id.main_screen_email);
        main_screen_email.setText(getIntent().getStringExtra("email"));

        database.getReference().getRoot().child("Users").child(auth.getCurrentUser().getUid()).child("name").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                main_screen_name.setText(dataSnapshot.getValue(String.class));

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        database.getReference().getRoot().child("Users").child(auth.getCurrentUser().getUid()).child("phone").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                main_screen_email.setText(dataSnapshot.getValue(String.class));

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

//        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
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
        } else{
            super.onBackPressed();
        }
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
        }

        return super.onOptionsItemSelected(item);
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

        } else if (id == R.id.nav_settings) {

        } else if (id == R.id.add_contacts) {
            ContactsFragment contactsFragment = new ContactsFragment();
            manager.beginTransaction().replace(R.id.content_main_screen_layout,contactsFragment).commit();
        } else if (id == R.id.help_and_fb) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onListFragmentInteraction(Contacts.ContactItem item) {
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
