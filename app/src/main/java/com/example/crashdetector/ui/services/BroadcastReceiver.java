package com.example.crashdetector.ui.services;

import android.content.Context;
import android.content.Intent;

import java.util.Objects;

public class BroadcastReceiver extends android.content.BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Objects.equals(intent.getAction(), Intent.ACTION_BOOT_COMPLETED)) {
            Intent serviceIntent = new Intent(context, FallDetectorSensor.class);
            context.startForegroundService(serviceIntent);
        }
    }
}
