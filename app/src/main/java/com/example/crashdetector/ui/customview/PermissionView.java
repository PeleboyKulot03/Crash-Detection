package com.example.crashdetector.ui.customview;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.crashdetector.R;

public class PermissionView extends Dialog {
    private final String text;
    private Activity activity;
    public PermissionView(@NonNull Context context, String text) {
        super(context);
        this.text = text;
        this.activity = (Activity) context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_result_view);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        TextView body = findViewById(R.id.body);
        Button button = findViewById(R.id.okay);


        body.setText(text);
        button.setOnClickListener(v -> {
            dismiss();
            activity.finish();
        });
    }
}