package com.example.crashdetector.ui.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.crashdetector.R;
import com.example.crashdetector.ui.homepage.HomePageActivity;

import java.util.ArrayList;
import java.util.List;

public class LeftPhoneService extends Service implements SensorEventListener {
    private static final int reqCode = 5;
    private static final String CHANNEL_ID = "LEFT_PHONE_CHANNEL";
    private float mAccelCurrent;
    private int counter = 0;
    private float mAccel;
    private float mAccelLast;


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.hasExtra("off")) {
            stopForeground(true);
            stopSelfResult(startId);
        }
        NotificationChannel chan = new NotificationChannel(
                "LEFT_PHONE",
                "My Left Phone Service",
                NotificationManager.IMPORTANCE_HIGH);
        chan.setLightColor(Color.BLUE);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(chan);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(
                this, "LEFT_PHONE");
        Notification notification = notificationBuilder.setOngoing(true)
                .setSmallIcon(R.mipmap.logo)
                .setContentTitle("Left Phone service is running on foreground")
                .setPriority(NotificationManager.IMPORTANCE_HIGH)
                .setCategory(Notification.CATEGORY_SERVICE)
                .setChannelId("LEFT_PHONE")
                .build();

        startForeground(200, notification);
        SensorManager sensorManagerAccelerometer = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
        Sensor accelerometorSensor = sensorManagerAccelerometer.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);


        mAccelCurrent = SensorManager.GRAVITY_EARTH;
        mAccel = 0.00f;
        mAccelLast = SensorManager.GRAVITY_EARTH;

        if (accelerometorSensor != null) {
            sensorManagerAccelerometer.registerListener(this, accelerometorSensor, SensorManager.SENSOR_DELAY_FASTEST);
        }
        else {
            Intent newIntent = new Intent(getApplicationContext(), HomePageActivity.class);
            showNotification(getApplicationContext(), "Warning Notice", "Sorry but an error occured while using your accelerometer", newIntent, reqCode);
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float[] mGravity = event.values.clone();

            float x = mGravity[0];
            float y = mGravity[1];
            float z = mGravity[2];
            mAccelLast = mAccelCurrent;
            mAccelCurrent = (float) Math.sqrt(x*x + y*y + z*z);
            float delta = mAccelCurrent - mAccelLast;
            mAccel = mAccel * 0.9f + delta;

            if (mAccel > 1){
                counter = 0;
            }
        }
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
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
            CharSequence name = "Left Phone Channel";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
            notificationManager.createNotificationChannel(mChannel);
        }
        notificationManager.notify(reqCode, notificationBuilder.build());
    }
}
