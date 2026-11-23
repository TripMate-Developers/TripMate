package com.example.tripmate;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class ExpensesActivity extends AppCompatActivity {
    long tripId;
    DBHelper db;
    EditText etTitle, etAmount;
    LinearLayout llList;
    TextView tvTotal, tvBudget, tvRemaining, tvPerPerson, tvSplitNote, tvTripName;
    double budget = 0;
    int people = 1;

    // Receives the result from the calculator screen
    ActivityResultLauncher<Intent> calcLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    double v = result.getData().getDoubleExtra("value", 0);
                    etAmount.setText(v == Math.floor(v)
                            ? String.valueOf((long) v)
                            : String.format(Locale.US, "%.2f", v));
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expenses);

        db = new DBHelper(this);
        tripId = getIntent().getLongExtra("trip_id", -1L);

        etTitle = findViewById(R.id.etExpTitle);
        etAmount = findViewById(R.id.etExpAmount);
        llList = findViewById(R.id.llList);
        tvTotal = findViewById(R.id.tvTotal);
        tvBudget = findViewById(R.id.tvBudget);
        tvRemaining = findViewById(R.id.tvRemaining);
        tvPerPerson = findViewById(R.id.tvPerPerson);
        tvSplitNote = findViewById(R.id.tvSplitNote);
        tvTripName = findViewById(R.id.tvTripName);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Load the trip's budget and number of people
        Cursor c = db.getTrip(tripId);
        if (c.moveToFirst()) {
            tvTripName.setText(c.getString(c.getColumnIndexOrThrow("name")));
            budget = c.getDouble(c.getColumnIndexOrThrow("budget"));
            people = Math.max(1, c.getInt(c.getColumnIndexOrThrow("people")));
        }
        c.close();

        findViewById(R.id.btnCalc).setOnClickListener(v ->
                calcLauncher.launch(new Intent(this, CalculatorActivity.class)));

        findViewById(R.id.btnAddExpense).setOnClickListener(v -> addExpense());

        render();
    }

    private void addExpense() {
        String title = etTitle.getText().toString().trim();
        String amtStr = etAmount.getText().toString().trim();

        if (title.isEmpty() || amtStr.isEmpty()) {
            toast("Enter a title and an amount");
            return;
        }
        double amount;
        try {
            amount = Double.parseDouble(amtStr);
        } catch (NumberFormatException e) {
            toast("Invalid amount");
            return;
        }
        if (amount <= 0) {
            toast("Amount must be more than 0");
            return;
        }

        db.addExpense(tripId, title, amount);
        etTitle.setText("");
        etAmount.setText("");
        render();
    }

    private void render() {
        llList.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);

        int count = 0;
        Cursor c = db.getExpenses(tripId);
        while (c.moveToNext()) {
            final long id = c.getLong(c.getColumnIndexOrThrow("id"));
            String title = c.getString(c.getColumnIndexOrThrow("title"));
            double amount = c.getDouble(c.getColumnIndexOrThrow("amount"));

            View row = inflater.inflate(R.layout.item_expense, llList, false);
            ((TextView) row.findViewById(R.id.tvExpTitle)).setText(title);
            ((TextView) row.findViewById(R.id.tvExpAmount)).setText(money(amount));
            row.findViewById(R.id.btnDeleteExpense).setOnClickListener(v -> {
                db.deleteExpense(id);
                render();
            });
            llList.addView(row);
            count++;
        }
        c.close();

        if (count == 0) {
            TextView empty = new TextView(this);
            empty.setText("No expenses yet. Add your first one above.");
            empty.setTextColor(0xFF626B65);
            empty.setTextSize(14);
            llList.addView(empty);
        }

        // Summary: total, remaining budget and equal split
        double total = db.getTotalExpense(tripId);
        tvTotal.setText(money(total));
        tvBudget.setText(money(budget));

        double remaining = budget - total;
        if (remaining >= 0) {
            tvRemaining.setText(money(remaining));
            tvRemaining.setTextColor(0xFFFFFFFF);
        } else {
            tvRemaining.setText("-" + money(-remaining));
            tvRemaining.setTextColor(0xFFFFB4AB);   // over budget
        }

        tvPerPerson.setText(money(total / people));
        tvSplitNote.setText("Split equally between " + people
                + (people == 1 ? " person" : " people"));
    }

    private String money(double v) {
        return v == Math.floor(v)
                ? "₹" + String.format(Locale.US, "%.0f", v)
                : "₹" + String.format(Locale.US, "%.2f", v);
    }

    private void toast(String m) {
        Toast.makeText(this, m, Toast.LENGTH_SHORT).show();
    }
}