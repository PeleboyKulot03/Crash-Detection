package com.example.crashdetector.ui.homepage;

import static com.example.crashdetector.ui.homepage.fragments.FallDetector.isBetween;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.crashdetector.R;
import com.example.crashdetector.ui.customview.ResultView;
import com.example.crashdetector.ui.homepage.fragments.FallDetector;
import com.example.crashdetector.ui.homepage.fragments.IFallDetector;
import com.example.crashdetector.ui.homepage.fragments.configs.GMailSender;
import com.example.crashdetector.ui.login.LoginPage;
import com.example.crashdetector.ui.services.FallDetectorSensor;
import com.example.crashdetector.ui.services.LeftPhoneService;
import com.example.crashdetector.utils.HomePageModel;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class HomePageActivity extends AppCompatActivity implements IHomePage, IFallDetector, SensorEventListener {
    private DrawerLayout drawerLayout;
    private long finalTime;
//    private FallDetector fallDetector;
//    private final int FALL_DETECTOR = R.id.fallDetector;
//    private final int DASH_BOARD = R.id.dashboard;
    private TextView name, email;
    private ImageView profilePic;
    private String displayName;
    private Uri imageUri;
    private EditText timeET;
    private static final Float THRESHOLD = 15.0f;
    private Context context;
    private Activity activity;
    final float alpha = 0.8f;
    private final float[] gravity = {0, 0, 0};
    private final float[] linear_acceleration = {0, 0, 0};
    private TextView xAxis;
    private TextView yAxis;
    private TextView zAxis;
    private boolean isOn = true;
    private SensorManager sensorManagerAccelerometer;
    private boolean isFalling = false;
    private ArrayList<Float> fallingValues;
    private String dropOrNot = "";
    private String isDropped = "";
    SwitchMaterial leftPhoneSwitch;
    SwitchMaterial accelerometerSwitch;
    private float mAccelCurrent;
    private int counter = 0;
    private boolean beenFreeFall = false;
    private int startFreeFallIndex = 0;
    private Sensor accelerometorSensor;
    private boolean isLeftPhoneRunning = false;
    private boolean isEdit = false;
    private boolean isFirst = true;
    private LocalTime time;
    TextView accelerometerWarning;
    TextView leftPhoneWarning;
    private ProgressBar progressBar;

    @RequiresApi(api = Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);
        HomePageModel model = new HomePageModel(this);
        context = getApplicationContext();
        progressBar  = findViewById(R.id.progressBar);
        activity = HomePageActivity.this;

        drawerLayout = findViewById(R.id.my_drawer_layout);
        MaterialToolbar toolbar = findViewById(R.id.toolBar);
//        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavBar);
        NavigationView navView = findViewById(R.id.navView);
        View header = navView.getHeaderView(0);
        TextView logout = findViewById(R.id.logout);
        name = header.findViewById(R.id.nameTV);
        email = header.findViewById(R.id.emailTV);
        profilePic = header.findViewById(R.id.profilePic);
        model.getNewTime();
//        model.getInformation();
//        switchFragment(fallDetector);

//        bottomNavigationView.setOnItemSelectedListener(menuItem -> {
//            if (menuItem.getItemId() == FALL_DETECTOR) {
//                switchFragment(fallDetector);
//                toolbar.setTitle("Fall Detector");
//                return true;
//            }
//            if (menuItem.getItemId() == DASH_BOARD) {
////                switchFragment();
//                toolbar.setTitle("Dashboard");
//                return true;
//            }
//            return true;
//        });
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

        mAccelCurrent = SensorManager.GRAVITY_EARTH;
        String cur = LocalTime.now().toString();
        time = LocalTime.parse(cur);
        sensorManagerAccelerometer = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        accelerometorSensor = sensorManagerAccelerometer.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        leftPhoneSwitch = findViewById(R.id.leftPhoneSwitch);
        accelerometerSwitch = findViewById(R.id.accelerometerSwitch);
        CardView timeSetting = findViewById(R.id.time);
        timeET = findViewById(R.id.timeET);
        ImageView edit = findViewById(R.id.edit);
//        ImageView done = findViewById(R.id.done);
        timeSetting.setOnClickListener(v -> {
            leftPhoneSwitch.setChecked(false);
            timeET.setTextColor(getColor(R.color.white));
            timeET.setEnabled(true);
            edit.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        });

        edit.setOnClickListener(v -> {
            finalTime = Long.parseLong(timeET.getText().toString());

            timeET.setTextColor(getColor(R.color.gray));
            timeET.setEnabled(false);
            edit.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT));
            model.setNewTime(Integer.parseInt(timeET.getText().toString()));
            leftPhoneSwitch.setChecked(true);
        });
        xAxis = findViewById(R.id.xAxis);
        yAxis = findViewById(R.id.yAxis);
        zAxis = findViewById(R.id.zAxis);
        accelerometerWarning = findViewById(R.id.warningAccelerometer);
        leftPhoneWarning = findViewById(R.id.leftPhoneWarning);
        fallingValues = new ArrayList<>();
    }



