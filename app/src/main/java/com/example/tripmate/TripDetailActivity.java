package com.example.tripmate;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TripDetailActivity extends AppCompatActivity {
    long tripId;
    String username;
    DBHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_detail);

        db = new DBHelper(this);
        tripId = getIntent().getLongExtra("trip_id", -1L);
        username = getIntent().getStringExtra("username");

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        findViewById(R.id.btnItinerary).setOnClickListener(v -> {
            Intent i = new Intent(this, ItineraryActivity.class);
            i.putExtra("trip_id", tripId);
            startActivity(i);
        });
        findViewById(R.id.btnTripExpenses).setOnClickListener(v -> {
            Intent i = new Intent(this, ExpensesActivity.class);
            i.putExtra("trip_id", tripId);
            startActivity(i);
        });
        findViewById(R.id.btnTripReminders).setOnClickListener(v -> soon("Reminders"));
        findViewById(R.id.btnTripTimer).setOnClickListener(v -> {
            Intent i = new Intent(this, TimerActivity.class);
            i.putExtra("trip_id", tripId);
            startActivity(i);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTrip();
    }

    private void loadTrip() {
        Cursor c = db.getTrip(tripId);
        if (!c.moveToFirst()) {
            c.close();
            finish();
            return;
        }
        String name = c.getString(c.getColumnIndexOrThrow("name"));
        String dest = c.getString(c.getColumnIndexOrThrow("destination"));
        String start = c.getString(c.getColumnIndexOrThrow("start_date"));
        String end = c.getString(c.getColumnIndexOrThrow("end_date"));
        double budget = c.getDouble(c.getColumnIndexOrThrow("budget"));
        int people = c.getInt(c.getColumnIndexOrThrow("people"));
        c.close();

        ((TextView) findViewById(R.id.tvDetailName)).setText(name);
        ((TextView) findViewById(R.id.tvDetailDest)).setText("📍 " + dest);
        ((TextView) findViewById(R.id.tvDetailDates))
                .setText("📅  " + start + "  →  " + end + "  (" + daysBetween(start, end) + " days)");
        ((TextView) findViewById(R.id.tvDetailBudget))
                .setText("₹" + String.format(Locale.getDefault(), "%.0f", budget)
                        + "  ·  ₹" + String.format(Locale.getDefault(), "%.0f",
                        budget / Math.max(people, 1)) + " / person  ·  " + people + " people");
    }

    static int daysBetween(String s, String e) {
        try {
            SimpleDateFormat f = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date a = f.parse(s);
            Date b = f.parse(e);
            if (a == null || b == null) return 1;
            long d = (b.getTime() - a.getTime()) / 86400000L + 1;
            return (int) Math.max(1, Math.min(d, 30));
        } catch (Exception ex) {
            return 1;
        }
    }

    private void soon(String name) {
        Toast.makeText(this, name + " coming soon", Toast.LENGTH_SHORT).show();
    }
}