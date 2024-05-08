package com.example.crashdetector.ui.registration;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.crashdetector.R;
import com.example.crashdetector.ui.customview.PermissionView;
import com.example.crashdetector.ui.customview.ResultView;
import com.example.crashdetector.ui.homepage.HomePageActivity;
import com.example.crashdetector.utils.RegistrationModel;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.Objects;

public class RegistrationActivity extends AppCompatActivity implements IRegistrationPage{

    private ArrayList<LinearLayout> prompts;
    private TextView headerPrompt;
    private int currentLevel = 0;
    private ProgressBar progressBar;
    private EditText nameET, middleNameET, surnameET, usernameET, emailET, passwordET;
    private String finalName = "", finalMiddleName = "", finalSurname = "", finalUsername = "", finalEmail = "", finalPassword = "";
    private Button navButton;
    private boolean isStart = true, isValid = false, isValidUsername = false;
    private static final String REGEX_EMAIL = "^[a-z0-9](\\.?[a-z0-9]){3,}@g(oogle)?mail\\.com$";
    private RegistrationModel model;
    private TextView fileName;
    private ImageView profilePic;
    private Uri finalUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        MaterialToolbar toolbar = findViewById(R.id.materialToolbar);
        progressBar = findViewById(R.id.progressBar);
        model = new RegistrationModel(this);
        LinearLayout namePrompt = findViewById(R.id.namePrompt);
        LinearLayout credentialsPrompt = findViewById(R.id.credentialsPrompt);
        LinearLayout picturePrompt = findViewById(R.id.picturePrompt);
        LinearLayout imageSelector = findViewById(R.id.imageSelector);

        nameET = findViewById(R.id.nameET);
        middleNameET = findViewById(R.id.middleNameET);
        surnameET = findViewById(R.id.surnameET);
        headerPrompt = findViewById(R.id.headerPrompt);
        emailET = findViewById(R.id.emailET);
        usernameET = findViewById(R.id.usernameET);
        passwordET = findViewById(R.id.passwordET);
        fileName = findViewById(R.id.fileName);
        profilePic = findViewById(R.id.profilePic);

        prompts = new ArrayList<>();
        prompts.add(namePrompt);
        prompts.add(credentialsPrompt);
        prompts.add(picturePrompt);


        // clicking next
        navButton = findViewById(R.id.navButton);
        navButton.setOnClickListener(view -> {
            if (isStart){
                progressLayout(prompts.get(currentLevel), 1);
                isStart = false;
                headerPrompt.setText(getString(R.string.name_prompt));
                navButton.setText(getString(R.string.next));
                return;
            }

            if (isCorrect()){
                if (currentLevel >= 2) {
                    progressBar.setVisibility(View.VISIBLE);
                    finalUsername = usernameET.getText().toString();
                    finalEmail = emailET.getText().toString();
                    finalPassword = passwordET.getText().toString();
                    isValid = true;
                    RegistrationModel registrationPageModel = new RegistrationModel(finalUsername, finalEmail, finalName, finalMiddleName, finalSurname);
                    model.createNewUser(registrationPageModel, finalPassword, finalUri);
                    return;
                }
                progress();
            }

        });

        toolbar.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(RegistrationActivity.this);
            builder.setTitle("Warning Notice")
                    .setMessage("Are you sure you want to go back to sign in page?")
                    .setPositiveButton("Yes", (dialogInterface, i) -> finish())
                    .setNegativeButton("No", (dialogInterface, i) -> dialogInterface.dismiss())
                    .create();

