package com.example.crashdetector.ui.homepage.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.crashdetector.R;
import com.example.crashdetector.ui.services.FallDetectorSensor;
import com.example.crashdetector.ui.services.LeftPhoneService;

public class LeftPhoneFragment extends Fragment {

    private Context context;
    private Activity activity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        if (getContext() != null) {
            context = getContext();
        }
        if (getActivity() != null) {
            activity = getActivity();
        }
        Intent intent = new Intent(getActivity(), LeftPhoneService.class);
        context.startForegroundService(intent);


        return inflater.inflate(R.layout.fragment_left_phone, container, false);
    }
}