package com.example.tripmate;

import android.app.DatePickerDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AddTripActivity extends AppCompatActivity {
    EditText etName, etDest, etStart, etEnd, etBudget, etPeople;
    DBHelper db;
    String username;
    long tripId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_trip);

        db = new DBHelper(this);
        username = getIntent().getStringExtra("username");
        tripId = getIntent().getLongExtra("trip_id", -1L);

        etName = findViewById(R.id.etTripName);
        etDest = findViewById(R.id.etDestination);
        etStart = findViewById(R.id.etStart);
        etEnd = findViewById(R.id.etEnd);
        etBudget = findViewById(R.id.etBudget);
        etPeople = findViewById(R.id.etPeople);

        // Back button: closes this screen and returns to the trips list
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        etStart.setOnClickListener(v -> pickDate(etStart));
        etEnd.setOnClickListener(v -> pickDate(etEnd));

        // Edit mode: load the existing trip
        if (tripId != -1) {
            ((TextView) findViewById(R.id.tvFormTitle)).setText("Edit Trip");
            Cursor c = db.getTrip(tripId);
            if (c.moveToFirst()) {
                etName.setText(c.getString(c.getColumnIndexOrThrow("name")));
                etDest.setText(c.getString(c.getColumnIndexOrThrow("destination")));
                etStart.setText(c.getString(c.getColumnIndexOrThrow("start_date")));
                etEnd.setText(c.getString(c.getColumnIndexOrThrow("end_date")));
                etBudget.setText(String.valueOf(
                        c.getDouble(c.getColumnIndexOrThrow("budget"))));
                etPeople.setText(String.valueOf(
                        c.getInt(c.getColumnIndexOrThrow("people"))));
            }
            c.close();
        }

        findViewById(R.id.btnSaveTrip).setOnClickListener(v -> saveTrip());
    }

    private void pickDate(EditText target) {
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(this, (view, y, m, d) ->
                target.setText(String.format(Locale.getDefault(),
                        "%02d/%02d/%04d", d, m + 1, y)),
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void saveTrip() {
        String name = etName.getText().toString().trim();
        String dest = etDest.getText().toString().trim();
        String start = etStart.getText().toString().trim();
        String end = etEnd.getText().toString().trim();
        String budgetStr = etBudget.getText().toString().trim();
        String peopleStr = etPeople.getText().toString().trim();

        if (name.isEmpty() || dest.isEmpty() || start.isEmpty() || end.isEmpty()) {
            toast("Fill in name, destination and both dates");
            return;
        }

        // End date must not be before start date
        try {
            SimpleDateFormat f = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date s = f.parse(start);
            Date e = f.parse(end);
            if (s != null && e != null && e.before(s)) {
                toast("End date cannot be before start date");
                return;
            }
        } catch (ParseException ex) {
            toast("Invalid date");
            return;
        }

        double budget = budgetStr.isEmpty() ? 0 : Double.parseDouble(budgetStr);
        int people = peopleStr.isEmpty() ? 1 : Integer.parseInt(peopleStr);
        if (people < 1) people = 1;

        if (tripId == -1) {
            db.addTrip(username, name, dest, start, end, budget, people);
            toast("Trip created");
        } else {
            db.updateTrip(tripId, name, dest, start, end, budget, people);
            toast("Trip updated");
        }
        finish();
    }

    private void toast(String m) {
        Toast.makeText(this, m, Toast.LENGTH_SHORT).show();
    }
}