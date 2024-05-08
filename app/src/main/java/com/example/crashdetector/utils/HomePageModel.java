package com.example.crashdetector.utils;

import android.net.Uri;

import com.example.crashdetector.ui.homepage.IHomePage;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class HomePageModel {
    private String name, email;
    private FirebaseAuth auth;
    private Uri imageUri;
    private IHomePage iHomePageActivity;
    private DatabaseReference databaseReference;

    public HomePageModel(Uri imageUri, String name, String email) {
        this.imageUri = imageUri;
        this.name = name;
        this.email = email;
    }
    public HomePageModel(IHomePage iHomePageActivity) {
        auth = FirebaseAuth.getInstance();
        this.iHomePageActivity = iHomePageActivity;
        databaseReference = FirebaseDatabase.getInstance().getReference("users").child(Objects.requireNonNull(auth.getCurrentUser()).getUid()).child("informations");
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public Uri getImageUri() {
        return imageUri;
    }

    public void getInformation() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            Uri uri = user.getPhotoUrl();
            String name = user.getDisplayName();
            String email = user.getEmail();
            HomePageModel model = new HomePageModel(uri, name, email);
            iHomePageActivity.onGetUserData(model);
        }
    }
    public void logout() {
        auth.signOut();
    }

}
