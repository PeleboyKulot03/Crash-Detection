package com.example.crashdetector.utils;

import androidx.annotation.NonNull;

import com.example.crashdetector.ui.homepage.fragments.FallDetector;
import com.example.crashdetector.ui.homepage.fragments.IFallDetector;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FallDetectorModel {
    private String deviceName;
    private String dateTime;
    private String type;
    private DatabaseReference reference;
    private IFallDetector iFallDetector;
    private FirebaseUser user;
    public FallDetectorModel() {

    }
    public FallDetectorModel(IFallDetector iFallDetector) {
        this.iFallDetector = iFallDetector;
        user = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("users");
    }
    public FallDetectorModel(String deviceName, String dateTime, String type) {
        this.deviceName = deviceName;
        this.dateTime = dateTime;
        this.type = type;
    }

    public void generateReport(FallDetectorModel model) {
        reference.child(user.getUid()).child("reports").push().setValue(model).addOnSuccessListener(unused -> iFallDetector.onGenerateReport("Report Generated Successfully!")).addOnFailureListener(e -> iFallDetector.onGenerateReport(e.getLocalizedMessage()));
    }

}
