package com.example.tripmate;

import android.content.Intent;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class CalculatorActivity extends AppCompatActivity {
    TextView tvResult;
    EditText etInput;

    double acc = 0;          // stored first operand
    String op = "";          // pending operator
    boolean fresh = true;    // next digit starts a new number

    final DecimalFormat df =
            new DecimalFormat("#.########", DecimalFormatSymbols.getInstance(Locale.US));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calculator);

        tvResult = findViewById(R.id.tvResult);
        etInput = findViewById(R.id.etInput);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        findViewById(R.id.btnUseResult).setOnClickListener(v -> {
            Intent data = new Intent();
            data.putExtra("value", current());
            setResult(RESULT_OK, data);
            finish();
        });

        buildKeypad();
    }

    private void buildKeypad() {
        LinearLayout pad = findViewById(R.id.llKeypad);
        String[][] keys = {
                {"7", "8", "9", "÷"},
                {"4", "5", "6", "×"},
                {"1", "2", "3", "−"},
                {".", "0", "=", "+"},
                {"C"}
        };

        for (String[] row : keys) {
            LinearLayout r = new LinearLayout(this);
            r.setOrientation(LinearLayout.HORIZONTAL);

            for (String k : row) {
                Button b = new Button(this);
                b.setText(k);
                b.setAllCaps(false);
                b.setTextSize(22);
                b.setTypeface(null, android.graphics.Typeface.BOLD);

                boolean isAction = "÷×−+=C".contains(k);
                b.setBackgroundResource(isAction ? R.drawable.bg_button : R.drawable.bg_card);
                b.setTextColor(isAction ? 0xFFFFFFFF : 0xFF17251E);

                LinearLayout.LayoutParams lp =
                        new LinearLayout.LayoutParams(0, dp(64), 1f);
                lp.setMargins(dp(4), dp(4), dp(4), dp(4));
                b.setLayoutParams(lp);

                b.setOnClickListener(v -> onKey(k));
                r.addView(b);
            }
            pad.addView(r, new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
        }
    }

    private void onKey(String k) {
        switch (k) {
            case "C":
                resetAll();
                break;
            case "+":
            case "−":
            case "×":
            case "÷":
                operator(k);
                break;
            case "=":
                calculate();
                break;
            case ".":
                if (fresh) {
                    etInput.setText("0.");
                    fresh = false;
                } else if (!etInput.getText().toString().contains(".")) {
                    etInput.append(".");
                }
                break;
            default:   // digits
                if (fresh || etInput.getText().toString().equals("0")) {
                    etInput.setText(k);
                } else {
                    etInput.append(k);
                }
                fresh = false;
        }
    }

    private void operator(String k) {
        double cur = current();
        if (!op.isEmpty() && !fresh) {
            double res = apply(acc, op, cur);
            if (Double.isNaN(res)) {
                resetAll();
                return;
            }
            acc = res;
            etInput.setText(df.format(acc));
        } else {
            acc = cur;
        }
        op = k;
        fresh = true;
        tvResult.setText(df.format(acc) + " " + op);
    }

    private void calculate() {
        if (op.isEmpty()) return;
        double cur = current();
        double res = apply(acc, op, cur);
        if (Double.isNaN(res)) {
            resetAll();
            return;
        }
        tvResult.setText(df.format(acc) + " " + op + " " + df.format(cur) + " =");
        etInput.setText(df.format(res));
        acc = res;
        op = "";
        fresh = true;
    }

    private double apply(double a, String o, double b) {
        switch (o) {
            case "+":
                return a + b;
            case "−":
                return a - b;
            case "×":
                return a * b;
            default:
                if (b == 0) {
                    Toast.makeText(this, "Cannot divide by zero", Toast.LENGTH_SHORT).show();
                    return Double.NaN;
                }
                return a / b;
        }
    }

    private double current() {
        try {
            return Double.parseDouble(etInput.getText().toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void resetAll() {
        acc = 0;
        op = "";
        fresh = true;
        etInput.setText("0");
        tvResult.setText("");
    }

    private int dp(int v) {
        return (int) (v * getResources().getDisplayMetrics().density);
    }
}