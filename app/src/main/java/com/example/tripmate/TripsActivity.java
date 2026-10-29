package com.example.tripmate;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class TripsActivity extends AppCompatActivity {
    String username;
    DBHelper db;
    ListView lvTrips;
    View llEmpty;
    TextView tvCount;
    ArrayList<TripItem> trips = new ArrayList<>();
    TripAdapter adapter;

    static class TripItem {
        long id;
        String name, dest, start, end;
        double budget;
        int people;
    }

    class TripAdapter extends ArrayAdapter<TripItem> {
        TripAdapter() {
            super(TripsActivity.this, 0, trips);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext())
                        .inflate(R.layout.item_trip, parent, false);
            }
            TripItem t = trips.get(position);

            ((TextView) convertView.findViewById(R.id.tvTripName)).setText(t.name);
            ((TextView) convertView.findViewById(R.id.tvTripDest)).setText("📍 " + t.dest);
            ((TextView) convertView.findViewById(R.id.tvTripDays))
                    .setText(daysLabel(t.start, t.end));
            ((TextView) convertView.findViewById(R.id.tvTripDates))
                    .setText("📅  " + t.start + "  →  " + t.end);
            ((TextView) convertView.findViewById(R.id.tvTripBudget))
                    .setText("₹" + String.format(Locale.getDefault(), "%.0f", t.budget)
                            + "  ·  ₹" + String.format(Locale.getDefault(), "%.0f",
                            t.budget / Math.max(t.people, 1)) + " / person");
            ((TextView) convertView.findViewById(R.id.tvTripPeople))
                    .setText("👥 " + t.people);
            return convertView;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trips);

        username = getIntent().getStringExtra("username");
        if (username == null) username = "Traveler";

        db = new DBHelper(this);
        lvTrips = findViewById(R.id.lvTrips);
        llEmpty = findViewById(R.id.llEmpty);
        tvCount = findViewById(R.id.tvCount);

        // Back button: closes this screen and returns to the home screen
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        adapter = new TripAdapter();
        lvTrips.setAdapter(adapter);

        findViewById(R.id.btnAddTrip).setOnClickListener(v -> {
            Intent i = new Intent(this, AddTripActivity.class);
            i.putExtra("username", username);
            i.putExtra("trip_id", -1L);
            startActivity(i);
        });

        lvTrips.setOnItemClickListener((parent, view, position, id) ->
                showOptions(trips.get(position).id));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTrips();
    }

    private void loadTrips() {
        trips.clear();
        Cursor c = db.getTrips(username);
        while (c.moveToNext()) {
            TripItem t = new TripItem();
            t.id = c.getLong(c.getColumnIndexOrThrow("id"));
            t.name = c.getString(c.getColumnIndexOrThrow("name"));
            t.dest = c.getString(c.getColumnIndexOrThrow("destination"));
            t.start = c.getString(c.getColumnIndexOrThrow("start_date"));
            t.end = c.getString(c.getColumnIndexOrThrow("end_date"));
            t.budget = c.getDouble(c.getColumnIndexOrThrow("budget"));
            t.people = c.getInt(c.getColumnIndexOrThrow("people"));
            trips.add(t);
        }
        c.close();
        adapter.notifyDataSetChanged();

        boolean empty = trips.isEmpty();
        llEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        lvTrips.setVisibility(empty ? View.GONE : View.VISIBLE);
        tvCount.setText(trips.size() == 1 ? "1 trip planned"
                : trips.size() + " trips planned");
    }

    private String daysLabel(String s, String e) {
        try {
            SimpleDateFormat f = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date a = f.parse(s);
            Date b = f.parse(e);
            if (a == null || b == null) return "";
            long d = (b.getTime() - a.getTime()) / 86400000L + 1;
            return d + (d == 1 ? " day" : " days");
        } catch (Exception ex) {
            return "";
        }
    }

    private void showOptions(long tripId) {
        String[] options = {"Edit", "Delete"};
        new AlertDialog.Builder(this)
                .setTitle("Trip options")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        Intent i = new Intent(this, AddTripActivity.class);
                        i.putExtra("username", username);
                        i.putExtra("trip_id", tripId);
                        startActivity(i);
                    } else {
                        confirmDelete(tripId);
                    }
                })
                .show();
    }

    private void confirmDelete(long tripId) {
        new AlertDialog.Builder(this)
                .setTitle("Delete trip?")
                .setMessage("This also removes its itinerary, expenses and reminders.")
                .setPositiveButton("Delete", (d, w) -> {
                    db.deleteTrip(tripId);
                    loadTrips();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}