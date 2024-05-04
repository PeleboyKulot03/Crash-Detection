package com.example.crashdetector.ui.homepage.fragments;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.crashdetector.R;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.Locale;

public class FallDetector extends Fragment {
    private Context context;
    private Activity activity;
    final float alpha = 0.8f;
    private float[] gravity = {0, 0, 0};
    private float[] linear_acceleration = {0, 0, 0};


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_fall_detector, container, false);
        if (getContext() != null) {
            context = getContext();
        }
        if (getActivity() != null) {
            activity = getActivity();
        }
        SensorManager sensorManagerAccelerometer = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        SensorManager sensorManagerGyroscope = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        Sensor accelerometorSensor = sensorManagerAccelerometer.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Sensor gyroscopeSensor = sensorManagerGyroscope.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        SwitchMaterial gyroscopeSwitch = view.findViewById(R.id.gyroscopeSwitch);
        SwitchMaterial accelerometerSwitch = view.findViewById(R.id.accelerometerSwitch);

        TextView xAxis = view.findViewById(R.id.xAxis);
        TextView yAxis = view.findViewById(R.id.yAxis);
        TextView zAxis = view.findViewById(R.id.zAxis);
        TextView accelerometerWarning = view.findViewById(R.id.warningAccelerometer);
        TextView gyroscopeWarning = view.findViewById(R.id.warningGyroscope);

        if (accelerometorSensor != null) {
            sensorManagerAccelerometer.registerListener(new SensorEventListener() {
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
            }, accelerometorSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
        else {
            accelerometerWarning.setVisibility(View.VISIBLE);
        }
        if (gyroscopeSensor != null) {
            sensorManagerAccelerometer.registerListener(new SensorEventListener() {
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
            }, gyroscopeSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
        else {
            gyroscopeWarning.setVisibility(View.VISIBLE);
            gyroscopeSwitch.setEnabled(false);
            gyroscopeSwitch.setActivated(false);
        }
        return view;
    }
}