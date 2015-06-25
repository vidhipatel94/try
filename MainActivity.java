package com.example.vidhipatel.myapplication;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListener;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class MainActivity extends AppCompatActivity {
    DrawerLayout drawerLayout;
    Toolbar toolbar;
    ContentFragment fragment;
    android.support.v4.app.FragmentTransaction fragmentTransaction;

    ListView lv;
    MyAdapterUser adapter1;
    List<User> mUserList;
    User mUser;
    DatabaseHandler db;
    SwipeRefreshLayout mSwipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerLayout.openDrawer(GravityCompat.START);

        toolbar=(Toolbar)findViewById(R.id.toolbar);
        toolbar.setTitle("Users");
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setVisibility(View.VISIBLE);

        //navigation button in actionbar
        toolbar.setNavigationIcon(R.drawable.ic_menu_white_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });


        NavigationView view = (NavigationView) findViewById(R.id.navigation_view);
        view.getMenu().getItem(2).setChecked(true); //by default select Users
        fragment = new ContentFragment();
        fragmentTransaction = getSupportFragmentManager().beginTransaction();
        view.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                 menuItem.setChecked(true);
                switch (menuItem.getItemId()) {

                    case R.id.navigation_home:
                        if(fragmentTransaction.isEmpty()) {
                            fragmentTransaction.replace(R.id.content_frame, fragment).commit();
                        }
                        Toast.makeText(getApplicationContext(), menuItem.getTitle(),Toast.LENGTH_LONG).show();
                        break;
                    case R.id.navigation_message:
                        if(fragmentTransaction.isEmpty()) {
                            fragmentTransaction.replace(R.id.content_frame, fragment).commit();
                        }
                        Toast.makeText(getApplicationContext(), menuItem.getTitle(),Toast.LENGTH_LONG).show();
                        break;
                    default:
                        if(!fragmentTransaction.isEmpty()) {
                            fragmentTransaction.remove(fragment).commit();
                        }
                }

                drawerLayout.closeDrawers();

                return true;
            }
        });

        mUserList = new ArrayList<User>();
        lv = (ListView) findViewById(R.id.list);

        loadUserData();

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.activity_main_swipe_refresh_layout);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.accent_material_light);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadUserData();
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addUser();
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();  // Always call the superclass method first
        displayUser();
    }

        @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);

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

        if (id == R.id.search) {

        }

        return super.onOptionsItemSelected(item);
    }

    private void loadUserData() {
        this.deleteDatabase("userdb");
        db = new DatabaseHandler(this);

        Api api = new RestAdapter.Builder()
                .setEndpoint("http://jsonplaceholder.typicode.com")
                .build()
                .create(Api.class);
        api.getUser(new Callback<List<User>>() {
            @Override
            public void success(List<User> users, Response response) {
                // Log.i("JSON",users.get(0).getAddress());
                for (int i = 0; i < users.size(); i++) {
                    mUser = users.get(i);
                    //mUserList.add(mUser);
                    db.addUser(mUser);
                }
                displayUser();
                mSwipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void failure(RetrofitError error) {
                Toast.makeText(getApplicationContext(), "Connection error", Toast.LENGTH_LONG).show();
             /*   mUser = new User();
                mUser.setId(mUserList.size() + 1);
                mUser.setName("Vidhi Patel");
                mUser.setUsername("vidhi");
                mUser.setEmail("vidhi@xyz.com");
                mUserList.add(mUser);
                db.addUser(mUser);

                mUser = new User();
                mUser.setId(mUserList.size() + 1);
                mUser.setName("Radhi Patel");
                mUser.setUsername("vidhi");
                mUser.setEmail("vidhi@xyz.com");
                mUserList.add(mUser);
                db.addUser(mUser);
                displayUser();
                */
            }
        });
    }

    private void displayUser() {
        mUserList = db.getAllUsers();
        adapter1 =
                new MyAdapterUser(this, mUserList);
        lv.setAdapter(adapter1);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                // ListView Clicked item value
              //  User itemValue = (User) lv.getItemAtPosition(position);

                Intent intent = new Intent(getApplicationContext(), EmployeeInfo.class);
                //intent.putExtra("User", itemValue);
                intent.putExtra("User", position);
                startActivity(intent);

            }

        });
        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                db.deleteUser(mUserList.get(position));
                mUserList.remove(position);
                adapter1.notifyDataSetChanged();
                return true;
            }
        });
    }

    private void addUser() {
        mUser = new User();
        mUser.setId(mUserList.size() + 1);
        mUser.setName("Vidhi Patel");
        mUser.setUsername("vidhi");
        mUser.setEmail("vidhi@xyz.com");
        mUserList.add(mUser);
        adapter1.notifyDataSetChanged();
        db.addUser(mUser);
        Toast.makeText(getApplicationContext(), "User is added ", Toast.LENGTH_LONG).show();
    }

}