            builder.show();
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (currentLevel > 0){
                    progressLayout(prompts.get(currentLevel), 0);
                    currentLevel--;
                    String levelText = ((currentLevel + 1) * 20) + "%";
                    progressLayout(prompts.get(currentLevel), 1);
                    navButton.setText(getString(R.string.next));
                    if (currentLevel == 2) {
                        navButton.setText(getString(R.string.validate));
                        isValidUsername = false;
                        isValid = false;
                    }
                    return;
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(RegistrationActivity.this);
                builder.setTitle("Warning Notice")
                        .setMessage("Are you sure you want to go back to sign in page?")
                        .setPositiveButton("Yes", (dialogInterface, i) -> finish())
                        .setNegativeButton("No", (dialogInterface, i) -> dialogInterface.dismiss())
                        .create();

                builder.show();
            }
        });



        ActivityResultLauncher<PickVisualMediaRequest> pickMedia =
                registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                    // Callback is invoked after the user selects a media item or closes the
                    // photo picker.
                    if (uri != null) {
                        Glide.with(RegistrationActivity.this)
                                .load(uri)
                                .centerCrop()
                                .circleCrop()
                                .into(profilePic);
                        fileName.setText(uri.getPath());
                        finalUri = uri;
                        int flag = Intent.FLAG_GRANT_READ_URI_PERMISSION;
                        RegistrationActivity.this.getContentResolver().takePersistableUriPermission(finalUri, flag);
                    } else {
                        Toast.makeText(this, "No image is selected.", Toast.LENGTH_SHORT).show();
                    }
                });

        imageSelector.setOnClickListener(view -> pickMedia.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build()));
    }

    private void progressLayout(LinearLayout layout, int weight) {
        layout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, weight));
    }

    public void hideSoftKeyboard(View view){
        InputMethodManager imm =(InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
    private void progress() {
        progressLayout(prompts.get(currentLevel), 0);
        currentLevel += 1;
        progressLayout(prompts.get(currentLevel), 1);
    }

    private boolean isCorrect(){
        switch (currentLevel) {
            case 0:
                if (finalName.isEmpty() && finalSurname.isEmpty()) {
                    if (nameET.getText().toString().isEmpty()){
                        nameET.setError("Please add your given name first");
                        nameET.requestFocus();
                        return false;
                    }
                    if (surnameET.getText().toString().isEmpty()){
                        surnameET.setError("Please add your surname first");
                        surnameET.requestFocus();
                        return false;
                    }
                    finalName = nameET.getText().toString();
                    finalMiddleName = middleNameET.getText().toString();
                    finalSurname = surnameET.getText().toString();
                }
                hideSoftKeyboard(surnameET);
                break;

            case 1:
                if (finalEmail.isEmpty() && finalPassword.isEmpty()){
                    if (usernameET.getText().toString().isEmpty()){
                        usernameET.setError("Username cannot be empty!");
                        usernameET.requestFocus();
                        return false;
                    }
                    if (usernameET.getText().toString().length() < 6){
                        usernameET.requestFocus();
                        usernameET.setError("Please enter at least 6 characters");
                        return false;
                    }
                    if (emailET.getText().toString().isEmpty()){
                        emailET.setError("Please add your email address first");
                        emailET.requestFocus();
                        return false;
                    }
                    if (!emailET.getText().toString().matches(REGEX_EMAIL)){
                        emailET.requestFocus();
                        emailET.setError("Please enter valid email address");
                        return false;
                    }

                    if (passwordET.getText().toString().isEmpty()){
                        passwordET.setError("Please add your password first");
                        passwordET.requestFocus();
                        return false;
                    }
                    if (passwordET.getText().toString().length() < 8){
                        passwordET.setError("Please add at least 8 character password");
                        passwordET.requestFocus();
                        return false;
                    }
                    hideSoftKeyboard(passwordET);
                    if (!isValid) {
                        progressBar.setVisibility(View.VISIBLE);
                        model.hasUser("username", usernameET.getText().toString());
                        return false;
                    }
                }
                break;

            case 2:
                if (finalUri == null) {
                    PermissionView customAlertDialogActivity = new PermissionView(RegistrationActivity.this, "Sorry but you have to choose a profile picture to continue.");
                    Objects.requireNonNull(customAlertDialogActivity.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    customAlertDialogActivity.show();
                    return false;
                }
                break;

        }

        return true;

    }

    @Override
    public void hasUser(boolean verdict) {
        progressBar.setVisibility(View.GONE);
        if (!isValidUsername) {
            if (verdict){
                AlertDialog.Builder builder = new AlertDialog.Builder(RegistrationActivity.this);
                builder.setTitle("Warning Notice")
                        .setMessage("Sorry but the provided username is currently in use, please change the username or forgot the password in login page if " +
                                "you forgot it.")
                        .setPositiveButton("Okay", (dialogInterface, i) -> dialogInterface.dismiss())
                        .create();

                builder.show();
                return;
            }
            isValidUsername = true;
            model.hasUser("email", emailET.getText().toString());
            return;
        }
        if (verdict){
            AlertDialog.Builder builder = new AlertDialog.Builder(RegistrationActivity.this);
            builder.setTitle("Warning Notice")
                    .setMessage("Sorry but the provided email address is currently a user, please change the email address or forgot the password in login page if " +
                            "you forgot it.")
                    .setPositiveButton("Okay", (dialogInterface, i) -> dialogInterface.dismiss())
                    .create();

            builder.show();
            isValidUsername = false;
            return;
        }
        Toast.makeText(this, "Thank you for validating, you can now continue!", Toast.LENGTH_SHORT).show();
        isValid = true;
        navButton.setText(getString(R.string.create));
        progress();
    }

    @Override
    public void onCreateNewUser(boolean verdict, String localizedMessage) {
        progressBar.setVisibility(View.GONE);
        if (verdict) {
            startActivity(new Intent(getApplicationContext(), HomePageActivity.class));
            finish();
            return;
        }
        Toast.makeText(this, "Sorry " + localizedMessage, Toast.LENGTH_SHORT).show();
    }
}