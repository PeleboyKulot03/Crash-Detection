package com.example.crashdetector.ui.homepage.fragments;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.ServiceCompat;

import com.example.crashdetector.R;
import com.example.crashdetector.ui.customview.ResultView;
import com.example.crashdetector.ui.homepage.HomePageActivity;
import com.example.crashdetector.ui.report.ReportActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class ExampleService extends Service implements SensorEventListener{
    private static final Float THRESHOLD = 15.0f;
    private static final String CHANNEL_ID = "channel_name";
    private boolean isOn = true;
    private SensorManager sensorManagerAccelerometer;
    private boolean isFalling = false;
    private ArrayList<Float> fallingValues;
    private String dropOrNot = "";
    private float[] mGravity;
    private float mAccel;
    private float mAccelCurrent;
    private float mAccelLast;
    private int counter = 0;
    private boolean beenFreeFall = false;
    private int startFreeFallIndex = 0;
    private ArrayList<Float> zValues;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        NotificationChannel chan = new NotificationChannel(
                "MyChannelId",
                "My Foreground Service",
                NotificationManager.IMPORTANCE_HIGH);
        chan.setLightColor(Color.BLUE);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(chan);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(
                this, "MyChannelId");
        Notification notification = notificationBuilder.setOngoing(true)
                .setSmallIcon(R.mipmap.logo)
                .setContentTitle("App is running on foreground")
                .setPriority(NotificationManager.IMPORTANCE_HIGH)
                .setCategory(Notification.CATEGORY_SERVICE)
                .setChannelId("MyChannelId")
                .build();

        startForeground( 100, notification);

        zValues = new ArrayList<>();
        fallingValues = new ArrayList<>();

        sensorManagerAccelerometer = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
        Sensor accelerometorSensor = sensorManagerAccelerometer.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);


        mAccel = 0.00f;
        mAccelCurrent = SensorManager.GRAVITY_EARTH;
        mAccelLast = SensorManager.GRAVITY_EARTH;
        if (accelerometorSensor != null) {
            sensorManagerAccelerometer.registerListener(this, accelerometorSensor, SensorManager.SENSOR_DELAY_FASTEST);
        }
        else {

        }


        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            mGravity = event.values.clone();
            // Shake detection
            float x = mGravity[0];
            float y = mGravity[1];
            float z = mGravity[2];
            mAccelLast = mAccelCurrent;
            mAccelCurrent = (float) Math.sqrt(x*x + y*y + z*z);

            Log.i("TAGERISTA", "onSensorChanged: " + mAccelCurrent);
            if (!beenFreeFall) {
                zValues.add(z);
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
//                    Log.i("TAGERISTA", "onSensorChanged: " + temp);
//                    Toast.makeText(context, dropOrNot, Toast.LENGTH_SHORT).show();
//                    Log.i("TAGERISTA", "onSensorChanged: " + temp1);
//                    Log.i("TAGERISTA", "onSensorChanged: " + dropOrNot);

//                    ResultView resultView = new ResultView(ExampleService.this, dropOrNot);
//                    Objects.requireNonNull(resultView.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//                    resultView.show();


                    int reqCode = 1;
                    Intent intent = new Intent(getApplicationContext(), ReportActivity.class);
                    showNotification(this, "Warning Notice", dropOrNot, intent, reqCode);
                    sensorManagerAccelerometer.unregisterListener(this);
                    return;
                }
                counter = 0;
            }

        }
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void showNotification(Context context, String title, String message, Intent intent, int reqCode) {

        PendingIntent pendingIntent = PendingIntent.getActivity(context, reqCode, intent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.logo)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            CharSequence name = "Accelerometer Channel";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
            notificationManager.createNotificationChannel(mChannel);
        }
        notificationManager.notify(reqCode, notificationBuilder.build());

        Log.d("showNotification", "showNotification: " + reqCode);
    }

}
