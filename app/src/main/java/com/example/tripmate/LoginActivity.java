package com.example.tripmate;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {
    EditText etUser, etPass;
    Button btnLogin;
    TextView tvAttempts;
    DBHelper db;
    int attempts = 0;          // only 2 attempts allowed (Expt 5)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etUser = findViewById(R.id.etLUser);
        etPass = findViewById(R.id.etLPass);
        btnLogin = findViewById(R.id.btnLogin);
        tvAttempts = findViewById(R.id.tvAttempts);
        db = new DBHelper(this);

        // Read the Bundle sent from SignupActivity (if any)
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            etUser.setText(extras.getString("username", ""));
        }

        btnLogin.setOnClickListener(v -> {
            String u = etUser.getText().toString().trim();
            String p = etPass.getText().toString();

            if (db.checkUser(u, p)) {
                Toast.makeText(this, "Successful Login", Toast.LENGTH_SHORT).show();

                // Pass the username to the home screen using a Bundle
                Intent i = new Intent(LoginActivity.this, MainActivity.class);
                Bundle out = new Bundle();
                out.putString("username", u);
                i.putExtras(out);
                startActivity(i);
                finish();
            } else {
                attempts++;
                if (attempts >= 2) {
                    Toast.makeText(this, "Failed Login Attempts", Toast.LENGTH_LONG).show();
                    btnLogin.setEnabled(false);
                    tvAttempts.setText("Sign in disabled");
                } else {
                    Toast.makeText(this, "Login Failed", Toast.LENGTH_SHORT).show();
                    tvAttempts.setText("1 attempt remaining");
                }
            }
        });
    }
}