//    public void switchFragment(Fragment fragment) {
//        getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, fragment).commit();
//    }
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
        progressBar.setVisibility(View.GONE);
        if (model != null) {
            name.setText(model.getName());
            imageUri = model.getImageUri();
            displayName = model.getName();
            Glide.with(HomePageActivity.this)
                    .load(model.getImageUri())
                    .centerCrop()
                    .circleCrop()
                    .into(profilePic);

            email.setText(model.getEmail());
//            fallDetector = new FallDetector(model.getEmail());

            timeET.setText(String.valueOf(model.getTime()));
            finalTime = model.getTime();
        }

        if (accelerometorSensor != null) {
            sensorManagerAccelerometer.registerListener(HomePageActivity.this, accelerometorSensor, SensorManager.SENSOR_DELAY_FASTEST);
            Intent intent = new Intent(HomePageActivity.this, FallDetectorSensor.class);
            intent.putExtra("email", email.getText().toString());
            context.startForegroundService(intent);
            if (!isBetween(time, LocalTime.of(20, 0, 0), LocalTime.of(8, 0, 0))) {
                Intent intent1 = new Intent(HomePageActivity.this, LeftPhoneService.class);
                intent1.putExtra("email", email.getText().toString());
                intent1.putExtra("time", finalTime);
                context.startForegroundService(intent1);
                isLeftPhoneRunning = true;
            }
        }
        else {
            accelerometerWarning.setVisibility(View.VISIBLE);
            accelerometerSwitch.setChecked(false);
            accelerometerSwitch.setEnabled(false);
        }

        accelerometerSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!isChecked) {
                fallingValues = new ArrayList<>();
                startFreeFallIndex = 0;
                counter = 0;
                beenFreeFall = false;
                isFalling = false;
                if (isServiceRunningInForeground(getApplicationContext(), FallDetectorSensor.class)) {
                    Intent intent = new Intent(HomePageActivity.this, FallDetectorSensor.class);
                    intent.putExtra("off", "");
                    context.startForegroundService(intent);
                    sensorManagerAccelerometer.unregisterListener(HomePageActivity.this);
                    accelerometerWarning.setVisibility(View.VISIBLE);
                    accelerometerWarning.setText(activity.getString(R.string.accelerometer_off));
                    return;
                }
            }
            sensorManagerAccelerometer.registerListener(HomePageActivity.this, accelerometorSensor, SensorManager.SENSOR_DELAY_FASTEST);
            accelerometerWarning.setVisibility(View.GONE);
            Intent intent = new Intent(HomePageActivity.this, FallDetectorSensor.class);
            intent.putExtra("email", email.getText().toString());
            context.startForegroundService(intent);
        });
        if (isBetween(time, LocalTime.of(20, 0, 0), LocalTime.of(8, 0, 0))) {
            leftPhoneWarning.setVisibility(View.VISIBLE);
            leftPhoneWarning.setText(getString(R.string.sleep_mode_text));
            leftPhoneSwitch.setChecked(false);
        }

        leftPhoneSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Log.i("TAGERISTA", "onCreate: ");
            if (!isChecked) {
                if (isServiceRunningInForeground(getApplicationContext(), LeftPhoneService.class)) {
                    Intent intent = new Intent(HomePageActivity.this, LeftPhoneService.class);
                    intent.putExtra("off", "");
                    context.startForegroundService(intent);
                    isLeftPhoneRunning = false;
                }
                leftPhoneWarning.setVisibility(View.VISIBLE);
                if (isBetween(time, LocalTime.of(20, 0, 0), LocalTime.of(8, 0, 0))) {
                    leftPhoneWarning.setText(getString(R.string.sleep_mode_text));
                    return;
                }
                leftPhoneWarning.setText(getString(R.string.left_phone_warning));
                Log.i("TAGERISTA", "onCreate: ");
            }
            else {
                Log.i("TAGERISTA", "onCreateView: ");
                leftPhoneWarning.setVisibility(View.GONE);
                Intent intent = new Intent(HomePageActivity.this, LeftPhoneService.class);
                intent.putExtra("email", email.getText().toString());
                intent.putExtra("time", finalTime);
                context.startForegroundService(intent);
                isLeftPhoneRunning = true;
            }
        });

    }

    @Override
    public void onGetTime(int time) {
        timeET.setText(String.valueOf(time));
        finalTime = time;
        Log.i("TAGERISTAX", "onGetTime: "+ time);
    }

    @Override
    public void onGenerateReport(String message) {
        GMailSender sender = new GMailSender(email.getText().toString(), "Fall Detected", "Hi, this email is to inform you that your phone " + getDeviceName() + " has been drop.");
        sender.execute();
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
    public String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.toLowerCase().startsWith(manufacturer.toLowerCase())) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }

    private String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

            float[] mGravity = event.values.clone();

            float x = mGravity[0];
            float y = mGravity[1];
            float z = mGravity[2];
            mAccelCurrent = (float) Math.sqrt(x*x + y*y + z*z);

            if (isOn) {
                Handler handler = new Handler();
                handler.postDelayed(() -> isOn = true, 300);
                activity.runOnUiThread(() -> {
                    String xAxisText = String.format(Locale.getDefault(), "%.2f", x > 0.99 ? x:0) + " m/s²";
                    String yAxisText = String.format(Locale.getDefault(), "%.2f", y > 0.99 ? y:0) + " m/s²";
                    String zAxisText = String.format(Locale.getDefault(), "%.2f", z > 0.99 ? z:0) + " m/s²";
                    xAxis.setText(xAxisText);
                    yAxis.setText(yAxisText);
                    zAxis.setText(zAxisText);
                });
                isOn = false;
            }
            if (!beenFreeFall) {
                if (fallingValues.size() >= 1000 && !isFalling) {
                    fallingValues.subList(0, fallingValues.size() - 100).clear();
                }
                fallingValues.add(mAccelCurrent);
            }

            if (!isFalling) {
                if (mAccelCurrent < 1) {
                    isFalling = true;
                    startFreeFallIndex = fallingValues.size();
                }
            }
            if (isFalling) {
                if (mAccelCurrent >= 9 && mAccelCurrent <= 10 && counter < 10) {
                    counter += 1;
                    return;
                }
                if (counter >= 10) {
                    beenFreeFall = true;
                    isFalling = false;
                    List<Float> temp = new ArrayList<>(fallingValues.subList(startFreeFallIndex - 20, fallingValues.size() - 9));
                    for (int i = 0; i < 20; i++) {
                        if (temp.get(i) > THRESHOLD) {
                            dropOrNot = "Your phone has been thrown!";
                            isDropped = " thrown!";
                            break;
                        }
                        dropOrNot = "Your phone has been dropped!";
                        isDropped = " drop!";
                    }
                    if (dropOrNot.equals("Your phone has been dropped!")) {
                        for (int i = 1; i <= 8; i++) {
                            if (temp.get(i+20) > 2) {
                                isDropped = " thrown!";
                                dropOrNot = "Your phone has been thrown!";
                                break;
                            }
                            isDropped = " drop!";
                            dropOrNot = "Your phone has been dropped!";
                        }
                    }
                    sensorManagerAccelerometer.unregisterListener(this);
                    if (getApplicationContext() != null) {
                        ResultView resultView = new ResultView(HomePageActivity.this, dropOrNot, HomePageActivity.this);
                        Objects.requireNonNull(resultView.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        resultView.show();
                    }
                    if (dropOrNot.equals("Your phone has been dropped!")) {
                        GMailSender sender = new GMailSender(email.getText().toString(), "Fall Detected", "Hi, this email is to inform you that your phone " + getDeviceName() + "has been" + isDropped);
                        sender.execute();
                    }
//                    FallDetectorModel model = new FallDetectorModel(getDeviceName(), currentDate.toString(), "Fall");
//                    fallDetectorModel.generateReport(model);
                    return;
                }
                counter = 0;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onFall() {
        fallingValues = new ArrayList<>();
        startFreeFallIndex = 0;
        counter = 0;
        beenFreeFall = false;
        isFalling = false;
        sensorManagerAccelerometer.registerListener(HomePageActivity.this, accelerometorSensor, SensorManager.SENSOR_DELAY_FASTEST);
        Intent intent = new Intent(HomePageActivity.this, FallDetectorSensor.class);
        intent.putExtra("stop", "");
        getApplicationContext().startForegroundService(intent);
    }
}