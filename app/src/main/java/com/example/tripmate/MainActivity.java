package com.example.tripmate;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        username = getIntent().getStringExtra("username");
        if (username == null) username = "Traveler";

        TextView tvReady = findViewById(R.id.tvReady);
        tvReady.setText("Ready to explore, " + username + "?");

        findViewById(R.id.btnTrips).setOnClickListener(v -> {
            Intent i = new Intent(MainActivity.this, TripsActivity.class);
            i.putExtra("username", username);
            startActivity(i);
        });
        findViewById(R.id.btnExpenses).setOnClickListener(v -> soon("Expenses"));
        findViewById(R.id.btnReminders).setOnClickListener(v -> soon("Reminders"));
        findViewById(R.id.btnProfile).setOnClickListener(v -> soon("My Profile"));
    }

    private void soon(String name) {
        Toast.makeText(this, name + " coming soon", Toast.LENGTH_SHORT).show();
    }
}