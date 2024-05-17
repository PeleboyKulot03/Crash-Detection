package com.example.crashdetector.utils;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.crashdetector.ui.homepage.IHomePage;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Objects;

public class HomePageModel {
    private String name, email;
    private FirebaseAuth auth;
    private Uri imageUrl;
    private IHomePage iHomePageActivity;
    private DatabaseReference databaseReference;
    private long time= 0;

    public HomePageModel() {}
    public HomePageModel(Uri imageUrl, String name, String email) {
        this.imageUrl = imageUrl;
        this.name = name;
        this.email = email;
    }
    public HomePageModel(IHomePage iHomePageActivity) {
        auth = FirebaseAuth.getInstance();
        this.iHomePageActivity = iHomePageActivity;
        databaseReference = FirebaseDatabase.getInstance().getReference("users").child(Objects.requireNonNull(auth.getCurrentUser()).getUid());
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public Uri getImageUri() {
        return imageUrl;
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
    public void getModels() {
        HashMap<String, String> models = new HashMap<>();
        databaseReference.child("models").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot model: snapshot.getChildren()) {
                    String key = model.getKey();
                    String value = model.child("lastLoc").getValue(String.class);
                    models.put(key, value);
                }
                iHomePageActivity.onGetModels(models);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                iHomePageActivity.onGetModels(null);
            }
        });
    }

    public long getTime() {
        return time;
    }

    public void setNewTime(int i) {
        databaseReference.child("time").setValue(i);
    }
}
