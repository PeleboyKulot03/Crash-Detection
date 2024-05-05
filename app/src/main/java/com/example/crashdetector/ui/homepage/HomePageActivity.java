package com.example.crashdetector.ui.homepage;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import com.example.crashdetector.R;
import com.example.crashdetector.ui.homepage.fragments.ExampleService;
import com.example.crashdetector.ui.homepage.fragments.FallDetector;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomePageActivity extends AppCompatActivity {
    private DrawerLayout drawerLayout;
    private FallDetector fallDetector;
    private final int FALL_DETECTOR = R.id.fallDetector;
    private final int DASH_BOARD = R.id.dashboard;
    private final int PHONE_LEFT = R.id.phoneLeft;
    @RequiresApi(api = Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);
        fallDetector = new FallDetector();

        drawerLayout = findViewById(R.id.my_drawer_layout);
        MaterialToolbar toolbar = findViewById(R.id.toolBar);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavBar);
        switchFragment(fallDetector);

        bottomNavigationView.setOnItemSelectedListener(menuItem -> {
            if (menuItem.getItemId() == FALL_DETECTOR) {
                switchFragment(fallDetector);
                toolbar.setTitle("Fall Detector");
                return true;
            }
            if (menuItem.getItemId() == DASH_BOARD) {
                switchFragment(fallDetector);
                toolbar.setTitle("Dashboard");
                return true;
            }
            if (menuItem.getItemId() == PHONE_LEFT) {
                switchFragment(fallDetector);
                toolbar.setTitle("Left Phone Detector");
                return true;
            }
            return true;
        });
        toolbar.setNavigationOnClickListener(item -> drawerLayout.openDrawer(GravityCompat.START));

    }

    public void switchFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, fragment).commit();
    }
}