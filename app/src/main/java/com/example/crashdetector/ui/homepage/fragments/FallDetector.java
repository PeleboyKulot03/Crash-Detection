package com.example.crashdetector.ui.homepage.fragments;

import android.app.Activity;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.example.crashdetector.R;
import com.example.crashdetector.ui.customview.ResultView;
import com.example.crashdetector.ui.services.FallDetectorSensor;
import com.google.android.material.switchmaterial.SwitchMaterial;
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
    private float mAccelCurrent;
    private int counter = 0;
    private boolean beenFreeFall = false;
    private int startFreeFallIndex = 0;
    private Sensor accelerometorSensor;

    @RequiresApi(api = Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_fall_detector, container, false);
        mAccelCurrent = SensorManager.GRAVITY_EARTH;
        if (getContext() != null) {
            context = getContext();
        }
        if (getActivity() != null) {
            activity = getActivity();
        }

        Intent intent = new Intent(getActivity(), FallDetectorSensor.class);
        context.startForegroundService(intent);

        sensorManagerAccelerometer = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        SensorManager sensorManagerGyroscope = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        accelerometorSensor = sensorManagerAccelerometer.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Sensor gyroscopeSensor = sensorManagerGyroscope.getDefaultSensor(Sensor.TYPE_MOTION_DETECT);

        SwitchMaterial gyroscopeSwitch = view.findViewById(R.id.gyroscopeSwitch);
        SwitchMaterial accelerometerSwitch = view.findViewById(R.id.accelerometerSwitch);

        xAxis = view.findViewById(R.id.xAxis);
        yAxis = view.findViewById(R.id.yAxis);
        zAxis = view.findViewById(R.id.zAxis);
        TextView accelerometerWarning = view.findViewById(R.id.warningAccelerometer);
        TextView gyroscopeWarning = view.findViewById(R.id.warningGyroscope);
        fallingValues = new ArrayList<>();

        if (accelerometorSensor != null) {
            sensorManagerAccelerometer.registerListener(FallDetector.this, accelerometorSensor, SensorManager.SENSOR_DELAY_FASTEST);
        }
        else {
            accelerometerWarning.setVisibility(View.VISIBLE);
            accelerometerSwitch.setChecked(false);
            accelerometerSwitch.setEnabled(false);
        }
        if (gyroscopeSensor != null) {
            sensorManagerGyroscope.registerListener(new SensorEventListener() {
                @Override
                public void onSensorChanged(SensorEvent event) {
                    gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
                    gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
                    gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];

                    linear_acceleration[0] = event.values[0] - gravity[0];
                    linear_acceleration[1] = event.values[1] - gravity[1];
                    linear_acceleration[2] = event.values[2] - gravity[2];

                    activity.runOnUiThread(() -> {
                        String xAxisText = String.format(Locale.getDefault(), "%.2f", (linear_acceleration[0]) > 0.99 ? linear_acceleration[0]:0) + " m/s²";
                        String yAxisText = String.format(Locale.getDefault(), "%.2f", (linear_acceleration[1]) > 0.99 ? linear_acceleration[1]:0) + " m/s²";
                        String zAxisText = String.format(Locale.getDefault(), "%.2f", (linear_acceleration[2]) > 0.99 ? linear_acceleration[2]:0) + " m/s²";
                        xAxis.setText(xAxisText);
                        yAxis.setText(yAxisText);
                        zAxis.setText(zAxisText);
                    });
                }

                @Override
                public void onAccuracyChanged(Sensor sensor, int accuracy) {

                }
            }, gyroscopeSensor, Sensor.REPORTING_MODE_CONTINUOUS);
        }
        else {
            gyroscopeWarning.setVisibility(View.VISIBLE);
            gyroscopeSwitch.setEnabled(false);
            gyroscopeSwitch.setChecked(false);
        }

        accelerometerSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!isChecked) {
                fallingValues = new ArrayList<>();
                startFreeFallIndex = 0;
                counter = 0;
                beenFreeFall = false;
                isFalling = false;
                sensorManagerAccelerometer.unregisterListener(FallDetector.this);
                accelerometerWarning.setVisibility(View.VISIBLE);
                accelerometerWarning.setText(activity.getString(R.string.accelerometer_off));
                return;
            }
            sensorManagerAccelerometer.registerListener(FallDetector.this, accelerometorSensor, SensorManager.SENSOR_DELAY_FASTEST);
            accelerometerWarning.setVisibility(View.GONE);
        });
        return view;
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
                            break;
                        }
                        dropOrNot = "Your phone has been dropped!";
                    }
                    if (dropOrNot.equals("Your phone has been dropped!")) {
                        for (int i = 1; i <= 8; i++) {
                            if (temp.get(i+20) > 2) {
                                dropOrNot = "Your phone has been thrown!";
                                break;
                            }
                            dropOrNot = "Your phone has been dropped!";
                        }
                    }
                    sensorManagerAccelerometer.unregisterListener(this);
                    if (getActivity() != null && getContext() != null) {
                        ResultView resultView = new ResultView(context, dropOrNot, FallDetector.this);
                        Objects.requireNonNull(resultView.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        resultView.show();
                    }
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
}