package com.example.crashdetector.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.example.crashdetector.R;
import com.example.crashdetector.ui.homepage.HomePageActivity;
import com.example.crashdetector.ui.registration.RegistrationActivity;
import com.example.crashdetector.utils.LoginModel;

public class LoginPage extends AppCompatActivity implements ILoginPage {
    private ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        progressBar = findViewById(R.id.progressBar);
        EditText usernameET = findViewById(R.id.usernameET);
        EditText passwordET = findViewById(R.id.passwordET);
        LoginModel model = new LoginModel(this);
        Button loginButton = findViewById(R.id.logIn);
        loginButton.setOnClickListener(v -> {
            progressBar.setVisibility(View.VISIBLE);
            String username = usernameET.getText().toString();
            String password = passwordET.getText().toString();
            model.signIn(username, password);
        });

        Button signUp = findViewById(R.id.signUp);
        signUp.setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(), RegistrationActivity.class));
            finish();
        });
    }

    @Override
    public void onLogin(boolean verdict, String message) {
        progressBar.setVisibility(View.GONE);
        if (verdict) {
            Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(getApplicationContext(), HomePageActivity.class));
            finish();
            return;
        }
        Toast.makeText(this, "Login Failed, " + message, Toast.LENGTH_SHORT).show();
    }
}