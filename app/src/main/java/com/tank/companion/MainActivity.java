package com.tank.companion;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends Activity {
    private EditText wordleInput;
    private EditText dateInput;
    private TextView outputText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        wordleInput = findViewById(R.id.wordleInput);
        dateInput = findViewById(R.id.dateInput);
        outputText = findViewById(R.id.outputText);

        dateInput.setText(new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date()));

        Button calculateButton = findViewById(R.id.calculateButton);
        calculateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                runTankSequence();
            }
        });
    }

    private void runTankSequence() {
        String wordle = wordleInput.getText().toString().trim();
        String date = dateInput.getText().toString().trim();

        try {
            TankCore.calculate(wordle, date);

            outputText.setText(
                "T.A.N.K. sequence completed.\n\n" +
                "Verification complete."
            );
        } catch (Exception ex) {
            String message = ex.getMessage();
            if (message == null || message.trim().isEmpty()) {
                message = "Invalid input.";
            }
            outputText.setText("INPUT ERROR\n" + message);
        }
    }
}
