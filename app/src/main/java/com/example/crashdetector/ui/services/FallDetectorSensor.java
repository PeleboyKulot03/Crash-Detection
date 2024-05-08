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
import com.example.crashdetector.ui.homepage.fragments.GMailSender;

import java.util.ArrayList;
import java.util.List;

public class FallDetectorSensor extends Service implements SensorEventListener{
    private static final Float THRESHOLD = 15.0f;
    private static final int reqCode = 1;
    private static final String CHANNEL_ID = "channel_name";
    private SensorManager sensorManagerAccelerometer;
    private boolean isFalling = false;
    private ArrayList<Float> fallingValues;
    private String dropOrNot = "";
    private float mAccelCurrent;
    private int counter = 0;
    private boolean beenFreeFall = false;
    private int startFreeFallIndex = 0;
    private Sensor accelerometorSensor;
    private String email;
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            return super.onStartCommand(intent, flags, startId);
        }
        if (intent.hasExtra("off")) {
            stopForeground(true);
            stopSelfResult(startId);
            return super.onStartCommand(intent, flags, startId);
        }

        if (intent.hasExtra("email")) {
            email = intent.getStringExtra("email");
        }
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
        fallingValues = new ArrayList<>();

        sensorManagerAccelerometer = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
        accelerometorSensor = sensorManagerAccelerometer.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);


        mAccelCurrent = SensorManager.GRAVITY_EARTH;
        if (accelerometorSensor != null) {
            sensorManagerAccelerometer.registerListener(this, accelerometorSensor, SensorManager.SENSOR_DELAY_FASTEST);
        }
        else {
            Intent newIntent = new Intent(getApplicationContext(), HomePageActivity.class);
            showNotification(getApplicationContext(), "Warning Notice", "Sorry but an error occured while using your accelerometer", newIntent, reqCode);
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
            float[] mGravity = event.values.clone();
            // Shake detection
            float x = mGravity[0];
            float y = mGravity[1];
            float z = mGravity[2];
            mAccelCurrent = (float) Math.sqrt(x*x + y*y + z*z);

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
                    Intent intent = new Intent(getApplicationContext(), HomePageActivity.class);
                    showNotification(this, "Warning Notice", dropOrNot, intent, reqCode);
                    fallingValues = new ArrayList<>();
                    startFreeFallIndex = 0;
                    counter = 0;
                    beenFreeFall = false;
                    isFalling = false;
                    sensorManagerAccelerometer.registerListener(this, accelerometorSensor, SensorManager.SENSOR_DELAY_FASTEST);
                    GMailSender sender = new GMailSender(email, "Fall Detected", "Hi, this email is to inform you that your phone " + getDeviceName() + " has been drop.");
                    sender.execute();
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
