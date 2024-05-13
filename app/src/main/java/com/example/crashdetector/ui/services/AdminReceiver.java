package com.example.crashdetector.ui.services;

import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import com.example.crashdetector.R;
import com.example.crashdetector.ui.homepage.fragments.configs.GMailSender;

public class AdminReceiver extends DeviceAdminReceiver {
    private int counter = 0;
    @Override
    public void onEnabled(Context ctxt, Intent intent) {
        ComponentName cn=new ComponentName(ctxt, AdminReceiver.class);
        DevicePolicyManager mgr=
                (DevicePolicyManager)ctxt.getSystemService(Context.DEVICE_POLICY_SERVICE);

        mgr.setPasswordQuality(cn,
                DevicePolicyManager.PASSWORD_QUALITY_ALPHANUMERIC);

        onPasswordChanged(ctxt, intent);
    }


    @Override
    public void onPasswordFailed(Context ctxt, Intent intent) {
        SharedPreferences sh = ctxt.getSharedPreferences("MySharedPref", Context.MODE_PRIVATE);
        String email = sh.getString("name", "");
        String location = sh.getString("location", "");
        DevicePolicyManager mgr=
                (DevicePolicyManager)ctxt.getSystemService(Context.DEVICE_POLICY_SERVICE);

        if (mgr.getCurrentFailedPasswordAttempts() >= 3) {
            GMailSender sender = new GMailSender(email, "Snatch Detector", "Hi, this email is to inform you that your phone " + getDeviceName()  +" might be stolen. Last location it was seen was in " + location);
            sender.execute();
        }
    }


    @Override
    public void onPasswordSucceeded(Context ctxt, Intent intent) {
        Toast.makeText(ctxt, R.string.password_success, Toast.LENGTH_LONG)
                .show();
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
