package com.example.crashdetector.ui.customview;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.crashdetector.R;
import com.example.crashdetector.ui.homepage.fragments.IFallDetector;

public class ResultView extends Dialog {
    private final String text;
    private IFallDetector iFallDetector;
    public ResultView(@NonNull Context context, String text, IFallDetector iFallDetector) {
        super(context);
        this.text = text;
        this.iFallDetector = iFallDetector;
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
            iFallDetector.onFall();
            dismiss();
        });
    }
}