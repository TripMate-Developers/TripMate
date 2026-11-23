package com.example.tripmate;

import android.database.Cursor;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class ItineraryActivity extends AppCompatActivity {
    long tripId;
    DBHelper db;
    Spinner spDay;
    EditText etActivity;
    LinearLayout llList;
    TextView tvProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_itinerary);

        db = new DBHelper(this);
        tripId = getIntent().getLongExtra("trip_id", -1L);

        spDay = findViewById(R.id.spDay);
        etActivity = findViewById(R.id.etActivity);
        llList = findViewById(R.id.llList);
        tvProgress = findViewById(R.id.tvProgress);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Build "Day 1 ... Day N" from the trip's start and end dates
        int days = 1;
        Cursor c = db.getTrip(tripId);
        if (c.moveToFirst()) {
            days = TripDetailActivity.daysBetween(
                    c.getString(c.getColumnIndexOrThrow("start_date")),
                    c.getString(c.getColumnIndexOrThrow("end_date")));
        }
        c.close();

        ArrayList<String> dayLabels = new ArrayList<>();
        for (int d = 1; d <= days; d++) dayLabels.add("Day " + d);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, dayLabels);
        spDay.setAdapter(adapter);

        findViewById(R.id.btnAddActivity).setOnClickListener(v -> {
            String title = etActivity.getText().toString().trim();
            if (title.isEmpty()) {
                Toast.makeText(this, "Enter an activity", Toast.LENGTH_SHORT).show();
                return;
            }
            int day = spDay.getSelectedItemPosition() + 1;
            db.addActivity(tripId, day, title);
            etActivity.setText("");
            render();
        });

        render();
    }

    private void render() {
        llList.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);
        float density = getResources().getDisplayMetrics().density;

        int lastDay = -1;
        int total = 0, done = 0;

        Cursor c = db.getActivities(tripId);
        while (c.moveToNext()) {
            final long id = c.getLong(c.getColumnIndexOrThrow("id"));
            int day = c.getInt(c.getColumnIndexOrThrow("day"));
            String title = c.getString(c.getColumnIndexOrThrow("title"));
            boolean isDone = c.getInt(c.getColumnIndexOrThrow("done")) == 1;
            total++;
            if (isDone) done++;

            // Day header when the day changes
            if (day != lastDay) {
                TextView header = new TextView(this);
                header.setText("Day " + day);
                header.setTextColor(0xFF176B57);
                header.setTextSize(16);
                header.setTypeface(null, Typeface.BOLD);
                header.setPadding(0, (int) (10 * density), 0, (int) (8 * density));
                llList.addView(header);
                lastDay = day;
            }

            View row = inflater.inflate(R.layout.item_activity, llList, false);
            final CheckBox cb = row.findViewById(R.id.cbDone);
            cb.setText(title);
            cb.setChecked(isDone);
            applyStrike(cb, isDone);

            cb.setOnCheckedChangeListener((button, checked) -> {
                db.setActivityDone(id, checked);
                applyStrike(cb, checked);
                updateProgress();
            });

            row.findViewById(R.id.btnDeleteActivity).setOnClickListener(v -> {
                db.deleteActivity(id);
                render();
            });

            llList.addView(row);
        }
        c.close();

        if (total == 0) {
            TextView empty = new TextView(this);
            empty.setText("No activities yet.\nPick a day above and add your first stop.");
            empty.setTextColor(0xFF626B65);
            empty.setTextSize(14);
            empty.setPadding(0, (int) (30 * density), 0, 0);
            llList.addView(empty);
        }
        updateProgress();
    }

    private void updateProgress() {
        int total = 0, done = 0;
        Cursor c = db.getActivities(tripId);
        while (c.moveToNext()) {
            total++;
            if (c.getInt(c.getColumnIndexOrThrow("done")) == 1) done++;
        }
        c.close();
        tvProgress.setText(total == 0 ? "Nothing planned yet"
                : done + " of " + total + " activities done");
    }

    private void applyStrike(CheckBox cb, boolean done) {
        if (done) {
            cb.setPaintFlags(cb.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            cb.setAlpha(0.55f);
        } else {
            cb.setPaintFlags(cb.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
            cb.setAlpha(1f);
        }
    }
}