package com.example.tripmate;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class DashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Bundle data (username) sent from the previous activity
        String user = getIntent().getStringExtra("username");

        TextView tvWelcome = findViewById(R.id.tvWelcome);
        tvWelcome.setText("Welcome, " + (user == null ? "Traveler" : user));
    }
}