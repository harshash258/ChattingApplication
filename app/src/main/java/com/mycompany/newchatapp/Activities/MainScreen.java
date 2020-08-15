package com.mycompany.newchatapp.Activities;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.mycompany.newchatapp.Fragment.ChatsFragment;
import com.mycompany.newchatapp.Fragment.RoomFragment;
import com.mycompany.newchatapp.Fragment.UsersFragment;
import com.mycompany.newchatapp.R;

import java.util.ArrayList;

public class MainScreen extends AppCompatActivity {


    TabLayout tabLayout;
    ViewPager viewPager;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);

        ViewPageAdapter viewPageAdapter = new ViewPageAdapter(getSupportFragmentManager());
        viewPageAdapter.addFragment(new ChatsFragment(), "Chats");
        viewPageAdapter.addFragment(new RoomFragment(), "Room");
        viewPageAdapter.addFragment(new UsersFragment(), "Friends");

        viewPager.setAdapter(viewPageAdapter);
        tabLayout.setupWithViewPager(viewPager);

        String deviceId = FirebaseInstanceId.getInstance().getToken();
        databaseReference = FirebaseDatabase.getInstance().getReference()
                .child("Users").child(user.getUid());
            databaseReference.child("deviceId").setValue(deviceId);
        askPermissions();
    }

    private void askPermissions() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS},
                    1);
        }
    }

    private void updateStatus(String status) {
        String deviceId = FirebaseInstanceId.getInstance().getToken();
        databaseReference = FirebaseDatabase.getInstance().getReference()
                .child("Users").child(user.getUid());
        databaseReference.child("status").setValue(status);
        databaseReference.child("deviceId").setValue(deviceId);
        databaseReference.child("typingTo").setValue("noOne");

    }

    @Override
    public void onBackPressed() {
        Intent a = new Intent(Intent.ACTION_MAIN);
        a.addCategory(Intent.CATEGORY_HOME);
        a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(a);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateStatus("online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        updateStatus("offline");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu1, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();

        switch (id){
            case R.id.profileActivity:
                Intent profile = new Intent(MainScreen.this, ProfileActivity.class);
                startActivity(profile);
                break;
            case R.id.createGroup:
                Intent group = new Intent(MainScreen.this, CreateGroup.class);
                startActivity(group);
                break;
            case R.id.settingActivity:
                Intent setting = new Intent(MainScreen.this, SettingActivity.class);
                startActivity(setting);
                break;
            case R.id.aboutActivity:
                Intent about = new Intent(MainScreen.this, AboutActivity.class);
                startActivity(about);
                break;
            case R.id.exit:
                Intent a = new Intent(Intent.ACTION_MAIN);
                a.addCategory(Intent.CATEGORY_HOME);
                a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(a);
        }

        return super.onOptionsItemSelected(item);
    }

    static class ViewPageAdapter extends FragmentPagerAdapter {

        private ArrayList<Fragment> fragments;
        private ArrayList<String> titles;

        ViewPageAdapter(@NonNull FragmentManager fm) {
            super(fm);
            this.fragments = new ArrayList<>();
            this.titles = new ArrayList<>();
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        void addFragment(Fragment fragment, String title) {
            fragments.add(fragment);
            titles.add(title);
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return titles.get(position);
        }
    }
}