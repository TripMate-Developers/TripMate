package com.example.tripmate;

import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Locale;

public class TimerActivity extends AppCompatActivity {
    long tripId;
    DBHelper db;
    AutoCompleteTextView etActivity;
    TextView tvTimer, tvStatus, tvTotal;
    Button btnStart, btnStop;
    LinearLayout llLog;

    final Handler handler = new Handler(Looper.getMainLooper());
    boolean running = false;
    long startedAt = 0;
    String currentActivity = "";

    final Runnable ticker = new Runnable() {
        @Override
        public void run() {
            if (!running) return;
            showTime(SystemClock.elapsedRealtime() - startedAt);
            handler.postDelayed(this, 500);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer);

        db = new DBHelper(this);
        tripId = getIntent().getLongExtra("trip_id", -1L);

        etActivity = findViewById(R.id.etActivity);
        tvTimer = findViewById(R.id.tvTimer);
        tvStatus = findViewById(R.id.tvStatus);
        tvTotal = findViewById(R.id.tvTotal);
        btnStart = findViewById(R.id.btnStart);
        btnStop = findViewById(R.id.btnStop);
        llLog = findViewById(R.id.llLog);

        Cursor c = db.getTrip(tripId);
        if (c.moveToFirst()) {
            TextView tvTripName = findViewById(R.id.tvTripName);
            tvTripName.setText(c.getString(c.getColumnIndexOrThrow("name")));
        }
        c.close();

        loadSuggestions();

        // Show the itinerary suggestions as soon as the field is tapped
        etActivity.setOnClickListener(v -> etActivity.showDropDown());
        etActivity.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) etActivity.showDropDown();
        });

        btnStart.setOnClickListener(v -> startSession());
        btnStop.setOnClickListener(v -> stopAndSave());
        findViewById(R.id.btnReset).setOnClickListener(v -> discardSession());

        // Back button and system back both save a running session first
        findViewById(R.id.btnBack).setOnClickListener(v -> leave());
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                leave();
            }
        });

        updateButtons();
        renderLog();
    }

    private void loadSuggestions() {
        ArrayList<String> names = new ArrayList<>();
        Cursor c = db.getActivities(tripId);
        while (c.moveToNext()) {
            String t = c.getString(c.getColumnIndexOrThrow("title"));
            if (!names.contains(t)) names.add(t);
        }
        c.close();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, names);
        etActivity.setAdapter(adapter);
    }

    private void startSession() {
        if (running) return;
        String name = etActivity.getText().toString().trim();
        if (name.isEmpty()) {
            toast("Pick or type the activity first");
            return;
        }
        currentActivity = name;
        running = true;
        startedAt = SystemClock.elapsedRealtime();
        etActivity.dismissDropDown();
        etActivity.setEnabled(false);
        tvStatus.setText("IN PROGRESS: " + currentActivity);
        updateButtons();
        handler.post(ticker);
    }

    private void stopAndSave() {
        if (!running) return;
        long seconds = (SystemClock.elapsedRealtime() - startedAt) / 1000;
        running = false;
        handler.removeCallbacks(ticker);

        if (seconds >= 1) {
            db.addSession(tripId, currentActivity, seconds);
            db.markActivityDoneByTitle(tripId, currentActivity);
            toast("Logged " + currentActivity + " (" + formatTime(seconds) + ")");
        } else {
            toast("Too short to log");
        }
        resetUi();
        renderLog();
    }

    private void discardSession() {
        if (!running) return;
        running = false;
        handler.removeCallbacks(ticker);
        toast("Session discarded");
        resetUi();
    }

    private void leave() {
        if (running) stopAndSave();
        finish();
    }

    private void resetUi() {
        currentActivity = "";
        etActivity.setText("");
        etActivity.setEnabled(true);
        tvTimer.setText("00:00:00");
        tvStatus.setText("READY");
        updateButtons();
    }

    private void renderLog() {
        llLog.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);

        long total = 0;
        int count = 0;
        Cursor c = db.getSessions(tripId);
        while (c.moveToNext()) {
            final long id = c.getLong(c.getColumnIndexOrThrow("id"));
            String activity = c.getString(c.getColumnIndexOrThrow("activity"));
            long seconds = c.getLong(c.getColumnIndexOrThrow("seconds"));
            total += seconds;
            count++;

            View row = inflater.inflate(R.layout.item_expense, llLog, false);
            TextView tvName = row.findViewById(R.id.tvExpTitle);
            TextView tvTime = row.findViewById(R.id.tvExpAmount);
            tvName.setText(activity);
            tvTime.setText(formatTime(seconds));
            row.findViewById(R.id.btnDeleteExpense).setOnClickListener(v -> {
                db.deleteSession(id);
                renderLog();
            });
            llLog.addView(row);
        }
        c.close();

        if (count == 0) {
            TextView empty = new TextView(this);
            empty.setText("Nothing logged yet. Pick an activity and tap START.");
            empty.setTextColor(0xFF626B65);
            empty.setTextSize(14);
            llLog.addView(empty);
            tvTotal.setText("");
        } else {
            String label = (count == 1) ? " activity" : " activities";
            String summary = count + label + " · total time " + formatTime(total);
            tvTotal.setText(summary);
        }
    }

    private void showTime(long ms) {
        String text = formatTime(ms / 1000);
        tvTimer.setText(text);
    }

    private String formatTime(long totalSeconds) {
        long h = totalSeconds / 3600;
        long m = (totalSeconds % 3600) / 60;
        long s = totalSeconds % 60;
        return String.format(Locale.getDefault(), "%02d:%02d:%02d", h, m, s);
    }

    private void updateButtons() {
        btnStart.setEnabled(!running);
        btnStop.setEnabled(running);
        btnStart.setAlpha(running ? 0.5f : 1f);
        btnStop.setAlpha(running ? 1f : 0.5f);
    }

    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(ticker);
    }
}