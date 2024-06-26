package com.example.crashdetector.ui.homepage.fragments;

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
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.crashdetector.R;
import com.example.crashdetector.ui.customview.ResultView;
import com.example.crashdetector.ui.homepage.fragments.configs.GMailSender;
import com.example.crashdetector.ui.services.FallDetectorSensor;
import com.example.crashdetector.ui.services.LeftPhoneService;
import com.example.crashdetector.utils.FallDetectorModel;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class FallDetector extends Fragment implements SensorEventListener, IFallDetector {
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

    private float mAccelCurrent;
    private int counter = 0;
    private boolean beenFreeFall = false;
    private int startFreeFallIndex = 0;
    private Sensor accelerometorSensor;
    private String email;
    private FallDetectorModel fallDetectorModel;
    public FallDetector(String email) {
        this.email = email;
    }
    private boolean isLeftPhoneRunning = false;
    @RequiresApi(api = Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_fall_detector, container, false);
        mAccelCurrent = SensorManager.GRAVITY_EARTH;

        String cur = LocalTime.now().toString();
        LocalTime time = LocalTime.parse(cur);
//        fallDetectorModel = new FallDetectorModel(this);
        if (getContext() != null) {
            context = getContext();
        }
        if (getActivity() != null) {
            activity = getActivity();
        }

        sensorManagerAccelerometer = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        accelerometorSensor = sensorManagerAccelerometer.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        SwitchMaterial leftPhoneSwitch = view.findViewById(R.id.leftPhoneSwitch);
        SwitchMaterial accelerometerSwitch = view.findViewById(R.id.accelerometerSwitch);

        xAxis = view.findViewById(R.id.xAxis);
        yAxis = view.findViewById(R.id.yAxis);
        zAxis = view.findViewById(R.id.zAxis);
        TextView accelerometerWarning = view.findViewById(R.id.warningAccelerometer);
        TextView leftPhoneWarning = view.findViewById(R.id.leftPhoneWarning);
        fallingValues = new ArrayList<>();

        if (accelerometorSensor != null) {
            sensorManagerAccelerometer.registerListener(FallDetector.this, accelerometorSensor, SensorManager.SENSOR_DELAY_FASTEST);
            Intent intent = new Intent(getActivity(), FallDetectorSensor.class);
            intent.putExtra("email", email);
            context.startForegroundService(intent);
            if (!isBetween(time, LocalTime.of(20, 0, 0), LocalTime.of(8, 0, 0))) {
                Intent intent1 = new Intent(getActivity(), LeftPhoneService.class);
                intent1.putExtra("email", email);
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
                if (isServiceRunningInForeground(getContext(), FallDetectorSensor.class)) {
                    Intent intent = new Intent(getActivity(), FallDetectorSensor.class);
                    intent.putExtra("off", "");
                    context.startForegroundService(intent);
                    sensorManagerAccelerometer.unregisterListener(FallDetector.this);
                    accelerometerWarning.setVisibility(View.VISIBLE);
                    accelerometerWarning.setText(activity.getString(R.string.accelerometer_off));
                    return;
                }
            }
            sensorManagerAccelerometer.registerListener(FallDetector.this, accelerometorSensor, SensorManager.SENSOR_DELAY_FASTEST);
            accelerometerWarning.setVisibility(View.GONE);
            Intent intent = new Intent(getActivity(), FallDetectorSensor.class);
            intent.putExtra("email", email);
            context.startForegroundService(intent);
        });

        leftPhoneSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!isChecked) {
                if (isServiceRunningInForeground(getContext(), LeftPhoneService.class)) {
                    Intent intent = new Intent(getActivity(), LeftPhoneService.class);
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
            }
            else {
                if (!isServiceRunningInForeground(getContext(), LeftPhoneService.class) && !isLeftPhoneRunning) {
                    Log.i("TAGERISTA", "onCreateView: ");
                    leftPhoneWarning.setVisibility(View.GONE);
                    Intent intent = new Intent(getActivity(), LeftPhoneService.class);
                    intent.putExtra("email", email);
                    context.startForegroundService(intent);
                    isLeftPhoneRunning = true;
                }
            }
        });
        return view;
    }

    public static boolean isBetween(LocalTime candidate, LocalTime start, LocalTime end) {
        return candidate.isAfter(start) || candidate.isBefore(end);
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
                    if (getActivity() != null && getContext() != null) {
                        ResultView resultView = new ResultView(context, dropOrNot, FallDetector.this);
                        Objects.requireNonNull(resultView.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        resultView.show();
                    }
                    GMailSender sender = new GMailSender(email, "Fall Detected", "Hi, this email is to inform you that your phone " + getDeviceName() + "has been" + isDropped);
                    sender.execute();
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
        sensorManagerAccelerometer.registerListener(FallDetector.this, accelerometorSensor, SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    public void onGenerateReport(String message) {
        GMailSender sender = new GMailSender(email, "Fall Detected", "Hi, this email is to inform you that your phone " + getDeviceName() + " has been drop.");
        sender.execute();
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();

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
}