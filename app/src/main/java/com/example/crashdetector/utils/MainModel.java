package com.example.crashdetector.utils;

import com.example.crashdetector.ui.main.IMain;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainModel {
    private final FirebaseUser user;
    private final IMain iMainActivity;
    public MainModel(IMain iMainActivity) {
        user = FirebaseAuth.getInstance().getCurrentUser();
        this.iMainActivity = iMainActivity;
    }

    public void isSignedIn() {
        if (user == null) {
            iMainActivity.isSignedIn(false);
            return;
        }
        iMainActivity.isSignedIn(true);
    }
}
