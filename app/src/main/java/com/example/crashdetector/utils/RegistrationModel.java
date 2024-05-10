package com.example.crashdetector.utils;

import android.net.Uri;

import androidx.annotation.NonNull;

import com.example.crashdetector.ui.registration.IRegistrationPage;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Objects;

public class RegistrationModel {
    private FirebaseAuth auth;
    private DatabaseReference reference;
    private StorageReference storageReference;

    private IRegistrationPage iRegistrationPage;
    private boolean isDone = false;
    private String username, email, firstName, middleName, lastName, imageUrl;
    private int time;
    public RegistrationModel() {

    }

    public RegistrationModel(String username, String email, String firstName, String middleName, String lastName, String imageUrl, int time) {
        this.username = username;
        this.email = email;
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
        this.time = time;
        this.imageUrl = imageUrl;
    }

    public RegistrationModel(IRegistrationPage iRegistrationPage) {
        auth = FirebaseAuth.getInstance();
        reference = FirebaseDatabase.getInstance().getReference("users");
        storageReference = FirebaseStorage.getInstance().getReference();
        this.iRegistrationPage = iRegistrationPage;
    }


    public void hasUser(String toValid, String value) {
        isDone = false;
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot user: snapshot.getChildren()) {
                    if (Objects.equals(user.child(toValid).getValue(String.class), value)){
                        iRegistrationPage.hasUser(true);
                        isDone = true;
                        break;
                    }
                }
                if (!isDone) {
                    iRegistrationPage.hasUser(false);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                iRegistrationPage.hasUser(true);
            }
        });
    }
    public void createNewUser(RegistrationModel model, String password, Uri imageUri) {
        auth.createUserWithEmailAndPassword(model.getEmail(), password).addOnSuccessListener(task -> {
            storageReference = storageReference.child("Users/").child(task.getUser().getUid());
            UploadTask uploadTask = storageReference.putFile(imageUri);
            Task<Uri> urlTask = uploadTask.continueWithTask(task1 -> {
                if (!task1.isSuccessful()) {
                    throw Objects.requireNonNull(task1.getException());
                }

                return storageReference.getDownloadUrl();
            }).addOnCompleteListener(task12 -> {
                if (task12.isSuccessful()) {
                    Uri downloadUri = task12.getResult();
                    UserProfileChangeRequest.Builder builder = new UserProfileChangeRequest.Builder();
                    builder.setPhotoUri(downloadUri);
                    builder.setDisplayName(model.getUsername());
                    auth.getCurrentUser().updateProfile(builder.build()).addOnSuccessListener(unused -> {
                        model.setImageUrl(downloadUri.toString());
                        reference.child(task.getUser().getUid()).setValue(model).addOnSuccessListener(unused1 -> iRegistrationPage.onCreateNewUser(true, "Adding new employee complete!")).addOnFailureListener(e -> iRegistrationPage.onCreateNewUser(false, e.getLocalizedMessage()));
                    }).addOnFailureListener(e -> iRegistrationPage.onCreateNewUser(false, e.getLocalizedMessage()));
                } else {
                    iRegistrationPage.onCreateNewUser(false, "An error occurred, please try again later");
                }
            });

        }).addOnFailureListener(e -> iRegistrationPage.onCreateNewUser(false, e.getLocalizedMessage()));
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getImageUrl() {
        return imageUrl;
    }
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
