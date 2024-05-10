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
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.crashdetector.R;
import com.example.crashdetector.ui.homepage.HomePageActivity;
import com.example.crashdetector.ui.homepage.fragments.configs.GMailSender;

public class LeftPhoneService extends Service implements SensorEventListener {
    private static final int reqCode = 5;
    private static final String CHANNEL_ID = "LEFT_PHONE_CHANNEL";
    private float mAccelCurrent;
    private float mAccel;
    private float mAccelLast;
    private final long MAX_TIME = 3600000;
    private final long diff = 1000;

    private CountDownTimer firstCounter;
    private CountDownTimer secondCounter;
    private boolean isInSecond = false;
    private String email;
    private long time;
    private SensorManager sensorManagerAccelerometer;
    private Ringtone r;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        sensorManagerAccelerometer = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
        Sensor accelerometorSensor = sensorManagerAccelerometer.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (intent.hasExtra("off")) {
            Log.i("TAGERISTA", "onStartCommand: ");
            stopForeground(true);
            stopSelfResult(startId);
            stopSound();
            firstCounter.cancel();
            secondCounter.cancel();
            stopSelf();
            sensorManagerAccelerometer.unregisterListener(this);
            return START_NOT_STICKY;
        }
        if (intent.hasExtra("email")) {
            email = intent.getStringExtra("email");
        }
        if (intent.hasExtra("time")) {
            time = intent.getLongExtra("time", 0);
            time *= 60000;
        }
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (intent.hasExtra("remove")) {
            manager.cancel(200);
            Log.i("TAGERISTA", "onStartCommand: test");
        }
        Uri uri = Uri.parse("android.resource://"
                + getApplicationContext().getPackageName() + "/" + R.raw.siren);
        NotificationChannel chan = new NotificationChannel(
                "LEFT_PHONE",
                "My Left Phone Service",
                NotificationManager.IMPORTANCE_HIGH);

        chan.setLightColor(Color.BLUE);

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

        startForeground(100, notification);

        mAccelCurrent = SensorManager.GRAVITY_EARTH;
        mAccel = 0.00f;
        mAccelLast = SensorManager.GRAVITY_EARTH;

        if (accelerometorSensor != null) {
            sensorManagerAccelerometer.registerListener(this, accelerometorSensor, SensorManager.SENSOR_DELAY_FASTEST);
        }
        else {
            Intent newIntent = new Intent(getApplicationContext(), HomePageActivity.class);
            showNotification(getApplicationContext(), "Warning Notice", "Sorry but an error occurred while using your accelerometer", newIntent, reqCode);
        }
        secondCounter = new CountDownTimer(time / 2, diff) {
            public void onTick(long millisUntilFinished) {
                Log.i("TAGERISTA", "onTick: " + millisUntilFinished / 1000);
            }

            public void onFinish() {
                // TODO: SEND  EMAIL
                GMailSender sender = new GMailSender(email, "Left Phone Detector", "Hi, we notice an inactivity in with your phone. This email is to inform you that you might left your phone " + getDeviceName() + ".");
                sender.execute();
                sensorManagerAccelerometer.unregisterListener(LeftPhoneService.this);
            }
        };
        firstCounter = new CountDownTimer(time, diff) {
            public void onTick(long millisUntilFinished) {
                Log.i("TAGERISTA", "onTick: " + millisUntilFinished / 1000);
            }

            public void onFinish() {
                // TODO: SEND  EMAIL
                Log.i("TAGERISTA", "onFinish: are you awake?");
                Intent intentYes = new Intent(getApplicationContext(), HomePageActivity.class);
                intentYes.putExtra("remove", "");
                showNotification(getApplicationContext(), "Alert Notice", "Are you still using the phone?", intentYes, 200);
                secondCounter.start();
            }
        }.start();
        return START_NOT_STICKY;
    }

    public void onOff() {
        sensorManagerAccelerometer.unregisterListener(LeftPhoneService.this);
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

            if (mAccel > .5) {
                firstCounter.cancel();
                firstCounter.start();
                secondCounter.cancel();
                stopSound();
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
                .setContentIntent(pendingIntent)
                .setSound(null)
                .setSilent(true);
        playSound(getApplicationContext(), "C:\\Users\\Rose Ann Guarin\\StudioProjects\\Crash-Detection\\app\\src\\main\\res\\raw\\siren.mp3");

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            CharSequence name = "Left Phone Channel";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
            notificationManager.createNotificationChannel(mChannel);
        }
        notificationManager.notify(reqCode, notificationBuilder.build());
    }

    private void playSound(Context context, String SoundUri){
        Uri rawPathUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.siren1);
        r = RingtoneManager.getRingtone(context, rawPathUri);
        r.play();
    }

    private void stopSound() {
        if (r != null) {
            r.stop();
        }
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
