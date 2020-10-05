package com.example.test.sub;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.example.test.R;

public class SubActivityText extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sub_show_text);

        final Button logoutButton = findViewById(R.id.back);
        final TextView textView = findViewById(R.id.textView_sub);

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View view) {
                finish();
            }

        });
        Intent intent = this.getIntent();
        String text = intent.getStringExtra("sendText");
        textView.setText(text);
    }
}

