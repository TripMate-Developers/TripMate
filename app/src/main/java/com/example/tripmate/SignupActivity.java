package com.example.tripmate;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SignupActivity extends AppCompatActivity {
    EditText etUser, etPass;
    DBHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        etUser = findViewById(R.id.etUser);
        etPass = findViewById(R.id.etPass);
        db = new DBHelper(this);

        findViewById(R.id.btnSignup).setOnClickListener(v -> {
            String u = etUser.getText().toString().trim();
            String p = etPass.getText().toString();

            if (u.isEmpty()) {
                toast("Enter a username");
                return;
            }

            String err = validatePassword(p);
            if (err != null) {
                toast(err);
                return;
            }

            if (db.registerUser(u, p)) {
                toast("Signup successful");

                // Bundle to pass data to the Login activity (Expt 5)
                Intent i = new Intent(SignupActivity.this, LoginActivity.class);
                Bundle extras = new Bundle();
                extras.putString("username", u);
                extras.putString("password", p);
                i.putExtras(extras);
                startActivity(i);
                finish();
            } else {
                toast("Username already exists");
            }
        });

        // Fixed: close this screen so Back from the home screen doesn't return here
        findViewById(R.id.tvGoLogin).setOnClickListener(v -> {
            startActivity(new Intent(SignupActivity.this, LoginActivity.class));
            finish();
        });
    }

    // Password rules from Experiment 5
    private String validatePassword(String p) {
        if (p.length() < 8) return "Minimum 8 characters";
        if (!p.matches(".*[A-Z].*")) return "Add an uppercase letter";
        if (!p.matches(".*[a-z].*")) return "Add a lowercase letter";
        if (!p.matches(".*[0-9].*")) return "Add a number";
        if (!p.matches(".*[^A-Za-z0-9].*")) return "Add a special character";
        return null;
    }

    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}