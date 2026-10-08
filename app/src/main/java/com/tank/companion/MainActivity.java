package com.tank.companion;

import android.app.Activity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends Activity {
    private EditText wordle;
    private EditText date;
    private TextView output;
    private String lastPassword = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        wordle = findViewById(R.id.wordleInput);
        date = findViewById(R.id.dateInput);
        output = findViewById(R.id.outputText);
        output.setMovementMethod(new ScrollingMovementMethod());

        date.setText(new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date()));

        Button calc = findViewById(R.id.calculateButton);
        Button copy = findViewById(R.id.copyButton);

        calc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calculate();
            }
        });

        copy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (lastPassword.length() == 0) {
                    Toast.makeText(MainActivity.this, "Calculate a password first.", Toast.LENGTH_SHORT).show();
                    return;
                }
                ClipboardManager cm = (ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
                cm.setPrimaryClip(ClipData.newPlainText("T.A.N.K. password", lastPassword));
                Toast.makeText(MainActivity.this, "Password copied.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void calculate() {
        try {
            TankCore.Result r = TankCore.calculate(
                wordle.getText().toString(),
                date.getText().toString()
            );

            lastPassword = r.password;

            String text =
                "Layer 1 - Wordle\n" + r.wordle + "\n\n" +
                "Layer 2 - Daily coordinate\n" + r.coordinate + "\n\n" +
                "Layer 3 - Kalebration 3301\n" + r.kalebration + "\n\n" +
                "Layer 4 - Cicada transform\n" + r.cicada + "\n\n" +
                "Layer 7 - Distorted coordinate\n" + r.distorted + "\n\n" +
                "Layer 8 - Waypoint token\n" + r.waypoint + "\n\n" +
                "Layer 9 - TODAY'S T.A.N.K. PASSWORD\n" + r.password;

            output.setText(text);
        } catch (Exception ex) {
            lastPassword = "";
            output.setText("ERROR\n" + ex.getMessage());
        }
    }
}
