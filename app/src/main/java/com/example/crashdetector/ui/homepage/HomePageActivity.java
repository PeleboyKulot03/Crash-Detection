package com.example.crashdetector.ui.homepage;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.crashdetector.R;
import com.example.crashdetector.ui.homepage.fragments.FallDetector;
import com.example.crashdetector.ui.homepage.fragments.IFallDetector;
import com.example.crashdetector.ui.login.LoginPage;
import com.example.crashdetector.ui.services.FallDetectorSensor;
import com.example.crashdetector.utils.HomePageModel;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

public class HomePageActivity extends AppCompatActivity implements IHomePage, IFallDetector {
    private DrawerLayout drawerLayout;
    private FallDetector fallDetector;
    private final int FALL_DETECTOR = R.id.fallDetector;
    private final int DASH_BOARD = R.id.dashboard;
    private TextView name, email;
    private ImageView profilePic;
    private String displayName;
    private Uri imageUri;

    @RequiresApi(api = Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);
        HomePageModel model = new HomePageModel(this);

        drawerLayout = findViewById(R.id.my_drawer_layout);
        MaterialToolbar toolbar = findViewById(R.id.toolBar);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavBar);
        NavigationView navView = findViewById(R.id.navView);
        View header = navView.getHeaderView(0);
        TextView logout = findViewById(R.id.logout);
        name = header.findViewById(R.id.nameTV);
        email = header.findViewById(R.id.emailTV);
        profilePic = header.findViewById(R.id.profilePic);
        model.getInformation();
        switchFragment(fallDetector);

        bottomNavigationView.setOnItemSelectedListener(menuItem -> {
            if (menuItem.getItemId() == FALL_DETECTOR) {
                switchFragment(fallDetector);
                toolbar.setTitle("Fall Detector");
                return true;
            }
            if (menuItem.getItemId() == DASH_BOARD) {
//                switchFragment();
                toolbar.setTitle("Dashboard");
                return true;
            }
            return true;
        });
        toolbar.setNavigationOnClickListener(item -> drawerLayout.openDrawer(GravityCompat.START));
        logout.setOnClickListener(v -> {
            if (isServiceRunningInForeground(getApplicationContext(), FallDetectorSensor.class)) {
                Intent intent = new Intent(HomePageActivity.this, FallDetectorSensor.class);
                intent.putExtra("off", "");
                getApplicationContext().startForegroundService(intent);
            }
            model.logout();
            startActivity(new Intent(getApplicationContext(), LoginPage.class));
            finish();

        });
    }

    public void switchFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, fragment).commit();
    }
    public static boolean isServiceRunningInForeground(Context context, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return service.foreground;
            }
        }
        return false;
    }

    @Override
    public void onGetUserData(HomePageModel model) {
        if (model != null) {
            name.setText(model.getName());
            imageUri = model.getImageUri();
            displayName = model.getName();
//            Glide.with(HomePageActivity.this)
//                    .load(model.getImageUri())
//                    .centerCrop()
//                    .circleCrop()
//                    .into(profilePic);

            email.setText(model.getEmail());
            fallDetector = new FallDetector(model.getEmail());
        }

    }

    @Override
    public void onFall() {

    }

    @Override
    public void onGenerateReport(String message) {

    }
}